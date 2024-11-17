package org.oreo.oreosCustomCrafting.menus.recipeTogglingMenu

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.Recipe
import org.oreo.oreosCustomCrafting.CustomCrafting
import org.oreo.oreosCustomCrafting.utils.Utils

class RecipeInventory(val player: Player,val type: ViewType, val showOnlyCustom : Boolean) {

    private val rows = 5
    private val columns = 9
    private val invSize = rows * columns
    private val craftingInvName = "Recipe settings"
    private val craftingInv = Bukkit.createInventory(null, invSize, craftingInvName)

    private val itemsPerPage = invSize - columns // Reserve last row for navigation
    private var currentPage : Int = 0

    var slotToRecipe : MutableMap<Int, Recipe> = mutableMapOf()

    init {
        loadPage(0)
        openInventory()
    }


    /**
     * Loads a specified page of recipes into the crafting inventory.
     * @param page The page number to load (0-based).
     */
    fun loadPage(page: Int) {

        if (page < 0) throw IllegalArgumentException("Page can't be negative")

        // Update recipes based on current ViewType to reflect any changes
        val recipes: List<Recipe> = if (showOnlyCustom) {
            when (type) {
                // ViewType.ENABLED: Only get enabled recipes (those not in disabledRecipes)
                ViewType.ENABLED -> CustomCrafting.customRecipes
                    .filterNot { it.recipe in CustomCrafting.disabledRecipes }
                    .map { it.recipe }

                // ViewType.DISABLED: Only get disabled recipes (those in disabledRecipes)
                ViewType.DISABLED -> CustomCrafting.customRecipes
                    .filter { it.recipe in CustomCrafting.disabledRecipes }
                    .map { it.recipe }

                // ViewType.ALL: Get all recipes, regardless of their state
                ViewType.ALL -> CustomCrafting.customRecipes.map { it.recipe }
            }
        } else {
            when (type) {
                // ViewType.ENABLED: Get all enabled recipes (those not in disabledRecipes)
                ViewType.ENABLED -> CustomCrafting.allRecipesSaved
                    .filterNot { it in CustomCrafting.disabledRecipes }

                // ViewType.DISABLED: Combine disabledRecipes and non-enabled recipes from customRecipes
                ViewType.DISABLED -> CustomCrafting.disabledRecipes.plus(
                    CustomCrafting.customRecipes
                        .filter { it.recipe in CustomCrafting.disabledRecipes }
                        .map { it.recipe }
                )

                // ViewType.ALL: Get all saved recipes, including custom and disabled ones
                ViewType.ALL -> CustomCrafting.allRecipesSaved
            }
        }




        slotToRecipe.clear()
        currentPage = page
        craftingInv.clear() // Clear the inventory before loading the new page

        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, recipes.size)

        var i = startIndex
        var recipeNumber = i
        while (i < endIndex) {
            val slot = i - startIndex

            val recipe: Recipe = try {
                recipes[recipeNumber]
            } catch (e : IndexOutOfBoundsException) {
                break
            }


            if (recipe.result.type == Material.AIR) {
                recipeNumber++
                continue
            }



            // Update item status based on whether it’s enabled or disabled
            val isDisabled : Boolean = CustomCrafting.disabledRecipes.contains(recipe)

            val statusName = if (isDisabled) "Disabled" else "Enabled"
            val statusPrefix = if (isDisabled) "§l§c" else "§l§a"

            val itemResult = Utils.createGuiItem(
                item = recipe.result,
                name = statusName,
                prefix = statusPrefix,
                recipe.result.itemMeta?.displayName
            )

            slotToRecipe[slot] = recipe
            craftingInv.setItem(slot, itemResult)
            recipeNumber++
            i++
        }

        // Set navigation items in the last row
        if (currentPage > 0) {
            craftingInv.setItem(invSize - 7, Utils.createGuiItem(Material.CRIMSON_SIGN, "Previous", null))
        }
        if (!hasBlank()) {
            craftingInv.setItem(invSize - 3, Utils.createGuiItem(Material.WARPED_SIGN, "Next", null))
        }
    }

    /**
     * Opens the custom crafting inventory for a player, and write the object into the list
     */
    private fun openInventory() {
        val newInventory = craftingInv
        player.openInventory(newInventory)
        openInventories[newInventory] = this
    }

    /**
     * Closes the custom crafting inventory for a player and remove its references
     */
    fun closeInventory() {
        openInventories.remove(craftingInv)
        try {
            craftingInv.close()
        } catch (_: Exception){}
    }

    /**
     * Handle any item being clicked
     */
    fun handleClickedItem(slot : Int){

        val item = craftingInv.getItem(slot) ?: return

        val name = item.itemMeta?.displayName ?: return

        if (name.contains("Next")){
            loadPage(currentPage + 1)
        } else if (name.contains("Previous")){
            loadPage(currentPage-1)
        } else {

            val recipe = slotToRecipe[slot] ?: return

            if (name.contains("Enabled")) {

                if (!CustomCrafting.disabledRecipes.contains(recipe)) {
                    CustomCrafting.disabledRecipes.add(recipe)
                }
                craftingInv.setItem(
                    slot, Utils.createGuiItem(
                        item = item, name = "Disabled",
                        prefix = "§l§c", name
                    )
                )
            } else if (name.contains("Disabled")) {
                CustomCrafting.disabledRecipes.remove(recipe)
                craftingInv.setItem(
                    slot, Utils.createGuiItem(
                        item = item, name = "Enabled",
                        prefix = "§l§a", name
                    )
                )
            }



        }
    }

    /**
     * Checks if the inventory has a blank space
     */
    private fun hasBlank() : Boolean {

        for (row in 0 until rows-1) {
            for (column in 0 until columns) {
                val item = craftingInv.getItem(row * column)
                if (item == null || item.type == Material.AIR) return true
            }
        }

        return false
    }



    companion object {

        val openInventories = mutableMapOf<Inventory, RecipeInventory>()

        /**
         * Checks if the inventory is a custom crafting instance
         */
        fun isCustomInventory(inv: Inventory): Boolean {

            return openInventories.contains(inv)
        }

        /**
         * Get the entire CustomCraftingInventory instance from its inventory
         */
        fun getCustomCraftingInventory(inv: Inventory): RecipeInventory? {
            return openInventories[inv]
        }
    }

}

enum class ViewType {
    ENABLED,
    DISABLED,
    ALL
}