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

    // 解析物品ID列表（不含取反）
    fun parseItemIds(ids: List<String>): Set<Item> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.Include) {
            ResourceLocation.tryParse(entry.id)
                ?.let { BuiltInRegistries.ITEM.get(it) }
                ?.takeIf { it != Items.AIR }
        } else null
    }.toSet()

    // 解析物品标签列表（不含取反）
    fun parseItemTags(ids: List<String>): Set<TagKey<Item>> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.IncludeTag) {
            ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ITEM, it) }
        } else null
    }.toSet()

    // 解析物品排除ID列表
    fun parseItemExcludeIds(ids: List<String>): Set<Item> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.Exclude) {
            ResourceLocation.tryParse(entry.id)
                ?.let { BuiltInRegistries.ITEM.get(it) }
                ?.takeIf { it != Items.AIR }
        } else null
    }.toSet()

    // 解析物品排除标签列表
    fun parseItemExcludeTags(ids: List<String>): Set<TagKey<Item>> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.ExcludeTag) {
            ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ITEM, it) }
        } else null
    }.toSet()

    // 解析实体类型ID列表
    fun parseEntityIds(ids: List<String>): Set<EntityType<*>> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.Include) {
            ResourceLocation.tryParse(entry.id)?.let { BuiltInRegistries.ENTITY_TYPE.get(it) }
        } else null
    }.toSet()

    // 解析实体标签列表
    fun parseEntityTags(ids: List<String>): Set<TagKey<EntityType<*>>> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.IncludeTag) {
            ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ENTITY_TYPE, it) }
        } else null
    }.toSet()

    // 解析实体排除ID列表
    fun parseEntityExcludeIds(ids: List<String>): Set<EntityType<*>> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.Exclude) {
            ResourceLocation.tryParse(entry.id)?.let { BuiltInRegistries.ENTITY_TYPE.get(it) }
        } else null
    }.toSet()

    // 解析实体排除标签列表
    fun parseEntityExcludeTags(ids: List<String>): Set<TagKey<EntityType<*>>> = parseEntries(ids).mapNotNull { entry ->
        if (entry is FilterEntry.ExcludeTag) {
            ResourceLocation.tryParse(entry.id)?.let { TagKey.create(Registries.ENTITY_TYPE, it) }
        } else null
    }.toSet()

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
        
        // 取反条件优先：如果在排除列表中，不跳过（即不清理）
        if (stack.item in excludeIds) return false
        if (excludeTags.any { stack.`is`(it) }) return false
        
        // 正常黑名单检查
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
        
        // 取反条件优先：如果在排除列表中，不跳过（即不清理）
        if (entity.type in excludeIds) return false
        if (excludeTags.any { entity.type.`is`(it) }) return false
        
        // 白名单检查：ID或标签匹配其一即可
        if (whitelistIds.isNotEmpty() || whitelistTags.isNotEmpty()) {
            val inWhitelist = entity.type in whitelistIds || whitelistTags.any { entity.type.`is`(it) }
            if (!inWhitelist) return true
        }
        if (skipNamed && entity.hasCustomName()) return true
        if (skipPersistent && entity is Mob && entity.isPersistenceRequired) return true
        return false
    }
}
