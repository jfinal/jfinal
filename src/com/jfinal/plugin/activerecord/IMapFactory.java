package com.jfinal.plugin.activerecord;

import java.util.Map;

public interface IMapFactory {
	Map<String, Object> getAttrsMap();
	Map<String, Object> getColumnsMap();
}
