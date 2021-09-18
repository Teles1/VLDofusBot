package fr.lewon.dofus.bot.util.filemanagers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import fr.lewon.dofus.bot.gui.MainPanel
import fr.lewon.dofus.bot.model.characters.CharacterStore
import fr.lewon.dofus.bot.model.characters.DofusCharacter
import fr.lewon.dofus.bot.model.characters.DofusClass
import fr.lewon.dofus.bot.model.characters.MapInformation
import fr.lewon.dofus.bot.scripts.DofusBotScript
import fr.lewon.dofus.bot.scripts.DofusBotScriptParameter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object DTBCharacterManager {

    private val characterStore: CharacterStore
    private val dataStoreFile: File = File("config/user_data")
    private val mapper = ObjectMapper()

    init {
        val module = SimpleModule()
        mapper.registerModule(module)
        if (dataStoreFile.exists()) {
            characterStore = ObjectMapper().readValue(dataStoreFile)
        } else {
            characterStore = CharacterStore()
            saveUserData()
        }
    }

    private fun saveUserData() {
        with(OutputStreamWriter(FileOutputStream(dataStoreFile, false), StandardCharsets.UTF_8)) {
            write(ObjectMapper().writeValueAsString(characterStore))
            close()
        }
    }

    fun setCurrentCharacter(character: DofusCharacter) {
        characterStore.currentCharacterLogin = character.login
        characterStore.currentCharacterPseudo = character.pseudo
        MainPanel.refreshScriptsTab()
        saveUserData()
    }

    fun getCurrentCharacter(): DofusCharacter? {
        val login = characterStore.currentCharacterLogin ?: return null
        val pseudo = characterStore.currentCharacterPseudo ?: return null
        return getCharacter(login, pseudo)
    }

    fun getCharacter(login: String, pseudo: String): DofusCharacter? {
        return characterStore.characters.firstOrNull { it.login == login && it.pseudo == pseudo }
    }

    fun getCharacters(): List<DofusCharacter> {
        return characterStore.characters.toList()
    }

    fun addCharacter(login: String, password: String, pseudo: String, dofusClass: DofusClass): DofusCharacter {
        getCharacter(login, pseudo)?.let {
            error("Character already registered : [$login, $pseudo]")
        }
        val character = DofusCharacter(login, password, pseudo, dofusClass)
        characterStore.characters.add(character)
        if (getCurrentCharacter() == null) {
            setCurrentCharacter(character)
        }
        saveUserData()
        return character
    }

    fun removeCharacter(character: DofusCharacter) {
        characterStore.characters.remove(character)
        if (getCurrentCharacter() == character) {
            characterStore.currentCharacterLogin = null
            characterStore.currentCharacterPseudo = null
        }
        MainPanel.refreshScriptsTab()
        saveUserData()
    }

    fun updateCharacter(
        character: DofusCharacter,
        login: String,
        password: String,
        pseudo: String,
        dofusClass: DofusClass
    ) {
        val existingCharacter = getCharacter(login, pseudo)
        if (existingCharacter != null && existingCharacter != character) {
            error("Character already registered : [$login, $pseudo]")
        }
        character.login = login
        character.password = password
        character.pseudo = pseudo
        character.dofusClass = dofusClass
        saveUserData()
    }

    fun updateZaapDestinations(zaapDestinations: ArrayList<MapInformation>) {
        getCurrentCharacter()?.let { it.zaapDestinations = zaapDestinations }
        saveUserData()
    }

    fun updateParamValue(character: DofusCharacter, script: DofusBotScript, param: DofusBotScriptParameter) {
        character.scriptValues.updateParamValue(script.name, param.key, param.value)
        saveUserData()
    }

    fun getParamValue(character: DofusCharacter, script: DofusBotScript, param: DofusBotScriptParameter): String? {
        return character.scriptValues.getParamValue(script.name, param.key)
    }

}
