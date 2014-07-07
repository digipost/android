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

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtilities {
	public static final String TEMP_FILE_NAME = "temp";
	public static final String TEMP_FILE_DIRECTORY = Environment.getExternalStorageDirectory() + "/digipost/";

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

	public static File writeFileToSD(final String fileName, final String fileType, final byte[] data) throws Exception {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File file = new File(path.getAbsolutePath(), fileName + "." + fileType);

		return writeData(file, data);
	}

	public static File writeTempFile(final String fileType, final byte[] data) throws IOException {
		File path = new File(TEMP_FILE_DIRECTORY);
		path.mkdir();
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
			if (f.getName().startsWith(TEMP_FILE_NAME)) {
				f.delete();
			}
		}
	}

	private static File writeData(File file, byte[] data) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(data);
		stream.close();

		return file;
	}

	public static void makeFileVisible(Context context, File file) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(file));
		context.sendBroadcast(intent);
	}

    public static String getFileUri(File file) {

        return ((Object)Uri.fromFile(file)).toString();
    }
}
