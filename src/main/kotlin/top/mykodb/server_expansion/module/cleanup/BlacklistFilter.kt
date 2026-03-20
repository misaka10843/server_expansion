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
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.ItemEnchantments

/**
 * 黑名单过滤器 - 支持ID、标签、NBT过滤
 */
object BlacklistFilter {

    // 解析物品ID列表
    fun parseItemIds(ids: List<String>): Set<Item> = ids.mapNotNull { id ->
        if (id.startsWith("#")) return@mapNotNull null
        ResourceLocation.tryParse(id)
            ?.let { BuiltInRegistries.ITEM.get(it) }
            ?.takeIf { it != Items.AIR }
    }.toSet()

    // 解析物品标签列表
    fun parseItemTags(ids: List<String>): Set<TagKey<Item>> = ids.mapNotNull { id ->
        if (!id.startsWith("#")) return@mapNotNull null
        ResourceLocation.tryParse(id.substring(1))?.let { TagKey.create(Registries.ITEM, it) }
    }.toSet()

    // 解析实体类型ID列表
    fun parseEntityIds(ids: List<String>): Set<EntityType<*>> = ids.mapNotNull { id ->
        if (id.startsWith("#")) return@mapNotNull null
        ResourceLocation.tryParse(id)?.let { BuiltInRegistries.ENTITY_TYPE.get(it) }
    }.toSet()

    // 解析实体标签列表
    fun parseEntityTags(ids: List<String>): Set<TagKey<EntityType<*>>> = ids.mapNotNull { id ->
        if (!id.startsWith("#")) return@mapNotNull null
        ResourceLocation.tryParse(id.substring(1))?.let { TagKey.create(Registries.ENTITY_TYPE, it) }
    }.toSet()

    // 检查物品是否应该被跳过
    fun shouldSkipItem(
        stack: ItemStack,
        blacklistIds: Set<Item>,
        blacklistTags: Set<TagKey<Item>>,
        skipComponents: Set<DataComponentType<*>>
    ): Boolean {
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
        skipComponents: Set<DataComponentType<*>>
    ): Boolean {
        if (!entity.isAlive) return true
        return shouldSkipItem(entity.item, blacklistIds, blacklistTags, skipComponents)
    }

    // 检查实体是否应该被跳过
    fun shouldSkipEntity(
        entity: Entity,
        whitelistIds: Set<EntityType<*>>,
        whitelistTags: Set<TagKey<EntityType<*>>>,
        blacklistTags: Set<TagKey<EntityType<*>>>,
        skipNamed: Boolean,
        skipPersistent: Boolean
    ): Boolean {
        if (!entity.isAlive) return true
        // 白名单检查：ID或标签匹配其一即可
        if (whitelistIds.isNotEmpty() || whitelistTags.isNotEmpty()) {
            val inWhitelist = entity.type in whitelistIds || whitelistTags.any { entity.type.`is`(it) }
            if (!inWhitelist) return true
        }
        if (blacklistTags.any { entity.type.`is`(it) }) return true
        if (skipNamed && entity.hasCustomName()) return true
        if (skipPersistent && entity is Mob && entity.isPersistenceRequired) return true
        return false
    }
}
