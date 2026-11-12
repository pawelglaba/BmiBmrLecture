package edu.mad.bmibmr

import android.app.Activity
import android.content.Intent
import android.view.View
import android.graphics.Typeface
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.NumberFormat

class ResultActivity : Activity() {

    private lateinit var activitySlider: SeekBar
    private lateinit var preview: BodyData
    private lateinit var editor: View
    private lateinit var form: MeasurementForm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        activitySlider = findViewById(R.id.activitySlider)
        editor = findViewById(R.id.editor)
        form = MeasurementForm(this)

        // Step 5. On first launch, read the form sent by MainActivity.
        // After rotation, use the measurements behind the last calculated result.
        val data = if (savedInstanceState == null) {
            readBodyData(intent.extras)
        } else {
            readBodyData(savedInstanceState.getBundle("preview"))
        }
        if (data == null) {
            closeWithError(R.string.error_extras)
            return
        }
        if (!showResults(data)) {
            finish()
            return
        }
        activitySlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar, level: Int, fromUser: Boolean) {
                updateDailyEstimate(level)
            }
            override fun onStartTrackingTouch(bar: SeekBar) {}
            override fun onStopTrackingTouch(bar: SeekBar) {}
        })
        setEditing(savedInstanceState?.getBoolean("editing") == true)

        findViewById<Button>(R.id.editButton).setOnClickListener {
            form.show(preview)
            setEditing(true)
        }

        // Step 3. Change 70 to 80 and recalculate. Only this screen gets a new preview.
        findViewById<Button>(R.id.recalculateButton).setOnClickListener {
            val draft = form.readAndValidateInput()
            if (draft != null) showResults(draft)
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            // Step 4. Read the fields again: the user may have typed since Recalculate.
            val draft = form.readAndValidateInput()
            if (draft != null && showResults(draft)) {
                val reply = Intent()
                reply.putExtras(measurementBundle(draft))
                setResult(RESULT_OK, reply)
                finish()
            }
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setEditing(editing: Boolean) {
        editor.visibility = if (editing) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.editButton).visibility =
            if (editing) View.GONE else View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Step 6. Recalculate at 80 kg, type 82, then rotate before pressing Save.
        // Android restores the draft fields by ID. We save the displayed result and edit mode.
        if (::preview.isInitialized) outState.putBundle("preview", measurementBundle(preview))
        outState.putBoolean("editing", editor.visibility == View.VISIBLE)
    }

    private fun showResults(data: BodyData): Boolean {
        val bmi = calculateBmi(data)
        val energy = calculateBmr(data)
        if (!bmi.isFinite() || bmi <= 0.0 || !energy.isFinite()) {
            Toast.makeText(this, R.string.error_calculation, Toast.LENGTH_LONG).show()
            return false
        }

        // Classify the original number. A displayed 25.0 may come from 24.99.
        val category = bmiCategory(bmi)
        findViewById<TextView>(R.id.bmiText).text = getString(R.string.bmi_value, bmi)
        findViewById<TextView>(R.id.energyText).text = getString(R.string.energy_value, energy)
        showCategory(category)
        showMeasurements(data)

        preview = data
        updateDailyEstimate(activitySlider.progress)
        return true
    }

    private fun updateDailyEstimate(level: Int) {
        // Use the last calculated measurements. Typing a draft does not change the preview.
        val factor = 1.40 + 0.10 * level
        val dailyEnergy = calculateBmr(preview) * factor
        findViewById<TextView>(R.id.levelText).text = getString(R.string.level_format, level, factor)
        findViewById<TextView>(R.id.dailyText).text = getString(R.string.energy_value, dailyEnergy)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // The slider has an ID. Android has now restored its position.
        // Render the label and total from that position and the restored preview.
        if (::preview.isInitialized) updateDailyEstimate(activitySlider.progress)
    }

    private fun showMeasurements(data: BodyData) {
        val number = NumberFormat.getNumberInstance().apply {
            isGroupingUsed = false
            maximumFractionDigits = 340
        }
        val variant = when (data.sex) {
            Sex.FEMALE -> R.string.female
            Sex.MALE -> R.string.male
        }
        findViewById<TextView>(R.id.measurementsText).text = getString(
            R.string.measurements_format,
            number.format(data.weightKg),
            number.format(data.heightCm),
            data.ageYears,
            getString(variant)
        )
    }

    private fun showCategory(category: BmiCategory) {
        val (labelId, rowId) = when (category) {
            BmiCategory.UNDERWEIGHT -> R.string.category_underweight to R.id.underweightRange
            BmiCategory.HEALTHY_WEIGHT -> R.string.category_healthy to R.id.healthyRange
            BmiCategory.OVERWEIGHT -> R.string.category_overweight to R.id.overweightRange
            BmiCategory.OBESITY -> R.string.category_obesity to R.id.obesityRange
        }
        findViewById<TextView>(R.id.categoryText).setText(labelId)

        // Recalculate can move the tick to another row. Reset all four labels first.
        val rows = listOf(
            R.id.underweightRange to R.string.range_underweight,
            R.id.healthyRange to R.string.range_healthy,
            R.id.overweightRange to R.string.range_overweight,
            R.id.obesityRange to R.string.range_obesity
        )
        for ((id, text) in rows) {
            val range = findViewById<TextView>(id)
            range.setText(text)
            range.contentDescription = null
            range.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            range.setTextColor(getColor(R.color.ink))
            range.setTypeface(Typeface.DEFAULT)
        }
        // Mark the active range with a tick as well as colour. Both end ranges are open.
        val row = findViewById<TextView>(rowId)
        val label = row.text
        row.text = getString(R.string.selected_range, label)
        row.contentDescription = getString(R.string.selected_range_spoken, label)
        row.setTypeface(row.typeface, Typeface.BOLD)
        row.setTextColor(getColor(R.color.burgundy))
        row.setBackgroundResource(R.drawable.selected_range)
    }

    private fun closeWithError(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
        finish()
    }
}
