package project.anjana.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import project.anjana.R
import project.anjana.model.FormField

class FormAdapter(
    private val formFields: List<FormField>,
    private val listener: FormSubmitListener
) : RecyclerView.Adapter<FormAdapter.FormViewHolder>() {

    private val formData = mutableMapOf<FormField, String>()

    interface FormSubmitListener {
        fun onFormFieldChanged(field: FormField, value: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FormViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_form_field, parent, false)
        return FormViewHolder(view)
    }

    override fun onBindViewHolder(holder: FormViewHolder, position: Int) {
        holder.bind(formFields[position])
    }

    override fun getItemCount() = formFields.size

    fun getFormData(): Map<FormField, String> = formData

    inner class FormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
        private val requiredIndicator: TextView = itemView.findViewById(R.id.requiredIndicator)

        private val textInputLayout: TextInputLayout =
            itemView.findViewById(R.id.textInputLayout)
        private val inputEditText: TextInputEditText =
            itemView.findViewById(R.id.inputEditText)

        private val dropdownSpinner: Spinner =
            itemView.findViewById(R.id.dropdownSpinner)
        private val checkbox: CheckBox =
            itemView.findViewById(R.id.checkbox)

        private var textWatcher: TextWatcher? = null

        fun bind(field: FormField) {

            // ---------------- RESET EVERYTHING (MOST IMPORTANT) ----------------
            textInputLayout.visibility = View.GONE
            dropdownSpinner.visibility = View.GONE
            checkbox.visibility = View.GONE

            textInputLayout.hint = ""
            textInputLayout.error = null

            inputEditText.setText("")
            inputEditText.hint = null

            textWatcher?.let { inputEditText.removeTextChangedListener(it) }
            textWatcher = null

            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = false

            // ---------------- LABEL ----------------
            labelTextView.text = field.label
            requiredIndicator.visibility =
                if (field.isRequired) View.VISIBLE else View.GONE

            // ---------------- FIELD TYPES ----------------
            when (field.fieldType) {

                "text", "email", "number" -> {
                    textInputLayout.visibility = View.VISIBLE
                    textInputLayout.hint = field.hint ?: ""

                    inputEditText.inputType = when (field.fieldType) {
                        "email" -> android.text.InputType.TYPE_CLASS_TEXT or
                                android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        "number" -> android.text.InputType.TYPE_CLASS_NUMBER
                        else -> android.text.InputType.TYPE_CLASS_TEXT
                    }

                    field.defaultValue?.let {
                        inputEditText.setText(it)
                        formData[field] = it
                    }

                    textWatcher = object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val value = s.toString()
                            formData[field] = value
                            listener.onFormFieldChanged(field, value)
                        }
                    }

                    inputEditText.addTextChangedListener(textWatcher)
                }

                "dropdown" -> {
                    dropdownSpinner.visibility = View.VISIBLE

                    val options = field.options ?: emptyList()
                    val adapter = ArrayAdapter(
                        itemView.context,
                        android.R.layout.simple_spinner_item,
                        options
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    dropdownSpinner.adapter = adapter

                    field.defaultValue?.let { default ->
                        val index = options.indexOf(default)
                        if (index >= 0) dropdownSpinner.setSelection(index)
                        formData[field] = default
                    }

                    dropdownSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                if (position >= 0 && position < options.size) {
                                    val value = options[position]
                                    formData[field] = value
                                    listener.onFormFieldChanged(field, value)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }

                "checkbox" -> {
                    checkbox.visibility = View.VISIBLE
                    checkbox.text = field.label

                    val checked = field.defaultValue?.toBoolean() ?: false
                    checkbox.isChecked = checked
                    formData[field] = checked.toString()

                    checkbox.setOnCheckedChangeListener { _, isChecked ->
                        val value = isChecked.toString()
                        formData[field] = value
                        listener.onFormFieldChanged(field, value)
                    }
                }
            }
        }
    }
}