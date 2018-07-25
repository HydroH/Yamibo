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
        context.getSharedPreferences(Constants.ARG_PREF_APP, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.ARG_PREF_APP_FIRST_LAUNCH, isFirstLaunch)
                .apply()
    }

    fun getFirstLaunch(context: Context): Boolean {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_APP, Context.MODE_PRIVATE)
        return preference.getBoolean(Constants.ARG_PREF_APP_FIRST_LAUNCH, true)
    }

    fun setCookiePreference(context: Context, cookie: MutableMap<String, String>) {
        context.getSharedPreferences(Constants.ARG_PREF_COOKIE, Context.MODE_PRIVATE)
                .edit()
                .putString(Constants.ARG_PREF_COOKIE_COOKIE, Gson().toJson(cookie))
                .apply()
    }

    fun getCookiePreference(context: Context): MutableMap<String, String>? {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_COOKIE, Context.MODE_PRIVATE)
        return Gson().fromJson(preference.getString(Constants.ARG_PREF_COOKIE_COOKIE, ""),
                object : TypeToken<MutableMap<String, String>>() {}.type)
                ?: LinkedHashMap()
    }

    fun updatePostHistory(context: Context, post: Post) {
        val history = getPostHistory(context)
        history.update(post)
        Log.d(TAG, "updatePostHistory: ${Gson().toJson(history)}")
        context.getSharedPreferences(Constants.ARG_PREF_HISTORY, Context.MODE_PRIVATE)
                .edit()
                .putString(Constants.ARG_PREF_HISTORY_POST, Gson().toJson(history))
                .apply()
    }

    fun getPostHistory(context: Context): History<Post> {
        val preference = context.getSharedPreferences(Constants.ARG_PREF_HISTORY, Context.MODE_PRIVATE)
        return Gson().fromJson(preference.getString(Constants.ARG_PREF_HISTORY_POST, ""),
                object : TypeToken<History<Post>>() {}.type)
                ?: History(50)
    }
}
