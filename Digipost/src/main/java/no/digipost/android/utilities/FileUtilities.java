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

package no.digipost.android.utilities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.webkit.MimeTypeMap;
import net.danlew.android.joda.JodaTimeAndroid;
import no.digipost.android.documentstore.DocumentContentStore;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtilities {
    public static final String TEMP_FILE_NAME = "temp";
    public static final String TEMP_FILE_DIRECTORY = Environment.getExternalStorageDirectory() + "/Android/data/no.digipost.android/files/";

    public static void openFileWithIntent(Context context, File file) throws ActivityNotFoundException {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), getMimeType(file));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openFileWithIntent(final Context context, final String fileType, final byte[] data) throws ActivityNotFoundException, IOException {
        File file = writeTempFile(fileType, data);
        openFileWithIntent(context, file);
    }

    public static String getMimeType(File file) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = FilenameUtils.getExtension(file.getName());
        return mime.getMimeTypeFromExtension(ext);
    }

    public static void writeFileToDevice(final Context context) throws Exception {
        if(isExternalStorageWritable() && isStorageWriteAllowed(context)) {
            byte[] data = DocumentContentStore.getDocumentContent();
            File file =  writeData(getDownloadsStorageDir(getAttachmentFullFilename()), data);
            makeFileVisible(context, file);
        }else{
            throw new Exception();
        }
    }

    private static String getAttachmentFullFilename(){
        String fileType = DocumentContentStore.getDocumentAttachment().getFileType();
        String creatorName = DocumentContentStore.getDocumentParent().getCreatorName();
        String fileName = DocumentContentStore.getDocumentAttachment().getSubject();

        String dateCreated = "";

        try{
            String tempDate = DocumentContentStore.getDocumentParent().getCreated();
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            DateTime jodatime = dtf.parseDateTime(tempDate);
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy.MM.dd");
            dateCreated = dtfOut.print(jodatime) + " ";
        }catch (Exception e){
        }

        String fullFileName = (dateCreated + creatorName.toUpperCase() + " " + fileName + "." +fileType);
        fullFileName = fullFileName.replaceAll("[()\\?:!,;{}-]+", "").replaceAll("[\\t\\n\\s]+", "_");

        return fullFileName;
    }

    public static File writeTempFile(final String fileType, final byte[] data) throws IOException {
        File file = new File(new File(TEMP_FILE_DIRECTORY), TEMP_FILE_NAME + "." + fileType);
        file.getParentFile().mkdirs();
        file.createNewFile();

        return writeData(file, data);
    }

    public static boolean isExternalStorageWritable(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isStorageWriteAllowed(Context context){
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    public static File getDownloadsStorageDir(String fileName) throws Exception{
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        try {
            file.createNewFile();
        }catch(Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public static void deleteTempFiles() {
        File path = new File(TEMP_FILE_DIRECTORY);
        File[] cache = path.listFiles();

        if (cache == null) {
            return;
        }

        for (File f : cache) {
            if (f.getName().startsWith(TEMP_FILE_NAME)) {
                f.delete();
            }
        }
    }

    private static File writeData(File file, byte[] data) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(data);
        stream.close();
        return file;
    }

    public static void makeFileVisible(Context context, File file) {
        JodaTimeAndroid.init(context);
        Long contentLength = Long.parseLong(DocumentContentStore.getDocumentAttachment().getFileSize());
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        String mimeType = getMimeType(file);
        String validMimeType = mimeType != null ? mimeType :  "application/binary";
        manager.addCompletedDownload(getAttachmentFullFilename(), " ", true, validMimeType, file.getAbsolutePath(), contentLength, true);
    }

    public static String getFileUri(File file) {
        return Uri.fromFile(file).toString();
    }
}
