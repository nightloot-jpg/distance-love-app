package com.example.distancelove.data.remote

import android.content.Context
import android.util.Log
import com.example.distancelove.BuildConfig
import com.example.distancelove.data.local.AuthSession
import com.example.distancelove.data.local.AuthSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class SupabaseService(context: Context) {

    private val sessionManager = AuthSessionManager(context)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val baseUrl: String
        get() = try {
            val url = BuildConfig.SUPABASE_URL
            if (!url.isNullOrBlank()) url else "https://dulakbeuaabrbzplqjti.supabase.co"
        } catch (e: Throwable) {
            "https://dulakbeuaabrbzplqjti.supabase.co"
        }

    val anonKey: String
        get() = try {
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (!key.isNullOrBlank()) key else "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR1bGFrYmV1YWFicmJ6cGxxanRpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAzNTk4MTgsImV4cCI6MjEwNTkzNTgxOH0.AkDzeYzRre9Z2IRftPwGgvY6kuQ5BlF--S50z6U1EgU"
        } catch (e: Throwable) {
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR1bGFrYmV1YWFicmJ6cGxxanRpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAzNTk4MTgsImV4cCI6MjEwNTkzNTgxOH0.AkDzeYzRre9Z2IRftPwGgvY6kuQ5BlF--S50z6U1EgU"
        }

    private var currentSession: AuthSession? = null

    init {
        currentSession = sessionManager.getSession()
    }

    fun getSavedSession(): AuthSession? = currentSession ?: sessionManager.getSession()

    private fun getValidToken(): String {
        return currentSession?.accessToken ?: anonKey
    }

    private fun getHeaders(preferRepresentation: Boolean = false): Headers {
        val builder = Headers.Builder()
            .add("apikey", anonKey)
            .add("Authorization", "Bearer ${getValidToken()}")

        if (preferRepresentation) {
            builder.add("Prefer", "return=representation")
        }
        return builder.build()
    }

    // --- Supabase Auth Real Endpoints ---

    suspend fun signUp(email: String, pass: String, metadata: JSONObject = JSONObject()): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", pass)
                put("data", metadata)
            }

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/signup")
                .headers(Headers.Builder().add("apikey", anonKey).build())
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val userObj = json.optJSONObject("user") ?: json
                val userId = userObj.optString("id")
                val accessToken = json.optString("access_token")
                val refreshToken = json.optString("refresh_token")
                val expiresIn = json.optLong("expires_in", 3600L)
                val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)

                if (accessToken.isNotBlank() && userId.isNotBlank()) {
                    val session = AuthSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        expiresAt = expiresAt,
                        userId = userId,
                        email = email.trim().lowercase()
                    )
                    currentSession = session
                    sessionManager.saveSession(session)
                    Result.success(session)
                } else if (userId.isNotBlank()) {
                    // Email confirmation enabled case - auto sign in or return partial
                    val session = AuthSession(
                        accessToken = anonKey,
                        refreshToken = "",
                        expiresAt = System.currentTimeMillis() + 86400000L,
                        userId = userId,
                        email = email.trim().lowercase()
                    )
                    currentSession = session
                    sessionManager.saveSession(session)
                    Result.success(session)
                } else {
                    Result.failure(Exception("Respuesta inesperada al crear usuario"))
                }
            } else {
                val errorMsg = parseErrorMessage(responseBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "signUp exception", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<AuthSession> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim().lowercase())
                put("password", pass)
            }

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/token?grant_type=password")
                .headers(Headers.Builder().add("apikey", anonKey).build())
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val userObj = json.optJSONObject("user")
                val userId = userObj?.optString("id") ?: json.optString("id")
                val accessToken = json.optString("access_token")
                val refreshToken = json.optString("refresh_token")
                val expiresIn = json.optLong("expires_in", 3600L)
                val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)

                val session = AuthSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = expiresAt,
                    userId = userId,
                    email = email.trim().lowercase()
                )
                currentSession = session
                sessionManager.saveSession(session)
                Result.success(session)
            } else {
                val errorMsg = parseErrorMessage(responseBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "signIn exception", e)
            Result.failure(e)
        }
    }

    suspend fun refreshSession(): Result<AuthSession> = withContext(Dispatchers.IO) {
        val saved = currentSession ?: sessionManager.getSession()
        if (saved == null || saved.refreshToken.isBlank()) {
            return@withContext Result.failure(Exception("No refresh token available"))
        }

        try {
            val bodyJson = JSONObject().apply {
                put("refresh_token", saved.refreshToken)
            }

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/token?grant_type=refresh_token")
                .headers(Headers.Builder().add("apikey", anonKey).build())
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val userObj = json.optJSONObject("user")
                val userId = userObj?.optString("id") ?: saved.userId
                val accessToken = json.optString("access_token")
                val newRefreshToken = json.optString("refresh_token", saved.refreshToken)
                val expiresIn = json.optLong("expires_in", 3600L)
                val expiresAt = System.currentTimeMillis() + (expiresIn * 1000L)

                val newSession = AuthSession(
                    accessToken = accessToken,
                    refreshToken = newRefreshToken,
                    expiresAt = expiresAt,
                    userId = userId,
                    email = saved.email
                )
                currentSession = newSession
                sessionManager.saveSession(newSession)
                Result.success(newSession)
            } else {
                Result.failure(Exception("Token refresh failed: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email.trim().lowercase())
            }

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/recover")
                .headers(Headers.Builder().add("apikey", anonKey).build())
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val body = response.body?.string().orEmpty()
                Result.failure(Exception(parseErrorMessage(body)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = currentSession?.accessToken
            if (token != null) {
                val request = Request.Builder()
                    .url("$baseUrl/auth/v1/logout")
                    .headers(getHeaders())
                    .post("{}".toRequestBody(jsonMediaType))
                    .build()
                client.newCall(request).execute()
            }
        } catch (e: Exception) {
            Log.d("SupabaseService", "SignOut remote note: ${e.message}")
        } finally {
            currentSession = null
            sessionManager.clearSession()
        }
        Result.success(true)
    }

    // --- REST Table Operations ---

    suspend fun getTable(table: String, queryParams: String = "select=*"): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            ensureSessionValid()
            val url = "$baseUrl/rest/v1/$table?$queryParams"
            val request = Request.Builder()
                .url(url)
                .headers(getHeaders())
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success(JSONArray(responseBody))
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun insertRow(table: String, jsonObject: JSONObject): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            ensureSessionValid()
            val url = "$baseUrl/rest/v1/$table"
            val request = Request.Builder()
                .url(url)
                .headers(getHeaders(preferRepresentation = true))
                .post(jsonObject.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val result = if (responseBody.startsWith("[")) JSONArray(responseBody) else JSONArray().put(JSONObject(responseBody))
                Result.success(result)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertRow(table: String, jsonObject: JSONObject): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            ensureSessionValid()
            val url = "$baseUrl/rest/v1/$table"
            val headers = Headers.Builder()
                .add("apikey", anonKey)
                .add("Authorization", "Bearer ${getValidToken()}")
                .add("Prefer", "resolution=merge-duplicates,return=representation")
                .build()

            val request = Request.Builder()
                .url(url)
                .headers(headers)
                .post(jsonObject.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val result = if (responseBody.startsWith("[")) JSONArray(responseBody) else JSONArray().put(JSONObject(responseBody))
                Result.success(result)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRow(table: String, matchQuery: String, jsonObject: JSONObject): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            ensureSessionValid()
            val url = "$baseUrl/rest/v1/$table?$matchQuery"
            val request = Request.Builder()
                .url(url)
                .headers(getHeaders())
                .patch(jsonObject.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRow(table: String, matchQuery: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            ensureSessionValid()
            val url = "$baseUrl/rest/v1/$table?$matchQuery"
            val request = Request.Builder()
                .url(url)
                .headers(getHeaders())
                .delete()
                .build()

            val response = client.newCall(request).execute()
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Storage Upload ---

    suspend fun uploadStorageFile(bucket: String, path: String, file: File, mimeType: String = "image/jpeg"): Result<String> = withContext(Dispatchers.IO) {
        try {
            ensureSessionValid()
            val url = "$baseUrl/storage/v1/object/$bucket/$path"
            val body = file.readBytes().toRequestBody(mimeType.toMediaType())

            val headers = Headers.Builder()
                .add("apikey", anonKey)
                .add("Authorization", "Bearer ${getValidToken()}")
                .add("x-upsert", "true")
                .build()

            val request = Request.Builder()
                .url(url)
                .headers(headers)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val publicUrl = "$baseUrl/storage/v1/object/public/$bucket/$path"
                Result.success(publicUrl)
            } else {
                val err = response.body?.string().orEmpty()
                Result.failure(Exception("Storage Upload Error ${response.code}: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun ensureSessionValid() {
        val s = currentSession ?: sessionManager.getSession()
        if (s != null && sessionManager.isSessionExpired(s) && s.refreshToken.isNotBlank()) {
            refreshSession()
        }
    }

    private fun parseErrorMessage(rawJson: String): String {
        return try {
            val obj = JSONObject(rawJson)
            obj.optString("error_description",
                obj.optString("message",
                    obj.optString("msg", rawJson)
                )
            )
        } catch (e: Exception) {
            rawJson
        }
    }
}
