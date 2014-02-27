/*
 * Copyright (C) 2014 Niko Rehnbäck
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

package com.networking.nemo.deserializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * Checks through class T object member fields for {@link JsonRequired}
 * annotations. If annotation found, but value <code>null</code>,
 * {@link JsonKeyNotFoundExpection} will be thrown and request handled as error.
 * 
 * @author Niko Rehnbäck
 * 
 * @param <T>
 *            Class of the object to be deserialized from Json.
 */
public class JsonRequiredDeserializer<T> implements JsonDeserializer<T> {

	@Override
	public T deserialize(JsonElement element, Type type,
			JsonDeserializationContext JDContext) throws JsonParseException {
		T object = new Gson().fromJson(element, type);
		Field[] fields = object.getClass().getDeclaredFields();

		for (Field f : fields) {
			if (f.getAnnotation(JsonRequired.class) != null
					&& f.getAnnotation(SerializedName.class) != null) {
				try {
					f.setAccessible(true);

					if (f.get(object) == null) {
						SerializedName key = f
								.getAnnotation(SerializedName.class);

						throw new JsonKeyNotFoundExpection(key.value());
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return object;
	}
}
