package de.lulebe.kreuzzuege.data

import de.lulebe.kreuzzuege.R

data class Field (
        val terrain: Int,
        val building: Int?,
        val buildingFaction: Int?,
        val initiallyOwned: Boolean
) {
    class Terrain {
        companion object {
            const val SEA = 1
            const val SOIL = 2
            const val HEDGES = 3
            const val FOREST = 4
            const val HILLS = 5
            const val ROAD = 6
            const val RIVER = 7
            const val SWAMP = 8
        }
    }
    class Building(val unitsToRecruit: List<Int>, val unitsToSupport: List<Int>, val taxes: Int, val name: Int) {
        companion object {
            const val VILLAGE = 1
            const val CITY = 2
            const val HQ = 3
            const val BARRACKS = 4
            const val STABLE = 5
            const val FACTORY = 6
            const val HARBOUR = 7
            const val AIRPORT = 8
            val Data = mapOf(
                    Pair(VILLAGE, Building(emptyList(), emptyList(), 1, R.string.building_village)),
                    Pair(CITY, Building(emptyList(), listOf(
                            Unit.Type.GUARD,
                            Unit.Type.SPEAR,
                            Unit.Type.SWORD,
                            Unit.Type.ARCHER,
                            Unit.Type.LIGHT_KAV,
                            Unit.Type.ARCHER_KAV,
                            Unit.Type.HEAVY_KAV,
                            Unit.Type.CATAPULT,
                            Unit.Type.BALLISTA,
                            Unit.Type.TREBUCHET,
                            Unit.Type.TOWER
                    ), 4, R.string.building_city)),
                    Pair(HQ, Building(emptyList(), listOf(
                            Unit.Type.GUARD,
                            Unit.Type.SPEAR,
                            Unit.Type.SWORD,
                            Unit.Type.ARCHER,
                            Unit.Type.LIGHT_KAV,
                            Unit.Type.ARCHER_KAV,
                            Unit.Type.HEAVY_KAV,
                            Unit.Type.CATAPULT,
                            Unit.Type.BALLISTA,
                            Unit.Type.TREBUCHET,
                            Unit.Type.TOWER,
                            Unit.Type.AIR
                    ), 2, R.string.building_village)),
                    Pair(BARRACKS, Building(listOf(
                            Unit.Type.GUARD,
                            Unit.Type.SPEAR,
                            Unit.Type.SWORD,
                            Unit.Type.ARCHER
                    ), emptyList(), 0, R.string.building_barracks)),
                    Pair(STABLE, Building(listOf(
                            Unit.Type.LIGHT_KAV,
                            Unit.Type.ARCHER_KAV,
                            Unit.Type.HEAVY_KAV
                    ), emptyList(), 0, R.string.building_stable)),
                    Pair(FACTORY, Building(listOf(
                            Unit.Type.CATAPULT,
                            Unit.Type.BALLISTA,
                            Unit.Type.TREBUCHET,
                            Unit.Type.TOWER
                    ), emptyList(), 0, R.string.building_factory)),
                    Pair(HARBOUR, Building(listOf(
                            Unit.Type.SHIP
                    ), listOf(
                            Unit.Type.SHIP
                    ), 0, R.string.building_harbour)),
                    Pair(AIRPORT, Building(listOf(
                            Unit.Type.AIR
                    ), listOf(
                            Unit.Type.AIR
                    ), 0, R.string.building_airport))
            )
        }
    }
}