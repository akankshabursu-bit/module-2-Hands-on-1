package project.anjana

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import project.anjana.adapter.FormAdapter
import project.anjana.model.FormField

class MainActivity : AppCompatActivity(), FormAdapter.FormSubmitListener {

    private lateinit var formAdapter: FormAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var submitButton: Button
    private lateinit var resultTextView: TextView
    private val formFields = mutableListOf<FormField>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.formRecyclerView)
        submitButton = findViewById(R.id.submitButton)
        resultTextView = findViewById(R.id.resultTextView)

        setupRecyclerView()
        loadFormConfiguration()
        setupSubmitButton()
    }

    private fun setupRecyclerView() {
        formAdapter = FormAdapter(formFields, this)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = formAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadFormConfiguration() {
        try {
            val jsonString = assets.open("form_config.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<FormField>>() {}.type
            val fields = Gson().fromJson<List<FormField>>(jsonString, type)

            formFields.clear()
            formFields.addAll(fields)
            formAdapter.notifyDataSetChanged()

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading form configuration: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            validateAndSubmitForm()
        }
    }

    private fun validateAndSubmitForm() {
        val formData = formAdapter.getFormData()

        // Validate all fields
        for ((field, value) in formData) {
            if (field.isRequired && value.isBlank()) {
                Toast.makeText(this, "${field.label} is required", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Submit form data
        val jsonData = Gson().toJson(formData.map { it.key.fieldId to it.value }.toMap())
        Toast.makeText(this, "Form submitted successfully!", Toast.LENGTH_LONG).show()

        // Display results
        resultTextView.text = "Form Data:\n$jsonData"
        resultTextView.visibility = android.view.View.VISIBLE
    }

    override fun onFormFieldChanged(field: FormField, value: String) {
        // Handle individual field changes
    }
}