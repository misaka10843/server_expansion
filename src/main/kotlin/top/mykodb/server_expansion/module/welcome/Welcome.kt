package top.mykodb.server_expansion.module.welcome

import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import top.mykodb.server_expansion.Config
import top.mykodb.server_expansion.module.i18n.I18nHelper
import top.mykodb.server_expansion.module.i18n.LangKeys

object Welcome {
    @SubscribeEvent
    fun onLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        if (!Config.enableWelcome) return
        player.sendSystemMessage(
            I18nHelper.translateComponent(player, LangKeys.WELCOME_MESSAGE, player.displayName?.string ?: player.name.string)
        )

    }

}