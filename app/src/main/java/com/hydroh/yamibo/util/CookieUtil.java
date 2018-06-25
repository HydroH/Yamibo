package com.hydroh.yamibo.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.content.ContentValues.TAG;

public class CookieUtil {
    private static final CookieUtil ourInstance = new CookieUtil();
    private Map<String, String> cookie;
    private Boolean cookieSet;

    public static CookieUtil getInstance() {
        return ourInstance;
    }

    private CookieUtil() {
        Log.d(TAG, "CookieUtil: Initiated.");
        cookieSet = false;
    }


    public void setCookiePreference(Context context, Map<String, String> cookie) {
        SharedPreferences preference = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        Gson gson = new Gson();
        editor.putString("cookie", gson.toJson(cookie));
        editor.apply();
        this.cookie = cookie;
        Log.d(TAG, "setCookiePreference: " + cookie);
    }

    public Map<String, String> getCookiePreference(Context context) {
        SharedPreferences preference = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        //cookie = gson.fromJson(preference.getString("cookie", ""),
        //        new TypeToken<Map<String, String>>() {}.getType());
        Map<String, String> cookie_obj = new HashMap<String, String >();
        cookie_obj.put("EeqY_2132_saltkey", "JZ00iabf");
        cookie_obj.put("EeqY_2132_lastvisit", "1524034182");
        cookie_obj.put("EeqY_2132_nofavfid", "1");
        cookie_obj.put("EeqY_2132_lastact", "1524040861%09forum.php%09");
        cookie_obj.put("EeqY_2132_ulastactivity", "8d35b11%2BRa14bJNJHCJ3bD4t%2BzAuH1DAGPXJFOwV80uq4yDnN%2B9d");
        cookie_obj.put("EeqY_2132_sid", "K2y0vE");
        cookie_obj.put("EeqY_2132_auth", "6ee8si48ZjCAK21I1rYJiOLLeVGCgwX1%2BLJa7v9DrsYSmnxoQb0ypvhxgKC15IfKWr9yciitRRcigOU9e%2BpBC5bpIxs");
        cookie_obj.put("EeqY_2132_lastcheckfeed", "325122%7C1524040836");
        cookie_obj.put("EeqY_2132_lip", "101.81.14.238%2C1524040836");
        cookie_obj.put("EeqY_2132_onlineusernum", "396");
        cookie = cookie_obj;
        cookieSet = !(cookie == null);
        Log.d(TAG, "getCookiePreference: " + preference.getString("cookie", ""));
        return cookie;
    }

    public Map<String, String> getCookie() {
        return cookie;
    }

    public Boolean isCookieSet() {
        return cookieSet;
    }
}
