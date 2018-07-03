package com.hydroh.yamibo.network

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.google.gson.internal.LinkedTreeMap
import com.hydroh.yamibo.network.callback.CookieCallbackListener
import com.hydroh.yamibo.network.callback.DocumentCallbackListener
import com.hydroh.yamibo.util.PrefUtils
import com.hydroh.yamibo.util.DocumentParser
import com.hydroh.yamibo.util.removeScripts
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.util.*
import javax.security.auth.login.LoginException


object WebRequest {
    const val BASE_URL = "https://bbs.yamibo.com/"

    private const val UA_DESKTOP = "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0"
    private const val UA_MOBILE = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Mobile Safari/537.36"

    private const val LOGIN_FORM_URL = "${BASE_URL}member.php"
    private const val LOGIN_REQUEST_URL = "$LOGIN_FORM_URL?mod=logging&action=login&loginsubmit=yes&handlekey=login&loginhash=%s&inajax=1"

    fun getHtmlDocument(url: String, isMobile: Boolean, context: Context, listener: DocumentCallbackListener?) {
        Thread(Runnable {
            try {
                var fullUrl = url
                if (!url.startsWith("http")) {
                    fullUrl = BASE_URL + url
                }
                val ua = if (isMobile) UA_MOBILE else UA_DESKTOP
                val conn = Jsoup.connect(fullUrl).header("User-Agent", ua)
                val cookies = PrefUtils.getCookiePreference(context)
                cookies?.let {
                    Log.d(TAG, "run: Cookies loaded: $cookies")
                    conn.cookies(cookies)
                }
                listener?.onFinish(DocumentParser(conn.get(), isMobile))
            } catch (e: Exception) {
                listener?.onError(e)
            }
        }).start()
    }

    fun getLogonCookies(username: String, password: String, context: Context, listener: CookieCallbackListener?) {
        Thread(Runnable {
            try {
                val cookies = /*PrefUtils.getCookiePreference(context) ?:*/ LinkedTreeMap<String, String>()
                var response = Jsoup.connect(LOGIN_FORM_URL)
                        .method(Connection.Method.GET)
                        .data(
                                "mod", "logging",
                                "action", "login",
                                "infloat", "yes",
                                "handelkey", "login",
                                "inajax", "1",
                                "ajaxtarget", "fwin_content_login")
                        .cookies(cookies)
                        .timeout(8000)
                        .execute()
                cookies.putAll(response.cookies())
                Log.d(TAG, "run: Cookies: Before login: $cookies")

                val doc = response.parse()
                val rawHtml = doc.html()
                var index = rawHtml.indexOf("name=\"formhash\"")
                val formHash = rawHtml.substring(index + 23, index + 31)
                index = rawHtml.indexOf("loginform_")
                val loginHash = rawHtml.substring(index + 10, index + 15)
                Log.d(TAG, "run: $formHash $loginHash")

                response = Jsoup.connect(LOGIN_REQUEST_URL.format(loginHash))
                        .method(Connection.Method.POST)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .postDataCharset("GBK")
                        .data(
                                "answer", "",
                                "cookietime", "2592000",
                                "formhash", formHash,
                                "loginfield", "username",
                                "username", username,
                                "password", password,
                                "questionid", "0",
                                "referer", "https://bbs.yamibo.com/forum.php")
                        .cookies(cookies)
                        .timeout(8000)
                        .execute()

                Log.d(TAG, "run: " + response.parse().outerHtml())
                cookies.putAll(response.cookies())
                cookies.values.removeAll(Collections.singleton("deleted"))

                if (!response.parse().outerHtml().contains("欢迎")) {
                    val docRes = response.parse()
                    val message = docRes.select("p").first()?.text() ?: docRes.text().removeScripts()
                    throw LoginException(message)
                }
                listener?.onFinish(cookies)
            } catch (e: Exception) {
                listener?.onError(e)
            }
        }).start()
    }
}
