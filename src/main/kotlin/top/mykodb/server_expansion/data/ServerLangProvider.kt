package top.mykodb.server_expansion.data

import com.google.gson.JsonObject
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import top.mykodb.server_expansion.MODID
import top.mykodb.server_expansion.module.i18n.LangKeys
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * 服务端语言文件生成器，输出到 data/{modid}/lang/
 * 用于 ServerI18nAPI
 */
abstract class ServerLangProvider(
    private val output: PackOutput,
    private val locale: String
) : DataProvider {
    private val data: MutableMap<String, String> = TreeMap()

    protected fun add(key: String, value: String) {
        if (data.put(key, value) != null) {
            throw IllegalStateException("Duplicate translation key $key")
        }
    }

    protected abstract fun addTranslations()

    override fun getName(): String = "Server Languages: $locale"

    override fun run(cache: CachedOutput): CompletableFuture<*> {
        addTranslations()
        if (data.isEmpty()) return CompletableFuture.allOf()

        val path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
            .resolve(MODID)
            .resolve("lang")
            .resolve("$locale.json")

        val json = JsonObject()
        data.forEach { (key, value) -> json.addProperty(key, value) }
        return DataProvider.saveStable(cache, json, path)
    }




    class EnUs(output: PackOutput) : ServerLangProvider(output, "en_us") {
        override fun addTranslations() {
            add(LangKeys.WELCOME_MESSAGE, "Welcome %s, the little fox missed you!")
            add(LangKeys.CLEANUP_ITEM_WARNING, "Warning: Dropped items will be cleaned in 30 seconds!")
            add(LangKeys.CLEANUP_ITEM_STATS, "Cleaned %s stacks (%s items) in %sms")
            add(LangKeys.CLEANUP_ITEM_NONE, "No items to clean")
            add(LangKeys.CLEANUP_ENTITY_WARNING, "Warning: Entities will be cleaned in 30 seconds!")
            add(LangKeys.CLEANUP_ENTITY_STATS, "Cleaned %s entities in %sms")
            add(LangKeys.CLEANUP_ENTITY_NONE, "No entities to clean")
            add(LangKeys.CLEANUP_RECOVERY_EXPIRED, "Recoverable items have expired")
            add(LangKeys.CLEANUP_RECOVERY_EMPTY, "No items to recover")
            add(LangKeys.CLEANUP_CMD_PLAYER_ONLY, "This command can only be used by players")
        }
    }

    class ZhCn(output: PackOutput) : ServerLangProvider(output, "zh_cn") {
        override fun addTranslations() {
            add(LangKeys.WELCOME_MESSAGE, "欢迎 %s 小主，小狐想你啦！")
            add(LangKeys.CLEANUP_ITEM_WARNING, "警告：30秒后将清理掉落物！")
            add(LangKeys.CLEANUP_ITEM_STATS, "清理物品 %s 组（%s 个），用时 %s 毫秒")
            add(LangKeys.CLEANUP_ITEM_NONE, "没有需要清理的物品")
            add(LangKeys.CLEANUP_ENTITY_WARNING, "警告：30秒后将清理实体！")
            add(LangKeys.CLEANUP_ENTITY_STATS, "清理实体 %s 个，用时 %s 毫秒")
            add(LangKeys.CLEANUP_ENTITY_NONE, "没有需要清理的实体")
            add(LangKeys.CLEANUP_RECOVERY_EXPIRED, "可回收的物品已过期")
            add(LangKeys.CLEANUP_RECOVERY_EMPTY, "没有可回收的物品")
            add(LangKeys.CLEANUP_CMD_PLAYER_ONLY, "该命令只能由玩家执行")
        }
    }
}
