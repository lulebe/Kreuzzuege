package de.lulebe.kreuzzuege

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        btn_signout.setOnClickListener {
            (application as Kreuzzuege).apiClient.signOut()
            Toast.makeText(this, "You've been signed out.", Toast.LENGTH_SHORT).show()
        }
    }
}
