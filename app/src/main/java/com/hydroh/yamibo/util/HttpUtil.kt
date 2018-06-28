package com.hydroh.yamibo.util

import android.content.ContentValues.TAG
import android.util.Log
import org.jsoup.Connection
import org.jsoup.Jsoup
import javax.security.auth.login.LoginException

object HttpUtil {
    const val BASE_URL = "http://bbs.yamibo.com/"

    private const val UA_DESKTOP = "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0"
    private const val UA_MOBILE = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Mobile Safari/537.36"

    fun getHtmlDocument(url: String, isMobile: Boolean, listener: HttpCallbackListener?) {
        Thread(Runnable {
            try {
                var fullUrl = url
                if (!url.startsWith("http")) {
                    fullUrl = BASE_URL + url
                }
                val ua = if (isMobile) UA_MOBILE else UA_DESKTOP
                val conn = Jsoup.connect(fullUrl).header("User-Agent", ua)
                if (CookieUtil.instance.isCookieSet!!) {
                    Log.d(TAG, "run: Cookies loaded: ${CookieUtil.instance.cookie!!}")
                    conn.cookies(CookieUtil.instance.cookie)
                }
                listener?.onFinish(DocumentParser(conn.get(), isMobile))
            } catch (e: Exception) {
                listener?.onError(e)
            }
        }).start()
    }

    fun forumLogin(username: String, password: String, listener: RequestCallbackListener?) {
        Thread(Runnable {
            try {
                val request = Jsoup.connect("https://bbs.yamibo.com/member.php")
                        .data("mod", "logging", "action", "login", "infloat", "yes", "handelkey", "login", "inajax", "1", "ajaxtarget", "fwin_content_login")
                        .method(Connection.Method.GET)
                        .timeout(8000)
                if (CookieUtil.instance.isCookieSet!!) {
                    request.cookies(CookieUtil.instance.cookie)
                }
                var response = request.execute()

                for ((key, value) in response.cookies()) {
                    Log.d(TAG, "run: Cookies: $key: $value")
                }

                val doc = response.parse()
                val rawHtml = doc.html()
                var index = rawHtml.indexOf("name=\"formhash\"")
                val formHash = rawHtml.substring(index + 23, index + 31)
                index = rawHtml.indexOf("loginform_")
                val loginHash = rawHtml.substring(index + 10, index + 15)
                Log.d(TAG, "run: $formHash $loginHash")

                response = Jsoup.connect("https://bbs.yamibo.com/member.php?mod=logging&action=login&loginsubmit=yes&handlekey=login&loginhash=$loginHash&inajax=1")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .postDataCharset("GBK")
                        .data("answer", "", "cookietime", "2592000", "formhash", formHash, "loginfield", "username", "password", password, "questionid", "0", "referer", "https://bbs.yamibo.com/forum.php", "username", username)
                        .cookies(response.cookies())
                        .method(Connection.Method.POST)
                        .timeout(8000)
                        .execute()

                Log.d(TAG, "run: " + response.parse().outerHtml())
                if (!response.parse().outerHtml().contains("欢迎")) {
                    throw LoginException("Wrong Password")
                }

                listener?.onFinish(response.cookies())
            } catch (e: Exception) {
                listener?.onError(e)
            }
        }).start()
    }
}
