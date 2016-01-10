/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
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

import java.util.HashSet;
import java.util.Set;
import com.jfinal.kit.StrKit;

/**
 * JavaKeyword.
 */
public class JavaKeyword {
	
	private String[] keywordArray = {
		"abstract",
		"assert",
		"boolean",
		"break",
		"byte",
		"case",
		"catch",
		"char",
		"class",
		"const",
		"continue",
		"default",
		"do",
		"double",
		"else",
		"enum",
		"extends",
		"final",
		"finally",
		"float",
		"for",
		"goto",
		"if",
		"implements",
		"import",
		"instanceof",
		"int",
		"interface",
		"long",
		"native",
		"new",
		"package",
		"private",
		"protected",
		"public",
		"return",
		"strictfp",
		"short",
		"static",
		"super",
		"switch",
		"synchronized",
		"this",
		"throw",
		"throws",
		"transient",
		"try",
		"void",
		"volatile",
		"while"
	};
	
	private Set<String> set = initKeyword();
	
	public void addKeyword(String keyword) {
		if (StrKit.notBlank(keyword)) {
			set.add(keyword);
		}
	}
	
	private Set<String> initKeyword() {
		HashSet<String> ret = new HashSet<String>();
		for (String keyword : keywordArray) {
			ret.add(keyword);
		}
		return ret;
	}
	
	public boolean contains(String str) {
		return set.contains(str);
	}
}






