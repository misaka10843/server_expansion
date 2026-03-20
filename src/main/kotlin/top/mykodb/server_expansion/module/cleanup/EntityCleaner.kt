package top.mykodb.server_expansion.module.cleanup

import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import kotlin.system.measureNanoTime

/**
 * 实体清理器 - 负责清理生物实体
 */
object EntityCleaner {
    
    data class CleanupResult(val count: Int, val elapsedNs: Long)

    /**
     * 执行实体清理
     */
    fun clean(
        server: MinecraftServer,
        whitelistIds: Set<EntityType<*>>,
        whitelistTags: Set<TagKey<EntityType<*>>>,
        excludeIds: Set<EntityType<*>>,
        excludeTags: Set<TagKey<EntityType<*>>>,
        skipNamed: Boolean,
        skipPersistent: Boolean
    ): CleanupResult {
        var totalCount = 0
        
        val elapsed = measureNanoTime {
            server.allLevels.forEach { level ->
                level.allEntities
                    .filter { entity -> shouldClean(entity, whitelistIds, whitelistTags, excludeIds, excludeTags, skipNamed, skipPersistent) }
                    .forEach { entity ->
                        totalCount++
                        entity.discard()
                    }
            }
        }
        
        return CleanupResult(totalCount, elapsed)
    }

    private fun shouldClean(
        entity: Entity,
        whitelistIds: Set<EntityType<*>>,
        whitelistTags: Set<TagKey<EntityType<*>>>,
        excludeIds: Set<EntityType<*>>,
        excludeTags: Set<TagKey<EntityType<*>>>,
        skipNamed: Boolean,
        skipPersistent: Boolean
    ): Boolean {
        // 跳过玩家和掉落物（掉落物由 ItemCleaner 处理）
        if (entity is Player || entity is ItemEntity) return false
        
        return !BlacklistFilter.shouldSkipEntity(
            entity, whitelistIds, whitelistTags, excludeIds, excludeTags, skipNamed, skipPersistent
        )
    }
}
