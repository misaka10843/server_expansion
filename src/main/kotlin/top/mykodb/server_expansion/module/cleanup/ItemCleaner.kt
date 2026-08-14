package top.mykodb.server_expansion.module.cleanup

import net.minecraft.core.component.DataComponentType
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import top.mykodb.server_expansion.Config
import kotlin.system.measureNanoTime


object ItemCleaner {

    class ReadOnlyContainer : SimpleContainer(54) {
        private var insertAllowed = false

        override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean = insertAllowed

        fun <T> withInsertAllowed(block: () -> T): T {
            insertAllowed = true
            return try {
                block()
            } finally {
                insertAllowed = false
            }
        }
    }

    private val displayContainer = ReadOnlyContainer()
    private val storageItems = mutableListOf<ItemStack>()
    private var savedData: CleanupSavedData? = null
    private var storedDays = 0

    data class CleanupResult(val stacks: Int, val items: Long, val elapsedNs: Long)

    fun init(savedData: CleanupSavedData) {
        this.savedData = savedData
        // 从 SavedData 加载已保存的物品
        storageItems.clear()
        storageItems.addAll(savedData.getStorageItems())
        // 恢复容器状态
        displayContainer.clearContent()
        displayContainer.withInsertAllowed {
            savedData.getContainerItems().forEach { stack ->
                displayContainer.addItem(stack.copy())
            }
        }
        // 加载已存储天数
        storedDays = savedData.getStoredDays()
    }

    fun incrementDay() {
        storedDays++
        savedData?.setStoredDays(storedDays)
    }

    fun isExpired(): Boolean = storedDays >= Config.recoveryExpireDays

    fun clean(
        server: MinecraftServer,
        blacklistIds: Set<Item>,
        blacklistTags: Set<TagKey<Item>>,
        excludeIds: Set<Item>,
        excludeTags: Set<TagKey<Item>>,
        skipComponents: Set<DataComponentType<*>>
    ): CleanupResult {
        var totalStacks = 0
        var totalItems = 0L
        val newItems = mutableListOf<ItemStack>()
        
        val elapsed = measureNanoTime {
            server.allLevels.forEach { level ->
                level.getEntities(EntityType.ITEM) { entity ->
                    !BlacklistFilter.shouldSkipItemEntity(
                        entity, blacklistIds, blacklistTags, excludeIds, excludeTags, skipComponents
                    )
                }.forEach { entity ->
                    totalStacks++
                    totalItems += entity.item.count
                    // storageItems 与 SavedData 各存独立副本，避免共享可变引用
                    storageItems.add(entity.item.copy())
                    newItems.add(entity.item.copy())
                    entity.discard()
                }
            }
        }
        
        // 保存到 SavedData
        savedData?.addItemsToStorage(newItems)
        
        return CleanupResult(totalStacks, totalItems, elapsed)
    }

    fun getDisplayContainer(): Container = displayContainer

    fun rummageToContainer() {
        displayContainer.withInsertAllowed {
            val iterator = storageItems.listIterator()
            while (iterator.hasNext()) {
                val stack = iterator.next()
                val remaining = displayContainer.addItem(stack)
                if (!remaining.isEmpty) {
                    iterator.set(remaining)
                    break
                }
                iterator.remove()
            }
        }
        // 同时持久化移出后的 storageItems 与容器内容，避免重启后物品重新出现
        savedData?.setItems(storageItems, getContainerItemsList())
    }

    // 回收箱关闭后回写容器当前内容，避免玩家取走的物品在重启后重新出现
    fun persistContainerState() {
        savedData?.setItems(storageItems, getContainerItemsList())
    }

    fun hasStorageItems(): Boolean = storageItems.isNotEmpty()

    fun hasDisplayItems(): Boolean = !displayContainer.isEmpty

    fun clearStorage() {
        displayContainer.clearContent()
        storageItems.clear()
        storedDays = 0
        savedData?.clear()
    }

    private fun getContainerItemsList(): List<ItemStack> {
        val items = mutableListOf<ItemStack>()
        for (i in 0 until displayContainer.containerSize) {
            val stack = displayContainer.getItem(i)
            if (!stack.isEmpty) {
                items.add(stack.copy())
            }
        }
        return items
    }
}
