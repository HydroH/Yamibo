package com.hydroh.yamibo.util


import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.util.*

class CookieUtil private constructor() {
    var cookie: Map<String, String>? = null
        private set
    var isCookieSet: Boolean? = null
        private set

    init {
        Log.d(TAG, "CookieUtil: Initiated.")
        isCookieSet = false
    }


    fun setCookiePreference(context: Context, cookie: Map<String, String>) {
        val preference = context.getSharedPreferences("cookie", Context.MODE_PRIVATE)
        val editor = preference.edit()
        val gson = Gson()
        editor.putString("cookie", gson.toJson(cookie))
        editor.apply()
        this.cookie = cookie
        Log.d(TAG, "setCookiePreference: $cookie")
    }

    fun getCookiePreference(context: Context): Map<String, String>? {
        val preference = context.getSharedPreferences("cookie", Context.MODE_PRIVATE)
        val gson = Gson()
        //cookie = gson.fromJson(preference.getString("cookie", ""),
        //        new TypeToken<Map<String, String>>() {}.getType());
        val cookieObj = HashMap<String, String>()
        cookieObj["EeqY_2132_saltkey"] = "JZ00iabf"
        cookieObj["EeqY_2132_lastvisit"] = "1524034182"
        cookieObj["EeqY_2132_nofavfid"] = "1"
        cookieObj["EeqY_2132_lastact"] = "1524040861%09forum.php%09"
        cookieObj["EeqY_2132_ulastactivity"] = "8d35b11%2BRa14bJNJHCJ3bD4t%2BzAuH1DAGPXJFOwV80uq4yDnN%2B9d"
        cookieObj["EeqY_2132_sid"] = "K2y0vE"
        cookieObj["EeqY_2132_auth"] = "6ee8si48ZjCAK21I1rYJiOLLeVGCgwX1%2BLJa7v9DrsYSmnxoQb0ypvhxgKC15IfKWr9yciitRRcigOU9e%2BpBC5bpIxs"
        cookieObj["EeqY_2132_lastcheckfeed"] = "325122%7C1524040836"
        cookieObj["EeqY_2132_lip"] = "101.81.14.238%2C1524040836"
        cookieObj["EeqY_2132_onlineusernum"] = "396"
        cookie = cookieObj
        isCookieSet = cookie != null
        Log.d(TAG, "getCookiePreference: " + preference.getString("cookie", "")!!)
        return cookie
    }

    companion object {
        val instance = CookieUtil()
    }
}
