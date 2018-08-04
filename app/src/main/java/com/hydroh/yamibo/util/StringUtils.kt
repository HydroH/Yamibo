package com.hydroh.yamibo.util

import com.hydroh.yamibo.network.UrlUtils
import java.util.regex.Pattern

fun String.removeScripts(): String {
    val p = Pattern.compile("<script[^>]*>(.*?)</script>",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE)
    return p.matcher(this).replaceAll("")
}

fun String.toIntOrNullIgnore(): Int? = "\\d+".toRegex().find(this)?.value?.toIntOrNull()

fun String.toAvatarSize(size: String): String = replace(UrlUtils.AvatarSize.SMALL, size)
            .replace(UrlUtils.AvatarSize.MIDDLE, size)
            .replace(UrlUtils.AvatarSize.BIG, size)

fun String.getUid(): String = "space-uid-\\d+\\.html".toRegex().find(this)?.value ?: ""