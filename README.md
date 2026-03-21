# Server Expansion

Minecraft 1.21.1 + NeoForge 服务端扩展模组

## 功能

| 功能     | 说明                           |
|--------|------------------------------|
| 自动清理   | 定时清理掉落物/实体，支持黑白名单、标签过滤、组件过滤  |
| 物品回收   | 清理的掉落物暂存可配置天数（默认游戏内1天），可随时找回 |
| 欢迎语    | 玩家登录时发送自定义欢迎消息               |
| 方块实体调试 | 检测并报告损坏的方块实体(实验性)            |

## 命令

| 命令                  | 权限 | 说明        |
|---------------------|----|-----------|
| `/cleanup items`    | 0  | 手动清理掉落物   |
| `/cleanup entities` | 0  | 手动清理实体    |
| `/cleanup rummage`  | 0  | 打开回收箱找回物品 |
| `/cleanup clear`    | 1  | 清空回收列表    |

## 配置

配置文件位于 `config/server_expansion/config.toml`：

```toml
# 欢迎语
[welcome]
enable_welcome = true

# 物品清理
[cleanup.items]
enable = true
interval = 6000  # 5分钟
blacklist = ["minecraft:dragon_egg", "#minecraft:flowers", "!minecraft:diamond"]
skip_components = ["custom_name", "enchantments"]

# 实体清理
[cleanup.entities]
enable = false
interval = 6000
whitelist = ["minecraft:experience_orb", "minecraft:arrow", "!minecraft:villager"]
skip_named = true
skip_persistent = true

# 回收设置
[cleanup.recovery]
expire_days = 1  # 回收物品保存天数（1-30）
```

支持游戏内配置界面。


