package top.mykodb.server_expansion

import net.minecraft.core.component.DataComponentType
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.neoforge.common.ModConfigSpec
import top.mykodb.server_expansion.module.cleanup.BlacklistFilter
import top.mykodb.server_expansion.module.i18n.LangKeys

object Config {

    class Spec(builder: ModConfigSpec.Builder) {
        // 是否启用欢迎语
        val enableWelcome: ModConfigSpec.BooleanValue = builder
            .translation(LangKeys.CONFIG_ENABLE_WELCOME)
            .define(listOf("welcome","enable_welcome"), true)

        // 物品清理
        val itemsEnable: ModConfigSpec.BooleanValue = builder
            .translation(LangKeys.CONFIG_CLEANUP_ITEMS_ENABLE)
            .define(listOf("cleanup", "items", "enable"), true)

        val itemsInterval: ModConfigSpec.IntValue = builder
            .translation(LangKeys.CONFIG_CLEANUP_INTERVAL)
            .defineInRange(listOf("cleanup", "items", "interval"), 6000, 20, Int.MAX_VALUE)

        val itemBlacklist: ModConfigSpec.ConfigValue<List<String>> = builder
            .translation(LangKeys.CONFIG_CLEANUP_ITEMS_BLACKLIST)
            .defineListAllowEmpty(listOf("cleanup", "items", "blacklist"), listOf("minecraft:dragon_egg", "#minecraft:flowers", "!minecraft:diamond"), { "" }) { it is String }

        val itemSkipComponents: ModConfigSpec.ConfigValue<List<String>> = builder
            .translation(LangKeys.CONFIG_CLEANUP_ITEMS_SKIP_COMPONENTS)
            .defineListAllowEmpty(listOf("cleanup", "items", "skip_components"), listOf("custom_name", "enchantments"), { "" }) { it is String }

        // 实体清理
        val entitiesEnable: ModConfigSpec.BooleanValue = builder
            .translation(LangKeys.CONFIG_CLEANUP_ENTITIES_ENABLE)
            .define(listOf("cleanup", "entities", "enable"), false)

        val entityInterval: ModConfigSpec.IntValue = builder
            .translation(LangKeys.CONFIG_CLEANUP_ENTITIES_INTERVAL)
            .defineInRange(listOf("cleanup", "entities", "interval"), 6000, 20, Int.MAX_VALUE)

        val entityWhitelist: ModConfigSpec.ConfigValue<List<String>> = builder
            .translation(LangKeys.CONFIG_CLEANUP_ENTITIES_WHITELIST)
            .defineListAllowEmpty(listOf("cleanup", "entities", "whitelist"), listOf("minecraft:experience_orb", "minecraft:arrow", "!minecraft:villager"), { "" }) { it is String }

        val entitySkipNamed: ModConfigSpec.BooleanValue = builder
            .translation(LangKeys.CONFIG_CLEANUP_ENTITIES_SKIP_NAMED)
            .define(listOf("cleanup", "entities", "skip_named"), true)

        val entitySkipPersistent: ModConfigSpec.BooleanValue = builder
            .translation(LangKeys.CONFIG_CLEANUP_ENTITIES_SKIP_PERSISTENT)
            .define(listOf("cleanup", "entities", "skip_persistent"), true)

        // Debug
        val debugBlockEntity: ModConfigSpec.BooleanValue = builder
            .translation(LangKeys.CONFIG_DEBUG_BLOCK_ENTITY)
            .define(listOf("debug", "block_entity_checker"), false)

        // 回收设置
        val recoveryExpireDays: ModConfigSpec.IntValue = builder
            .translation(LangKeys.CONFIG_RECOVERY_EXPIRE_DAYS)
            .defineInRange(listOf("cleanup", "recovery", "expire_days"), 1, 1, 30)
    }

    private val specPair = ModConfigSpec.Builder().configure(::Spec)
    val spec: ModConfigSpec = specPair.right
    private val SPEC: Spec = specPair.left

    // 缓存
    var enableWelcome = true; private set
    var itemsInterval = 1200; private set
    
    var itemsEnable = true; private set
    var itemBlacklistIds: Set<Item> = emptySet(); private set
    var itemBlacklistTags: Set<TagKey<Item>> = emptySet(); private set
    var itemExcludeIds: Set<Item> = emptySet(); private set
    var itemExcludeTags: Set<TagKey<Item>> = emptySet(); private set
    var itemSkipComponents: Set<DataComponentType<*>> = emptySet(); private set
    
    var entitiesEnable = false; private set
    var entityInterval = 6000; private set
    var entityWhitelistIds: Set<EntityType<*>> = emptySet(); private set
    var entityWhitelistTags: Set<TagKey<EntityType<*>>> = emptySet(); private set
    var entityExcludeIds: Set<EntityType<*>> = emptySet(); private set
    var entityExcludeTags: Set<TagKey<EntityType<*>>> = emptySet(); private set
    var entitySkipNamed = true; private set
    var entitySkipPersistent = true; private set
    var debugBlockEntity = false; private set
    var recoveryExpireDays = 1; private set
    private fun updateCache() {
        enableWelcome = SPEC.enableWelcome.get()
        itemsInterval = SPEC.itemsInterval.get()
        
        itemsEnable = SPEC.itemsEnable.get()
        val itemFilters = BlacklistFilter.parseItemFilters(SPEC.itemBlacklist.get())
        itemBlacklistIds = itemFilters.ids
        itemBlacklistTags = itemFilters.tags
        itemExcludeIds = itemFilters.excludeIds
        itemExcludeTags = itemFilters.excludeTags
        itemSkipComponents = BlacklistFilter.parseComponents(SPEC.itemSkipComponents.get())
        
        entitiesEnable = SPEC.entitiesEnable.get()
        entityInterval = SPEC.entityInterval.get()
        val entityFilters = BlacklistFilter.parseEntityFilters(SPEC.entityWhitelist.get())
        entityWhitelistIds = entityFilters.ids
        entityWhitelistTags = entityFilters.tags
        entityExcludeIds = entityFilters.excludeIds
        entityExcludeTags = entityFilters.excludeTags
        entitySkipNamed = SPEC.entitySkipNamed.get()
        entitySkipPersistent = SPEC.entitySkipPersistent.get()
        debugBlockEntity = SPEC.debugBlockEntity.get()
        recoveryExpireDays = SPEC.recoveryExpireDays.get()
    }

    fun register(modEventBus: IEventBus, modContainer: ModContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, spec, "$MODID/config.toml")
        modEventBus.addListener<ModConfigEvent.Loading> { if (it.config.spec == spec) updateCache() }
        modEventBus.addListener<ModConfigEvent.Reloading> { if (it.config.spec == spec) updateCache() }
    }
}