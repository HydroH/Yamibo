package com.hydroh.yamibo.network

object UrlUtils {
    private const val APP_UPDATE_URL = "http://hydroh.me/yamibo/version.json"

    private const val BASE_URL = "https://bbs.yamibo.com/"
    private const val DEFAULT_URL = "${BASE_URL}forum.php"

    private const val LOGIN_FORM_URL = "${BASE_URL}member.php"
    private const val LOGIN_REQUEST_URL = "$LOGIN_FORM_URL?mod=logging&action=login&loginsubmit=yes&handlekey=login&loginhash=%s&inajax=1"

    @JvmStatic
    fun getFullUrl(url: String) =
            if (url.startsWith("http")) url
            else BASE_URL + url.removePrefix("/")

    @JvmStatic
    fun getDefaultUrl() = DEFAULT_URL

    @JvmStatic
    fun isDefaultUrl(url: String) = (DEFAULT_URL == url || DEFAULT_URL == getFullUrl(url))

    @JvmStatic
    fun getAppUpdateUrl() = APP_UPDATE_URL

    @JvmStatic
    fun getLoginFormUrl() = LOGIN_FORM_URL

    @JvmStatic
    fun getLoginRequestUrl(loginHash: String) = LOGIN_REQUEST_URL.format(loginHash)
}