package edu.mad.bmibmr

enum class BmiCategory { UNDERWEIGHT, HEALTHY_WEIGHT, OVERWEIGHT, OBESITY }

fun calculateBmi(data: BodyData): Double {
    val heightMetres = data.heightCm / 100.0
    return data.weightKg / (heightMetres * heightMetres)
}

fun calculateBmr(data: BodyData): Double {
    // The Mifflin–St Jeor equation estimates resting energy in kcal per day.
    val offset = when (data.sex) {
        Sex.FEMALE -> -161
        Sex.MALE -> 5
    }
    return 10.0 * data.weightKg + 6.25 * data.heightCm - 5.0 * data.ageYears + offset
}

fun bmiCategory(bmi: Double): BmiCategory = when {
    bmi < 18.5 -> BmiCategory.UNDERWEIGHT
    bmi < 25.0 -> BmiCategory.HEALTHY_WEIGHT
    bmi < 30.0 -> BmiCategory.OVERWEIGHT
    else -> BmiCategory.OBESITY
}
