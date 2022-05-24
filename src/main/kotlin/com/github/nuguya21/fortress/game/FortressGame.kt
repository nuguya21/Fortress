package com.github.nuguya21.fortress.game

import org.bukkit.entity.Player

interface FortressGame {
    val gameMode: GameMode
    val players: List<Player>
    val forwardLimit: Double
    fun start()
    fun stop()
}