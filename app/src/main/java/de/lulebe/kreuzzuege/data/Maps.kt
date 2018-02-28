package de.lulebe.kreuzzuege.data

import android.content.Context
import de.lulebe.kreuzzuege.R
import java.io.File


object Maps {

    val overview = arrayOf(
            Map(1, 10, 10, arrayOf()),
            Map(2, 10, 10, arrayOf()),
            Map(3, 20, 10, arrayOf()),
            Map(4, 20, 20, arrayOf()),
            Map(5, 20, 10, arrayOf()),
            Map(6, 10, 20, arrayOf()),
            Map(7, 10, 20, arrayOf()),
            Map(8, 15, 15, arrayOf()),
            Map(9, 15, 15, arrayOf()),
            Map(10, 20, 10, arrayOf())
    )

    fun getFullMap (id: Int, context: Context) : Map {
        val rawFields = context.assets.open("mapData" + File.separator + "map"+id.toString()+".txt")
                .bufferedReader().use { it.readText() }
        val fields = rawFields.split(";").filter { it.isNotEmpty() }.map {
            val fieldData = it.split(",").map { Integer.parseInt(it) }
            val building: Int? = if (fieldData[1] > 0) fieldData[1] else null
            val buildingFaction: Int? = if (fieldData[2] > 0) fieldData[2] else null
            Field(fieldData[0], building, buildingFaction, fieldData[3] == 1)
        }.toTypedArray()
        return Map(id, overview[id-1].sizeX, overview[id-1].sizeY, fields)
    }

    fun getMapOverview (id: Int) : Int {
        return when (id) {
            1 -> R.drawable.map1_preview
            2 -> R.drawable.map2_preview
            3 -> R.drawable.map3_preview
            4 -> R.drawable.map4_preview
            5 -> R.drawable.map5_preview
            6 -> R.drawable.map6_preview
            7 -> R.drawable.map7_preview
            8 -> R.drawable.map8_preview
            9 -> R.drawable.map9_preview
            10 -> R.drawable.map10_preview
            11 -> R.drawable.map11_preview
            12 -> R.drawable.map12_preview
            13 -> R.drawable.map13_preview
            14 -> R.drawable.map14_preview
            15 -> R.drawable.map15_preview
            16 -> R.drawable.map16_preview
            17 -> R.drawable.map17_preview
            18 -> R.drawable.map18_preview
            19 -> R.drawable.map19_preview
            20 -> R.drawable.map20_preview
            else -> 0
        }
    }

}