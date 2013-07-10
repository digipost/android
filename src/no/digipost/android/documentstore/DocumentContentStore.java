package no.digipost.android.documentstore;

import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;

public class DocumentContentStore {
    public static byte[] documentContent = null;
    public static Attachment documentMeta = null;
    public static Letter documentParent = null;

    public static void setContent(byte[] content, Attachment meta, Letter parent) {
        documentContent = content;
        documentMeta = meta;
        documentParent = parent;
    }

    public static void clearContent() {
        documentContent = null;
        documentMeta = null;
        documentParent = null;
    }
}
