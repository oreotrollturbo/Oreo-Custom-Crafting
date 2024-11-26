package org.oreo.oreosCustomCrafting.menus

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.oreo.oreosCustomCrafting.menus.menuClasses.CustomCraftingInventory
import org.oreo.oreosCustomCrafting.menus.menuClasses.CustomCraftingInventory.Companion.openInventories

class   CustomInventoryListener : Listener {

    /**
     * Handles any clicking of items that shouldn't be moved.
     */
    @EventHandler
    fun handleInvalidClick(e: InventoryClickEvent) {

        val inv = e.clickedInventory ?: return

        val invInstance = AbstractInventoryMenu.getCustomInventory(inv) ?: return

        if (invInstance is CustomCraftingInventory) {

            e.isCancelled = invInstance.handleCraftingItemClicked(e.slot)

            return
        }

        e.isCancelled = true

        invInstance.handleClickedItem(e.slot)
    }


    /**
     * Make sure the object is properly deleted
     */
    @EventHandler
    fun playerCloseCraftingInv(e: InventoryCloseEvent) {

        val invInstance = AbstractInventoryMenu.getCustomInventory(e.inventory) ?: return

        openInventories.remove(invInstance.inventory)
    }
}