package de.lulebe.kreuzzuege.data

import com.beust.klaxon.JsonObject


data class TurnAction (
        val type: Int,
        val unitId: Int?,
        val enemyId: Int?,
        val field: Int?,
        val oldField: Int?,
        val unitDamage: Int?,
        val enemyDamage: Int?
) {

    fun toJSON() : JsonObject {
        val json = JsonObject()
        json["type"] = type
        unitId?.let { json["unitId"] = it }
        enemyId?.let { json["enemyId"] = it }
        field?.let { json["field"] = it }
        oldField?.let { json["oldField"] = it }
        unitDamage?.let { json["unitDamage"] = it }
        enemyDamage?.let { json["enemyDamage"] = it }
        return json
    }

    companion object {

        const val TYPE_CREATE = 1 //unit id, field
        const val TYPE_MOVE = 2 //unit id, oldField, newField
        const val TYPE_FIGHT = 3 //unit id, enemy id, unit damage, enemy damage
        const val TYPE_NEUTRALIZE = 4 //field
        const val TYPE_CONQUER = 5 //field

        fun fromJSON (json: JsonObject) : TurnAction {
            return TurnAction(
                    json.int("type")!!,
                    json.int("unitId"),
                    json.int("enemyId"),
                    json.int("field"),
                    json.int("oldField"),
                    json.int("unitDamage"),
                    json.int("enemyDamage")
            )
        }

    }
}