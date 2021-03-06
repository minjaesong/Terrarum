package net.torvald.terrarum.gameactors.faction

import net.torvald.terrarum.utils.JsonFetcher
import net.torvald.terrarum.ModMgr

import java.io.IOException

/**
 * Created by minjaesong on 2016-02-15.
 */
object FactionFactory {

    /**
     * @param filename with extension
     */
    @Throws(IOException::class)
    fun create(module: String, path: String): Faction {
        val jsonObj = JsonFetcher(ModMgr.getFile(module, path))
        val factionObj = Faction(jsonObj.get("factionname").asString)

        jsonObj.get("factionamicable").asJsonArray.forEach { factionObj.addFactionAmicable(it.asString) }
        jsonObj.get("factionneutral").asJsonArray.forEach { factionObj.addFactionNeutral(it.asString) }
        jsonObj.get("factionhostile").asJsonArray.forEach { factionObj.addFactionHostile(it.asString) }
        jsonObj.get("factionfearful").asJsonArray.forEach { factionObj.addFactionFearful(it.asString) }

        return factionObj
    }
}
