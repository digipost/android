package no.digipost.android.documentstore;

import no.digipost.android.model.Attachment;

public class DocumentContentStore {
    public static byte[] documentContent = null;
    public static Attachment documentMeta = null;

    public static void setContent(byte[] content, Attachment meta) {
        documentContent = content;
        documentMeta = meta;
    }

    public static void clearContent() {
        documentContent = null;
        documentMeta = null;
    }
}
