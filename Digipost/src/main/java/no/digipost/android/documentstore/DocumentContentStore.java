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

import java.util.ArrayList;

import no.digipost.android.model.Attachment;
import no.digipost.android.model.Document;
import no.digipost.android.model.Folder;
import no.digipost.android.model.Receipt;

public class DocumentContentStore {
	private static byte[] documentContent = null;
	private static Document documentParent = null;
	private static int documentAttachmentPosition = 0;
	private static Receipt documentReceipt = null;
    private static ArrayList<Folder> moveFolders;

	public static void setContent(byte[] content, Document parent, int attachmentPosition) {
		documentContent = content;
		documentParent = parent;
		documentAttachmentPosition = attachmentPosition;
	}

    public static void setContent(Receipt receipt) {
        documentReceipt = receipt;
    }

    public static byte[] getDocumentContent() {
        return documentContent;
    }

    public static ArrayList<Folder> getMoveFolders(){
        return moveFolders;
    }

    public static void setMoveFolders(ArrayList<Folder> folders){
        moveFolders = folders;
    }

    public static Document getDocumentParent() {
        return documentParent;
    }

    public static void setDocumentParent(Document parent) {
        documentParent = parent;
    }

    public static Attachment getDocumentAttachment() {
        return documentParent.getAttachment().get(documentAttachmentPosition);
    }

    public static Receipt getDocumentReceipt() {
        return documentReceipt;
    }

	public static void clearContent() {
		documentContent = null;
		documentParent = null;
		documentAttachmentPosition = 0;
		documentReceipt = null;
	}

}
