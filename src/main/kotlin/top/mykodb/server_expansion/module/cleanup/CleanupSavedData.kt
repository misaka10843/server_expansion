package top.mykodb.server_expansion.module.cleanup

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.saveddata.SavedData

class CleanupSavedData : SavedData() {

    private val storageItems = mutableListOf<ItemStack>()
    private val containerItems = mutableListOf<ItemStack>()
    private var storedDays = 0

    fun getStorageItems(): List<ItemStack> = storageItems.toList()

    fun getContainerItems(): List<ItemStack> = containerItems.toList()

    fun getStoredDays(): Int = storedDays

    fun setStoredDays(days: Int) {
        storedDays = days
        setDirty()
    }

    fun setItems(storage: List<ItemStack>, container: List<ItemStack>) {
        storageItems.clear()
        storageItems.addAll(storage)
        containerItems.clear()
        containerItems.addAll(container)
        setDirty()
    }

    fun addItemsToStorage(items: List<ItemStack>) {
        storageItems.addAll(items)
        setDirty()
    }

    fun clear() {
        storageItems.clear()
        containerItems.clear()
        storedDays = 0
        setDirty()
    }

    fun isEmpty(): Boolean = storageItems.isEmpty() && containerItems.isEmpty()

    override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        // 保存 storageItems
        val storageList = ListTag()
        storageItems.forEach { stack ->
            if (!stack.isEmpty) {
                storageList.add(stack.save(provider))
            }
        }
        tag.put("storage_items", storageList)

        // 保存 containerItems
        val containerList = ListTag()
        containerItems.forEach { stack ->
            if (!stack.isEmpty) {
                containerList.add(stack.save(provider))
            }
        }
        tag.put("container_items", containerList)
        tag.putInt("stored_days", storedDays)

        return tag
    }

    companion object {
        fun create(): CleanupSavedData = CleanupSavedData()

        fun load(tag: CompoundTag, provider: HolderLookup.Provider): CleanupSavedData {
            val data = create()

            // 加载 storageItems
            val storageList = tag.getList("storage_items", 10)
            for (i in 0 until storageList.size) {
                ItemStack.parse(provider, storageList.getCompound(i)).ifPresent {
                    if (!it.isEmpty) data.storageItems.add(it)
                }
            }

            // 加载 containerItems
            val containerList = tag.getList("container_items", 10)
            for (i in 0 until containerList.size) {
                ItemStack.parse(provider, containerList.getCompound(i)).ifPresent {
                    if (!it.isEmpty) data.containerItems.add(it)
                }
            }

            // 加载存储天数
            data.storedDays = tag.getInt("stored_days")

            return data
        }

        const val FILE_NAME = "cleanup_recoverable_items"
    }
}
