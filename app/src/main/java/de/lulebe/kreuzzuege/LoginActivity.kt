package de.lulebe.kreuzzuege

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
        if (sp.contains("username") && sp.contains("password")) {
            signIn(sp.getString("username", ""), sp.getString("password", ""))
        }
    }

    private fun setupViews() {
        btn_signin.setOnClickListener {
            tv_wronginfo.visibility = View.GONE
            val username = et_username.text.toString()
            val password = et_password.text.toString()
            signIn(username, password)
        }
        btn_signup.setOnClickListener {
            tv_wronginfo.visibility = View.GONE
            val username = et_username.text.toString()
            val password = et_password.text.toString()
            signUp(username, password)
        }
    }

    private fun showSignInSpinnerDialog() : AlertDialog {
        val view = ProgressBar(this)
        view.isIndeterminate = true
        return AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Logging in...")
                .setView(view)
                .show()
    }

    private fun signIn(username: String, password: String, auto: Boolean = false) {
        val spinnerDialog = showSignInSpinnerDialog()
        doAsync {
            val success = (application as Kreuzzuege).apiClient.signIn(username, password)
            val editor = getSharedPreferences("login", Context.MODE_PRIVATE).edit()
            if (success) {
                editor.putString("username", username)
                editor.putString("password", password)
                uiThread {
                    spinnerDialog.dismiss()
                    finish()
                }
            } else {
                editor.remove("username")
                editor.remove("password")
                uiThread {
                    spinnerDialog.dismiss()
                    if (!auto)
                        tv_wronginfo.visibility = View.VISIBLE
                }
            }
            editor.apply()
        }
    }

    private fun signUp(username: String, password: String) {
        val spinnerDialog = showSignInSpinnerDialog()
        doAsync {
            val success = (application as Kreuzzuege).apiClient.signUp(username, password)
            val editor = getSharedPreferences("login", Context.MODE_PRIVATE).edit()
            if (success) {
                editor.putString("username", username)
                editor.putString("password", password)
                uiThread {
                    spinnerDialog.dismiss()
                    finish()
                }
            } else {
                editor.remove("username")
                editor.remove("password")
                uiThread {
                    spinnerDialog.dismiss()
                }
            }
            editor.apply()
        }
    }
}
