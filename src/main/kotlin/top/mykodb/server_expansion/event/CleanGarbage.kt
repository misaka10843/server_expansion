package top.mykodb.server_expansion.event

import com.mojang.brigadier.Command
import net.minecraft.commands.Commands
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BundleContents
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import top.mykodb.server_expansion.Config.SERVER
import top.mykodb.server_expansion.Config.cleanupBlacklist
import top.mykodb.server_expansion.Config.cleanupInterval
import top.mykodb.server_expansion.Config.enableCleanup
import top.mykodb.server_expansion.LOGGER
import kotlin.system.measureNanoTime


object CleanGarbage {
    val mutableContents = mutableListOf<ItemStack>()

    @SubscribeEvent
    fun onServerStopping(event: ServerStoppingEvent) {
        if (!enableCleanup) return
        mutableContents.clear()
    }
    @SubscribeEvent
    fun onServerTickIn(event: ServerTickEvent.Post){
        if (!enableCleanup) return
        val server = event.server
        val currentTime = server.overworld().gameTime
        val playerList = server.playerList.players

        var totalItems = 0L
        var totalEntities = 0
        var elapsed = 0L

        val is30SecondsBeforeCleanupBundle = (currentTime + 600) % 24000 == 0L
        val isCleanupBundleTime = currentTime % 24000 == 0L
        val is30SecondsBeforeCleanup = cleanupInterval > 600 &&(currentTime + 600) % cleanupInterval == 0L
        val isCleanupTime = currentTime % cleanupInterval == 0L

        if (isCleanupBundleTime){mutableContents.clear()}
        if (isCleanupTime) {
            elapsed = measureNanoTime {
                server.allLevels.forEach {
                    it.getEntities(EntityType.ITEM) { it -> it.isAlive && it.item.item !in cleanupBlacklist}
                        .forEach { it ->
                                totalEntities++
                                totalItems += it.item.count
                                mutableContents.add(it.item)
                                it.discard()
                        }
                }
            }
        }
        playerList.forEach {player->
            if (is30SecondsBeforeCleanupBundle){
                player.displayClientMessage( Component.literal(SERVER.is30SecondsBeforeCleanupBundle.get()),true)
            }
            if (isCleanupBundleTime){
                player.displayClientMessage( Component.literal(SERVER.isCleanupBundleTime.get()),true)
            }
            if (is30SecondsBeforeCleanup){
                player.displayClientMessage(Component.literal(SERVER.is30SecondsBeforeCleanup.get()),true)
            }
            if (isCleanupTime){
                if (totalEntities == 0) {
                    player.displayClientMessage(Component.literal(SERVER.noGarbage.get()),true)
                }else{
                    player.displayClientMessage(
                        Component.literal(
                            SERVER.cleanupStats.get()
                                .replace("[totalEntities]",totalEntities.toString())
                                .replace("[totalItems]",totalItems.toString())
                                .replace("[elapsed]",(elapsed/1000000.0).toString())
                        ),
                        true
                    )
                }
            }

        }
    }

    fun viewGarbage(player: ServerPlayer?) {
        val viewGarbage = SERVER.viewGarbage.get()
        val bundle = ItemStack(Items.BUNDLE)
        bundle.set(DataComponents.CUSTOM_NAME, Component.literal(viewGarbage ))
        bundle.set(DataComponents.BUNDLE_CONTENTS,BundleContents(this.mutableContents) )
        if (player==null){
            LOGGER.info(this.mutableContents.toString())
            return
        }
        player.sendSystemMessage(
            Component.literal(viewGarbage)
                .withStyle(Style.EMPTY.withHoverEvent(
                    HoverEvent(HoverEvent.Action.SHOW_ITEM, HoverEvent.ItemStackInfo (bundle))
                ))
                .withStyle(
                    Style.EMPTY.withClickEvent(
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/garbage get")
                    )
                )
        )
    }
    fun getItem(player: ServerPlayer?){
        if (player==null){ return }
        mutableContents.forEach {
            player.addItem(it)
        }
        mutableContents.clear()
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            Commands.literal("garbage")
                .then(Commands.literal("view")
                    .executes{
                        viewGarbage(it.source.player)
                        Command.SINGLE_SUCCESS
                    }
                )
                .then(Commands.literal("get")
                    .executes{
                        getItem(it.source.player)
                        Command.SINGLE_SUCCESS
                    }
                )
        )
    }
}