/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.model;

public enum Origin {
    CORPORATION,
    PRIVATE_PERSON,
    PUBLIC_ENTITY,
    UPLOADED;

    public static Origin parse(String origin) {
        try {
            return Origin.valueOf(origin);
        } catch (IllegalArgumentException e) {
            // Fall back to CORPORATION if Digipost has added a new origin-type that this app doesn't understand
            return CORPORATION;
        }
    }
}
