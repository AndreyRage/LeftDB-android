/*
 * Copyright 2017 Andrii Horishnii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.andreyrage.leftdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.andreyrage.leftdb.entities.AllFields;
import com.github.andreyrage.leftdb.entities.ChildMany;
import com.github.andreyrage.leftdb.entities.ChildOne;
import com.github.andreyrage.leftdb.utils.SerializeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

public class DBUtilsMigration extends LeftDBUtils {

	public static DBUtilsMigration newInstance(Context context, String name, int version) {
		DBUtilsMigration dbUtils = new DBUtilsMigration();
		dbUtils.setDBContext(context, name, version);
		return dbUtils;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		super.onCreate(db);
		createTable(db, AllFields.class);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onUpgrade(db, oldVersion, newVersion);
		if (oldVersion == 1 && newVersion > 1) {
			createTable(db, ChildOne.class);
			oldVersion = 2;
		}
		if (oldVersion == 2 && newVersion > 2) {
			createTable(db, ChildMany.class);
			oldVersion = 3;
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
		if (oldVersion == 3 && newVersion < 3) {
			deleteTable(db, ChildMany.class);
			oldVersion = 2;
		}
		if (oldVersion == 2 && newVersion < 2) {
			deleteTables(db, Arrays.asList(ChildMany.class, ChildOne.class));
			oldVersion = 1;
		}
	}

	@Override
	protected String serializeObject(Object object) {
		try {
			return Arrays.toString(SerializeUtils.serialize(object));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected <T> T deserializeObject(String string, Class<T> tClass, Type genericType) {
		String[] byteValues = string.substring(1, string.length() - 1).split(",");
		byte[] bytes = new byte[byteValues.length];
		for (int i=0, len=bytes.length; i<len; i++) {
			bytes[i] = Byte.parseByte(byteValues[i].trim());
		}

		try {
			Object o = SerializeUtils.deserialize(bytes);
			if (o != null) {
				return tClass.cast(o);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
