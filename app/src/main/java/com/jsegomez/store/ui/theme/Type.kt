package com.jsegomez.store.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jsegomez.store.R

val Roboto = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_semibold, FontWeight.SemiBold),
    Font(R.font.roboto_bold, FontWeight.Bold)
)

private fun robotoStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit
) = TextStyle(
    fontFamily = Roboto,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight
)

// Uso: Text(text = "...", style = AppTextStyles.BodyLargeMedium)
object AppTextStyles {
    // Headline - 28sp
    val HeadlineBold = robotoStyle(FontWeight.Bold, 28.sp, 36.sp)

    // Body XLarge - 18sp
    val BodyXLargeMedium = robotoStyle(FontWeight.Medium, 18.sp, 26.sp)

    // Body Large - 16sp
    val BodyLargeSemiBold = robotoStyle(FontWeight.SemiBold, 16.sp, 24.sp)
    val BodyLargeMedium = robotoStyle(FontWeight.Medium, 16.sp, 24.sp)
    val BodyLargeRegular = robotoStyle(FontWeight.Normal, 16.sp, 24.sp)

    // Body Medium - 14sp
    val BodyMediumBold = robotoStyle(FontWeight.Bold, 14.sp, 20.sp)
    val BodyMediumSemiBold = robotoStyle(FontWeight.SemiBold, 14.sp, 20.sp)
    val BodyMediumMedium = robotoStyle(FontWeight.Medium, 14.sp, 20.sp)
    val BodyMediumRegular = robotoStyle(FontWeight.Normal, 14.sp, 20.sp)

    // Body Small - 12sp
    val BodySmallBold = robotoStyle(FontWeight.Bold, 12.sp, 16.sp)
    val BodySmallSemiBold = robotoStyle(FontWeight.SemiBold, 12.sp, 16.sp)
    val BodySmallMedium = robotoStyle(FontWeight.Medium, 12.sp, 16.sp)
    val BodySmallRegular = robotoStyle(FontWeight.Normal, 12.sp, 16.sp)
}

// Los estilos por defecto de Material usan la variante Regular
val Typography = Typography(
    bodyLarge = AppTextStyles.BodyLargeRegular,
    bodyMedium = AppTextStyles.BodyMediumRegular,
    bodySmall = AppTextStyles.BodySmallRegular
)
