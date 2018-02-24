package de.lulebe.kreuzzuege

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import de.lulebe.kreuzzuege.data.Game
import de.lulebe.kreuzzuege.ui.SidePanel
import kotlinx.android.synthetic.main.activity_game.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class GameActivity : AppCompatActivity() {

    private var game: Game? = null
    private var gameJSON: JsonObject? = null
    private val onTurnEndListener = {
        game?.let { g ->
            gameJSON?.let { j ->
                if (g.finished) {
                    // TODO stuff when game's finished
                }
                doAsync {
                    j["data"] = g.toJSON()
                    (application as Kreuzzuege).apiClient.uploadGame(j)
                    uiThread {
                        Toast.makeText(this@GameActivity, "Turn was uploaded", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_game)
        gameJSON = Parser().parse(StringBuilder(intent.getStringExtra("gameJson"))) as JsonObject
        game = Game.fromJSON(
                gameJSON!!,
                this,
                getSharedPreferences("login", Context.MODE_PRIVATE).getInt("userId", 0)
        )
        val sidePanel = SidePanel(lSidePanel, onTurnEndListener)
        sidePanel.game = game
        sidePanel.showGeneralInfo()
        map.loadMap(game!!.map)
        map.renderGame(game!!)
    }
}
