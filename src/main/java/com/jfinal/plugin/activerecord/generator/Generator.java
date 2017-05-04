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

import java.util.List;
import javax.sql.DataSource;
import com.jfinal.plugin.activerecord.dialect.Dialect;

/**
 * 生成器
 * 1：生成时会强制覆盖 Base model、MappingKit、DataDictionary，建议不要修改三类文件，在数据库有变化重新生成一次便可
 * 2：生成  Model 不会覆盖已经存在的文件，Model 通常会被人为修改和维护
 * 3：MappingKit 文件默认会在生成 Model 文件的同时生成
 * 4：DataDictionary 文件默认不会生成。只有在设置 setGenerateDataDictionary(true)后，会在生成 Model文件的同时生成
 * 5：可以通过继承 BaseModelGenerator、ModelGenerator、MappingKitGenerator、DataDictionaryGenerator
 *   来创建自定义生成器，然后使用 Generator 的 setter 方法指定自定义生成器来生成
 * 6：生成模板文字属性全部为 protected 可见性，方便自定义 Generator 生成符合。。。。
 */
public class Generator {
	
	protected Dialect dialect = null;
	protected MetaBuilder metaBuilder;
	protected BaseModelGenerator baseModelGenerator;
	protected ModelGenerator modelGenerator;
	protected MappingKitGenerator mappingKitGenerator;
	protected DataDictionaryGenerator dataDictionaryGenerator;
	protected boolean generateDataDictionary = false;
	
	/**
	 * 构造 Generator，生成 BaseModel、Model、MappingKit 三类文件，其中 MappingKit 输出目录与包名与 Model相同
	 * @param dataSource 数据源
	 * @param baseModelPackageName base model 包名
	 * @param baseModelOutputDir base mode 输出目录
	 * @param modelPackageName model 包名
	 * @param modelOutputDir model 输出目录
	 */
	public Generator(DataSource dataSource, String baseModelPackageName, String baseModelOutputDir, String modelPackageName, String modelOutputDir) {
		this(dataSource, new BaseModelGenerator(baseModelPackageName, baseModelOutputDir), new ModelGenerator(modelPackageName, baseModelPackageName, modelOutputDir));
	}
	
	/**
	 * 构造 Generator，只生成 baseModel
	 * @param dataSource 数据源
	 * @param baseModelPackageName base model 包名
	 * @param baseModelOutputDir base mode 输出目录
	 */
	public Generator(DataSource dataSource, String baseModelPackageName, String baseModelOutputDir) {
		this(dataSource, new BaseModelGenerator(baseModelPackageName, baseModelOutputDir));
	}
	
