/**
 * Copyright (c) 2011-2017, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord.generator;

/**
 * ColumnMeta
 */
public class ColumnMeta {
	
	public String name;				// 字段名
	public String javaType;			// 字段对应的 java 类型
	public String attrName;			// 字段对应的属性名
	
	// ---------
	
	/*
	-----------+---------+------+-----+---------+----------------
	 Field     | Type    | Null | Key | Default | Remarks
	-----------+---------+------+-----+---------+----------------
	 id		   | int(11) | NO	| PRI | NULL	| remarks here	
	*/
	public String type;				// 字段类型(附带字段长度与小数点)，例如：decimal(11,2)
	public String isNullable;		// 是否允许空值
	public String isPrimaryKey;		// 是否主键
	public String defaultValue;		// 默认值
	public String remarks;			// 字段备注
}

