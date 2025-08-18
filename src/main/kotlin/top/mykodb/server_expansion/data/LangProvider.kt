package top.mykodb.server_expansion.data

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider
import top.mykodb.server_expansion.MODID

object LangProvider{
    const val configuration = "$MODID.configuration"

    class EnUs(output: PackOutput): LanguageProvider(output, MODID, "en_us") {
        override fun addTranslations() {
            add("$configuration.welcome", "Welcome Message")
            add("$configuration.enable_welcome", "Enable Welcome Message")
            add("$configuration.enable_welcome.tooltip","Enable server welcome message?")
            add("$configuration.welcomeList", "Set Welcome Messages")
            add("$configuration.cleanup", "Item Cleanup")
            add("$configuration.lang","setUpPrompts")
            add("$configuration.enable_cleanup", "Enable Item Cleanup")
            add("$configuration.enable_cleanup.tooltip","Enable dropped item cleanup?")
            add("$configuration.cleanup_interval", "Cleanup Interval")
            add("$configuration.cleanup_interval.tooltip","Cleanup interval (ticks)")
            add("$configuration.cleanup_blacklist", "Cleanup Blacklist")
        }
    }
    class ZhCn(output: PackOutput): LanguageProvider(output, MODID,"zh_cn") {
        override fun addTranslations() {
            add("$configuration.welcome","欢迎语")
            add("$configuration.enable_welcome","启用 欢迎语")
            add("$configuration.enable_welcome.tooltip","是否启用服务器欢迎语")
            add("$configuration.welcomeList","设置 欢迎语")
            add("$configuration.cleanup","掉落物清理")
            add("$configuration.lang","设置提示")
            add("$configuration.enable_cleanup","启用 掉落物清理")
            add("$configuration.enable_cleanup.tooltip","是否启用掉落物清理")
            add("$configuration.cleanup_interval","清理间隔")
            add("$configuration.cleanup_interval.tooltip","清理间隔(tick)")
            add("$configuration.cleanup_blacklist","清理黑名单")
        }
    }

}