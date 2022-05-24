package com.github.nuguya21.fortress.event

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class FortressExplodeEvent(val player: Player, val location: Location, val blocks: MutableList<Block>, var yield: Float) : Event(), Cancellable {

    private var cancelled = false

    companion object {
        private val HANDLER_LIST = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}