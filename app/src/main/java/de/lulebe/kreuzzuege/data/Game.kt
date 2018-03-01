package de.lulebe.kreuzzuege.data

import android.content.Context
import android.graphics.Point
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import java.util.*


data class Game(
        val type: Int,
        val map: Map,
        val playerCrusaders: Player,
        val playerSaracen: Player,
        var lastUnitId: Int,
        var turn: Int,
        var winner: Int?,
        val turnActions: MutableList<TurnAction>,
        @Transient val myFaction: Int,
        @Transient val myId: Int,
        @Transient val enemyId: Int,
        @Transient val enemyName: String
) {

    companion object {
        fun fromJSON (json: JsonObject, context: Context, userId: Int) : Game {
            val gameJson = json.obj("data")!!
            val map = Maps.getFullMap(gameJson.int("map")!!, context)
            val playerCrusaders = Player.fromJSON(gameJson.obj("playerCrusaders")!!)
            val playerSaracen = Player.fromJSON(gameJson.obj("playerSaracen")!!)
            return Game(
                    gameJson.int("type")!!,
                    map,
                    playerCrusaders,
                    playerSaracen,
                    gameJson.int("lastUnitId")!!,
                    gameJson.int("turn")!!,
                    gameJson.int("winner"),
                    gameJson.array<JsonObject>("turnActions")!!.map { TurnAction.fromJSON(it) }.toMutableList(),
                    json.array<JsonObject>("players")!!.first { it.int("id")!! == userId }.int("order")!! + 1,
                    userId,
                    json.array<JsonObject>("players")!!.first { it.int("id")!! != userId }.int("id")!!,
                    json.array<JsonObject>("players")!!.first { it.int("id")!! != userId }.string("name")!!
            )
        }
    }

    class Type {
        companion object {
            const val SINGLEPLAYER = 1
            const val MULTIPLAYER_LOCAL = 2
            const val MULTIPLAYER_REMOTE = 3
        }
    }

    @Transient val unitsById = mutableMapOf<Int, Unit>()

    @Transient private var _selectedField: Int? = null
    var selectedField: Int?
        get() = _selectedField
        set(value) {
            _selectedField = value
            if (selectedUnitFightOptions.contains(value))
                attackField(selectedUnit!!, value!!)
            if (selectedUnitMovementOptions.contains(value))
                moveUnit(selectedUnit!!, value!!)
            selectedUnit = if (value != null)
                getUnitOnField(value)
            else
                null
            selectedUnitMovementOptions = if (selectedUnit != null) getReachableFields(selectedUnit!!) else emptyList()
            selectedUnitFightOptions = if (selectedUnit != null) getAttackableUnitsFields(selectedUnit!!) else emptyList()
            changedGame()
        }
    @Transient var selectedUnit: Unit? = null
    @Transient var selectedUnitMovementOptions: List<Int> = emptyList()
    @Transient var selectedUnitFightOptions: List<Int> = emptyList()
    @Transient private val mListeners = mutableListOf<() -> kotlin.Unit>()

    init {
        if (turn == 1) {
            map.fields.forEachIndexed { index, field ->
                if (field.initiallyOwned) {
                    if (field.buildingFaction == playerCrusaders.faction) {
                        playerCrusaders.buildings.add(index)
                    } else {
                        playerSaracen.buildings.add(index)
                    }
                }
            }
        }
        playerCrusaders.units.forEach {
            unitsById[it.id] = it
        }
        playerSaracen.units.forEach {
            unitsById[it.id] = it
        }
        turnActions.clear()
    }

    fun addListener (listener: () -> kotlin.Unit) {
        if (!mListeners.contains(listener))
            mListeners.add(listener)
    }

    fun removeListener (listener: () -> kotlin.Unit) {
        mListeners.remove(listener)
    }

    fun removeAllListeners() {
        mListeners.clear()
    }

    private fun changedGame() {
        mListeners.forEach {
            it.invoke()
        }
    }

    fun toJSON (): JsonObject {
        val map = mutableMapOf<String, Any>()
        map["type"] = type
        map["map"] = this.map.id
        map["playerCrusaders"] = playerCrusaders.toJSON()
        map["playerSaracen"] = playerSaracen.toJSON()
        map["lastUnitId"] = lastUnitId
        map["turn"] = turn
        winner?.let { map["winner"] = it }
        map["turnActions"] = JsonArray(turnActions.map { it.toJSON() })
        return JsonObject(map)
    }

    fun endTurn () {
        selectedField = null
        val player = if (myFaction == Faction.CRUSADERS) playerCrusaders else playerSaracen
        val enemy = if (myFaction == Faction.CRUSADERS) playerSaracen else playerCrusaders
        player.units.toList().forEach { unit ->
            unit.didMove = false
            unit.didFight = false
            map.fields[unit.field].building?.let { building ->
                if (!player.buildings.contains(unit.field)) {
                    if (enemy.buildings.contains(unit.field)) {
                        changeHP(unit, -1, false)
                        enemy.buildings.remove(unit.field)
                        if (building == Field.Building.HQ)
                            endGame()
                        turnActions.add(TurnAction(
                                TurnAction.TYPE_NEUTRALIZE,
                                null,
                                null,
                                unit.field,
                                null,
                                null,
                                null
                        ))
                    } else {
                        player.buildings.add(unit.field)
                        turnActions.add(TurnAction(
                                TurnAction.TYPE_CONQUER,
                                null,
                                null,
                                unit.field,
                                null,
                                null,
                                null
                        ))
                    }
                } else {
                    changeHP(unit, 1, false)
                    if (hasSupportingBuildingNearby(unit)) {
                        unit.ammo += if (unit.ammo < Unit.Data[unit.type]!!.ammo) 1 else 0
                        unit.food += if (unit.food < Unit.Data[unit.type]!!.food) 1 else 0
                    }
                }
                kotlin.Unit
            }
        }
        player.buildings.forEach {
            player.money += Field.Building.Data[map.fields[it].building]!!.taxes
        }
        turn++
        changedGame()
    }

    fun endGame () {
        winner = myId
    }

    fun getUnitOnField (fieldPos: Int): Unit? {
        val cUnit = playerCrusaders.units.find { it.field == fieldPos }
        if (cUnit != null)
            return cUnit
        return playerSaracen.units.find { it.field == fieldPos }
    }

    fun getBuildingOwner (fieldPos: Int): Int? {
        if (playerCrusaders.buildings.contains(fieldPos))
            return Faction.CRUSADERS
        if (playerSaracen.buildings.contains(fieldPos))
            return Faction.SARACEN
        return null
    }

    fun canRecruitUnitOnField (faction: Int, type: Int, fieldPos: Int) : Boolean {
        if (getBuildingOwner(fieldPos) != faction || getUnitOnField(fieldPos) != null)
            return false
        val buildingData = Field.Building.Data[map.fields[fieldPos].building]
        if (buildingData == null || !buildingData.unitsToRecruit.contains(type))
            return false
        return if (faction == Faction.CRUSADERS)
            playerCrusaders.money >= Unit.Data[type]!!.price
        else
            playerSaracen.money >= Unit.Data[type]!!.price
    }

    fun recruitUnit (faction: Int, type: Int, fieldPos: Int) {
        if (!canRecruitUnitOnField(faction, type, fieldPos))
            return
        Unit.Data[type]?.let { unitData ->
            if (faction == Faction.CRUSADERS) {
                playerCrusaders.money -= unitData.price
                lastUnitId++
                val unit = Unit.create(type, faction, fieldPos, lastUnitId)
                unit.didFight = true
                unit.didMove = true
                playerCrusaders.units.add(unit)
                turnActions.add(TurnAction(
                        TurnAction.TYPE_CREATE,
                        lastUnitId,
                        null,
                        fieldPos,
                        null,
                        null,
                        null
                ))
            }
            if (faction == Faction.SARACEN) {
                playerSaracen.money -= unitData.price
                lastUnitId++
                val unit = Unit.create(type, faction, fieldPos, lastUnitId)
                unit.didFight = true
                unit.didMove = true
                playerSaracen.units.add(unit)
                turnActions.add(TurnAction(
                        TurnAction.TYPE_CREATE,
                        lastUnitId,
                        null,
                        fieldPos,
                        null,
                        null,
                        null
                ))
            }
        }
        changedGame()
    }

    fun moveUnit (unit: Unit, field: Int) {
        turnActions.add(TurnAction(
                TurnAction.TYPE_MOVE,
                unit.id,
                null,
                field,
                unit.field,
                null,
                null
                ))
        unit.field = field
        if (unit.food > 0)
            unit.food--
        unit.didMove = true
        if (!Unit.Data[unit.type]!!.moveAndFight)
            unit.didFight = true
        changedGame()
    }

    fun attackField (unit: Unit, field: Int) {
        getUnitOnField(field)?.let { fight(unit, it) }
    }

    fun fight (attacker: Unit, defender: Unit) {
        var defenderDamage = 0
        var attackerDamage = 0
        var attackerInitiative = Unit.Data[attacker.type]!!.initiative
        var defenderInitiative = Unit.Data[defender.type]!!.initiative
        if(getDistance(attacker.field, defender.field) > 1) { //Distance attack, attacker will not get hurt
            defenderDamage = this.computeDamage(attacker,defender)
            if (attacker.ammo > 0)
                attacker.ammo--
            changeHP(defender, -defenderDamage)
        } else { // close combat, both may get hurt
            if(Field.Terrain.HBonuses[map.fields[defender.field].terrain]!! > 0)
                defenderInitiative--
            attackerInitiative--
            //depending on initiative, one starts the fight first or both at the same time
            //lower initiative starts the fight (what?!)
            if(attackerInitiative == defenderInitiative) {
                defenderDamage = this.computeDamage(attacker,defender)
                attackerDamage = this.computeDamage(defender,attacker)
                if (attacker.ammo > 0)
                    attacker.ammo--
                if (defender.ammo > 0)
                    defender.ammo--
                changeHP(defender, -defenderDamage)
                changeHP(attacker, -attackerDamage)
            } else if(attackerInitiative < defenderInitiative) {
                defenderDamage = this.computeDamage(attacker,defender)
                if (attacker.ammo > 0)
                    attacker.ammo--
                changeHP(defender, -defenderDamage)
                if(defender.hitPoints > 0) {
                    attackerDamage = this.computeDamage(defender,attacker)
                    if (defender.ammo > 0)
                        defender.ammo--
                    changeHP(attacker, -attackerDamage)
                }
            } else if(attackerInitiative > defenderInitiative) {
                attackerDamage = this.computeDamage(defender,attacker)
                if (defender.ammo > 0)
                    defender.ammo--
                changeHP(attacker, -attackerDamage)
                if(attacker.hitPoints > 0) {
                    defenderDamage = this.computeDamage(attacker,defender)
                    if (attacker.ammo > 0)
                        attacker.ammo--
                    changeHP(defender, -defenderDamage)
                }
            }
        }
        attacker.didFight = true
        attacker.didMove = true
        turnActions.add(TurnAction(
                TurnAction.TYPE_FIGHT,
                attacker.id,
                defender.id,
                null,
                null,
                attackerDamage,
                defenderDamage
        ))
    }

    fun computeDamage (attacker: Unit, defender: Unit) : Int {
        var damage = 0
        val distance = getDistance(attacker.field, defender.field)
        val rand = Random()
        for (i in 0..5) {
            var attackerNumberOfFights = Unit.Data[attacker.type]!!.attackNumberOfFights[defender.type]
            var defenderNumberOfFights = Unit.Data[defender.type]!!.defenseNumberOfFights[attacker.type]
            if(distance < Unit.Data[attacker.type]!!.minAttackDistance) {
                attackerNumberOfFights = 0
            }
            if(distance < Unit.Data[defender.type]!!.minAttackDistance) {
                defenderNumberOfFights = 0
            }
            val defenderCity = map.fields[defender.field].building
            if(defenderCity != null) {
                defenderNumberOfFights += Field.Building.Data[defenderCity]!!.battleBonus
                defenderNumberOfFights += Field.Terrain.HBonuses[map.fields[defender.field].terrain]!!
            }
            val attackerCity = map.fields[attacker.field].building
            if(attackerCity != null) {
                attackerNumberOfFights += Math.ceil(Field.Building.Data[attackerCity]!!.battleBonus.toDouble() / 2.0).toInt()
                attackerNumberOfFights += Math.ceil(Field.Terrain.HBonuses[map.fields[attacker.field].terrain]!!.toDouble() / 2.0).toInt()
            }
            val attackerHitpoints = attacker.hitPoints;
            var diceResult = 0
            var damageAdd = 0
            for (j in 0..(attackerHitpoints-1)) {
                var highestAttackerDiceResult = 0
                var highestDefenderDiceResult = 0
                for (k in 0..(attackerNumberOfFights-1)) {

                    diceResult = rand.nextInt(6) + 1
                    if(diceResult > highestAttackerDiceResult) {
                        highestAttackerDiceResult = diceResult
                    }
                }
                for (l in 0..(defenderNumberOfFights-1)) {
                    diceResult = rand.nextInt(6) + 1
                    if(diceResult > highestDefenderDiceResult) {
                        highestDefenderDiceResult = diceResult
                    }
                }
                if(highestDefenderDiceResult < highestAttackerDiceResult) {
                    damageAdd += 1
                }
            }
            damage += damageAdd
        }
        damage /= 6
        if(damage > defender.hitPoints) {
            damage = defender.hitPoints
        }
        if(damage > 8) {
            damage = 8
        }
        return damage
    }

    fun changeHP (unit: Unit, change: Int, notify: Boolean = true) {
        unit.hitPoints += change
        if (unit.hitPoints <= 0) {
            val owner = if (unit.faction == Faction.CRUSADERS) playerCrusaders else playerSaracen
            owner.units.remove(unit)
        } else if (unit.hitPoints > Unit.Data[unit.type]!!.hitPoints)
            unit.hitPoints = Unit.Data[unit.type]!!.hitPoints
        if (notify)
            changedGame()
    }

    fun getDistance (fieldA: Int, fieldB: Int) : Int {
        val ax = fieldA % map.sizeX
        val ay = fieldA / map.sizeX
        val bx = fieldB % map.sizeX
        val by = fieldB / map.sizeX
        val distx = Math.abs(ax - bx)
        val disty = Math.abs(ay - by)
        return distx + disty
    }

    fun getReachableFields (unit: Unit) : List<Int> {
        val movementPoints = if (unit.food != 0) Unit.Data[unit.type]!!.movementPoints else 1
        val movementCosts = Unit.Data[unit.type]!!.movementCosts
        /*
        Starting at unitPos, check movement costs to all 4 surrounding fields, store points left afterwards.
        Loop through as long as fields with points but checked=false are left
        checkedFields = Map<fieldPos -> Pair<pointsLeft, checked>>
         */
        val reachableFields = mutableMapOf<Int, Int>()
        reachableFields[unit.field] = movementPoints
        var allDone = 0
        if (unit.didMove || unit.faction != myFaction)
            allDone = 2
        while (allDone < 2) {
            var changed = false
            reachableFields.keys.toList().forEach {
                val fieldPos = it
                val pointsLeft = reachableFields[it]!!
                if (pointsLeft >= 1) {
                    //field above if field is not top row
                    if (fieldPos / map.sizeX > 0) {
                        val fieldAbovePos = fieldPos - map.sizeX
                        val movementCostsToAbove = movementCosts[map.fields[fieldAbovePos].terrain]
                        if (movementCostsToAbove > -1) {
                            if (getUnitOnField(fieldAbovePos) == null) {
                                val pointsLeftAbove = pointsLeft - movementCostsToAbove
                                if (reachableFields[fieldAbovePos] == null || reachableFields[fieldAbovePos]!! < pointsLeftAbove) {
                                    reachableFields[fieldAbovePos] = pointsLeftAbove
                                    changed = true
                                }
                            }
                        }
                    }
                    //field below if field is not bottom row
                    if (fieldPos / map.sizeX < map.sizeY - 1) {
                        val fieldBelowPos = fieldPos + map.sizeX
                        val movementCostsToBelow = movementCosts[map.fields[fieldBelowPos].terrain]
                        if (movementCostsToBelow > -1) {
                            if (getUnitOnField(fieldBelowPos) == null) {
                                val pointsLeftBelow = pointsLeft - movementCostsToBelow
                                if (reachableFields[fieldBelowPos] == null || reachableFields[fieldBelowPos]!! < pointsLeftBelow) {
                                    reachableFields[fieldBelowPos] = pointsLeftBelow
                                    changed = true
                                }
                            }
                        }
                    }
                    //field to Left if field is not left row
                    if (fieldPos % map.sizeX > 0) {
                        val fieldToLeftPos = fieldPos - 1
                        val movementCostsToLeft = movementCosts[map.fields[fieldToLeftPos].terrain]
                        if (movementCostsToLeft > -1) {
                            if (getUnitOnField(fieldToLeftPos) == null) {
                                val pointsLeftToLeft = pointsLeft - movementCostsToLeft
                                if (reachableFields[fieldToLeftPos] == null || reachableFields[fieldToLeftPos]!! < pointsLeftToLeft) {
                                    reachableFields[fieldToLeftPos] = pointsLeftToLeft
                                    changed = true
                                }
                            }
                        }
                    }
                    //field to Right if field is not left row
                    if (fieldPos % map.sizeX < map.sizeX - 1) {
                        val fieldToRightPos = fieldPos + 1
                        val movementCostsToRight = movementCosts[map.fields[fieldToRightPos].terrain]
                        if (movementCostsToRight > -1) {
                            if (getUnitOnField(fieldToRightPos) == null) {
                                val pointsLeftToRight = pointsLeft - movementCostsToRight
                                if (reachableFields[fieldToRightPos] == null || reachableFields[fieldToRightPos]!! < pointsLeftToRight) {
                                    reachableFields[fieldToRightPos] = pointsLeftToRight
                                    changed = true
                                }
                            }
                        }
                    }
                }
            }
            if (!changed)
                allDone++
        }
        val reachableFieldList = mutableListOf<Int>()
        val iterator = reachableFields.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.value >= 0)
                reachableFieldList.add(item.key)
        }
        reachableFieldList.remove(unit.field)
        return reachableFieldList
    }
    
    fun getAttackableUnitsFields (unit: Unit) : List<Int> {
        val minAttackDistance = Unit.Data[unit.type]!!.minAttackDistance
        val maxAttackDistance = Unit.Data[unit.type]!!.maxAttackDistance
        val attackableFields = mutableListOf<Int>()
        if (unit.didFight || unit.faction != myFaction)
            return emptyList()
        // 1. get all fields with minAttackDistance <= distance to field <= maxAttackDistance
        val fieldsToCheck = mutableListOf<Point>()
        if (minAttackDistance == 1) fieldsToCheck.addAll(FieldDistances.distance_1)
        if (maxAttackDistance >= 2) fieldsToCheck.addAll(FieldDistances.distance_2)
        if (maxAttackDistance >= 3) fieldsToCheck.addAll(FieldDistances.distance_3)
        if (maxAttackDistance >= 4) fieldsToCheck.addAll(FieldDistances.distance_4)
        val unitX = unit.field % map.sizeX
        val unitY = unit.field / map.sizeX
        val minX = - unitX
        val maxX = map.sizeX - unitX - 1
        val minY = - unitY
        val maxY = map.sizeY - unitY - 1
        fieldsToCheck.filter {
            it.x in minX..maxX && it.y in minY..maxY
        }.forEach { // 2. add all fields with enemy units on them to the final list
            val field = unit.field + it.x + (it.y * map.sizeX)
            getUnitOnField(field)?.let { u ->
                if (u.faction != unit.faction)
                    attackableFields.add(field)
            }
        }
        return attackableFields
    }

    fun hasSupportingBuildingNearby (unit: Unit) : Boolean {
        val player = if (unit.faction == Faction.CRUSADERS) playerCrusaders else playerSaracen
        if (
                unit.field / map.sizeX > 0 &&
                player.buildings.contains(unit.field - map.sizeX) &&
                Field.Building.Data[map.fields[unit.field - map.sizeX].building]!!.unitsToSupport.contains(unit.type)
        ) return true
        if (
                unit.field / map.sizeX < map.sizeY - 1 &&
                player.buildings.contains(unit.field + map.sizeX) &&
                Field.Building.Data[map.fields[unit.field + map.sizeX].building]!!.unitsToSupport.contains(unit.type)
        ) return true
        if (
                unit.field % map.sizeX > 0 &&
                player.buildings.contains(unit.field - 1) &&
                Field.Building.Data[map.fields[unit.field - 1].building]!!.unitsToSupport.contains(unit.type)
        ) return true
        if (
                unit.field % map.sizeX < map.sizeX - 1 &&
                player.buildings.contains(unit.field + 1) &&
                Field.Building.Data[map.fields[unit.field + 1].building]!!.unitsToSupport.contains(unit.type)
        ) return true
        return false
    }
}