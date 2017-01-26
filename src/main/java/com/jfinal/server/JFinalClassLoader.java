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

package com.jfinal.server;

import java.io.File;
import java.io.IOException;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * JFinalClassLoader
 */
class JFinalClassLoader extends WebAppClassLoader {
	private boolean initialized = false;
	
	public JFinalClassLoader(WebAppContext context, String classPath) throws IOException {
		super(context);
		if(classPath != null){
			String[] tokens = classPath.split(String.valueOf(File.pathSeparatorChar));
			for(String entry : tokens){
				String path = entry;
				if(path.startsWith("-y-") || path.startsWith("-n-")) {
					path = path.substring(3);
				}
				
				if(entry.startsWith("-n-") == false){
					super.addClassPath(path);
				}
			}
		}
		
		initialized = true;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Class loadClass(String name) throws ClassNotFoundException {
		try {
			return loadClass(name, false);
		}
		catch (NoClassDefFoundError e) {
			throw new ClassNotFoundException(name);
		}
	}
	
	public void addClassPath(String classPath) throws IOException {
		if (initialized) {
			if (!classPath.endsWith("WEB-INF/classes/"))
				return;
		}
		super.addClassPath(classPath);
	}
	
	public void addJars(Resource jars) {
		if (initialized) {
			return;
		}
		super.addJars(jars);
	}
}






