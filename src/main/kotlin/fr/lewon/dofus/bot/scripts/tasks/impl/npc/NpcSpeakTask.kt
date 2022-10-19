package fr.lewon.dofus.bot.scripts.tasks.impl.npc

import fr.lewon.dofus.bot.core.logs.LogItem
import fr.lewon.dofus.bot.scripts.tasks.DofusBotTask
import fr.lewon.dofus.bot.sniffer.model.messages.game.basic.BasicNoOperationMessage
import fr.lewon.dofus.bot.sniffer.model.messages.game.context.roleplay.npc.NpcDialogCreationMessage
import fr.lewon.dofus.bot.sniffer.model.messages.game.context.roleplay.npc.NpcDialogQuestionMessage
import fr.lewon.dofus.bot.sniffer.model.messages.game.dialog.LeaveDialogMessage
import fr.lewon.dofus.bot.util.game.InteractiveUtil
import fr.lewon.dofus.bot.util.geometry.PointRelative
import fr.lewon.dofus.bot.util.io.MouseUtil
import fr.lewon.dofus.bot.util.io.WaitUtil
import fr.lewon.dofus.bot.util.network.info.GameInfo
import kotlin.math.min

class NpcSpeakTask(private val npcId: Int, private val optionIndexes: List<Int>) : DofusBotTask<Boolean>() {

    companion object {
        private val FIRST_OPTION_LOCATION = PointRelative(0.36847493f, 0.7442455f)
        private val FIFTH_OPTION_LOCATION = PointRelative(0.36847493f, 0.6508951f)
        private val DELTA_OPTION = (FIRST_OPTION_LOCATION.y - FIFTH_OPTION_LOCATION.y) / 4f
    }

    override fun execute(logItem: LogItem, gameInfo: GameInfo): Boolean {
        val npcLocation = InteractiveUtil.getNpcClickPosition(gameInfo, npcId)
        gameInfo.eventStore.clear()
        MouseUtil.leftClick(gameInfo, npcLocation, 0)
        WaitUtil.waitForEvents(gameInfo, NpcDialogCreationMessage::class.java)
        for (optionIndex in optionIndexes) {
            WaitUtil.waitForEvents(gameInfo, NpcDialogQuestionMessage::class.java, BasicNoOperationMessage::class.java)
            WaitUtil.sleep(300)
            val dialogQuestionMessage = gameInfo.eventStore.getLastEvent(NpcDialogQuestionMessage::class.java)
                ?: error("Missing dialog question message")
            val optionCount = min(5, dialogQuestionMessage.visibleReplies.size)
            val optionLocation = FIRST_OPTION_LOCATION
                .getSum(PointRelative(0f, (optionIndex - optionCount + 1) * DELTA_OPTION))
            gameInfo.eventStore.clear()
            MouseUtil.leftClick(gameInfo, optionLocation, 0)
        }
        WaitUtil.waitForEvents(gameInfo, LeaveDialogMessage::class.java, BasicNoOperationMessage::class.java)
        gameInfo.eventStore.clearUntilLast(LeaveDialogMessage::class.java)
        return true
    }

    override fun onStarted(): String {
        return "Interacting with NPC ..."
    }
}