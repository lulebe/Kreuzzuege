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
import org.jetbrains.anko.doAsync
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
        deleteFCMToken()
    }

    fun signInSilent() : Boolean {
        val sp = context.getSharedPreferences("login", Context.MODE_PRIVATE)
        if (sp.contains("username") && sp.contains("password"))
            return signIn(sp.getString("username", ""), sp.getString("password", ""))
        return false
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
        val body = response.body()!!.string()
        Log.d("GAMES", body)
        if (response.isSuccessful) {
            return Parser().parse(StringBuilder(body)) as JsonArray<JsonObject>
        } else if (response.code() == 401 && mUsername != null && !retried && signInSilent())
            return getGames(true)
        if (response.code() == 401)
            throw AuthException()
        return JsonArray(emptyList())
    }

    fun uploadGame (data: JsonObject, retried: Boolean = false) : Boolean {
        data["players"] = data.array<JsonObject>("players")!!.map { it.int("id")!! }
        val json = data.toJsonString()
        Log.d("UPLOAD", json)
        val request = if (!data.containsKey("id")) { //is first turn
            val body = RequestBody.create(JSON, json)
            Request.Builder()
                    .url(BASE_URL + "/game")
                    .addHeader("authorization", mJwt)
                    .post(body)
                    .build()
        } else {
            val body = RequestBody.create(JSON, data.toJsonString())
            Request.Builder()
                    .url(BASE_URL + "/game/" + data["id"])
                    .addHeader("authorization", mJwt)
                    .put(body)
                    .build()
        }
        val response = mHttpClient.newCall(request).execute()
        Log.d("UPLOAD", response.code().toString())
        response.body()?.let { Log.d("UPLOAD", it.string()) }
        if (response.code() == 401 && mUsername != null && !retried && signInSilent())
            return uploadGame(data, true)
        return response.isSuccessful
    }

    fun sendFCMToken (token: String) {
        if (!signInSilent()) return
        val sp = context.getSharedPreferences("fcm", Context.MODE_PRIVATE)
        val oldToken = sp.getString("token", null)
        val json = JsonObject(mapOf(
                Pair("newToken", token)
        ))
        var oldIsNew = false
        oldToken?.let {
            json["oldToken"] = it
            oldIsNew = it == token
        }
        if (oldIsNew) return
        val body = RequestBody.create(JSON, json.toJsonString())
        val request = Request.Builder()
                .url(BASE_URL + "/user/device/fcm")
                .addHeader("authorization", mJwt)
                .post(body)
                .build()
        val response = mHttpClient.newCall(request).execute()
        if (response.isSuccessful)
            sp.edit().putString("token", token).apply()
        Log.d("TOKEN_UPDATE", response.code().toString())
    }

    fun deleteFCMToken() {
        val sp = context.getSharedPreferences("fcm", Context.MODE_PRIVATE)
        if (!sp.contains("token") || !signInSilent()) return
        val token = sp.getString("token", "")
        val json = JsonObject(mapOf(
                Pair("oldToken", token)
        ))
        val body = RequestBody.create(JSON, json.toJsonString())
        val request = Request.Builder()
                .url(BASE_URL + "/user/device/fcm")
                .addHeader("authorization", mJwt)
                .post(body)
                .build()
        mHttpClient.newCall(request).execute()
        sp.edit().clear().commit()
    }

    class AuthException: Exception()

}