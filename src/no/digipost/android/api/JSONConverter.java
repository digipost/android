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

import javax.ws.rs.core.MultivaluedMap;

import no.digipost.android.model.Letter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.core.util.MultivaluedMapImpl;

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

		//ignore-test
		String [] ignore = {"link","contentUri","deleteUri","updateUri","organizationLogo"};

		ObjectMapper objectMapper = new ObjectMapper();
		FilterProvider filters = new SimpleFilterProvider().addFilter("toJSON",
				SimpleBeanPropertyFilter.serializeAllExcept(ignore));

		Writer strWriter = new StringWriter();
		try {
			objectMapper.filteredWriter(filters).writeValue(strWriter, letter);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strWriter.toString();
	}

	public static JSONObject createJson (final Letter letter) {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("subject", letter.getSubject());
		params.add("creatorName", letter.getCreatorName());
		params.add("created", letter.getCreated());
		params.add("fileType", letter.getType());
		params.add("fileSize", letter.getFileSize());
		params.add("origin", letter.getOrigin());
		params.add("authentication-level", letter.getAuthenticationLevel());
		params.add("location", letter.getLocation());
		params.add("read", letter.getRead());
		params.add("type", letter.getType());

		//return params;

		JSONObject json = new JSONObject();

		try {
			json.put("subject", letter.getSubject());

		json.put("creatorName", letter.getCreatorName());
		json.put("created", letter.getCreated());
		json.put("fileType", letter.getType());
		json.put("fileSize", letter.getFileSize());
		json.put("origin", letter.getOrigin());
		json.put("authentication-level", letter.getAuthenticationLevel());
		json.put("location", letter.getLocation());
		json.put("read", letter.getRead());
		json.put("type", letter.getType());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}
}
