package top.mykodb.server_expansion.module.cleanup

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.ItemEnchantments


/**
 * 黑名单过滤器 - 支持ID、标签、!取反过滤
 */
object BlacklistFilter {
    /**
     * 过滤器条目类型
     */
    sealed class FilterEntry {
        abstract val id: String

        data class Include(override val id: String) : FilterEntry()
        data class IncludeTag(override val id: String) : FilterEntry()
        data class Exclude(override val id: String) : FilterEntry()
        data class ExcludeTag(override val id: String) : FilterEntry()
    }

    // 解析过滤条目
    private fun parseEntries(ids: List<String>): List<FilterEntry> = ids.mapNotNull { raw ->
        val id = raw.trim()
        when {
            id.startsWith("!#") -> ResourceLocation.tryParse(id.substring(2))?.let { FilterEntry.ExcludeTag(it.toString()) }
            id.startsWith("!") -> ResourceLocation.tryParse(id.substring(1))?.let { FilterEntry.Exclude(it.toString()) }
            id.startsWith("#") -> ResourceLocation.tryParse(id.substring(1))?.let { FilterEntry.IncludeTag(it.toString()) }
            else -> ResourceLocation.tryParse(id)?.let { FilterEntry.Include(it.toString()) }
        }
    }

    // 物品过滤结果（一次解析、四类集合）
    data class ItemFilters(
        val ids: Set<Item>,
        val tags: Set<TagKey<Item>>,
        val excludeIds: Set<Item>,
        val excludeTags: Set<TagKey<Item>>,
    )

    // 实体过滤结果（一次解析、四类集合）
    data class EntityFilters(
        val ids: Set<EntityType<*>>,
        val tags: Set<TagKey<EntityType<*>>>,
        val excludeIds: Set<EntityType<*>>,
        val excludeTags: Set<TagKey<EntityType<*>>>,
    )

    // 解析物品过滤（单次 parseEntries，避免重复解析同一列表）
    fun parseItemFilters(ids: List<String>): ItemFilters {
        val entries = parseEntries(ids)
        return ItemFilters(
            ids = entries.filterIsInstance<FilterEntry.Include>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)
                    ?.let { BuiltInRegistries.ITEM.get(it) }
                    ?.takeIf { it != Items.AIR }
            }.toSet(),
            tags = entries.filterIsInstance<FilterEntry.IncludeTag>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ITEM, it) }
            }.toSet(),
            excludeIds = entries.filterIsInstance<FilterEntry.Exclude>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)
                    ?.let { BuiltInRegistries.ITEM.get(it) }
                    ?.takeIf { it != Items.AIR }
            }.toSet(),
            excludeTags = entries.filterIsInstance<FilterEntry.ExcludeTag>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ITEM, it) }
            }.toSet(),
        )
    }

    // 解析实体过滤（单次 parseEntries，避免重复解析同一列表）
    fun parseEntityFilters(ids: List<String>): EntityFilters {
        val entries = parseEntries(ids)
        return EntityFilters(
            ids = entries.filterIsInstance<FilterEntry.Include>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)?.let { BuiltInRegistries.ENTITY_TYPE.get(it) }
            }.toSet(),
            tags = entries.filterIsInstance<FilterEntry.IncludeTag>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ENTITY_TYPE, it) }
            }.toSet(),
            excludeIds = entries.filterIsInstance<FilterEntry.Exclude>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)?.let { BuiltInRegistries.ENTITY_TYPE.get(it) }
            }.toSet(),
            excludeTags = entries.filterIsInstance<FilterEntry.ExcludeTag>().mapNotNull { entry ->
                ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ENTITY_TYPE, it) }
            }.toSet(),
        )
    }

    // 解析组件名列表
    fun parseComponents(names: List<String>): Set<DataComponentType<*>> = names.mapNotNull { name ->
        val loc = ResourceLocation.tryParse(name) ?: ResourceLocation.tryParse("minecraft:$name")
        loc?.let { BuiltInRegistries.DATA_COMPONENT_TYPE.get(it) }
    }.toSet()

    // 检查掉落物实体是否应该被跳过
    fun shouldSkipItemEntity(
        entity: ItemEntity,
        blacklistIds: Set<Item>,
        blacklistTags: Set<TagKey<Item>>,
        excludeIds: Set<Item>,
        excludeTags: Set<TagKey<Item>>,
        skipComponents: Set<DataComponentType<*>>
    ): Boolean {
        if (!entity.isAlive) return true
        val stack = entity.item
        
        // 空堆无法编码也无法回收，直接跳过
        if (stack.isEmpty) return true
        
        // 取反：!前缀表示“排除出黑名单”，这些物品会被清理
        if (stack.item in excludeIds) return false
        if (excludeTags.any { stack.`is`(it) }) return false
        
        // 正常黑名单检查：黑名单中的物品不清理
        if (stack.item in blacklistIds) return true
        if (blacklistTags.any { stack.`is`(it) }) return true
        
        for (comp in skipComponents) {
            val value = stack.get(comp) ?: continue
            // 附魔组件需检查非空
            if (value is ItemEnchantments && value.isEmpty) continue
            return true
        }
        return false
    }

    // 检查实体是否应该被跳过
    fun shouldSkipEntity(
        entity: Entity,
        whitelistIds: Set<EntityType<*>>,
        whitelistTags: Set<TagKey<EntityType<*>>>,
        excludeIds: Set<EntityType<*>>,
        excludeTags: Set<TagKey<EntityType<*>>>,
        skipNamed: Boolean,
        skipPersistent: Boolean
    ): Boolean {
        if (!entity.isAlive) return true
        
        // 取反：!前缀表示“排除出白名单”，这些实体不清理（受保护）
        if (entity.type in excludeIds) return true
        if (excludeTags.any { entity.type.`is`(it) }) return true
        
        // 白名单检查：ID或标签匹配其一即可
        // 如果白名单为空，默认不清理任何实体（安全策略）
        if (whitelistIds.isEmpty() && whitelistTags.isEmpty()) return true
        val inWhitelist = entity.type in whitelistIds || whitelistTags.any { entity.type.`is`(it) }
        if (!inWhitelist) return true
        if (skipNamed && entity.hasCustomName()) return true
        if (skipPersistent && entity is Mob && entity.isPersistenceRequired) return true
        return false
    }
}
