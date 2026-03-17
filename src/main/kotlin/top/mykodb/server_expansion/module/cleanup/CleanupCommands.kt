package top.mykodb.server_expansion.module.cleanup

import com.mojang.brigadier.Command
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.inventory.ChestMenu
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import top.mykodb.server_expansion.module.i18n.I18nHelper
import top.mykodb.server_expansion.module.i18n.LangKeys

object CleanupCommands {

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("cleanup")
                .then(Commands.literal("items")
                    .requires { it.hasPermission(1) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        val result = CleanupManager.manualItemCleanup(player)
                        ctx.source.sendSuccess({
                            I18nHelper.translateComponent(
                                player, LangKeys.CLEANUP_ITEM_STATS,
                                result.stacks, result.items, result.elapsedNs / 1_000_000.0
                            )
                        }, true)
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(Commands.literal("entities")
                    .requires { it.hasPermission(1) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        val result = CleanupManager.manualEntityCleanup(player)
                        ctx.source.sendSuccess({
                            I18nHelper.translateComponent(
                                player, LangKeys.CLEANUP_ENTITY_STATS,
                                result.count, result.elapsedNs / 1_000_000.0
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
        )
    }

    private fun openRecoveryContainer(player: ServerPlayer) {
        val container = ItemCleaner.getDisplayContainer()
        player.openMenu(SimpleMenuProvider(
            { id, inv, _ -> ChestMenu.sixRows(id, inv, container) },
            Component.literal("回收物品")
        ))
    }
}
