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

package com.jfinal.plugin.activerecord;

/**
 * IDbProFactory
 * 
 * 用于自义扩展 DbPro 实现类，实现定制化功能
 * 1：创建 DbPro 继承类： public class MyDbPro extends DbPro
 * 2：创建 IDbProFactory 实现类：public class MyDbProFactory implements IDbProFactory，让其 getDbPro 方法 返回 MyDbPro 对象
 * 3：配置生效： activeRecordPlugin.setDbProFactory(new MyDbProFactory())
 * 
 * 注意：每个 ActiveRecordPlugin 对象拥有独立的 IDbProFactory 对象，多数据源使用时注意要对每个 arp 进行配置
 */
public interface IDbProFactory {
	
	DbPro getDbPro(String configName);
	
	static final IDbProFactory defaultDbProFactory = new IDbProFactory() {
		public DbPro getDbPro(String configName) {
			return new DbPro(configName);
		}
	};
}


