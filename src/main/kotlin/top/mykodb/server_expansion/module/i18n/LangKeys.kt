package top.mykodb.server_expansion.module.i18n

import top.mykodb.server_expansion.MODID

object LangKeys {
    private const val CONFIG = "$MODID.configuration"
    
    // 配置分组
    const val CONFIG_TITLE = "$CONFIG.title"
    const val CONFIG_WELCOME = "$CONFIG.welcome"
    const val CONFIG_CLEANUP = "$CONFIG.cleanup"
    const val CONFIG_CLEANUP_ITEMS = "$CONFIG.items"
    const val CONFIG_CLEANUP_ENTITIES = "$CONFIG.entities"
    const val CONFIG_DEBUG = "$CONFIG.debug"
    
    // 欢迎模块配置
    const val CONFIG_ENABLE_WELCOME = "$CONFIG.enable_welcome"
    
    // 清理模块配置
    const val CONFIG_CLEANUP_INTERVAL = "$CONFIG.cleanup.interval"
    const val CONFIG_CLEANUP_ITEMS_ENABLE = "$CONFIG.cleanup.items.enable"
    const val CONFIG_CLEANUP_ITEMS_BLACKLIST = "$CONFIG.cleanup.items.blacklist"
    const val CONFIG_CLEANUP_ITEMS_SKIP_COMPONENTS = "$CONFIG.cleanup.items.skip_components"
    const val CONFIG_CLEANUP_ENTITIES_ENABLE = "$CONFIG.cleanup.entities.enable"
    const val CONFIG_CLEANUP_ENTITIES_INTERVAL = "$CONFIG.cleanup.entities.interval"
    const val CONFIG_CLEANUP_ENTITIES_WHITELIST = "$CONFIG.cleanup.entities.whitelist"
    const val CONFIG_CLEANUP_ENTITIES_SKIP_NAMED = "$CONFIG.cleanup.entities.skip_named"
    const val CONFIG_CLEANUP_ENTITIES_SKIP_PERSISTENT = "$CONFIG.cleanup.entities.skip_persistent"

    // Debug 配置
    const val CONFIG_DEBUG_BLOCK_ENTITY = "$CONFIG.debug.block_entity_checker"

    // 回收配置
    const val CONFIG_CLEANUP_RECOVERY = "$CONFIG.recovery"
    const val CONFIG_RECOVERY_EXPIRE_DAYS = "$CONFIG.recovery.expire_days"

    // 欢迎消息
    const val WELCOME_MESSAGE = "$MODID.welcome.message"

    // 清理模块消息
    const val CLEANUP_ITEM_WARNING = "$MODID.cleanup.item.warning"
    const val CLEANUP_ITEM_STATS = "$MODID.cleanup.item.stats"
    const val CLEANUP_ITEM_NONE = "$MODID.cleanup.item.none"
    const val CLEANUP_ENTITY_WARNING = "$MODID.cleanup.entity.warning"
    const val CLEANUP_ENTITY_STATS = "$MODID.cleanup.entity.stats"
    const val CLEANUP_ENTITY_NONE = "$MODID.cleanup.entity.none"
    const val CLEANUP_RECOVERY_EXPIRED = "$MODID.cleanup.recovery.expired"
    const val CLEANUP_RECOVERY_EMPTY = "$MODID.cleanup.recovery.empty"
    const val CLEANUP_CMD_PLAYER_ONLY = "$MODID.cleanup.cmd.player_only"
    const val CLEANUP_RECOVERY_TITLE = "$MODID.cleanup.recovery.title"
    const val CLEANUP_RECOVERY_CLEARED = "$MODID.cleanup.recovery.cleared"
}
