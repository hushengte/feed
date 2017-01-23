package com.disciples.feed.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

public abstract class MapUtils {

	public static String getString(Map<String, Object> map, String key) {
		return getValue(map, key, String.class);
	}
	
	public static Integer getInt(Map<String, Object> map, String key, Integer defaultValue) {
		Number value = getValue(map, key, Number.class);
		if (value != null) {
			return NumberUtils.convertNumberToTargetClass(value, Integer.class);
		}
		return defaultValue;
	}
	
	public static Integer getInt(Map<String, Object> map, String key) {
		return getInt(map, key, null);
	}
	
	public static Boolean getBoolean(Map<String, Object> map, String key) {
		Boolean value = getValue(map, key, Boolean.class);
		return value != null ? value : Boolean.FALSE;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> getValues(Map<String, Object> map, String key, Class<T> componentType) {
		List<Object> values = getValue(map, key, List.class);
		if (values != null) {
			List<T> result = new ArrayList<T>();
			for (Object val : values) {
				try {
					result.add(componentType.cast(val));
				} catch (ClassCastException e) {
					//ignore
				}
			}
			return result;
		}
		T value = getValue(map, key, componentType);
		if (value != null) {
			return Collections.singletonList(value);
		}
		return Collections.emptyList();
	}
	
	public static <T> T getValue(Map<String, Object> map, String key, Class<T> valueType) {
		Assert.notNull(key, "key must not be null");
		Assert.notNull(valueType, "valueType must not be null");
		if (map != null) {
			Object value = map.get(key);
			try {
				return valueType.cast(value);
			} catch (ClassCastException e) {
				if (String.class == valueType && value != null) {
					return valueType.cast(value.toString());
				}
				return null;
			}
		}
		return null;
	}

}
