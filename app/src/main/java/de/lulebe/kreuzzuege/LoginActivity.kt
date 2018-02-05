package de.lulebe.kreuzzuege

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import de.lulebe.kreuzzuege.games.ApiClient
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class LoginActivity : AppCompatActivity() {

    private val mApiClient = ApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupViews()
    }

    private fun setupViews() {
        btn_signin.setOnClickListener {
            tv_wronginfo.visibility = View.GONE
            val username = et_username.text.toString()
            val password = et_password.text.toString()
            signIn(username, password)
        }
    }

    private fun signIn(username: String, password: String) {
        doAsync {
            val success = mApiClient.signIn(username, password)
            val editor = getSharedPreferences("login", Context.MODE_PRIVATE).edit()
            if (success) {
                editor.putString("username", username)
                editor.putString("password", password)
                uiThread { finish() }
            } else {
                editor.remove("username")
                editor.remove("password")
                uiThread { tv_wronginfo.visibility = View.VISIBLE }
            }
            editor.apply()
        }
    }
}
