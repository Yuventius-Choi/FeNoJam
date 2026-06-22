package com.keygul.FeNoJam.utils.exts

import android.content.Context
import java.io.IOException

@Throws(IOException::class)
fun Context.getFestAsset(filename: String): String {
    return this.assets
        .open(filename)
        .bufferedReader()
        .use { it.readText() }
}