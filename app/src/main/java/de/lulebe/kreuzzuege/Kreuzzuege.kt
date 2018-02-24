package de.lulebe.kreuzzuege

import android.app.Application
import de.lulebe.kreuzzuege.games.ApiClient


class Kreuzzuege : Application() {
    val apiClient = ApiClient(this)
}