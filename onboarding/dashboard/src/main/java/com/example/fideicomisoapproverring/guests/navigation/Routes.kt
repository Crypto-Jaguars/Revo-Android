package com.example.fideicomisoapproverring.guests.navigation

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

enum class Routes(val value: String) {
    Home("home"),
    Search("search"),
    Categories("categories"),
    Settings("settings"),
    Wallet("wallet"),
    How("how_it_works"),
    FAQ("faq"),
    About("about_us"),
    ImageUpload("image_upload"),
    Activity("activity"),
}
