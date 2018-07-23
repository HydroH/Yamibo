package com.hydroh.yamibo.util

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hydroh.yamibo.common.Constants
import com.hydroh.yamibo.model.History
import com.hydroh.yamibo.model.Post

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

    fun updatePostHistory(context: Context, post: Post) {
        val history = getPostHistory(context)
        history.update(post)
        val preference = context.getSharedPreferences(Constants.ARG_PREF_HISTORY, Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putString(Constants.ARG_PREF_HISTORY_POST, Gson().toJson(history))
        editor.apply()
    }

    fun getPostHistory(context: Context): History<Post> {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_HISTORY, Context.MODE_PRIVATE)
        val history: History<Post> =
                Gson().fromJson(preference.getString(Constants.ARG_PREF_HISTORY_POST, ""), object : TypeToken<History<Post>>() {}.type)
                        ?: History(50)
        Log.d(TAG, "getPostHistory: $history")
        return history
    }
}
