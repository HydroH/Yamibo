package com.hydroh.yamibo.util;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.login.LoginException;

import static android.content.ContentValues.TAG;


public class HttpUtil {
    private static final String UA_DESKTOP = "Mozilla/5.0 (X11; Linux x86_64; rv:32.0) Gecko/    20100101 Firefox/32.0";
    private static final String UA_MOBILE = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Mobile Safari/537.36";

    public static void getHtmlDocument(final String url, final boolean isMobile, final HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn;
                    String ua = isMobile ? UA_MOBILE : UA_DESKTOP;
                    if (CookieUtil.getInstance().isCookieSet()){
                        Log.d(TAG, "run: Cookies loaded: " + CookieUtil.getInstance().getCookie());
                        conn = Jsoup.connect(url).header("User-Agent", ua)
                                .cookies(CookieUtil.getInstance().getCookie());
                    } else {
                        conn = Jsoup.connect(url).header("User-Agent", ua);
                    }
                    if (listener != null) {
                        listener.onFinish(new DocumentParser(conn.get()));
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }

    public static void forumLogin(final String username, final String password, final RequestCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection.Response res;
                    if (CookieUtil.getInstance().isCookieSet()){
                        res = Jsoup.connect("https://bbs.yamibo.com/member.php")
                                .data("mod", "logging", "action", "login", "infloat", "yes", "handelkey", "login", "inajax", "1", "ajaxtarget", "fwin_content_login")
                                .method(Connection.Method.GET)
                                .timeout(8000)
                                .cookies(CookieUtil.getInstance().getCookie())
                                .execute();
                    } else {
                        res = Jsoup.connect("https://bbs.yamibo.com/member.php")
                                .data("mod", "logging", "action", "login", "infloat", "yes", "handelkey", "login", "inajax", "1", "ajaxtarget", "fwin_content_login")
                                .method(Connection.Method.GET)
                                .timeout(8000)
                                .execute();
                    }


                    for (Map.Entry<String, String> entry : res.cookies().entrySet()) {
                        Log.d(TAG, "run: Cookies: " + entry.getKey() + ": " + entry.getValue());
                    }

                    Document doc = res.parse();
                    String rawHtml = doc.html();
                    int index = rawHtml.indexOf("name=\"formhash\"");
                    String formHash = rawHtml.substring(index + 23, index + 31);
                    index = rawHtml.indexOf("loginform_");
                    String loginHash = rawHtml.substring(index + 10, index + 15);
                    Log.d(TAG, "run: " + formHash + " " + loginHash);

                    res = Jsoup.connect("https://bbs.yamibo.com/member.php?mod=logging&action=login&loginsubmit=yes&handlekey=login&loginhash=" + loginHash + "&inajax=1")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .postDataCharset("GBK")
                            .data("answer", "", "cookietime", "2592000", "formhash", formHash, "loginfield", "username", "password", password, "questionid", "0", "referer", "https://bbs.yamibo.com/forum.php", "username", username)
                            .cookies(res.cookies())
                            .method(Connection.Method.POST)
                            .timeout(8000)
                            .execute();

                    Log.d(TAG, "run: " + res.parse().outerHtml());
                    if (!res.parse().outerHtml().contains("欢迎")){
                        throw new LoginException("Wrong Password");
                    }

                    if (listener != null) {
                        listener.onFinish(res.cookies());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                }
            }
        }).start();
    }
}
