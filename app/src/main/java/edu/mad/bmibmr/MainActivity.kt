package edu.mad.bmibmr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private lateinit var form: MeasurementForm

    // Step 2. Register once for each activity instance.
    // Android calls this block later, when the result screen closes.
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val edited = readBodyData(result.data?.extras)
            if (edited != null) {
                form.show(edited)
            }
        }
        // Demo: Cancel or press Back after editing. The original form still shows 70 kg.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        form = MeasurementForm(this)

        findViewById<Button>(R.id.calculateButton).setOnClickListener {
            val data = form.readAndValidateInput()
            if (data != null) {
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtras(measurementBundle(data))
                // Step 1. In the lab we called startActivity(intent) here.
                // Launch through this object so Android knows where to deliver the reply.
                resultLauncher.launch(intent)
            }
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener { form.clear() }
    }
}
