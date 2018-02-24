package de.lulebe.kreuzzuege

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.beust.klaxon.JsonObject
import de.lulebe.kreuzzuege.games.ApiClient
import de.lulebe.kreuzzuege.games.GamesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    private val mGamesAdapter = GamesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        rv_games.layoutManager = LinearLayoutManager(this)
        rv_games.adapter = mGamesAdapter
        mGamesAdapter.clickListener = {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("gameJson", it.toJsonString())
            startActivity(intent)
        }
        fab.setOnClickListener {
            createNewGame()
        }
    }

    override fun onResume() {
        super.onResume()
        loadGames()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuitem_settings -> { startActivity(Intent(this, SettingsActivity::class.java)) }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadGames() {
        doAsync {
            val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
            if (sp.contains("username") && sp.contains("password") && (application as Kreuzzuege).apiClient.signIn(sp.getString("username", ""), sp.getString("password", ""))) {
                try {
                    val games = (application as Kreuzzuege).apiClient.getGames()
                    uiThread {
                        mGamesAdapter.userId = sp.getInt("userId", 0)
                        showGames(games.toList())
                    }
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

    private fun createNewGame() {
        startActivity(Intent(this, CreateGameActivity::class.java))
    }

}
