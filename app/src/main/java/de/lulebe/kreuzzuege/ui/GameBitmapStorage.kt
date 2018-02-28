package de.lulebe.kreuzzuege.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import de.lulebe.kreuzzuege.data.Faction
import de.lulebe.kreuzzuege.data.Field
import de.lulebe.kreuzzuege.data.Map
import java.io.File


class GameBitmapStorage (private val context: Context) {

    companion object {
        const val ICON_CRUSADERS = 1
        const val ICON_SARACEN = 2
    }

    var mFieldSize = 0F
    var mIcSize = 0F

    var initialized = false
    private val crusadersBuildings = mutableMapOf<Int, Bitmap>()
    private val saracenBuildings = mutableMapOf<Int, Bitmap>()
    val icons = mutableMapOf<Int, Bitmap>()
    val crusadersUnits = mutableMapOf<Int, Bitmap>()
    val saracenUnits = mutableMapOf<Int, Bitmap>()


    fun init(fieldSize: Float, icSize: Float) {
        initialized = false
        crusadersBuildings.clear()
        saracenBuildings.clear()
        icons.clear()
        crusadersUnits.clear()
        saracenUnits.clear()
        mFieldSize = fieldSize
        mIcSize = icSize
        loadBuildings()
        loadIcons()
        loadUnits()
        initialized = true
    }

    fun createMap (map: Map) : Bitmap {
        while (!initialized) {}
        val bmpBackground = loadImgToWidth(
                "mapBackgrounds" + File.separator + "map" + map.id.toString() + ".jpg",
                (mFieldSize * map.sizeX).toInt()
        ).copy(Bitmap.Config.ARGB_8888, true)
        val bgCanvas = Canvas(bmpBackground!!)
        val buildingPaint = Paint()
        map.fields.forEachIndexed { index, field ->
            if (field.building != null) {
                val y = index / map.sizeX
                val x = index % map.sizeX
                val buildingBmp =
                        if (field.buildingFaction == Faction.CRUSADERS)
                            crusadersBuildings[field.building]!!
                        else
                            saracenBuildings[field.building]!!
                bgCanvas.drawBitmap(buildingBmp, mFieldSize * x, mFieldSize * y, buildingPaint)
            }
        }
        return bmpBackground
    }

    private fun loadBuildings () {
        listOf(
                Field.Building.VILLAGE,
                Field.Building.CITY,
                Field.Building.HQ,
                Field.Building.BARRACKS,
                Field.Building.STABLE,
                Field.Building.FACTORY,
                Field.Building.HARBOUR,
                Field.Building.AIRPORT
        ).forEach { buildingId ->
            val bmpC = loadImgToWidth(
                    "buildings" + File.separator + buildingId.toString() + "_c.png",
                    mFieldSize.toInt()
            )
            crusadersBuildings[buildingId] = bmpC
            val bmpS = loadImgToWidth(
                    "buildings" + File.separator + buildingId.toString() + "_s.png",
                    mFieldSize.toInt()
            )
            saracenBuildings[buildingId] = bmpS
        }
    }

    private fun loadIcons () {
        listOf(
                ICON_CRUSADERS,
                ICON_SARACEN
        ).forEach { iconId ->
            icons[iconId] = loadImgToWidth(
                    "other" + File.separator + "ic_" + iconId.toString() + ".png",
                    mIcSize.toInt()
            )
        }
    }

    private fun loadUnits () {
        for (unitId in 1..13) {
            crusadersUnits[unitId] = loadImgToWidth(
                    "units" + File.separator + unitId.toString() + "_c.png",
                    mFieldSize.toInt()
            )
            saracenUnits[unitId] = loadImgToWidth(
                    "units" + File.separator + unitId.toString() + "_s.png",
                    mFieldSize.toInt()
            )
        }
    }

    private fun loadImgToWidth (assetPath: String, width: Int) : Bitmap {
        val unscaledBmp = BitmapFactory.decodeStream(context.assets.open(assetPath), null, null)
        val height = ((width.toFloat() / unscaledBmp.width.toFloat()) * unscaledBmp.height.toFloat()).toInt()
        val scaledBmp = Bitmap.createScaledBitmap(unscaledBmp, width, height, true)
        unscaledBmp.recycle()
        return scaledBmp
    }
}