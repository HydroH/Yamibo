package com.hydroh.yamibo.util

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CookieUtil {

    fun setCookiePreference(context: Context, cookie: MutableMap<String, String>) {
        val preference = context.getSharedPreferences("cookie", Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putString("cookie", Gson().toJson(cookie))
        editor.apply()
        Log.d(TAG, "setCookiePreference: $cookie")
    }

    fun getCookiePreference(context: Context): MutableMap<String, String>? {
        val preference = context.getSharedPreferences("cookie", Context.MODE_PRIVATE)
        val cookie: MutableMap<String, String> =
                Gson().fromJson(preference.getString("cookie", ""), object : TypeToken<MutableMap<String, String>>() {}.type)
        Log.d(TAG, "getCookiePreference: $cookie")
        return cookie
    }
}
