package com.br.klibras.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.br.klibras.R


val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val josefinSansFontName = GoogleFont("Josefin Sans")



// Fonte principal pra titulos, logo etc
val JosefinSans = FontFamily(
    Font(googleFont = josefinSansFontName, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = josefinSansFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = josefinSansFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = josefinSansFontName, fontProvider = provider, weight = FontWeight.Bold)
)



// Tipografia do app
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = JosefinSans, fontWeight = FontWeight.Bold, fontSize = 57.sp),
    headlineLarge = TextStyle(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    titleLarge = TextStyle(fontFamily = JosefinSans, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontFamily = JosefinSans, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = JosefinSans, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = JosefinSans, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)
