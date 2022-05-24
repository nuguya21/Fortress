package com.github.nuguya21.fortress

import com.github.nuguya21.fortress.collection.Chain
import com.github.nuguya21.fortress.game.FortressGame
import com.github.nuguya21.fortress.game.GameMode
import com.github.nuguya21.fortress.util.Movement
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.attribute.Attribute
import org.bukkit.block.data.BlockData
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Consumer
import org.bukkit.util.Vector
import kotlin.math.absoluteValue
import kotlin.math.pow

class FortressManager(private val plugin: Plugin) {
    private val fortressWithId: Chain<String, Fortress> = Chain()
    private val playerWithFortress: Chain<Player, Fortress> = Chain()
    private val playerWithGame: Chain<Player, FortressGame> = Chain()
    private val banned: MutableSet<Player> = mutableSetOf()
    internal val shotWithFortress: Chain<ArmorStand, Fortress> = Chain()

    fun spawnFortress(id: String, location: Location, blockData: BlockData): Fortress {
        val block = if (blockData.material.isBlock) blockData else Bukkit.createBlockData(Material.STONE)
        fun Location.spawnEntity(type: EntityType): Entity {
            return world.spawnEntity(
                this,
                type,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
            )
        }

        fun Location.spawnEntity(type: EntityType, function: Consumer<Entity>): Entity {
            return world.spawnEntity(
                this,
                type,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                function
            )
        }

        fun spawnBarrel(location: Location): Zombie {
            return location.spawnEntity(EntityType.ZOMBIE) {
                it as Zombie
                it.isSilent = true
                it.isBaby = false
                it.lootTable = null
                it.setAI(false)
                it.isInvulnerable = true
                it.clearLootTable()
                it.equipment.helmet = ItemStack(block.material)
                it.equipment.setItemInMainHand(null)
                it.equipment.setItemInOffHand(null)
                it.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 255, false, false, false))
            } as Zombie
        }

        fun spawnWheel(location: Location): ArmorStand {
            return location.spawnEntity(EntityType.ARMOR_STAND) {
                it as ArmorStand
                it.isSilent = true
                it.isMarker = true
                it.isInvisible = true
                it.isSilent = true
                it.isInvulnerable = true
                it.equipment.helmet = ItemStack(Material.POLISHED_DEEPSLATE_WALL)
            } as ArmorStand
        }
        return object : Fortress {
            private val instead = this
            private fun spawnBarrels(): Array<Zombie> {
                val barrels: Array<Zombie?> = arrayOfNulls(4)
                val direction = core.location.direction.clone().apply {
                    multiply(1 / length())
                }
                for (i in 0..3) {
                    barrels[i] = spawnBarrel(
                        core.location.clone().add(direction.clone().multiply(i * 0.625)).subtract(0.0, 1.25, 0.0)
                    )
                }
                return barrels.requireNoNulls()
            }

            private fun spawnWheels(): Array<ArmorStand> {
                val wheels = arrayOfNulls<ArmorStand>(2)
                val direction = core.location.direction.setY(0).clone().apply {
                    multiply(1 / length())
                }
                for (i in 0..1) {
                    wheels[i] = spawnWheel(core.location.clone().add(direction.clone().apply {
                        y = 0.0
                        multiply(1 / length())
                        val oldX = x
                        val oldZ = z
                        x = (-1.0).pow(i) * -oldZ
                        z = (-1.0).pow(i) * oldX
                        multiply(0.4)
                    }).subtract(0.0, 1.5, 0.0))
                }
                return wheels.requireNoNulls()
            }

            override var speed: Double = 0.1

            override val core: ArmorStand = location.spawnEntity(EntityType.ARMOR_STAND) {
                it as ArmorStand
                it.isInvisible = true
                it.isSmall = true
                it.isSilent = true
                it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 81.0
                it.health = 81.0
            } as ArmorStand
            override var blockData: BlockData = block
                set(value) {
                    val newValue = if (value.material.isBlock) value else Bukkit.createBlockData(Material.STONE)
                    for (wheel in wheels) {
                        wheel.equipment.helmet = ItemStack(newValue.material)
                    }
                    field = newValue
                }
            override val barrels: Array<Zombie> = spawnBarrels()
            override val wheels: Array<ArmorStand> = spawnWheels()
            override fun forward() {
                for (wheel in wheels) {
                    wheel.world.spawnParticle(
                        Particle.REDSTONE, wheel.location.add(0.0, 1.25, 0.0), 10, 0.1, 0.1, 0.1, 0.0, DustOptions(
                            Color.BLACK,
                            1f
                        )
                    )
                }
                core.world.playSound(core.location, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1.0f, 1.0f)
                val forward = core.location.clone().add(core.location.direction.clone().apply {
                    y = 0.0
                    multiply((speed * 2) / length())
                })
                if (!forward.block.isSolid) {
                    core.velocity = core.velocity.clone().add(core.location.direction.clone().apply {
                        y = 0.0
                        multiply((speed / 2) / length())
                    })
                } else if (core.isOnGround) {
                    core.velocity = core.velocity.clone().add(Vector(0.0, 0.5, 0.0))
                }
            }

            override fun launch(power: Double) {
                val fireHole = barrels[3].location.add(0.0, 1.25, 0.0)
                fireHole.world.spawnParticle(Particle.EXPLOSION_HUGE, fireHole, 5, 0.25, 0.25, 0.25, 0.0)
                fireHole.world.playSound(fireHole, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 2.0f)
                fireHole.spawnEntity(EntityType.ARMOR_STAND) {
                    it as ArmorStand
                    it.isInvisible = true
                    it.isSilent = true
                    it.isMarker = true
                    it.equipment.helmet = ItemStack(Material.TNT)
                    shotWithFortress.plus(it, instead)
                    object : BukkitRunnable() {
                        val movement = Movement(it.location.add(0.0, 1.0, 0.0), 1.0, core.location.direction, power)
                        override fun run() {
                            if (it.isDead) cancel()
                            it.world.spawnParticle(
                                Particle.FLAME,
                                it.location.clone().add(0.0, 1.0, 0.0),
                                5,
                                0.5,
                                0.5,
                                0.5,
                                0.0
                            )
                            it.teleport(movement.nextTick().clone().subtract(0.0, 1.0, 0.0))
                            if (movement.deltaVector().length().absoluteValue <= 0.00000001) {
                                it.remove()
                                it.location.world.createExplosion(it.location, 5f, false, true, it)
                                cancel()
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 1L)
                }
            }

            override fun remove() {
                if (!isRemoved()) {
                    core.remove()
                    for (barrel in barrels) barrel.remove()
                    for (wheel in wheels) wheel.remove()
                    isRemoved = true
                    fortressWithId.remove(this)
                    playerWithFortress.remove(this)
                }
            }

            private var isRemoved = false

            override fun isRemoved(): Boolean {
                return isRemoved
            }

            init {
                object : BukkitRunnable() {
                    override fun run() {
                        val direction: Vector = core.location.direction.apply {
                            multiply(1 / length())
                        }
                        // core
                        if (core.isDead) {
                            remove()
                            cancel()
                        }
                        // barrel
                        for (i in 0..3) {
                            if (barrels[i].isDead) {
                                remove()
                                cancel()
                            }
                            barrels[i].teleport(
                                core.location.clone().add(direction.clone().multiply(i * 0.625))
                                    .subtract(0.0, 1.25, 0.0)
                            )
                        }
                        // wheel
                        for (i in 0..1) {
                            if (wheels[i].isDead) {
                                remove()
                                cancel()
                            }
                            wheels[i].teleport(core.location.clone().add(direction.clone().apply {
                                y = 0.0
                                multiply(1 / length())
                                val oldX = x
                                val oldZ = z
                                x = (-1.0).pow(i) * -oldZ
                                z = (-1.0).pow(i) * oldX
                                multiply(0.4)
                            }).subtract(0.0, 1.5, 0.0))
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L)
            }
        }.apply {
            fortressWithId.plus(id, this)
        }
    }

    fun createNewGame(gameMode: GameMode, players: List<Player>, forwardLimit: Double) {
        object : FortressGame {
            override val gameMode: GameMode = gameMode
            override val players: List<Player> = players
            override val forwardLimit: Double = forwardLimit

            override fun start() {
                for (player in players) {
                    if (getFortress(player) == null) register(
                        player,
                        spawnFortress(player.name, player.location, Bukkit.createBlockData(Material.STONE))
                    )
                }
            }

            override fun stop() {
                TODO("Not yet implemented")
            }
        }.apply {
            for (player in players) {
                playerWithGame.plus(player, this)
            }
        }
    }

    fun getFortress(id: String): Fortress? {
        return fortressWithId[id]
    }

    internal fun idSet(): Set<String> {
        return fortressWithId.values1()
    }

    fun getFortress(player: Player): Fortress? {
        return playerWithFortress[player]
    }

    fun getPlayer(fortress: Fortress): Player? {
        return playerWithFortress[fortress]
    }

    fun register(player: Player, fortress: Fortress) {
        playerWithFortress.plus(player, fortress)
    }

    fun unregister(player: Player) {
        playerWithFortress.remove(player)
    }

    fun ban(player: Player) {
        banned.add(player)
    }

    fun unban(player: Player) {
        banned.remove(player)
    }

    fun banned(): Collection<Player> {
        return banned
    }

    fun exists(id: String): Boolean {
        return fortressWithId.contains(id)
    }
}