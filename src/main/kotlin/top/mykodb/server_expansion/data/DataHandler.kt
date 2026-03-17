package top.mykodb.server_expansion.data

import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.data.event.GatherDataEvent


object DataHandler {

    fun onGatherData(event: GatherDataEvent) {
        val generator = event.generator
        val output = event.generator.packOutput

        // 客户端语言文件（配置界面）
        generator.addProvider(event.includeClient(), LangProvider.EnUs(output))
        generator.addProvider(event.includeClient(), LangProvider.ZhCn(output))

        // 服务端语言文件（ServerI18nAPI）
        generator.addProvider(event.includeServer(), ServerLangProvider.EnUs(output))
        generator.addProvider(event.includeServer(), ServerLangProvider.ZhCn(output))
    }
    fun register(modEventBus: IEventBus) {
        modEventBus.addListener(::onGatherData)
    }
}