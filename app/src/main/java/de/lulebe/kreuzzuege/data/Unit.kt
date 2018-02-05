package de.lulebe.kreuzzuege.data

import de.lulebe.kreuzzuege.R


data class Unit(
        val type: Int,
        val faction: Int,
        var field: Int,
        var hp: Int,
        var food: Int,
        var ammo: Int,
        @Transient var didMove: Boolean = false,
        @Transient var didFight: Boolean = false
) {
    class UnitData (val price: Int, val hitPoints: Int, val ammo: Int, val food: Int, val initiative: Int, val movementPoints: Int, val movementCosts: Array<Int>, val name: Int)
    class Type {
        companion object {
            const val GUARD = 1
            const val SPEAR = 2
            const val SWORD = 3
            const val ARCHER = 4
            const val LIGHT_KAV = 5
            const val ARCHER_KAV = 6
            const val HEAVY_KAV = 7
            const val CATAPULT = 8
            const val BALLISTA = 9
            const val TREBUCHET = 10
            const val TOWER = 11
            const val SHIP = 12
            const val AIR = 13
        }
    }
    companion object {
        val Data = mapOf<Int, UnitData>(
                Pair(Type.GUARD, UnitData(
                        price = 7,
                        hitPoints = 5,
                        ammo = -1,
                        food = -1,
                        initiative = 3,
                        movementPoints = 4,
                        movementCosts = arrayOf(0,-1, 2, -1, 4, -1, 1, -1, -1),
                        name = R.string.unit_guard
                )),
                Pair(Type.SPEAR, UnitData(
                        price = 10,
                        hitPoints = 10,
                        ammo = -1,
                        food = 10,
                        initiative = 3,
                        movementPoints = 5,
                        movementCosts = arrayOf(0,-1, 2, 2, 3, 3, 1, -1, 3),
                        name = R.string.unit_spear
                )),
                Pair(Type.SWORD, UnitData(
                        price = 15,
                        hitPoints = 10,
                        ammo = -1,
                        food = 10,
                        initiative = 3,
                        movementPoints = 5,
                        movementCosts = arrayOf(0,-1, 2, 2, 3, 3, 1, -1, 3),
                        name = R.string.unit_sword
                )),
                Pair(Type.ARCHER, UnitData(
                        price = 20,
                        hitPoints = 10,
                        ammo = 10,
                        food = 10,
                        initiative = 1,
                        movementPoints = 6,
                        movementCosts = arrayOf(0, -1, 2, 2, 3, 3, 1, -1, 3),
                        name = R.string.unit_archer
                )),
                Pair(Type.LIGHT_KAV, UnitData(
                        price = 25,
                        hitPoints = 10,
                        ammo = -1,
                        food = 8,
                        initiative = 3,
                        movementPoints = 8,
                        movementCosts = arrayOf(0, -1, 2, 2, 3, 3, 1, -1, 3),
                        name = R.string.unit_lightkav
                )),
                Pair(Type.ARCHER_KAV, UnitData(
                        price = 30,
                        hitPoints = 10,
                        ammo = 8,
                        food = 8,
                        initiative = 1,
                        movementPoints = 8,
                        movementCosts = arrayOf(0, -1, 2, 2, 3, 3, 1, -1, 3),
                        name = R.string.unit_archerkav
                )),
                Pair(Type.HEAVY_KAV, UnitData(
                        price = 35,
                        hitPoints = 12,
                        ammo = -1,
                        food = 6,
                        initiative = 4,
                        movementPoints = 7,
                        movementCosts = arrayOf(0, -1, 2, 2, 3, 3, 1, -1, 3),
                        name = R.string.unit_heavykav
                )),
                Pair(Type.CATAPULT, UnitData(
                        price = 30,
                        hitPoints = 10,
                        ammo = 6,
                        food = 10,
                        initiative = 3,
                        movementPoints = 4,
                        movementCosts = arrayOf(0, -1, 2, -1, 5, -1, 1, -1, -1),
                        name = R.string.unit_catapult
                )),
                Pair(Type.BALLISTA, UnitData(
                        price = 35,
                        hitPoints = 10,
                        ammo = 5,
                        food = 10,
                        initiative = 3,
                        movementPoints = 4,
                        movementCosts = arrayOf(0, -1, 2, -1, 4, -1, 1, -1, -1),
                        name = R.string.unit_ballista
                )),
                Pair(Type.TREBUCHET, UnitData(
                        price = 45,
                        hitPoints = 10,
                        ammo = 5,
                        food = 10,
                        initiative = 4,
                        movementPoints = 3,
                        movementCosts = arrayOf(0, -1, 2, -1, -1, -1, 1, -1, -1),
                        name = R.string.unit_trebuchet
                )),
                Pair(Type.TOWER, UnitData(
                        price = 45,
                        hitPoints = 12,
                        ammo = 10,
                        food = 10,
                        initiative = 4,
                        movementPoints = 3,
                        movementCosts = arrayOf(0, -1, 2, -1, -1, -1, 1, -1, -1),
                        name = R.string.unit_tower
                )),
                Pair(Type.SHIP, UnitData(
                        price = 40,
                        hitPoints = 10,
                        ammo = -1,
                        food = 10,
                        initiative = 3,
                        movementPoints = 8,
                        movementCosts = arrayOf(0, 1, -1, -1, -1, -1, 1, -1, -1),
                        name = R.string.unit_ship
                )),
                Pair(Type.AIR, UnitData(
                        price = 50,
                        hitPoints = 10,
                        ammo = -1,
                        food = 5,
                        initiative = 2,
                        movementPoints = 10,
                        movementCosts = arrayOf(0, 1, 1, 1, 1, 1, 1, 1, 1),
                        name = R.string.unit_air
                ))
        )
        /*
            const val SEA
            const val SOIL
            const val HEDGES
            const val FOREST
            const val HILLS
            const val ROAD
            const val RIVER
            const val SWAMP
         */

        fun create (type: Int, faction: Int, field: Int) : Unit {
            return Unit(type, faction, field, Data[type]!!.hitPoints, Data[type]!!.food, Data[type]!!.ammo)
        }
    }
}