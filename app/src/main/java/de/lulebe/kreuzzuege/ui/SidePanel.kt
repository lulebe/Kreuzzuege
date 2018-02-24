package de.lulebe.kreuzzuege.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import de.lulebe.kreuzzuege.R
import de.lulebe.kreuzzuege.data.Field
import de.lulebe.kreuzzuege.data.Game
import de.lulebe.kreuzzuege.data.Player
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

    fun showFieldDetails(fieldPos: Int) {
        game?.let { g ->
            val x = fieldPos % g.map.sizeX
            val y = fieldPos / g.map.sizeY
            val field = g.map.fields[fieldPos]
            val currentPlayer = if (g.turn % 2 == 0) g.playerSaracen else g.playerCrusaders
            val currentEnemy = if (g.turn % 2 == 0) g.playerCrusaders else g.playerSaracen
            val tv = layout.findViewById<TextView>(R.id.tv_selectedField)
            tv.text = "Selected Field: " + (x+1) + "|" + (y+1) + ": " + field.terrain
            val building = Field.Building.Data[field.building]
            if (building != null) {
                layout.findViewById<View>(R.id.tv_buildingname).visibility = View.VISIBLE
                layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.VISIBLE
                layout.findViewById<View>(R.id.l_recruitable_units_empty).visibility = View.VISIBLE
                layout.findViewById<View>(R.id.l_recruitable_units).visibility = View.VISIBLE
                val isOwnBuilding = currentPlayer.buildings.contains(fieldPos)
                showBuildingUI(building, isOwnBuilding, g.getUnitOnField(fieldPos) != null, currentPlayer)
            } else {
                layout.findViewById<View>(R.id.tv_buildingname).visibility = View.GONE
                layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.GONE
                layout.findViewById<View>(R.id.l_recruitable_units_empty).visibility = View.GONE
                layout.findViewById<View>(R.id.l_recruitable_units).visibility = View.GONE
            }
        }
    }

    fun hideFieldDetails () {
        layout.findViewById<TextView>(R.id.tv_selectedField).text = "No field selected."
        layout.findViewById<View>(R.id.tv_buildingname).visibility = View.GONE
        layout.findViewById<View>(R.id.tv_info_recruitableunits).visibility = View.GONE
        layout.findViewById<View>(R.id.l_recruitable_units_empty).visibility = View.GONE
        layout.findViewById<View>(R.id.l_recruitable_units).visibility = View.GONE
    }

    private fun showBuildingUI (building: Field.Building, isOwn: Boolean, isOccupied: Boolean, player: Player) {
        val unitList = layout.findViewById<ViewGroup>(R.id.l_recruitable_units)
        unitList.removeAllViews()
        if (building.unitsToRecruit.isEmpty() || !isOwn) {
            unitList.visibility = View.GONE
            layout.findViewById<View>(R.id.l_recruitable_units_empty).visibility = View.VISIBLE
        } else {
            unitList.visibility = View.VISIBLE
            layout.findViewById<View>(R.id.l_recruitable_units_empty).visibility = View.GONE
            building.unitsToRecruit.forEach { unitId ->
                Unit.Data[unitId]?.let { unitData ->
                    val unitLayout = LayoutInflater.from(layout.context).inflate(R.layout.listitem_recruitunit, unitList, false)
                    unitLayout.isClickable = !isOccupied && unitData.price <= player.money
                    unitLayout.findViewById<TextView>(R.id.tv_name).setText(unitData.name)
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