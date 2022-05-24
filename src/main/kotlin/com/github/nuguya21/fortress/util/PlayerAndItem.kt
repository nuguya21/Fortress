package com.github.nuguya21.fortress.util

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Player.giveItem(itemStack: ItemStack) {
    val item = world.dropItem(location, itemStack)
    item.owner = this.uniqueId
    item.pickupDelay = 0
    item.setCanMobPickup(false)
}

fun Player.clearItem(itemStack: ItemStack): Boolean {
    val ammo: MutableMap<Int, out ItemStack> = this.inventory.all(itemStack.type)
    var foundedAmount = 0

    for (index in ammo.keys) {
        val stack = ammo[index]
        if (stack!!.itemMeta == itemStack.itemMeta && stack.itemFlags == itemStack.itemFlags) {
            foundedAmount += stack.amount
        } else {
            ammo.remove(index)
        }
    }
    if (itemStack.amount > foundedAmount) {
        return false
    }
    for (i in 0 until itemStack.amount) {
        val itemStack1 = itemStack.clone()
        itemStack1.amount = 1
        this.inventory.removeItem(itemStack1)
    }
    this.updateInventory()
    return true
}