package com.github.nuguya21.fortress.event

import com.github.nuguya21.fortress.Fortress
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class EntityDamageByFortressEvent(val entity: Entity, val fortress: Fortress, var damage: Double): Event(), Cancellable {

    private var cancelled = false

    companion object {
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
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