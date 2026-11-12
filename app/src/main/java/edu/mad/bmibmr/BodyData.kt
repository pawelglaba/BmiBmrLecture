package edu.mad.bmibmr

enum class Sex { FEMALE, MALE }

data class BodyData(
    val weightKg: Double,
    val heightCm: Double,
    val ageYears: Int,
    val sex: Sex
)

// The lab uses adult BMI categories and sets an upper age limit for the form.
val allowedAges = 20..120

fun parsePositiveDecimal(text: String): Double? {
    val value = text.trim().replace(',', '.').toDoubleOrNull()
    return value?.takeIf { it.isFinite() && it > 0.0 }
}
