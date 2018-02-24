package de.lulebe.kreuzzuege.data

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject

data class Player(
        val faction: Int,
        var money: Int,
        val units: MutableList<Unit>,
        val buildings: MutableList<Int>
) {

    fun toJSON() : JsonObject {
        val map = mutableMapOf<String, Any>()
        map["faction"] = faction
        map["money"] = money
        map["units"] = JsonArray(units.map { it.toJSON() })
        map["buildings"] = JsonArray(buildings)
        return JsonObject(map)
    }

    companion object {
        fun fromJSON (json: JsonObject) : Player {
            val units = json.array<JsonObject>("units")!!.map { Unit.fromJSON(it) }.toMutableList()
            return Player(
                    json.int("faction")!!,
                    json.int("money")!!,
                    units,
                    json.array("buildings")!!
            )
        }
    }
}