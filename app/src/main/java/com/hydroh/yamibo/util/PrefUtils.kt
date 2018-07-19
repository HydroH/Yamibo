package com.hydroh.yamibo.util

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hydroh.yamibo.common.Constants

object PrefUtils {
    fun setFirstLaunch(context: Context, isFirstLaunch: Boolean) {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_APP, Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean(Constants.ARG_PREF_APP_FIRST_LAUNCH, isFirstLaunch)
        editor.apply()
    }

    fun getFirstLaunch(context: Context): Boolean {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_APP, Context.MODE_PRIVATE)
        return preference.getBoolean(Constants.ARG_PREF_APP_FIRST_LAUNCH, true)
    }

    fun setCookiePreference(context: Context, cookie: MutableMap<String, String>) {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_COOKIE, Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putString(Constants.ARG_PREF_COOKIE_COOKIE, Gson().toJson(cookie))
        editor.apply()
        Log.d(TAG, "setCookiePreference: $cookie")
    }

    fun getCookiePreference(context: Context): MutableMap<String, String>? {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_COOKIE, Context.MODE_PRIVATE)
        val cookie: MutableMap<String, String> =
                Gson().fromJson(preference.getString(Constants.ARG_PREF_COOKIE_COOKIE, ""), object : TypeToken<MutableMap<String, String>>() {}.type)
                        ?: LinkedHashMap()
        Log.d(TAG, "getCookiePreference: $cookie")
        return cookie
    }
}
