package top.mykodb.server_expansion

import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.config.ModConfigEvent
import net.neoforged.fml.event.config.ModConfigEvent.Reloading
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.ModConfigSpec
import org.apache.commons.lang3.tuple.Pair




object Config {

    class Server(builder:ModConfigSpec.Builder){

        val enableWelcome: ModConfigSpec.BooleanValue = builder
            .define(listOf("welcome","enable_welcome"), true)

        val welcomeList: ModConfigSpec.ConfigValue<List<String>> = builder
            .defineListAllowEmpty(
                listOf("welcome","welcomeList"),
                listOf(
                    "欢迎 §c[player]§r 小主，小狐想你啦！"
                ),
                {""},
                { it is String}
            )

        val enableCleanup: ModConfigSpec.BooleanValue = builder
            .define(listOf("cleanup","enable_cleanup"), true)

        val cleanupInterval: ModConfigSpec.ConfigValue<Int> = builder
            .defineInRange(listOf("cleanup","cleanup_interval"),1200,1,Int.MAX_VALUE )
        //黑名单
        val cleanupBlacklist: ModConfigSpec.ConfigValue<List<String>> = builder
            .defineListAllowEmpty(
                listOf("cleanup","cleanup_blacklist"),
                listOf(
                    "minecraft:dragon_egg"
                )
                ,
                {""},
                { it is String && it.count { c -> c == ':' } == 1 && it.indexOf(':').let { idx -> idx > 0 && idx < it.length - 1 } }
            )

        // Lang 自定义提示语
        val cleanupStats: ModConfigSpec.ConfigValue<String> = builder
            .define(listOf("cleanup","lang","cleanup_stats"),"清理物品[totalEntities]组，累计物品[totalItems]个；清理用时[elapsed]毫秒。")
        val noGarbage: ModConfigSpec.ConfigValue<String> = builder
            .define(listOf("cleanup","lang","no_garbage"),"清理完成，没有垃圾哦")
        val viewGarbage: ModConfigSpec.ConfigValue<String> = builder
            .define(listOf("cleanup","lang","view_garbage"),"[查看清理物品]")
        val isCleanupBundleTime : ModConfigSpec.ConfigValue<String> = builder
            .define(listOf("cleanup","lang","isCleanupBundleTime"),"垃圾袋被清理了！")
        val is30SecondsBeforeCleanupBundle: ModConfigSpec.ConfigValue<String> = builder
            .define(listOf("cleanup","lang","is30SecondsBeforeCleanupBundle"),"警告:30秒后将清理垃圾袋。")
        val is30SecondsBeforeCleanup: ModConfigSpec.ConfigValue<String> = builder
            .define(listOf("cleanup","lang","is30SecondsBeforeCleanup"),"警告:30秒后将清理所有掉落物!")
    }

    private var serverPair: Pair<Server, ModConfigSpec> = ModConfigSpec.Builder().configure(::Server)
    val serverSpec: ModConfigSpec = serverPair.getRight()
    val SERVER: Server = serverPair.getLeft()


    var enableWelcome: Boolean  = true
    var enableCleanup: Boolean = true
    var cleanupInterval: Int = 0
    var welcomeList: List<String> = emptyList()
    var cleanupBlacklist: Set<Item> = setOf()

    //缓存
    fun updateCache(){
        enableWelcome = SERVER.enableWelcome.get()
        cleanupInterval = SERVER.cleanupInterval.get()
        enableCleanup = SERVER.enableCleanup.get()
        welcomeList = SERVER.welcomeList.get()
        cleanupBlacklist = parseItemSet(SERVER.cleanupBlacklist.get())

    }
    private fun parseItemSet(ids: List<String>): Set<Item> = ids.mapNotNull { id ->
        id.split(":", limit = 2).takeIf { it.size == 2 }
            ?.let { (namespace, path) ->
                ResourceLocation.tryParse("$namespace:$path")
                    ?.let { BuiltInRegistries.ITEM.get(it) }
                    ?.takeIf { it != Items.AIR }
            }
    }.toSet()


    fun clientSetup (event: FMLClientSetupEvent, container:ModContainer){
        event.enqueueWork {
            container.registerExtensionPoint(
                IConfigScreenFactory::class.java,
                IConfigScreenFactory { mod: ModContainer, parent: Screen -> ConfigurationScreen(mod, parent) }
            )
        }
    }

    fun onLoad(event: ModConfigEvent.Loading) {
        updateCache()
    }

    fun onFileChange(event: Reloading) {
        updateCache()
    }

    fun register(modEventBus: IEventBus,modContainer:ModContainer){
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.serverSpec,"$MODID/server_config.toml")
        modEventBus.apply {
            addListener<FMLClientSetupEvent>{clientSetup(it,modContainer)}
            addListener(::onLoad)
            addListener(::onFileChange)
        }

    }

}