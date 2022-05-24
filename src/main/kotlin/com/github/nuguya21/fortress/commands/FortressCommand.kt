package com.github.nuguya21.fortress.commands

import com.github.nuguya21.fortress.FortressController
import com.github.nuguya21.fortress.commands.arguments.*
import com.github.nuguya21.fortress.plugin.FortressPlugin
import com.github.nuguya21.fortress.util.giveItem
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player

class FortressCommand: BukkitCommand("fortress") {

    companion object {
        const val fallbackPrefix: String = "minecraft"
    }

    init {
        description = "Spawn/Manage The Fortress"
        usageMessage = "/fortress"
    }

    override fun execute(p0: CommandSender, p1: String, p2: Array<out String>): Boolean {
        if (p0 is Player) {
            val mainHand = p0.inventory.itemInMainHand
            if (p2.checkArgument(CommandArgument("controller"), PlayerArgument(), NullArgument())) {
                Bukkit.getPlayer(p2[1])!!.giveItem(FortressController.FORWARD.itemStack)
                Bukkit.getPlayer(p2[1])!!.giveItem(FortressController.ANGLE.itemStack)
                Bukkit.getPlayer(p2[1])!!.giveItem(FortressController.LAUNCH.itemStack)
                p0.sendMessage(p2[1] + "에게 컨트롤러를 지급했습니다.")
            } else if (p2.checkArgument(CommandArgument("spawn"), StringArgument(), NullArgument())) {
                if (mainHand.type.isBlock) {
                    FortressPlugin.getFortressManager().spawnFortress(p2[1], p0.location, Bukkit.createBlockData(mainHand.type))
                    p0.sendMessage(p2[1] + " 을(를) 생성했습니다")
                }
            } else if (p2.checkArgument(CommandArgument("register"), PlayerArgument(), FortressArgument(), NullArgument())) {
                FortressPlugin.getFortressManager().register(Bukkit.getPlayer(p2[1])!!, FortressPlugin.getFortressManager().getFortress(p2[2])!!)
                p0.sendMessage(p2[2] + " 을(를) " + p1[1] + " 에게 등록했습니다")
            } else if (p2.checkArgument(CommandArgument("unregister"), PlayerArgument(), NullArgument())) {
                FortressPlugin.getFortressManager().unregister(Bukkit.getPlayer(p2[1])!!)
                p0.sendMessage(p2[1] + " 로부터 포트리스를 해지했습니다")
            } else if (p2.checkArgument(CommandArgument("remove"), StringArgument(), NullArgument())) {
                FortressPlugin.getFortressManager().getFortress(p0)?.remove()
                p0.sendMessage(p2[1] + " 을(를) 제거했습니다")
            } else if (p2.checkArgument(CommandArgument("launch"), PlayerArgument(), DoubleArgument(), NullArgument())) {
                FortressPlugin.getFortressManager().getFortress(p0)?.launch(p2[2].toDouble())
                p0.sendMessage(p2[1] + " 의 포트리스를 발포했습니다")
            }
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        val options = mutableListOf<String>()
        fun MutableList<String>.playerList() {
            for (player in Bukkit.getOnlinePlayers()) {
                add(player.name)
            }
        }
        if (args.checkArgument(NullArgument())) {
            options.add("spawn")
            options.add("remove")
            options.add("launch")
            options.add("register")
            options.add("unregister")
            options.add("controller")
        } else if (args.checkArgument(CommandArgument("register"), NullArgument())) {
            options.playerList()
        } else if (args.checkArgument(CommandArgument("register"), PlayerArgument(), NullArgument())) {
            options.addAll(FortressPlugin.getFortressManager().idSet())
        } else if (args.checkArgument(CommandArgument("unregister"), NullArgument())) {
            options.playerList()
        } else if (args.checkArgument(CommandArgument("remove"), NullArgument())) {
            options.addAll(FortressPlugin.getFortressManager().idSet())
        } else if (args.checkArgument(CommandArgument("launch"), NullArgument())) {
            options.playerList()
        } else if (args.checkArgument(CommandArgument("controller"), NullArgument())) {
            options.playerList()
        }
        return options
    }

    private fun Array<out String>.checkArgument(vararg args: Argument): Boolean {
        if (lastIndex == 0 && args.isEmpty()) return true
        for (i in args.indices) {
            try {
                when (val arg = args[i]) {
                    is StringArgument -> if (this[i].isEmpty()) return false
                    is CommandArgument -> if (this[i] != arg.name) return false
                    is PlayerArgument -> if (Bukkit.getPlayer(this[i]) == null) return false
                    is NullArgument -> if (this.getOrNull(i + 1) != null) return false
                    is DoubleArgument -> if (this[i].toDoubleOrNull() == null) return false
                    is FortressArgument -> if (!FortressPlugin.getFortressManager().exists(this[i])) return false
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                return false
            }
        }
        return true
    }
}