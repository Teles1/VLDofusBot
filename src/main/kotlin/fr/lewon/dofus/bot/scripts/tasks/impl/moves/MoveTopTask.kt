package fr.lewon.dofus.bot.scripts.tasks.impl.moves

import fr.lewon.dofus.bot.model.move.Direction
import fr.lewon.dofus.bot.util.filemanagers.ConfigManager
import fr.lewon.dofus.bot.util.geometry.PointRelative

class MoveTopTask : MoveTask(Direction.TOP) {

    override fun getMoveDest(): PointRelative {
        return ConfigManager.config.topAccessPos
    }

}