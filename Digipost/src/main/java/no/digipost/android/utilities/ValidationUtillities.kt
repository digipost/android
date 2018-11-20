package no.digipost.android.utilities

import android.widget.EditText
import no.digipost.android.model.ValidationRules

object ValidationUtillities {
    @JvmStatic
    fun emailAppersValid(validationRules: ValidationRules, email: EditText) : Boolean {
        return emailAppersValid(validationRules, email.text.toString())
    }

    @JvmStatic
    fun emailAppersValid(validationRules: ValidationRules, email: String) : Boolean {
        return email.isEmpty() || email.matches(validationRules.email.toRegex())
    }
}