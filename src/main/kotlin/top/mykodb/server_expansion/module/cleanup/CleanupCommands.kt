package top.mykodb.server_expansion.module.cleanup

import com.mojang.brigadier.Command
import java.util.Locale
import net.minecraft.commands.Commands
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.inventory.ChestMenu
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent
import top.mykodb.server_expansion.module.i18n.I18nHelper
import top.mykodb.server_expansion.module.i18n.LangKeys

object CleanupCommands {

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("cleanup")
                .then(Commands.literal("items")
                    .requires { it.hasPermission(0) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        if (player == null) {
                            ctx.source.sendFailure(I18nHelper.translateComponent(null, LangKeys.CLEANUP_CMD_PLAYER_ONLY))
                            return@executes 0
                        }
                        val result = CleanupManager.manualItemCleanup(player)
                        ctx.source.sendSuccess({
                            I18nHelper.translateComponent(
                                player, LangKeys.CLEANUP_ITEM_STATS,
                                result.stacks, result.items, String.format(Locale.ROOT, "%.2f", result.elapsedNs / 1_000_000.0)
                            )
                        }, true)
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(Commands.literal("entities")
                    .requires { it.hasPermission(0) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        if (player == null) {
                            ctx.source.sendFailure(I18nHelper.translateComponent(null, LangKeys.CLEANUP_CMD_PLAYER_ONLY))
                            return@executes 0
                        }
                        val result = CleanupManager.manualEntityCleanup(player)
                        ctx.source.sendSuccess({
                            I18nHelper.translateComponent(
                                player, LangKeys.CLEANUP_ENTITY_STATS,
                                result.count, String.format(Locale.ROOT, "%.2f", result.elapsedNs / 1_000_000.0)
                            )
                        }, true)
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(Commands.literal("rummage")
                    .requires { it.hasPermission(0) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        if (player != null) {
                            ItemCleaner.rummageToContainer()
                            if (ItemCleaner.hasDisplayItems()) {
                                openRecoveryContainer(player)
                            } else {
                                player.sendSystemMessage(I18nHelper.translateComponent(player, LangKeys.CLEANUP_RECOVERY_EMPTY))
                            }
                        } else ctx.source.sendFailure(I18nHelper.translateComponent(null, LangKeys.CLEANUP_CMD_PLAYER_ONLY))
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(Commands.literal("clear")
                    .requires { it.hasPermission(1) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        ItemCleaner.clearStorage()
                        ctx.source.sendSuccess({
                            I18nHelper.translateComponent(player, LangKeys.CLEANUP_RECOVERY_CLEARED)
                        }, true)
                        Command.SINGLE_SUCCESS
                    }
                )
        )
    }

    private fun openRecoveryContainer(player: ServerPlayer) {
        val container = ItemCleaner.getDisplayContainer()
        player.openMenu(SimpleMenuProvider(
            { id, inv, _ -> ChestMenu.sixRows(id, inv, container) },
            I18nHelper.translateComponent(player, LangKeys.CLEANUP_RECOVERY_TITLE)
        ))
    }

    // 回收箱关闭后回写容器内容，避免玩家取走的物品在重启后重新出现
    @SubscribeEvent
    fun onContainerClose(event: PlayerContainerEvent.Close) {
        if (event.entity.level().isClientSide) return
        val menu = event.container
        if (menu.slots.any { it.container === ItemCleaner.getDisplayContainer() }) {
            ItemCleaner.persistContainerState()
        }
    }
}
