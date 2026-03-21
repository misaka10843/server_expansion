package top.mykodb.server_expansion

import com.mojang.logging.LogUtils
import net.minecraft.client.gui.screens.Screen
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.NeoForge
import org.slf4j.Logger
import top.mykodb.server_expansion.data.DataHandler
import top.mykodb.server_expansion.module.debug.BlockEntityDebugger
import top.mykodb.server_expansion.module.cleanup.CleanupCommands
import top.mykodb.server_expansion.module.cleanup.CleanupManager
import top.mykodb.server_expansion.module.welcome.Welcome

const val MODID: String = "server_expansion"
val LOGGER: Logger = LogUtils.getLogger()

@Mod(value = MODID)
class Mod(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        NeoForge.EVENT_BUS.register(Welcome)
        NeoForge.EVENT_BUS.register(CleanupManager)
        NeoForge.EVENT_BUS.register(CleanupCommands)
        NeoForge.EVENT_BUS.register(BlockEntityDebugger)
        DataHandler.register(modEventBus)
        Config.register(modEventBus, modContainer)
    }
}

@Mod(value = MODID, dist = [Dist.CLIENT])
class ModClient(container: ModContainer) {
    init {
        container.registerExtensionPoint(
            IConfigScreenFactory::class.java,
            IConfigScreenFactory { mod: ModContainer, parent: Screen -> ConfigurationScreen(mod, parent) }
        )
    }
}