	public Generator(DataSource dataSource, BaseModelGenerator baseModelGenerator) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource can not be null.");
		}
		if (baseModelGenerator == null) {
			throw new IllegalArgumentException("baseModelGenerator can not be null.");
		}
		
		this.metaBuilder = new MetaBuilder(dataSource);
		this.baseModelGenerator = baseModelGenerator;
		this.modelGenerator = null;
		this.mappingKitGenerator = null;
		this.dataDictionaryGenerator = null;
	}
	
	/**
	 * 使用指定 BaseModelGenerator、ModelGenerator 构造 Generator 
	 * 生成 BaseModel、Model、MappingKit 三类文件，其中 MappingKit 输出目录与包名与 Model相同
	 */
	public Generator(DataSource dataSource, BaseModelGenerator baseModelGenerator, ModelGenerator modelGenerator) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource can not be null.");
		}
		if (baseModelGenerator == null) {
			throw new IllegalArgumentException("baseModelGenerator can not be null.");
		}
		if (modelGenerator == null) {
			throw new IllegalArgumentException("modelGenerator can not be null.");
		}
		
		this.metaBuilder = new MetaBuilder(dataSource);
		this.baseModelGenerator = baseModelGenerator;
		this.modelGenerator = modelGenerator;
		this.mappingKitGenerator = new MappingKitGenerator(modelGenerator.modelPackageName, modelGenerator.modelOutputDir);
		this.dataDictionaryGenerator = new DataDictionaryGenerator(dataSource, modelGenerator.modelOutputDir);
	}
	
	/**
	 * 设置 MetaBuilder，便于扩展自定义 MetaBuilder
	 */
	public void setMetaBuilder(MetaBuilder metaBuilder) {
		if (metaBuilder != null) {
			this.metaBuilder = metaBuilder;
		}
	}
	
	public void setTypeMapping(TypeMapping typeMapping) {
		this.metaBuilder.setTypeMapping(typeMapping);
	}
	
	/**
	 * 设置 MappingKitGenerator，便于扩展自定义 MappingKitGenerator
	 */
	public void setMappingKitGenerator(MappingKitGenerator mappingKitGenerator) {
		if (mappingKitGenerator != null) {
			this.mappingKitGenerator = mappingKitGenerator;
		}
	}
	
	/**
	 * 设置 DataDictionaryGenerator，便于扩展自定义 DataDictionaryGenerator
	 */
	public void setDataDictionaryGenerator(DataDictionaryGenerator dataDictionaryGenerator) {
		if (dataDictionaryGenerator != null) {
			this.dataDictionaryGenerator = dataDictionaryGenerator;
		}
	}
	
	/**
	 * 设置数据库方言，默认为 MysqlDialect
	 */
	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}
	
	/**
	 * 设置 BaseMode 是否生成链式 setter 方法
	 */
	public void setGenerateChainSetter(boolean generateChainSetter) {
		baseModelGenerator.setGenerateChainSetter(generateChainSetter);
	}
	
	/**
	 * 设置需要被移除的表名前缀，仅用于生成 modelName 与  baseModelName
	 * 例如表名  "osc_account"，移除前缀 "osc_" 后变为 "account"
	 */
	public void setRemovedTableNamePrefixes(String... removedTableNamePrefixes) {
		metaBuilder.setRemovedTableNamePrefixes(removedTableNamePrefixes);
	}
	
	/**
	 * 添加不需要处理的数据表
	 */
	public void addExcludedTable(String... excludedTables) {
		metaBuilder.addExcludedTable(excludedTables);
	}
	
	/**
	 * 设置是否在 Model 中生成 dao 对象，默认生成
	 */
	public void setGenerateDaoInModel(boolean generateDaoInModel) {
		if (modelGenerator != null) {
			modelGenerator.setGenerateDaoInModel(generateDaoInModel);
		}
	}
	
	/**
	 * 设置是否生成数据字典 Dictionary 文件，默认不生成
	 */
	public void setGenerateDataDictionary(boolean generateDataDictionary) {
		this.generateDataDictionary = generateDataDictionary;
	}
	
	/**
	 * 设置 MappingKit 文件输出目录，默认与 modelOutputDir 相同，
	 * 在设置此变量的同时需要设置 mappingKitPackageName
	 */
	public void setMappingKitOutputDir(String mappingKitOutputDir) {
		if (this.mappingKitGenerator != null) {
			this.mappingKitGenerator.setMappingKitOutputDir(mappingKitOutputDir);
		}
	}
	
	/**
	 * 设置 MappingKit 文件包名，默认与 modelPackageName 相同，
	 * 在设置此变的同时需要设置 mappingKitOutputDir
	 */
	public void setMappingKitPackageName(String mappingKitPackageName) {
		if (this.mappingKitGenerator != null) {
			this.mappingKitGenerator.setMappingKitPackageName(mappingKitPackageName);
		}
	}
	
	/**
	 * 设置 MappingKit 类名，默认值为: "_MappingKit"
	 */
	public void setMappingKitClassName(String mappingKitClassName) {
		if (this.mappingKitGenerator != null) {
			this.mappingKitGenerator.setMappingKitClassName(mappingKitClassName);
		}
	}
	
	/**
	 * 设置数据字典 DataDictionary 文件输出目录，默认与 modelOutputDir 相同
	 */
	public void setDataDictionaryOutputDir(String dataDictionaryOutputDir) {
		if (this.dataDictionaryGenerator != null) {
			this.dataDictionaryGenerator.setDataDictionaryOutputDir(dataDictionaryOutputDir);
		}
	}
	
	/**
	 * 设置数据字典 DataDictionary 文件输出目录，默认值为 "_DataDictionary.txt"
	 */
	public void setDataDictionaryFileName(String dataDictionaryFileName) {
		if (dataDictionaryGenerator != null) {
			dataDictionaryGenerator.setDataDictionaryFileName(dataDictionaryFileName);
		}
	}
	
	public void generate() {
		if (dialect != null) {
			metaBuilder.setDialect(dialect);
		}
		
		long start = System.currentTimeMillis();
		List<TableMeta> tableMetas = metaBuilder.build();
		if (tableMetas.size() == 0) {
			System.out.println("TableMeta 数量为 0，不生成任何文件");
			return ;
		}
		
		baseModelGenerator.generate(tableMetas);
		
		if (modelGenerator != null) {
			modelGenerator.generate(tableMetas);
		}
		
		if (mappingKitGenerator != null) {
			mappingKitGenerator.generate(tableMetas);
		}
		
		if (dataDictionaryGenerator != null && generateDataDictionary) {
			dataDictionaryGenerator.generate(tableMetas);
		}
		
		long usedTime = (System.currentTimeMillis() - start) / 1000;
		System.out.println("Generate complete in " + usedTime + " seconds.");
	}
}



