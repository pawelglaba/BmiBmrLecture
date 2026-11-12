package edu.mad.bmibmr

import android.os.Bundle

const val EXTRA_WEIGHT_KG = "edu.mad.bmibmr.WEIGHT_KG"
const val EXTRA_HEIGHT_CM = "edu.mad.bmibmr.HEIGHT_CM"
const val EXTRA_AGE_YEARS = "edu.mad.bmibmr.AGE_YEARS"
const val EXTRA_SEX = "edu.mad.bmibmr.SEX"

// A Bundle carries the same four named values on the outward and return trips.
fun measurementBundle(data: BodyData): Bundle {
    val values = Bundle()
    values.putDouble(EXTRA_WEIGHT_KG, data.weightKg)
    values.putDouble(EXTRA_HEIGHT_CM, data.heightCm)
    values.putInt(EXTRA_AGE_YEARS, data.ageYears)
    values.putString(EXTRA_SEX, data.sex.name)
    return values
}

fun readBodyData(values: Bundle?): BodyData? {
    if (values == null) return null
    // Invalid defaults let us detect a missing extra instead of inventing a measurement.
    val weight = values.getDouble(EXTRA_WEIGHT_KG, Double.NaN)
    val height = values.getDouble(EXTRA_HEIGHT_CM, Double.NaN)
    val age = values.getInt(EXTRA_AGE_YEARS, -1)
    val variantName = values.getString(EXTRA_SEX)
    val sex = Sex.entries.firstOrNull { it.name == variantName } ?: return null

    if (!weight.isFinite() || weight <= 0.0) return null
    if (!height.isFinite() || height <= 0.0) return null
    if (age !in allowedAges) return null
    return BodyData(weight, height, age, sex)
}

