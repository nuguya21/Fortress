package com.github.nuguya21.fortress.plugin

import com.github.nuguya21.fortress.FortressManager
import com.github.nuguya21.fortress.commands.FortressCommand
import com.github.nuguya21.fortress.event.EntityDamageByFortressEvent
import com.github.nuguya21.fortress.event.FortressExplodeEvent
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.plugin.java.JavaPlugin

class FortressPlugin: JavaPlugin() {

    companion object {
        private lateinit var fortressManager: FortressManager
        fun getFortressManager(): FortressManager {
            return fortressManager
        }
    }

    override fun onEnable() {
        fortressManager = FortressManager(this)
        Bukkit.getCommandMap().register("fortress", FortressCommand.fallbackPrefix, FortressCommand())
        Bukkit.getPluginManager().registerEvents(FortressListener(this), this)
        Bukkit.getPluginManager().registerEvents(object : Listener {

            @EventHandler(priority = EventPriority.HIGHEST)
            fun onExplosion(event: EntityExplodeEvent) {
                val shot = event.entity
                if (shot is ArmorStand) {
                    val fortress = getFortressManager().shotWithFortress[shot]
                    if (fortress != null) {
                        val player = getFortressManager().getPlayer(fortress)
                        if (player != null) {
                            val explodeEvent = FortressExplodeEvent(
                                player, event.location, event.blockList(), event.yield
                            )
                            Bukkit.getPluginManager().callEvent(explodeEvent)
                            getFortressManager().shotWithFortress.remove(shot)
                            event.isCancelled = explodeEvent.isCancelled
                            event.yield = explodeEvent.yield
                        }
                    }
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            fun onDamage(event: EntityDamageByEntityEvent) {
                val shot = event.damager
                if (shot is ArmorStand) {
                    val entity = event.entity
                    if (getFortressManager().shotWithFortress.contains(shot)) {
                        val damageEvent = EntityDamageByFortressEvent(
                            entity,
                            getFortressManager().shotWithFortress[shot]!!,
                            event.damage
                        )
                        Bukkit.getPluginManager().callEvent(damageEvent)
                        event.isCancelled = damageEvent.isCancelled
                        event.damage = damageEvent.damage
                    }
                }
            }
        }, this)
    }
}