package de.lulebe.kreuzzuege

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.beust.klaxon.JsonObject
import com.google.firebase.iid.FirebaseInstanceId
import de.lulebe.kreuzzuege.games.ApiClient
import de.lulebe.kreuzzuege.games.GamesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    private val gameClickListener: (JsonObject) -> Unit = {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("gameJson", it.toJsonString())
        startActivity(intent)
    }
    private var mUserId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        rv_games.layoutManager = LinearLayoutManager(this)
        fab.setOnClickListener {
            createNewGame()
        }
    }

    override fun onResume() {
        super.onResume()
        loadGames()
        checkFirebase()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuitem_signout -> { signout() }
            R.id.menuitem_refresh -> { loadGames() }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadGames() {
        Log.d("LOAD", "1")
        doAsync {
            val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
            if (sp.contains("username") && sp.contains("password") && (application as Kreuzzuege).apiClient.signIn(sp.getString("username", ""), sp.getString("password", ""))) {
                try {
                    Log.d("LOAD", "2")
                    val games = (application as Kreuzzuege).apiClient.getGames()
                    Log.d("LOAD", "3")
                    uiThread {
                        mUserId = sp.getInt("userId", 0)
                        Log.d("LOAD", "4")
                        showGames(games.toList())
                        Log.d("LOAD", "5")
                        tv_username.text = sp.getString("username", "")
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

    private fun showGames(gList: List<JsonObject>) {
        if (gList.isEmpty()) {
            Log.d("LOAD", "6")
            tv_emptyinfo.visibility = View.VISIBLE
            rv_games.visibility = View.GONE
        } else {
            Log.d("LOAD", "7")
            tv_emptyinfo.visibility = View.GONE
            rv_games.visibility = View.VISIBLE
        }
        rv_games.adapter = GamesAdapter(gList, mUserId, gameClickListener)
        Log.d("LOAD", "8")
    }

    private fun createNewGame() {
        startActivity(Intent(this, CreateGameActivity::class.java))
    }

    private fun signout() {
        mUserId = 0
        rv_games.adapter = GamesAdapter(emptyList(), 0, gameClickListener)
        tv_username.text = ""
        doAsync {
            (application as Kreuzzuege).apiClient.signOut()
            uiThread {
                showLoginScreen()
            }
        }
    }

    private fun checkFirebase() {
        FirebaseInstanceId.getInstance().token?.let {
            Log.d("TOKEN", it)
            doAsync {
                (application as Kreuzzuege).apiClient.sendFCMToken(it)
            }
        }
        Log.d("TOKEN", "check done.")
    }

}
