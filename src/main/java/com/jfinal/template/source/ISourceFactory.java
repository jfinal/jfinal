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

package com.jfinal.template.source;

/**
 * ISourceFactory 用于为 engine 切换不同的 ISource 实现类
 * 
 * FileSourceFactory 用于从指定的目录中加载模板文件
 * ClassPathSourceFactory 用于从 class path 以及 jar 文件中加载模板文件
 * 
 * 配置示例：
 * engine.setSourceFactory(new ClassPathSourceFactory());
 */
public interface ISourceFactory {
	ISource getSource(String baseTemplatePath, String fileName, String encoding);
}




