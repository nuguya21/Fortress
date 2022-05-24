package com.github.nuguya21.fortress

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class FortressController(val itemStack: ItemStack) {
    FORWARD(ItemStack(Material.CARROT_ON_A_STICK, 1).apply {
        editMeta {
            it.setDisplayName("${ChatColor.RESET}전진")
        }
    }),
    ANGLE(ItemStack(Material.STICK, 1).apply {
        editMeta {
            it.setDisplayName("${ChatColor.RESET}방향")
        }
    }),
    LAUNCH(ItemStack(Material.TNT, 1).apply {
        editMeta {
            it.setDisplayName("${ChatColor.RESET}발사")
        }
    });
}

fun ItemStack.isController(): Boolean {
    for (value in FortressController.values()) {
        if (this == value.itemStack) return true
    }
    return false
}