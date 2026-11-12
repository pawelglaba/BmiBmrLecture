package edu.mad.bmibmr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class CalculationsTest {

    @Test fun firstWorksheetExample() {
        checkExample(BodyData(70.0, 175.0, 25, Sex.MALE),
            "22.9", BmiCategory.HEALTHY_WEIGHT, "1674")
    }

    @Test fun secondWorksheetExample() {
        checkExample(BodyData(60.0, 165.0, 30, Sex.FEMALE),
            "22.0", BmiCategory.HEALTHY_WEIGHT, "1320")
    }

    @Test fun thirdWorksheetExample() {
        checkExample(BodyData(100.0, 180.0, 40, Sex.MALE),
            "30.9", BmiCategory.OBESITY, "1930")
    }

    private fun checkExample(data: BodyData, bmi: String, category: BmiCategory, energy: String) {
        val rawBmi = calculateBmi(data)
        assertEquals(bmi, String.format(Locale.US, "%.1f", rawBmi))
        assertEquals(category, bmiCategory(rawBmi))
        assertEquals(energy, String.format(Locale.US, "%.0f", calculateBmr(data)))
    }

    @Test fun categoryChangesAtTheExactBoundary() {
        assertEquals(BmiCategory.UNDERWEIGHT, bmiCategory(18.49))
        assertEquals(BmiCategory.HEALTHY_WEIGHT, bmiCategory(18.5))
        assertEquals(BmiCategory.HEALTHY_WEIGHT, bmiCategory(24.99))
        assertEquals(BmiCategory.OVERWEIGHT, bmiCategory(25.0))
        assertEquals(BmiCategory.OVERWEIGHT, bmiCategory(29.99))
        assertEquals(BmiCategory.OBESITY, bmiCategory(30.0))
    }

    @Test fun decimalCommaAndDecimalPointAreEquivalent() {
        assertEquals(70.5, parsePositiveDecimal(" 70,5 ")!!, 0.0)
        assertEquals(70.5, parsePositiveDecimal("70.5")!!, 0.0)
    }

    @Test fun invalidMeasurementsAreRejected() {
        for (input in listOf("", "0", "-1", "abc", "1,2,3", "NaN", "Infinity", "1e999")) {
            assertNull(input, parsePositiveDecimal(input))
        }
    }
}
