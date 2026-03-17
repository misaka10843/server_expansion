package top.mykodb.server_expansion.module.i18n

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import top.mykodb.server_expansion.LOGGER

/**
 * 国际化辅助类
 * - 服务端装有 ServerI18nAPI 时：使用服务端翻译
 * - 服务端没有 ServerI18nAPI 时：回退到客户端语言文件
 */
object I18nHelper {

    /**
     * ServerI18nAPI 是否可用
     */
    val isServerI18nAvailable: Boolean by lazy {
        try {
            Class.forName("com.iafenvoy.server.i18n.ServerI18n")
            LOGGER.info("ServerI18nAPI detected, using server-side translations")
            true
        } catch (e: ClassNotFoundException) {
            LOGGER.info("ServerI18nAPI not found, falling back to client-side translations")
            false
        }
    }

    /**
     * 获取翻译文本
     * @param player 目标玩家（用于 ServerI18nAPI 获取玩家语言）
     * @param key 翻译键（已包含 MODID 前缀）
     * @param args 格式化参数
     * @return 翻译后的字符串
     */
    fun translate(player: ServerPlayer?, key: String, vararg args: String): String {
        return if (isServerI18nAvailable && player != null) {
            ServerI18nWrapper.translate(player, key, *args)
        } else {
            // 回退到客户端翻译键
            key
        }
    }

    /**
     * 获取翻译组件
     * @param player 目标玩家
     * @param key 翻译键
     * @param args 格式化参数
     * @return Component 组件
     */
    fun translateComponent(player: ServerPlayer?, key: String, vararg args: Any): Component {
        return if (isServerI18nAvailable && player != null) {
            Component.literal(ServerI18nWrapper.translate(player, key, *args.map { it.toString() }.toTypedArray()))
        } else {
            // 回退：使用可翻译组件，客户端会自动翻译
            Component.translatable(key, *args)
        }
    }
}

/**
 * ServerI18nAPI 包装器
 * 单独抽离，避免在 ServerI18nAPI 不存在时类加载失败
 */
private object ServerI18nWrapper {
    fun translate(player: ServerPlayer, key: String, vararg args: String): String {
        return com.iafenvoy.server.i18n.ServerI18n.translate(player, key, *args)
    }
}
