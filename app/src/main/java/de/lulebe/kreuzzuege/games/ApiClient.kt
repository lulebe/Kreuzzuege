package de.lulebe.kreuzzuege.games

import de.lulebe.kreuzzuege.data.Game
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody


class ApiClient {

    companion object {
        const val BASE_URL = "https://kreuzzuege.lulebe.net"
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }

    private val mHttpClient = OkHttpClient()
    private var mJwt: String? = null
    private var username: String? = null
    private var password: String? = null

    fun signIn (username: String, password: String) : Boolean {
        this.username = username
        this.password = password
        val json = JsonObject(mapOf(Pair("username", username), Pair("password", password)))
        val body = RequestBody.create(JSON, json.toJsonString())
        val request = Request.Builder()
                .url(BASE_URL + "/users/signin")
                .post(body)
                .build()
        val response = mHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            mJwt = response.body().toString()
        }
        return response.isSuccessful
    }

    fun getGames (retried: Boolean = false) : JsonArray<JsonObject> {
        val request = Request.Builder()
                .url(BASE_URL + "/games")
                .addHeader("authorization", mJwt)
                .build()
        val response = mHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            return Parser().parse(StringBuilder(response.body().toString())) as JsonArray<JsonObject>
        } else if (response.code() == 401 && this.username != null && !retried && signIn(username!!, password!!))
            return getGames(true)
        throw AuthException()
    }

    class AuthException: Exception()

}