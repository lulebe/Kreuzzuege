package de.lulebe.kreuzzuege.data


data class Game(
        val type: Int,
        val map: Map,
        val playerCrusaders: Player,
        val playerSaracen: Player,
        var turn: Int,
        var finished: Boolean,
        @Transient val myFaction: Int,
        @Transient val enemyName: String
) {

    companion object {
        fun fromJSON (json: String) : Game {
            TODO("implement")
        }
    }

    class Type {
        companion object {
            const val SINGLEPLAYER = 1
            const val MULTIPLAYER_LOCAL = 2
            const val MULTIPLAYER_REMOTE = 3
        }
    }

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
    }

    @Transient private var _selectedField: Int? = null
    var selectedField: Int?
        get() = _selectedField
        set(value) {
            _selectedField = value
            if (selectedUnitMovementOptions.contains(value))
                moveUnit(selectedUnit!!, value!!)
            selectedUnit = if (value != null)
                getUnitOnField(value)
            else
                null
            selectedUnitMovementOptions = if (selectedUnit != null) getReachableFields(selectedUnit!!) else emptyList()
            changedGame()
        }
    @Transient var selectedUnit: Unit? = null
    @Transient var selectedUnitMovementOptions: List<Int> = emptyList()
    @Transient private val mListeners = mutableListOf<() -> kotlin.Unit>()

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

    fun toJSON (): String {
        return "TODO!!!"
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
                    } else
                        player.buildings.add(unit.field)
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
        // TODO
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
                val unit = Unit.create(type, faction, fieldPos)
                unit.didFight = true
                unit.didMove = true
                playerCrusaders.units.add(unit)
            }
        }
        changedGame()
    }

    fun moveUnit (unit: Unit, field: Int) {
        unit.field = field
        unit.food--
        unit.didMove = true
        changedGame()
    }

    fun changeHP (unit: Unit, change: Int, notify: Boolean = true) {
        unit.hp += change
        if (unit.hp <= 0) {
            val owner = if (unit.faction == Faction.CRUSADERS) playerCrusaders else playerSaracen
            owner.units.remove(unit)
        } else if (unit.hp > Unit.Data[unit.type]!!.hitPoints)
            unit.hp = Unit.Data[unit.type]!!.hitPoints
        if (notify)
            changedGame()
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
        if (unit.didMove)
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