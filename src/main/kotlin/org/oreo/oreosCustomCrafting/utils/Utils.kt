package org.oreo.oreosCustomCrafting.utils

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.oreo.oreosCustomCrafting.CustomCrafting
import java.io.File

object Utils {


    /**
     * Saves the current item as a file to save on the system
     */
    fun saveCustomItemAsFile(item: ItemStack, plugin: CustomCrafting): File? {
        // Initialize Gson with pretty printing for better readability

        // Serialize the ItemStack
        val itemSerialized: String = SerializeUtils.serializeItem(item)

        // Ensure itemDir is not null and directory exists
        if (plugin.itemDir == null) {
            plugin.logger.warning("Item directory is not set!")
            return null
        }

        val directory = File(plugin.itemDir!!.path)
        if (!directory.exists()) {
            directory.mkdirs()  // Create directories if they don't exist
        }

        //Naming the file with a flat number for now
        val fileNumber: Int = plugin.itemDir!!.listFiles()!!.size - 1
        // Create the file
        val newFile = File(directory, "$fileNumber.txt")
        try {
            // Write serialized item to the file
            newFile.writeText(itemSerialized)

            CustomCrafting.customItems["$fileNumber.txt"] = item

            plugin.logger.info("Custom item file created successfully!")
            return newFile
        } catch (e: Exception) {
            plugin.logger.warning("Failed to write custom item to file: ${e.message}")
        }

        return null
    }

    /**
     * Searches through the list and gets an item with the corresponding name
     */
    fun getCustomItem(itemName: String): ItemStack {

        return CustomCrafting.customItems[itemName]
            ?: throw IllegalArgumentException("Item '$itemName' not found in custom items.")
    }


    /**
     * Checks if the item has any custom properties
     */
    fun isCustomItem(item: ItemStack): Boolean {
        val meta = item.itemMeta ?: return false
        return meta.hasEnchants() || meta.hasLore() || meta.hasDisplayName()
    }

    /**
     * Checks if the item exists already
     */
    fun customItemExists(customItem: ItemStack): Boolean {
        return CustomCrafting.customItems.values.contains(customItem)
    }

    /**
     * @param material set the material it should be (what item)
     * @param name How it should be called like any renamed item from an anvil
     * @param lore This is to set extra text under the name itself it is optional
     * @return it just returns the item now created
     * This is a helper method to create items for a gui
     */
    fun createGuiItem(material: Material, name: String, vararg lore: String?): ItemStack {
        val item = ItemStack(material, 1)
        val meta = checkNotNull(item.itemMeta)

        meta.setDisplayName(name)
        if (lore.isEmpty()) {
            meta.lore = listOf(*lore)
        }

        item.setItemMeta(meta)
        return item
    }

    fun createGuiItem(
        item: ItemStack,
        name: String,
        prefix: String? = null,
        vararg lore: String?,
        addEnchantGlint: Boolean = false
    ): ItemStack {
        val meta = item.itemMeta ?: return item // Safely handle null meta by returning the original item

        val itemName = if (prefix != null) {
            prefix + name
        } else {
            name
        }

        meta.setDisplayName(itemName)

        // Only set lore if there are non-null, non-empty entries
        val filteredLore = lore.filterNotNull().filter { it.isNotEmpty() }
        if (filteredLore.isNotEmpty()) {
            meta.lore = filteredLore
        }

        if (addEnchantGlint) {
            meta.addEnchant(Enchantment.LUCK, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS) // Hide the enchantment from the item tooltip
        }

        item.itemMeta = meta
        return item
    }



}