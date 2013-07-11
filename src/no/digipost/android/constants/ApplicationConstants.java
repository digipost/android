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

package no.digipost.android.constants;
import no.digipost.android.R;

public class ApplicationConstants {
    public static final String HMACSHA256 = "HmacSHA256";

    public static final String SCREENLOCK_CHOICE = "SCREENLOCK_CHOICE";
    public static final int SCREENLOCK_CHOICE_HAS_NO_BEEN_TAKEN_YET = 0;
    public static final int SCREENLOCK_CHOICE_NO = 1;
    public static final int SCREENLOCK_CHOICE_YES = 2;

    public static final int MAILBOX = 1;
    public static final int RECEIPTS = 2;
    public static final int WORKAREA = 4;
    public static final int ARCHIVE = 5;

    public static final String[] titles = {"INNBOKS","Postkassen","Kvitteringer","MAPPER","Kjøkkenbenken", "Arkivet"};

}
