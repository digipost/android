package no.digipost.android.utilities

/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import no.digipost.android.model.ValidationRules

object ValidationUtillities {


    @JvmStatic
    fun phoneNumberAppearsValid(validationRules: ValidationRules, phoneNumber: String): Boolean {
        val trimmedPhoneNumber = phoneNumber.trim()
        return trimmedPhoneNumber.matches(validationRules.phoneNumber.toRegex())
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
}