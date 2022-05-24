package com.github.nuguya21.fortress.plugin

import com.github.nuguya21.fortress.FortressController
import com.github.nuguya21.fortress.isController
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class FortressListener(private val plugin: FortressPlugin) : Listener {
    private val cancel: MutableMap<Player, Boolean> = mutableMapOf()

    @EventHandler
    fun onInteract(event: PlayerToggleSneakEvent) {
        val player = event.player
        val item: ItemStack = player.inventory.itemInMainHand
        if (!FortressPlugin.getFortressManager().banned().contains(player)) {
            if (event.isSneaking) {
                if (player.isOnGround && item.isController()) {
                    val fortress = FortressPlugin.getFortressManager().getFortress(player)
                    if (fortress != null) {
                        when (item) {
                            FortressController.FORWARD.itemStack -> {
                                cancel[player] = false
                                object : BukkitRunnable() {
                                    override fun run() {
                                        fortress.forward()
                                        if (cancel[player] == true) cancel()
                                    }
                                }.runTaskTimer(plugin, 0L, 1L)
                            }
                            FortressController.ANGLE.itemStack -> {
                                cancel[player] = false
                                val standardPitch = player.location.pitch
                                val standardYaw = player.location.yaw
                                val originPitch = fortress.core.location.pitch
                                val originYaw = fortress.core.location.yaw
                                object : BukkitRunnable() {
                                    override fun run() {
                                        if (fortress.core.isOnGround) {
                                            fortress.core.teleport(fortress.core.location.clone().apply {
                                                pitch = originPitch + (player.location.pitch - standardPitch)
                                                yaw = originYaw + (player.location.yaw - standardYaw)
                                            })
                                        }
                                        if (cancel[player] == true) cancel()
                                    }
                                }.runTaskTimer(plugin, 0L, 1L)
                            }
                            FortressController.LAUNCH.itemStack -> {
                                cancel[player] = false
                                object : BukkitRunnable() {
                                    var power = 0.0
                                    val bossBar =
                                        Bukkit.createBossBar(
                                            "${ChatColor.WHITE}파워",
                                            BarColor.RED,
                                            BarStyle.SEGMENTED_20
                                        )
                                            .apply {
                                                progress = 0.0
                                                isVisible = true
                                                addPlayer(player)
                                            }

                                    override fun run() {
                                        if (cancel[player] == true) {
                                            fortress.launch(power)
                                            bossBar.removeAll()
                                            cancel()
                                        }
                                        power += 5.0
                                        if (power >= 500) power = 250.0
                                        bossBar.progress = (power / 250.0)
                                    }
                                }.runTaskTimer(plugin, 0L, 1L)
                            }
                        }
                    } else {
                        player.sendMessage("포트리스를 등록해야합니다")
                    }
                }
            } else {
                cancel[player] = true
            }
        }
    }
}