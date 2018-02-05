package de.lulebe.kreuzzuege.data

import android.content.Context
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
            var building: Int? = if (fieldData[1] > 0) fieldData[1] else null
            var buildingFaction: Int? = if (fieldData[2] > 0) fieldData[2] else null
            Field(fieldData[0], building, buildingFaction, fieldData[3] == 1)
        }.toTypedArray()
        return Map(id, overview[id].sizeX, overview[id].sizeY, fields)
    }

}