package top.mykodb.server_expansion.module.cleanup

import net.minecraft.core.component.DataComponentType
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.TagKey
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import kotlin.system.measureNanoTime


object ItemCleaner {

    class ReadOnlyContainer : SimpleContainer(54) {
        override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean = false
    }

    private val displayContainer = ReadOnlyContainer()
    private val storageItems = mutableListOf<ItemStack>()
    private var savedData: CleanupSavedData? = null
    
    data class CleanupResult(val stacks: Int, val items: Long, val elapsedNs: Long)

    fun init(savedData: CleanupSavedData) {
        this.savedData = savedData
        // 从 SavedData 加载已保存的物品
        storageItems.clear()
        storageItems.addAll(savedData.getStorageItems())
        // 恢复容器状态
        displayContainer.clearContent()
        savedData.getContainerItems().forEach { stack ->
            displayContainer.addItem(stack.copy())
        }
    }

    fun clean(
        server: MinecraftServer,
        blacklistIds: Set<Item>,
        blacklistTags: Set<TagKey<Item>>,
        skipComponents: Set<DataComponentType<*>>
    ): CleanupResult {
        var totalStacks = 0
        var totalItems = 0L
        val newItems = mutableListOf<ItemStack>()
        
        val elapsed = measureNanoTime {
            server.allLevels.forEach { level ->
                level.getEntities(EntityType.ITEM) { entity ->
                    !BlacklistFilter.shouldSkipItemEntity(
                        entity, blacklistIds, blacklistTags, skipComponents
                    )
                }.forEach { entity ->
                    totalStacks++
                    totalItems += entity.item.count
                    val copy = entity.item.copy()
                    storageItems.add(copy)
                    newItems.add(copy)
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
        // 保存容器状态到 SavedData
        saveContainerState()
    }

    private fun saveContainerState() {
        val items = mutableListOf<ItemStack>()
        for (i in 0 until displayContainer.containerSize) {
            val stack = displayContainer.getItem(i)
            if (!stack.isEmpty) {
                items.add(stack.copy())
            }
        }
        savedData?.updateContainer(items)
    }

    fun removeFromStorage(stack: ItemStack) {
        val iterator = storageItems.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (ItemStack.isSameItemSameComponents(item, stack)) {
                if (item.count <= stack.count) {
                    iterator.remove()
                } else {
                    item.shrink(stack.count)
                }
                savedData?.setItems(storageItems, getContainerItemsList())
                return
            }
        }
    }

    fun hasStorageItems(): Boolean = storageItems.isNotEmpty()

    fun hasDisplayItems(): Boolean = !displayContainer.isEmpty

    fun clearStorage() {
        displayContainer.clearContent()
        storageItems.clear()
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
