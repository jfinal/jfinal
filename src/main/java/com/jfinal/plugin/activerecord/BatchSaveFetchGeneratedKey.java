package com.jfinal.plugin.activerecord;

import java.util.List;

/**
 * Db.batchSave 支持获取生成的主键值
 * 配置方式：
 * arp.setDbProFactory(configName -> new DbProBatchSaveFetchGeneratedKey(configName));
 */
public class BatchSaveFetchGeneratedKey extends DbPro {
	
	public BatchSaveFetchGeneratedKey(String configName) {
		super(configName);
	}
	
	@SuppressWarnings("rawtypes")
	public int[] batchSave(List<? extends Model> modelList, int batchSize) {
		throw new RuntimeException("暂未实现");
	}
	
	public int[] batchSave(String tableName, List<? extends Record> recordList, int batchSize) {
		throw new RuntimeException("暂未实现");
	}
}




