package top.mykodb.server_expansion.module.cleanup

import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import top.mykodb.server_expansion.Config
import top.mykodb.server_expansion.module.i18n.I18nHelper
import top.mykodb.server_expansion.module.i18n.LangKeys

object CleanupManager {

    @SubscribeEvent
    fun onServerStarted(event: ServerStartedEvent) {
        val dataStorage = event.server.overworld().dataStorage
        val savedData = dataStorage.computeIfAbsent(
            net.minecraft.world.level.saveddata.SavedData.Factory(
                { CleanupSavedData.create() },
                { tag, provider -> CleanupSavedData.load(tag, provider) }
            ),
            CleanupSavedData.FILE_NAME
        )
        ItemCleaner.init(savedData)
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent.Post) {
        val server = event.server
        val currentTime = server.overworld().gameTime
        val players = server.playerList.players

        fun broadcast(key: String, vararg args: Any) = players.forEach {
            it.displayClientMessage(I18nHelper.translateComponent(it, key, *args), true)
        }

        // 物品清理
        if (Config.itemsEnable) {
            val interval = Config.itemsInterval
            val is30sBefore = interval > 600 && (currentTime + 600) % interval == 0L
            val isCleanupTime = currentTime % interval == 0L
            
            if (is30sBefore) broadcast(LangKeys.CLEANUP_ITEM_WARNING)
            
            if (isCleanupTime) {
                val result = ItemCleaner.clean(
                    server,
                    Config.itemBlacklistIds, Config.itemBlacklistTags,
                    Config.itemExcludeIds, Config.itemExcludeTags,
                    Config.itemSkipComponents
                )
                if (result.stacks == 0) broadcast(LangKeys.CLEANUP_ITEM_NONE)
                else broadcast(LangKeys.CLEANUP_ITEM_STATS, result.stacks, result.items, result.elapsedNs / 1_000_000.0)
            }
        }
        
        // 实体清理
        if (Config.entitiesEnable) {
            val interval = Config.entityInterval
            val is30sBefore = interval > 600 && (currentTime + 600) % interval == 0L
            val isCleanupTime = currentTime % interval == 0L
            
            if (is30sBefore) broadcast(LangKeys.CLEANUP_ENTITY_WARNING)
            
            if (isCleanupTime) {
                val result = EntityCleaner.clean(
                    server,
                    Config.entityWhitelistIds, Config.entityWhitelistTags,
                    Config.entityExcludeIds, Config.entityExcludeTags,
                    Config.entitySkipNamed, Config.entitySkipPersistent
                )
                if (result.count == 0) broadcast(LangKeys.CLEANUP_ENTITY_NONE)
                else broadcast(LangKeys.CLEANUP_ENTITY_STATS, result.count, result.elapsedNs / 1_000_000.0)
            }
        }

        // 回收列表过期清理 (每游戏天检查)
        if (currentTime % 24000 == 0L) {
            ItemCleaner.incrementDay()
            if (ItemCleaner.isExpired()) {
                val hadItems = ItemCleaner.hasStorageItems()
                ItemCleaner.clearStorage()
                if (hadItems) broadcast(LangKeys.CLEANUP_RECOVERY_EXPIRED)
            }
        }
    }

    fun manualItemCleanup(player: ServerPlayer?): ItemCleaner.CleanupResult {
        return ItemCleaner.clean(
            player?.server ?: return ItemCleaner.CleanupResult(0, 0, 0),
            Config.itemBlacklistIds, Config.itemBlacklistTags,
            Config.itemExcludeIds, Config.itemExcludeTags,
            Config.itemSkipComponents
        )
    }

    fun manualEntityCleanup(player: ServerPlayer?): EntityCleaner.CleanupResult {
        return EntityCleaner.clean(
            player?.server ?: return EntityCleaner.CleanupResult(0, 0),
            Config.entityWhitelistIds, Config.entityWhitelistTags,
            Config.entityExcludeIds, Config.entityExcludeTags,
            Config.entitySkipNamed, Config.entitySkipPersistent
        )
    }
}
