package fr.lewon.dofus.bot.handlers.movement

import fr.lewon.dofus.bot.gui2.main.scripts.characters.CharactersUIUtil
import fr.lewon.dofus.bot.gui2.main.scripts.characters.edit.global.CharacterGlobalInformationUIUtil
import fr.lewon.dofus.bot.sniffer.DofusConnection
import fr.lewon.dofus.bot.sniffer.model.messages.game.initialization.SetCharacterRestrictionsMessage
import fr.lewon.dofus.bot.sniffer.model.types.game.context.roleplay.GameRolePlayCharacterInformations
import fr.lewon.dofus.bot.sniffer.model.types.game.context.roleplay.HumanOptionOrnament
import fr.lewon.dofus.bot.sniffer.store.IEventHandler
import fr.lewon.dofus.bot.util.network.GameSnifferUtil

object SetCharacterRestrictionEventHandler : IEventHandler<SetCharacterRestrictionsMessage> {
    override fun onEventReceived(socketResult: SetCharacterRestrictionsMessage, connection: DofusConnection) {
        val gameInfo = GameSnifferUtil.getGameInfoByConnection(connection)
        if (gameInfo.initRequested) {
            gameInfo.playerId = socketResult.actorId
            gameInfo.updateCellData(gameInfo.currentMap.id)
            gameInfo.shouldInitBoard = false
            gameInfo.initRequested = false
            CharactersUIUtil.updateState(gameInfo.character)
            gameInfo.actors.firstOrNull { it.contextualId == gameInfo.playerId }?.let {
                CharactersUIUtil.updateSkin(gameInfo.character, it.look)
                if (it is GameRolePlayCharacterInformations) {
                    it.humanoidInfo.options.filterIsInstance<HumanOptionOrnament>().firstOrNull()?.let { option ->
                        CharacterGlobalInformationUIUtil.updateCharacterLevel(gameInfo.character.name, option.level)
                    }
                }
            }
            println("${gameInfo.character.name} initialized, ID : ${gameInfo.playerId}")
        }
    }
}