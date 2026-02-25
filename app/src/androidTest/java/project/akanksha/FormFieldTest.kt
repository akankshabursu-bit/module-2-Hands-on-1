package project.anjana // Changed package

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import project.anjana.model.FormField 

class FormFieldTest {

    @Test
    fun testFormFieldParsing() {
        val json = """
            {
                "fieldId": "test_field",
                "label": "Test Field",
                "fieldType": "text",
                "hint": "Enter test value",
                "isRequired": true
            }
        """.trimIndent()

        val formField = Gson().fromJson(json, FormField::class.java)

        assertEquals("test_field", formField.fieldId)
        assertEquals("Test Field", formField.label)
        assertEquals("text", formField.fieldType)
        assertEquals("Enter test value", formField.hint)
        assertTrue(formField.isRequired)
    }
}
