package com.github.nuguya21.fortress

import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Zombie

interface Fortress {
    var speed: Double
    val core: ArmorStand
    var blockData: BlockData
    val barrels: Array<Zombie>
    val wheels: Array<ArmorStand>
    fun forward()
    fun launch(power: Double)
    fun remove()
    fun isRemoved(): Boolean
}