package de.lulebe.kreuzzuege

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import de.lulebe.kreuzzuege.data.Game
import de.lulebe.kreuzzuege.data.Maps
import de.lulebe.kreuzzuege.data.Player
import de.lulebe.kreuzzuege.data.Faction
import de.lulebe.kreuzzuege.data.Unit
import de.lulebe.kreuzzuege.ui.SidePanel
import kotlinx.android.synthetic.main.activity_game.*



class GameActivity : AppCompatActivity() {

    private var game: Game? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_game)
        game = Game(
                Game.Type.SINGLEPLAYER,
                Maps.getFullMap(1, this),
                Player(Faction.CRUSADERS, 20, mutableListOf(
                        Unit.create(Unit.Type.GUARD, Faction.CRUSADERS, 12)
                ), mutableListOf()),
                Player(Faction.SARACEN, 20, mutableListOf(), mutableListOf()),
                1,
                false,
                Faction.CRUSADERS,
                "Tom"
        )
        val sidePanel = SidePanel(lSidePanel)
        sidePanel.game = game
        sidePanel.showGeneralInfo()
        map.loadMap(game!!.map)
        map.renderGame(game!!)
    }
}
