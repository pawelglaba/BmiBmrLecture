package edu.mad.bmibmr

import android.app.Activity
import android.text.method.TextKeyListener
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast

// Both screens use the lab form. Keep its existing input checks in one place.
class MeasurementForm(private val activity: Activity) {
    private lateinit var weightInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var sexGroup: RadioGroup

    init {
        weightInput = activity.findViewById(R.id.weightInput)
        heightInput = activity.findViewById(R.id.heightInput)
        ageInput = activity.findViewById(R.id.ageInput)
        sexGroup = activity.findViewById(R.id.sexGroup)

        // A numeric input filter may silently remove a pasted comma or minus sign.
        // Keep the keyboard chosen in XML, but let our validation read the original text.
        for (field in listOf(weightInput, heightInput, ageInput)) {
            val keyboardType = field.inputType
            field.keyListener = TextKeyListener.getInstance()
            field.setRawInputType(keyboardType)
        }
    }

    fun show(data: BodyData) {
        weightInput.setText(data.weightKg.toString())
        heightInput.setText(data.heightCm.toString())
        ageInput.setText(data.ageYears.toString())
        sexGroup.check(if (data.sex == Sex.FEMALE) R.id.femaleOption else R.id.maleOption)
    }

    fun clear() {
        weightInput.text.clear()
        heightInput.text.clear()
        ageInput.text.clear()
        sexGroup.clearCheck()
        weightInput.requestFocus()
    }

    fun readAndValidateInput(): BodyData? {
        // Each early return keeps the form open and reports just the first error.
        val weight = parsePositiveDecimal(weightInput.text.toString())
        if (weight == null) {
            weightInput.requestFocus()
            showError(R.string.error_weight)
            return null
        }

        val height = parsePositiveDecimal(heightInput.text.toString())
        if (height == null) {
            heightInput.requestFocus()
            showError(R.string.error_height)
            return null
        }

        val age = ageInput.text.toString().trim().toIntOrNull()
        if (age == null || age !in allowedAges) {
            ageInput.requestFocus()
            showError(R.string.error_age)
            return null
        }

        val sex = when (sexGroup.checkedRadioButtonId) {
            R.id.femaleOption -> Sex.FEMALE
            R.id.maleOption -> Sex.MALE
            else -> {
                showError(R.string.error_sex)
                return null
            }
        }
        return BodyData(weight, height, age, sex)
    }

    private fun showError(messageId: Int) {
        Toast.makeText(activity, messageId, Toast.LENGTH_LONG).show()
    }
}
