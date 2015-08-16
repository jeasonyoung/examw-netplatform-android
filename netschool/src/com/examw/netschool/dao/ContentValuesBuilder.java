package com.examw.netschool.dao;

import java.lang.reflect.Field;

import android.content.ContentValues;

import com.examw.netschool.annotation.Column;
import com.examw.netschool.entity.BaseEntity;

public class ContentValuesBuilder {
	private static ContentValuesBuilder instance;
	public static ContentValues mContentValues;

	// private static String TABLE_NAME = "tableName";

	public static ContentValuesBuilder getInstance() {
		if (null == instance) {
			instance = new ContentValuesBuilder();
		}
		mContentValues = new ContentValues();
		return instance;
	}

	public <T extends BaseEntity> ContentValues bulid(T domain)
			throws IllegalArgumentException, IllegalAccessException 
			{
		// Table table = domain.getClass().getAnnotation(Table.class);
		// mContentValues.put(TABLE_NAME, table.name());

		for (Field f : domain.getClass().getDeclaredFields()) {
			if (f.getAnnotations().length != 0) {
				f.setAccessible(true);
				f.getType().getName();
				// Class.forName(f.getType().getName()).newInstance();
				Object obj =  f.get(domain);
				mContentValues.put(f.getAnnotation(Column.class).name(), obj==null?null:obj.toString());
			}
		}

		// domain.getClass().isAnnotationPresent(Table.class);
		return mContentValues;

	}
}
