package com.example.distancelove.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object SupabaseConfig {
    const val URL = "https://dulakbeuaabrbzplqjti.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR1bGFrYmV1YWFicmJ6cGxxanRpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTAzNTk4MTgsImV4cCI6MjEwNTkzNTgxOH0.AkDzeYzRre9Z2IRftPwGgvY6kuQ5BlF--S50z6U1EgU"
}

class SupabaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    private fun getHeaders(preferRepresentation: Boolean = false): Headers {
        val builder = Headers.Builder()
            .add("apikey", SupabaseConfig.ANON_KEY)
            .add("Authorization", "Bearer ${authToken ?: SupabaseConfig.ANON_KEY}")

        if (preferRepresentation) {
            builder.add("Prefer", "return=representation")
        }
        return builder.build()
    }

    // --- Supabase Auth ---
    suspend fun signUp(email: String, pass: String, userData: JSONObject = JSONObject()): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email)
                put("password", pass)
                put("data", userData)
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/auth/v1/signup")
                .headers(getHeaders())
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val token = json.optString("access_token").takeIf { it.isNotBlank() }
                if (token != null) {
                    authToken = token
                }
                Result.success(json)
            } else {
                val errorMsg = try {
                    JSONObject(responseBody).optString("error_description", JSONObject(responseBody).optString("msg", responseBody))
                } catch (e: Exception) {
                    responseBody
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "signUp failed", e)
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, pass: String): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("email", email)
                put("password", pass)
            }
            val request = Request.Builder()
                .url("${SupabaseConfig.URL}/auth/v1/token?grant_type=password")
                .headers(getHeaders())
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val token = json.optString("access_token")
                authToken = token
                Result.success(json)
            } else {
                val errorMsg = try {
                    JSONObject(responseBody).optString("error_description", responseBody)
                } catch (e: Exception) {
                    responseBody
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "signIn failed", e)
            Result.failure(e)
        }
    }

    // --- REST Table Operations ---
    suspend fun getTable(table: String, queryParams: String = "select=*"): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.URL}/rest/v1/$table?$queryParams"
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
            val url = "${SupabaseConfig.URL}/rest/v1/$table"
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

    suspend fun updateRow(table: String, matchQuery: String, jsonObject: JSONObject): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = "${SupabaseConfig.URL}/rest/v1/$table?$matchQuery"
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
            val url = "${SupabaseConfig.URL}/rest/v1/$table?$matchQuery"
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
            val url = "${SupabaseConfig.URL}/storage/v1/object/$bucket/$path"
            val body = file.readBytes().toRequestBody(mimeType.toMediaType())

            val request = Request.Builder()
                .url(url)
                .headers(getHeaders())
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val publicUrl = "${SupabaseConfig.URL}/storage/v1/object/public/$bucket/$path"
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Storage Upload Error ${response.code}: ${response.body?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
