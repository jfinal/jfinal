package com.jfinal.plugin.activerecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class CaseInsensitiveMapFactory implements IMapFactory {
	
	public Map<String, Object> getAttrsMap() {
		return new CaseInsensitiveMap();
	}
	
	public Map<String, Object> getColumnsMap() {
		return new CaseInsensitiveMap();
	}
}

@SuppressWarnings({"rawtypes", "unchecked"})
class CaseInsensitiveMap extends HashMap {
	
	private static final long serialVersionUID = -3415001825854442053L;
	
	@Override
	public Object get(Object key) {
		Object k = (key instanceof String ? ((String)key).toUpperCase() : key);
		return super.get(k);
	}
	
	@Override
	public boolean containsKey(Object key) {
		Object k = (key instanceof String ? ((String)key).toUpperCase() : key);
		return super.containsKey(k);
	}
	
	@Override
	public Object put(Object key, Object value) {
		Object k = (key instanceof String ? ((String)key).toUpperCase() : key);
		return super.put(k, value);
	}
	
	@Override
	public void putAll(Map m) {
		for (Map.Entry e : (Set<Map.Entry>)(m.entrySet()))
            put(e.getKey(), e.getValue());
	}
	
	@Override
	public Object remove(Object key) {
		Object k = (key instanceof String ? ((String)key).toUpperCase() : key);
		return super.remove(k);
	}
}
