package top.mykodb.server_expansion.data

import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.data.event.GatherDataEvent


object DataHandler {

    fun onGatherData(event: GatherDataEvent) {
        val generator = event.generator
        val output = event.generator.packOutput

        // 添加语言文件生成器
        generator.addProvider(event.includeClient(), LangProvider.EnUs(output))
        generator.addProvider(event.includeClient(), LangProvider.ZhCn(output))

    }
    fun register(modEventBus: IEventBus) {
        modEventBus.addListener(::onGatherData)
    }
}