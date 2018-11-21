package no.digipost.android.utilities

import android.widget.EditText
import no.digipost.android.model.ValidationRules

object ValidationUtillities {
    @JvmStatic
    fun emailAppearsValid(validationRules: ValidationRules, email: EditText): Boolean {
        return emailAppearsValid(validationRules, email.text.toString())
    }

    @JvmStatic
    fun emailAppearsValid(validationRules: ValidationRules, email: String): Boolean {
        val trimmedEmail = email.trim()
        return trimmedEmail.isEmpty() ||
                (trimmedEmail.matches(validationRules.email.toRegex()) && emailNotBlacklisted(email))
    }

    private fun emailNotBlacklisted(email: String): Boolean {
        val splitEmail = email.split("@")

        if (splitEmail.size > 1) {
            val domain = splitEmail[1];

            val blacklistedDomains = arrayOf("digipost.no", "digipost.com", "example.com", "gmai.com", "gmail.co")
            blacklistedDomains.forEach {
                if (domain.toLowerCase() == it) {
                    return false
                }
            }
        }
        return true
    }


    @JvmStatic
    fun phoneNumberAppearsValid(validationRules: ValidationRules, phoneNumber: EditText): Boolean {
        return phoneNumberAppearsValid(validationRules, phoneNumber.text.toString())
    }

    @JvmStatic
    fun phoneNumberAppearsValid(validationRules: ValidationRules, phoneNumber: String): Boolean {
        val trimmedPhoneNumber = phoneNumber.trim()
        return trimmedPhoneNumber.matches(validationRules.phoneNumber.toRegex())
    }
}