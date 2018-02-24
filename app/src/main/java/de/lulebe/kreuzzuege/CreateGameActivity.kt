package de.lulebe.kreuzzuege

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.widget.ProgressBar
import android.widget.Toast
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import de.lulebe.kreuzzuege.data.*
import de.lulebe.kreuzzuege.data.Map
import de.lulebe.kreuzzuege.data.Unit
import de.lulebe.kreuzzuege.games.MapsAdapter
import de.lulebe.kreuzzuege.games.UsersAdapter
import kotlinx.android.synthetic.main.activity_create_game.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class CreateGameActivity : AppCompatActivity() {

    private val mUsersAdapter = UsersAdapter()
    private val mMapsAdapter = MapsAdapter()
    private var mSelectedUserId = 0
    private var mSelectedUsername = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        btn_search.setOnClickListener {
            searchUsers(et_search.text.toString())
        }
        rv_users.layoutManager = LinearLayoutManager(this)
        rv_users.adapter = mUsersAdapter
        mUsersAdapter.clickListener = {
            selectedUser(it)
        }
        mMapsAdapter.clickListener = {
            selectedMap(it)
        }
    }

    private fun showSearchSpinnerDialog() : AlertDialog {
        val view = ProgressBar(this)
        view.isIndeterminate = true
        return AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Searching Users...")
                .setView(view)
                .show()
    }

    private fun showGameCreationSpinnerDialog() : AlertDialog {
        val view = ProgressBar(this)
        view.isIndeterminate = true
        return AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Creating Game...")
                .setView(view)
                .show()
    }

    private fun searchUsers(query: String) {
        val spinnerDialog = showSearchSpinnerDialog()
        doAsync {
            val users = (application as Kreuzzuege).apiClient.searchUsers(query)
            uiThread {
                spinnerDialog.dismiss()
                mUsersAdapter.users = users
                mUsersAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun selectedUser(user: JsonObject) {
        mSelectedUserId = user.int("id")!!
        mSelectedUsername = user.string("name")!!
        rv_users.adapter = mMapsAdapter
        Toast.makeText(this, "Select a map to play against $mSelectedUsername!", Toast.LENGTH_SHORT).show()
    }

    private fun selectedMap(map: Map) {
        val spinnerDialog = showGameCreationSpinnerDialog()
        doAsync {
            val game = Game(
                    Game.Type.SINGLEPLAYER,
                    Maps.getFullMap(map.id, this@CreateGameActivity),
                    Player(Faction.CRUSADERS, 20, mutableListOf(), mutableListOf()),
                    Player(Faction.SARACEN, 20, mutableListOf(), mutableListOf()),
                    1,
                    false,
                    Faction.CRUSADERS,
                    mSelectedUsername
            )
            val json = JsonObject(
                    mapOf(
                            Pair("data", game.toJSON()),
                            Pair("players", JsonArray(listOf(JsonObject(mapOf(
                                    Pair("id", mSelectedUserId),
                                    Pair("name", mSelectedUsername)
                            )))))
                    )
            ).toJsonString()
            uiThread {
                val gameActivityIntent = Intent(this@CreateGameActivity, GameActivity::class.java)
                gameActivityIntent.putExtra("gameJson", json)
                spinnerDialog.dismiss()
                startActivity(gameActivityIntent)
                finish()
            }
        }
    }
}
