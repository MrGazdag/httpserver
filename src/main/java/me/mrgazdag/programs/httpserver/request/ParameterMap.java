package me.mrgazdag.programs.httpserver.request;

import java.util.*;

@SuppressWarnings("unused")
public class ParameterMap {
	private static Map<String, List<String>> map;
	public ParameterMap() {
		map = new HashMap<>();
	}
	public ParameterMap(String query) {
		this();
		parse(query);
	}
	public void parse(String query) {
		String[] parts = query.split("&");
        for (String string : parts) {
			String key;
			String[] values;
			if (string.contains("=")) {
				String[] split = string.split("=");
				key = split[0];
				if (split.length == 1) {
					values = new String[]{""};
				} else {
					values = split[1].split(",");
				}
			} else {
				key = string;
				values = new String[]{""};
			}
			List<String> list;
        	if (map.containsKey(key)) list = map.get(key);
        	else {
        		list = new ArrayList<>();
        		map.put(key, list);
        	}
			list.addAll(Arrays.asList(values));
		}
	}
	void add(String key, Object value) {
		add(key, String.valueOf(value));
	}
	void add(String key, String value) {
		List<String> list;
    	if (map.containsKey(key)) list = map.get(key);
    	else {
    		list = new ArrayList<>();
    		map.put(key, list);
    	}
		list.add(value);
	}
	void add(String key, String...values) {
		List<String> list;
    	if (map.containsKey(key)) list = map.get(key);
    	else {
    		list = new ArrayList<>();
    		map.put(key, list);
    	}
		list.addAll(Arrays.asList(values));
	}
	public String getString(String key) {
		if (!map.containsKey(key)) return null;
		return map.get(key).get(0);
	}
	public List<String> getStringList(String key) {
		if (!map.containsKey(key)) return null;
		return map.get(key);
	}
	public boolean isInt(String key) {
		if (!map.containsKey(key)) return false;
		try {
			Integer.parseInt(map.get(key).get(0));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public int getInt(String key) {
		if (!map.containsKey(key)) return -1;
		try {
			return Integer.parseInt(map.get(key).get(0));
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public List<Integer> getIntList(String key) {
		if (!map.containsKey(key)) return null;
		try {
			List<Integer> result = new ArrayList<>();
			for (String string : map.get(key)) {
				result.add(Integer.parseInt(string));
			}
			return result;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	public boolean isLong(String key) {
		if (!map.containsKey(key)) return false;
		try {
			Long.parseLong(map.get(key).get(0));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	public long getLong(String key) {
		if (!map.containsKey(key)) return -1;
		try {
			return Long.parseLong(map.get(key).get(0));
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	public List<Long> getLongList(String key) {
		if (!map.containsKey(key)) return null;
		try {
			List<Long> result = new ArrayList<>();
			for (String string : map.get(key)) {
				result.add(Long.parseLong(string));
			}
			return result;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	public boolean contains(String key) {
		return map.containsKey(key);
	}
	@Override
	public String toString() {
		return map.toString();
	}
}
