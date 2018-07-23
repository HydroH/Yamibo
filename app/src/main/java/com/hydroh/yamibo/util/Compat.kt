package com.hydroh.yamibo.util

import android.os.Build
import android.text.Html

class HtmlCompat {
    companion object {
        @JvmStatic
        @Suppress("DEPRECATION")
        fun fromHtml(html: String) =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    Html.fromHtml(html)
                }
    }
}



