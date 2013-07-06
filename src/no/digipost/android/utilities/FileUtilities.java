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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtilities {
    public static final String TEMP_FILE_NAME = "temp";
    public static final String TEMP_FILE_DIRECTORY = Environment.getExternalStorageDirectory() + "/digipost/";

    public static void openFileWithIntent(final Context context, final String fileType, final byte[] data) throws ActivityNotFoundException, IOException {
        File file = writeTempFile(fileType, data);

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext=file.getName().substring(file.getName().indexOf(".") + 1).toLowerCase();
        String type = mime.getMimeTypeFromExtension(ext);

        intent.setDataAndType(Uri.fromFile(file),type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static File writeFileToSD(final String fileName, final String fileType, final byte[] data) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, fileName + "." + fileType);

        return writeData(file, data);
    }

    public static File writeTempFile(final String fileType, final byte[] data) throws IOException {
        File path = new File(TEMP_FILE_DIRECTORY);
        boolean kk = path.mkdir();
        System.out.println("dir made: " + kk);
        System.out.println("Cache dir: " + path.toString());
        File file = new File(path, TEMP_FILE_NAME + "." + fileType);

        return writeData(file, data);
    }

    public static void deleteTempFiles() {
        File path = new File(TEMP_FILE_DIRECTORY);
        File[] cache = path.listFiles();

        if (cache == null) {
            return;
        }

        for (File f : cache) {
            System.out.println(f.getName());
            if (f.getName().startsWith(TEMP_FILE_NAME)) {
                System.out.println("Delete: " + f.getName());
                boolean del = f.delete();
                System.out.println("Delete successful: " + del);
            }
        }
    }

    private static File writeData(File file, byte[] data) throws IOException {
        FileOutputStream stream = new FileOutputStream(file, true);
        stream.write(data);
        stream.close();

        return file;
    }

    public static void makeFileVisible(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }
}
