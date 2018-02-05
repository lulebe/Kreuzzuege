package de.lulebe.kreuzzuege

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.beust.klaxon.JsonObject
import de.lulebe.kreuzzuege.data.Game
import de.lulebe.kreuzzuege.games.ApiClient
import de.lulebe.kreuzzuege.games.GamesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val mApiClient = ApiClient()
    private val mGamesAdapter = GamesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_games.layoutManager = LinearLayoutManager(this)
        rv_games.adapter = mGamesAdapter
        mGamesAdapter.clickListener = {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("jsonGame", it.toJsonString())
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        loadGames()
    }

    private fun loadGames() {
        doAsync {
            val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
            if (sp.contains("username") && sp.contains("password")) {
                mApiClient.signIn(sp.getString("username", ""), sp.getString("password", ""))
                try {
                    val games = mApiClient.getGames()
                    uiThread { showGames(games.toList()) }
                } catch (e: ApiClient.AuthException) {
                    uiThread { showLoginScreen() }
                }
            } else {
                uiThread { showLoginScreen() }
            }
        }
    }

    private fun showLoginScreen() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun showGames(games: List<JsonObject>) {
        if (games.isEmpty()) {
            tv_emptyinfo.visibility = View.VISIBLE
            rv_games.visibility = View.GONE
        } else {
            tv_emptyinfo.visibility = View.GONE
            rv_games.visibility = View.VISIBLE
        }
        mGamesAdapter.games = games
        mGamesAdapter.notifyDataSetChanged()
    }

}
