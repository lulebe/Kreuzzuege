package de.lulebe.kreuzzuege.games

import android.content.Context
import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import de.lulebe.kreuzzuege.data.Game
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URLEncoder


class ApiClient(private val context: Context) {

    companion object {
        const val BASE_URL = "https://api.kreuzzuege.lulebe.net"
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }

    private val mHttpClient = OkHttpClient()
    private var mJwt: String? = null
    private var mUsername: String? = null
    private var mPassword: String? = null
    private var mUserId: Int? = null

    fun signOut () {
        mJwt = null
        mUsername = null
        mPassword = null
        mUserId = null
        val spEditor = context.getSharedPreferences("login", Context.MODE_PRIVATE).edit()
        spEditor.remove("userId")
        spEditor.remove("username")
        spEditor.remove("password")
        spEditor.commit()
    }

    fun signIn (username: String, password: String) : Boolean {
        val json = JsonObject(mapOf(Pair("username", username), Pair("password", password)))
        val body = RequestBody.create(JSON, json.toJsonString())
        val request = Request.Builder()
                .url(BASE_URL + "/user/signin")
                .post(body)
                .build()
        val response = mHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val jsonResponse = Parser().parse(StringBuilder(response.body()!!.string())) as JsonObject
            mJwt = jsonResponse.string("token")!!
            mUserId = jsonResponse.int("id")!!
            val spEditor = context.getSharedPreferences("login", Context.MODE_PRIVATE).edit()
            spEditor.putInt("userId", mUserId!!)
            spEditor.commit()
            mUsername = username
            mPassword = password
        }
        return response.isSuccessful
    }

    fun signUp (username: String, password: String) : Boolean {
        val json = JsonObject(mapOf(Pair("username", username), Pair("password", password)))
        val body = RequestBody.create(JSON, json.toJsonString())
        val request = Request.Builder()
                .url(BASE_URL + "/user")
                .post(body)
                .build()
        val response = mHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val jsonResponse = Parser().parse(StringBuilder(response.body()!!.string())) as JsonObject
            mJwt = jsonResponse.string("token")!!
            mUserId = jsonResponse.int("id")!!
            val spEditor = context.getSharedPreferences("login", Context.MODE_PRIVATE).edit()
            spEditor.putInt("userId", mUserId!!)
            spEditor.commit()
            mUsername = username
            mPassword = password
        }
        return response.isSuccessful
    }

    fun searchUsers (name: String) : JsonArray<JsonObject> {
        val request = Request.Builder()
                .url(BASE_URL + "/user/search?q=" + URLEncoder.encode(name, "UTF-8"))
                .build()
        val response = mHttpClient.newCall(request).execute()
        if (response.isSuccessful)
            return Parser().parse(StringBuilder(response.body()!!.string())) as JsonArray<JsonObject>
        return JsonArray(emptyList())
    }

    fun getGames (retried: Boolean = false) : JsonArray<JsonObject> {
        val request = Request.Builder()
                .url(BASE_URL + "/game")
                .addHeader("authorization", mJwt)
                .build()
        val response = mHttpClient.newCall(request).execute()
        Log.d("GAMES", response.code().toString())
        Log.d("GAMES", response.body()!!.string())
        if (response.isSuccessful) {
            return Parser().parse(StringBuilder(response.body()!!.string())) as JsonArray<JsonObject>
        } else if (response.code() == 401 && mUsername != null && !retried && signIn(mUsername!!, mPassword!!))
            return getGames(true)
        if (response.code() == 401)
            throw AuthException()
        return JsonArray(emptyList())
    }

    fun uploadGame (data: JsonObject, retried: Boolean = false) : Boolean {
        val request = if (!data.containsKey("id")) { //is first turn
            val body = RequestBody.create(JSON, data.toJsonString())
            Request.Builder()
                    .url(BASE_URL + "/game")
                    .post(body)
                    .build()
        } else {
            val body = RequestBody.create(JSON, data.toJsonString())
            Request.Builder()
                    .url(BASE_URL + "/game/" + data["id"])
                    .put(body)
                    .build()
        }
        val response = mHttpClient.newCall(request).execute()
        if (response.code() == 401 && mUsername != null && !retried && signIn(mUsername!!, mPassword!!))
            return uploadGame(data, true)
        return response.isSuccessful
    }

    class AuthException: Exception()

}