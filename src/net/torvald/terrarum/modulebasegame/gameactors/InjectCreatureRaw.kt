package net.torvald.terrarum.modulebasegame.gameactors

import net.torvald.terrarum.utils.JsonFetcher
import net.torvald.random.Fudge3
import net.torvald.terrarum.langpack.Lang
import com.google.gson.JsonObject
import net.torvald.terrarum.ModMgr
import net.torvald.terrarum.gameactors.AVKey
import net.torvald.terrarum.gameactors.ActorValue
import java.security.SecureRandom

/**
 * Created by minjaesong on 2016-03-25.
 */
object InjectCreatureRaw {

    private const val JSONMULT = "mult" // one appears in JSON files

    /**
     * 'Injects' creature raw ActorValue to the ActorValue reference provided.
     *
     * @param actorValueRef ActorValue object to be injected.
     * @param jsonFileName with extension
     */
    operator fun invoke(actorValueRef: ActorValue, module: String, jsonFileName: String) {
        val jsonObj = JsonFetcher(ModMgr.getPath(module, "creatures/$jsonFileName"))

        val elementsInt = arrayOf(AVKey.BASEHEIGHT, AVKey.TOOLSIZE, AVKey.ENCUMBRANCE)
        val elementsString = arrayOf(AVKey.RACENAME, AVKey.RACENAMEPLURAL)
        val elementsDouble = arrayOf(AVKey.BASEMASS, AVKey.ACCEL)
        val elementsDoubleVariable = arrayOf(AVKey.STRENGTH, AVKey.SPEED, AVKey.JUMPPOWER, AVKey.SCALE)
        val elementsBoolean = arrayOf(AVKey.INTELLIGENT)
        // val elementsMultiplyFromOne = arrayOf()

        setAVInts(actorValueRef, elementsInt, jsonObj)
        setAVStrings(actorValueRef, elementsString, jsonObj)
        setAVDoubles(actorValueRef, elementsDouble, jsonObj)
        setAVDoublesVariable(actorValueRef, elementsDoubleVariable, jsonObj)
        // setAVMultiplyFromOne(actorValueRef, elementsMultiplyFromOne, jsonObj)
        setAVBooleans(actorValueRef, elementsBoolean, jsonObj)

        actorValueRef[AVKey.ACCEL] = ActorHumanoid.WALK_ACCEL_BASE
        actorValueRef[AVKey.ACCELBUFF] = 1.0
    }

    /**
     * Fetch and set actor values that have 'variable' appended. E.g. strength
     * @param avRef
     * *
     * @param elemSet
     * *
     * @param jsonObject
     */
    private fun setAVDoublesVariable(avRef: ActorValue, elemSet: Array<String>, jsonObject: JsonObject) {
        for (s in elemSet) {
            val baseValue = jsonObject.get(s).asDouble
            // roll fudge dice and get value [-3, 3] as [0, 6]
            val varSelected = Fudge3(SecureRandom()).rollForArray()
            // get multiplier from json. Assuming percentile
            val multiplier = jsonObject.get(s + JSONMULT).asJsonArray.get(varSelected).asInt
            val realValue = baseValue * multiplier / 100.0

            avRef[s] = realValue
            avRef[s + "buff"] = 1.0 // buffed value: use multiplied value as 'base' for all sort of things
        }
    }

    /**
     * Fetch and set string actor values
     * @param avRef
     * *
     * @param elemSet
     * *
     * @param jsonObject
     */
    private fun setAVStrings(avRef: ActorValue, elemSet: Array<String>, jsonObject: JsonObject) {
        for (s in elemSet) {
            val key = jsonObject.get(s).asString
            avRef[s] = Lang[key]
        }
    }

    /**
     * Fetch and set double actor values
     * @param avRef
     * *
     * @param elemSet
     * *
     * @param jsonObject
     */
    private fun setAVDoubles(avRef: ActorValue, elemSet: Array<String>, jsonObject: JsonObject) {
        for (s in elemSet) {
            avRef[s] = jsonObject.get(s).asDouble
        }
    }

    /**
     * Fetch and set int actor values
     * @param avRef
     * *
     * @param elemSet
     * *
     * @param jsonObject
     */
    private fun setAVInts(avRef: ActorValue, elemSet: Array<String>, jsonObject: JsonObject) {
        for (s in elemSet) {
            avRef[s] = jsonObject.get(s).asInt
        }
    }

    /**
     * Fetch and set actor values that should multiplier be applied to the base value of 1.
     * @param avRef
     * *
     * @param elemSet
     * *
     * @param jsonObject
     */
    private fun setAVMultiplyFromOne(avRef: ActorValue, elemSet: Array<String>, jsonObject: JsonObject) {
        for (s in elemSet) {
            val baseValue = 1.0
            // roll fudge dice and get value [-3, 3] as [0, 6]
            val varSelected = Fudge3(SecureRandom()).rollForArray()
            // get multiplier from json. Assuming percentile
            val multiplier = jsonObject.get(s).asJsonArray.get(varSelected).asInt
            val realValue = baseValue * multiplier / 100.0

            avRef[s] = realValue
        }
    }

    private fun setAVBooleans(avRef: ActorValue, elemSet: Array<String>, jsonObject: JsonObject) {
        for (s in elemSet) {
            avRef[s] = jsonObject.get(s).asBoolean
        }
    }
}