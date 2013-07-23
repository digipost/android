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

package no.digipost.android.documentstore;

import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;
import no.digipost.android.model.Receipt;

public class DocumentContentStore {
	public static byte[] documentContent = null;
	public static Attachment documentMeta = null;
	public static Letter documentParent = null;

	public static Receipt documentReceipt = null;

	public static void setContent(byte[] content, Attachment meta, Letter parent) {
		documentContent = content;
		documentMeta = meta;
		documentParent = parent;
	}

	public static void setContent(Receipt receipt) {
		documentReceipt = receipt;
	}

	public static void clearContent() {
		documentContent = null;
		documentMeta = null;
		documentParent = null;
		documentReceipt = null;
	}

}
