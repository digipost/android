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


    @JvmStatic
    fun phoneNumberAppersValid(validationRules: ValidationRules, phoneNumber: EditText): Boolean {
        return phoneNumberAppersValid(validationRules, phoneNumber.text.toString())
    }

    @JvmStatic
    fun phoneNumberAppersValid(validationRules: ValidationRules, phoneNumber: String): Boolean {
        return phoneNumber.matches(validationRules.phoneNumber.toRegex())
    }
}