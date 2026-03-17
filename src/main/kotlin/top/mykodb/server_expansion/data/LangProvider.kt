package top.mykodb.server_expansion.data

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider
import top.mykodb.server_expansion.MODID
import top.mykodb.server_expansion.module.i18n.LangKeys

object LangProvider {

    class EnUs(output: PackOutput): LanguageProvider(output, MODID, "en_us") {
        override fun addTranslations() {
            add(LangKeys.CONFIG_TITLE, "Server Expansion")
            add(LangKeys.CONFIG_WELCOME, "Welcome")
            add(LangKeys.CONFIG_CLEANUP, "Cleanup")
            add(LangKeys.CONFIG_CLEANUP_ITEMS, "Items")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES, "Entities")
            add(LangKeys.CONFIG_ENABLE_WELCOME, "Enable Welcome Message")
            add(LangKeys.CONFIG_CLEANUP_ENABLE, "Enable Cleanup")
            add(LangKeys.CONFIG_CLEANUP_INTERVAL, "Cleanup Interval (ticks)")
            add(LangKeys.CONFIG_CLEANUP_ITEMS_ENABLE, "Enable Item Cleanup")
            add(LangKeys.CONFIG_CLEANUP_ITEMS_BLACKLIST, "Item Blacklist")
            add(LangKeys.CONFIG_CLEANUP_ITEMS_SKIP_COMPONENTS, "Skip Components")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_ENABLE, "Enable Entity Cleanup")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_INTERVAL, "Cleanup Interval (ticks)")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_WHITELIST, "Entity Whitelist")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_BLACKLIST_TAGS, "Entity Tag Blacklist")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_SKIP_NAMED, "Skip Named Entities")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_SKIP_PERSISTENT, "Skip Persistent Entities")
            add(LangKeys.WELCOME_MESSAGE, "Welcome %s, the little fox missed you!")
            add(LangKeys.CLEANUP_ITEM_WARNING, "Warning: Dropped items will be cleaned in 30 seconds!")
            add(LangKeys.CLEANUP_ITEM_STATS, "Cleaned %s stacks (%s items) in %sms")
            add(LangKeys.CLEANUP_ITEM_NONE, "No items to clean")
            add(LangKeys.CLEANUP_ENTITY_WARNING, "Warning: Entities will be cleaned in 30 seconds!")
            add(LangKeys.CLEANUP_ENTITY_STATS, "Cleaned %s entities in %sms")
            add(LangKeys.CLEANUP_ENTITY_NONE, "No entities to clean")
            add(LangKeys.CLEANUP_RECOVERY_EXPIRED, "Recoverable items have expired")
            add(LangKeys.CLEANUP_RECOVERY_EMPTY, "No items to recover")
            add(LangKeys.CLEANUP_CMD_PLAYER_ONLY, "This command can only be used by players")
        }
    }

    class ZhCn(output: PackOutput): LanguageProvider(output, MODID, "zh_cn") {
        override fun addTranslations() {
            add(LangKeys.CONFIG_TITLE, "服务端拓展")
            add(LangKeys.CONFIG_WELCOME, "欢迎语")
            add(LangKeys.CONFIG_CLEANUP, "清理")
            add(LangKeys.CONFIG_CLEANUP_ITEMS, "物品")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES, "实体")
            add(LangKeys.CONFIG_ENABLE_WELCOME, "启用欢迎语")
            add(LangKeys.CONFIG_CLEANUP_ENABLE, "启用清理")
            add(LangKeys.CONFIG_CLEANUP_INTERVAL, "清理间隔(tick)")
            add(LangKeys.CONFIG_CLEANUP_ITEMS_ENABLE, "启用物品清理")
            add(LangKeys.CONFIG_CLEANUP_ITEMS_BLACKLIST, "物品黑名单")
            add(LangKeys.CONFIG_CLEANUP_ITEMS_SKIP_COMPONENTS, "跳过组件")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_ENABLE, "启用实体清理")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_INTERVAL, "清理间隔(tick)")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_WHITELIST, "实体白名单")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_BLACKLIST_TAGS, "实体标签黑名单")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_SKIP_NAMED, "跳过命名实体")
            add(LangKeys.CONFIG_CLEANUP_ENTITIES_SKIP_PERSISTENT, "跳过持久实体")
            add(LangKeys.WELCOME_MESSAGE, "欢迎 %s 小主，小狐想你啦！")
            add(LangKeys.CLEANUP_ITEM_WARNING, "警告：30秒后将清理掉落物！")
            add(LangKeys.CLEANUP_ITEM_STATS, "清理物品 %s 组（%s 个），用时 %s 毫秒")
            add(LangKeys.CLEANUP_ITEM_NONE, "没有需要清理的物品")
            add(LangKeys.CLEANUP_ENTITY_WARNING, "警告：30秒后将清理实体！")
            add(LangKeys.CLEANUP_ENTITY_STATS, "清理实体 %s 个，用时 %s 毫秒")
            add(LangKeys.CLEANUP_ENTITY_NONE, "没有需要清理的实体")
            add(LangKeys.CLEANUP_RECOVERY_EXPIRED, "可回收的物品已过期")
            add(LangKeys.CLEANUP_RECOVERY_EMPTY, "没有可回收的物品")
            add(LangKeys.CLEANUP_CMD_PLAYER_ONLY, "该命令只能由玩家执行")
        }
    }
}