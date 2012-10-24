package com.jfinal.plugin.activerecord;

import java.util.Map;

@SuppressWarnings("rawtypes")
public interface IMapFactory {
	Map getAttrsMap();
	Map getColumnsMap();
}
