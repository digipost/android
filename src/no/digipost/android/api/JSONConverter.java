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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import no.digipost.android.model.Letter;

import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONConverter {
	public static String getJsonStringFromInputStream(final InputStream inputStream) {
		String content = "";

		if (inputStream != null) {
			Writer writer = new StringWriter();
			int buffer_size = 1024;
			char[] buffer = new char[buffer_size];

			try {
				Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), buffer_size);
				int n;

				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}

				inputStream.close();
				reader.close();
				writer.close();
			} catch (Exception e) {
				// TODO Feilhåndtering
			}

			content = writer.toString();
			System.out.println(content + "");
		}

		return content;
	}

	public static <T> Object processJackson(final Class<T> type, final String data) {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonFactory fact = new JsonFactory();
		Object jacksonObject = null;

		try {
			JsonParser jp = fact.createJsonParser(data);
			jacksonObject = objectMapper.readValue(jp, type);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jacksonObject;
	}

	public static <T> Object processJackson(final Class<T> type, final InputStream data) {
		return processJackson(type, getJsonStringFromInputStream(data));
	}

	public static <T> String createJsonFromJackson(final Letter letter) {
		ObjectMapper objectMapper = new ObjectMapper();
		StringEntity se = null;
		String jsonstring = null;

		try {
			jsonstring = objectMapper.writeValueAsString(letter);
			System.out.println("JSON OUT " + jsonstring +"");
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonstring;

	}
}
