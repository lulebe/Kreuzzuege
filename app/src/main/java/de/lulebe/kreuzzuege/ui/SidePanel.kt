package de.lulebe.kreuzzuege.ui

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.lulebe.kreuzzuege.R
import de.lulebe.kreuzzuege.data.*
import de.lulebe.kreuzzuege.data.Unit


class SidePanel (private val layout: ViewGroup, private val onTurnEndListener: () -> kotlin.Unit) {

    private var _game: Game? = null
    var game: Game?
        get() = _game
        set(value) {
            _game?.removeListener(mGameChangeListener)
            _game = value
            value?.addListener(mGameChangeListener)
        }

    private val mGameChangeListener = {
        game?.let {
            showGeneralInfo()
            if (it.selectedField != null)
                showFieldDetails(it.selectedField!!)
            else
                hideFieldDetails()
        }
        kotlin.Unit
    }

    init {
        layout.findViewById<Button>(R.id.btn_endturn).setOnClickListener {
                    game?.endTurn()
                    endedTurn()
                }
    }

    fun showGeneralInfo () {
        game?.let { g ->
            layout.findViewById<TextView>(R.id.tv_money_cr).text = g.playerCrusaders.money.toString()
            layout.findViewById<TextView>(R.id.tv_money_sa).text = g.playerSaracen.money.toString()
        }
    }

    private fun showFieldDetails(fieldPos: Int) {
        game?.let { g ->
            val x = fieldPos % g.map.sizeX
            val y = fieldPos / g.map.sizeY
            val field = g.map.fields[fieldPos]
            val currentPlayer = if (g.myFaction == Faction.CRUSADERS) g.playerCrusaders else g.playerSaracen
            val currentEnemy = if (g.myFaction == Faction.CRUSADERS) g.playerSaracen else g.playerCrusaders
            val building = Field.Building.Data[field.building]
            if (building != null) {
                layout.findViewById<View>(R.id.tv_buildingname).visibility = View.VISIBLE
                layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.VISIBLE
                layout.findViewById<View>(R.id.l_recruitable_units).visibility = View.VISIBLE
                val isOwnBuilding = currentPlayer.buildings.contains(fieldPos)
                showBuildingUI(building, isOwnBuilding, g.getUnitOnField(fieldPos) != null, currentPlayer)
            } else {
                layout.findViewById<View>(R.id.tv_buildingname).visibility = View.GONE
                layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.GONE
                layout.findViewById<View>(R.id.l_recruitable_units).visibility = View.GONE
            }
        }
    }

    private fun hideFieldDetails () {
        layout.findViewById<View>(R.id.tv_buildingname).visibility = View.GONE
        layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.GONE
        layout.findViewById<View>(R.id.l_recruitable_units).visibility = View.GONE
    }

    private fun showBuildingUI (building: Field.Building, isOwn: Boolean, isOccupied: Boolean, player: Player) {
        val unitList = layout.findViewById<ViewGroup>(R.id.l_recruitable_units)
        unitList.removeAllViews()
        if (building.unitsToRecruit.isEmpty() || !isOwn || isOccupied) {
            unitList.visibility = View.GONE
            layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.GONE
        } else {
            unitList.visibility = View.VISIBLE
            val unitPicSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40F, layout.resources.displayMetrics).toInt()
            building.unitsToRecruit.forEach { unitId ->
                Unit.Data[unitId]?.let { unitData ->
                    val canAffordUnit = unitData.price <= player.money
                    val unitLayout = LayoutInflater.from(layout.context).inflate(R.layout.listitem_recruitunit, unitList, false)
                    unitLayout.isClickable = canAffordUnit
                    if (!canAffordUnit)
                        unitLayout.findViewById<TextView>(R.id.tv_name).setTextColor(Color.RED)
                    unitLayout.findViewById<TextView>(R.id.tv_name).text =
                            unitData.price.toString().padStart(2, '0') +
                            "\uD83D\uDD36 " +
                            unitLayout.resources.getString(unitData.name)
                    val unitFilename =  if (player.faction == Faction.CRUSADERS) "_c.png" else "_s.png"
                    Picasso.with(unitLayout.context)
                            .load("file:///android_asset/units/" + unitId.toString() + unitFilename)
                            .resize(unitPicSize, unitPicSize)
                            .into(unitLayout.findViewById<ImageView>(R.id.iv_unit))
                    unitLayout.findViewById<View>(R.id.btn_info).setOnClickListener {
                                // TODO open Unit info dialog
                            }
                    unitLayout.setOnClickListener {
                        game?.let {
                            it.recruitUnit(player.faction, unitId, it.selectedField!!)
                        }
                    }
                    unitList.addView(unitLayout)
                }
            }
        }
    }

    private fun endedTurn() {
        onTurnEndListener()
    }

}