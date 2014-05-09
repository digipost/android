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

public class ApplicationConstants {
    public static final int ACTION_DELETE = 0;
    public static final int ACTION_MOVE = 1;

	public static final String HMACSHA256 = "HmacSHA256";

	public static final String SCREENLOCK_CHOICE = "SCREENLOCK_CHOICE";
	public static final int SCREENLOCK_CHOICE_HAS_NO_BEEN_TAKEN_YET = 0;
	public static final int SCREENLOCK_CHOICE_NO = 1;
	public static final int SCREENLOCK_CHOICE_YES = 2;

    public static final boolean FEATURE_SEND_TO_BANK_VISIBLE = true;

	public static final String ENCODING = "UTF-8";
	public static final String MIME = "text/html";
    public static final String NUMBER_OF_TIMES_APP_HAS_RUN = "numberOfTimesAppHasRun";
    public static final int NUMBER_OF_TIMES_DRAWER_SHOULD_OPEN = 1;

    /*
    //Om det gjøres endringer i drawer så må koden hvor man velger favoritt startside endres.

    //Uten overskrifter (INNBOKS, MAPPER) og fullt navn. Bruk den utkommenterte linjen med setSubtitle i MainContentActivity

    public static final int NAME = 0;
	public static final int MAILBOX = 2;
	public static final int RECEIPTS = 3;
	public static final int WORKAREA = 5;
	public static final int ARCHIVE = 6;

	public static String[] titles = { "","INNBOKS", "Postkassen", "Kvitteringer", "MAPPER", "Kjøkkenbenken", "Arkivet" };

	 */

    public static final int MAILBOX = 1;
    public static final int RECEIPTS = 2;


    /*

    //Uten overskrifter (INNBOKS, MAPPER)

    public static final int MAILBOX = 0;
    public static final int RECEIPTS = 1;
    public static final int WORKAREA = 2;
    public static final int ARCHIVE = 3;

    public static String[] titles = { "Postkassen", "Kvitteringer", "Kjøkkenbenken", "Arkivet" };
    */
}
