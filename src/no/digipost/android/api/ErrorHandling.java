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

package no.digipost.android.api;

public class ErrorHandling {
	public static final int ERROR_SERVER = 0;
	public static final int ERROR_CLIENT = 1;
	public static final int ERROR_DEVICE = 3;
	public static final int ERROR_GENERAL = 4;
	public static final int ERROR_OK = 5;

	public static String LogError(final int error_type, final String error_message) {

		switch (error_type) {
		case ERROR_SERVER:
			return "En feil oppstod under oppkoblingen mot Digipost.";
		case ERROR_CLIENT:
			return "";
		case ERROR_DEVICE:
			return "";
		case ERROR_GENERAL:
			return "";
		default:
			return null;
		}
	}
}
