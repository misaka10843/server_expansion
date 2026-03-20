package top.mykodb.server_expansion.module.debug

import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.level.ChunkDataEvent
import top.mykodb.server_expansion.Config
import top.mykodb.server_expansion.LOGGER

object BlockEntityDebugger {

    @SubscribeEvent
    fun onChunkSave(event: ChunkDataEvent.Save) {
        if (!Config.debugBlockEntity) return
        
        val chunk = event.chunk as? LevelChunk ?: return
        val level = chunk.level ?: return
        val chunkPos = chunk.pos

        chunk.blockEntities.values.forEach { be ->
            try {
                be.saveWithFullMetadata(level.registryAccess())
            } catch (e: Exception) {
                val pos = be.blockPos
                LOGGER.error("[BlockEntityDebugger] 发现损坏的方块实体: ${be.type} 位于 ${formatPos(pos, chunkPos)}")
                LOGGER.error("  世界: ${level.dimension().location()}  坐标: X=${pos.x}, Y=${pos.y}, Z=${pos.z}")
                LOGGER.error("  异常类型: ${e.javaClass.simpleName}  消息: ${e.message}")
            }
        }
    }

    private fun formatPos(pos: BlockPos, chunkPos: ChunkPos): String {
        val chunkLocalX = pos.x and 15
        val chunkLocalZ = pos.z and 15
        return "区块(${chunkPos.x}, ${chunkPos.z}) 区块内(${chunkLocalX}, ${pos.y}, ${chunkLocalZ}) 绝对(${pos.x}, ${pos.y}, ${pos.z})"
    }
}