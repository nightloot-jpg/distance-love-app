package com.example.distancelove.data

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import com.example.distancelove.ui.screens.StreamingPlatform

object StreamingSessionManager {
    private const val PREFS_NAME = "streaming_platform_sessions"
    private const val KEY_DESKTOP_MODE_PREFIX = "desktop_mode_"
    private const val KEY_LOGGED_IN_PREFIX = "logged_in_"

    const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    const val MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isDesktopMode(context: Context, platform: StreamingPlatform): Boolean {
        // Desktop mode is enabled by default for major streaming providers to avoid mobile roadblocks
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_DESKTOP_MODE_PREFIX + platform.name, true)
    }

    fun setDesktopMode(context: Context, platform: StreamingPlatform, isDesktop: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DESKTOP_MODE_PREFIX + platform.name, isDesktop).apply()
    }

    fun isPlatformLoggedIn(context: Context, platform: StreamingPlatform): Boolean {
        val prefs = getPrefs(context)
        // Check stored flag or cookie manager presence
        val storedFlag = prefs.getBoolean(KEY_LOGGED_IN_PREFIX + platform.name, false)
        if (storedFlag) return true

        // Fallback: check if domain has cookies
        val domain = platform.initialUrl
        val cookies = CookieManager.getInstance().getCookie(domain)
        return !cookies.isNullOrBlank() && (cookies.contains("session") || cookies.contains("auth") || cookies.contains("user") || cookies.contains("netflix") || cookies.contains("id"))
    }

    fun setPlatformLoggedIn(context: Context, platform: StreamingPlatform, loggedIn: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOGGED_IN_PREFIX + platform.name, loggedIn).apply()
        CookieManager.getInstance().flush()
    }

    fun getLoggedInPlatforms(context: Context): Set<StreamingPlatform> {
        val prefs = getPrefs(context)
        val set = mutableSetOf<StreamingPlatform>()
        StreamingPlatform.entries.forEach { p ->
            val flag = prefs.getBoolean(KEY_LOGGED_IN_PREFIX + p.name, false)
            if (flag) {
                set.add(p)
            } else {
                val cookies = CookieManager.getInstance().getCookie(p.initialUrl)
                if (!cookies.isNullOrBlank() && (cookies.contains("session") || cookies.contains("auth") || cookies.contains("user"))) {
                    set.add(p)
                }
            }
        }
        // Always include YouTube and Twitch by default as public streaming
        set.add(StreamingPlatform.YOUTUBE)
        set.add(StreamingPlatform.TWITCH)
        return set
    }

    fun flushCookies() {
        CookieManager.getInstance().flush()
    }
}
