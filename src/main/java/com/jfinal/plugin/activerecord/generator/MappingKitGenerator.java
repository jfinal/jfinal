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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import com.jfinal.kit.Kv;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.template.Engine;

/**
 * MappingKit 文件生成器
 */
public class MappingKitGenerator {
	
	protected Engine engine;
	protected String template = "/com/jfinal/plugin/activerecord/generator/mapping_kit_template.jf";
	
	protected String mappingKitPackageName;
	protected String mappingKitOutputDir;
	protected String mappingKitClassName = "_MappingKit";
	
	public MappingKitGenerator(String mappingKitPackageName, String mappingKitOutputDir) {
		this.mappingKitPackageName = mappingKitPackageName;
		this.mappingKitOutputDir = mappingKitOutputDir;
		
		initEngine();
	}
	
	protected void initEngine() {
		engine = new Engine();
		engine.setToClassPathSourceFactory();
		engine.addSharedMethod(new StrKit());
	}
	
	/**
	 * 使用自定义模板生成 MappingKit
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public void setMappingKitOutputDir(String mappingKitOutputDir) {
		if (StrKit.notBlank(mappingKitOutputDir)) {
			this.mappingKitOutputDir = mappingKitOutputDir;
		}
	}
	
	public void setMappingKitPackageName(String mappingKitPackageName) {
		if (StrKit.notBlank(mappingKitPackageName)) {
			this.mappingKitPackageName = mappingKitPackageName;
		}
	}
	
	public void setMappingKitClassName(String mappingKitClassName) {
		if (StrKit.notBlank(mappingKitClassName)) {
			this.mappingKitClassName = StrKit.firstCharToUpperCase(mappingKitClassName);
		}
	}
	
	public void generate(List<TableMeta> tableMetas) {
		System.out.println("Generate MappingKit file ...");
		System.out.println("MappingKit Output Dir: " + mappingKitOutputDir);
		
		Kv data = Kv.by("mappingKitPackageName", mappingKitPackageName);
		data.set("mappingKitClassName", mappingKitClassName);
		data.set("tableMetas", tableMetas);
		
		String ret = engine.getTemplate(template).renderToString(data);
		writeToFile(ret);
	}
	
	/**
	 * _MappingKit.java 覆盖写入
	 */
	protected void writeToFile(String ret) {
		FileWriter fw = null;
		try {
			File dir = new File(mappingKitOutputDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			String target = mappingKitOutputDir + File.separator + mappingKitClassName + ".java";
			fw = new FileWriter(target);
			fw.write(ret);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (fw != null) {
				try {fw.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
			}
		}
	}
}




