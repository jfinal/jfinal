/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord.builder;

import com.jfinal.plugin.activerecord.CPI;
import com.jfinal.plugin.activerecord.Config;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.RecordBuilder;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * H2Database ResultRet to Record Builder.
 * <pre>
 * 使用示例：
 * H2Dialect dialect = new H2Dialect();
 * dialect.setRecordBuilder(H2RecordBuilder.me);
 * activeRecordPlugin.setDialect(dialect);
 * </pre>
 */
public class H2RecordBuilder extends RecordBuilder {

    public static final H2RecordBuilder me = new H2RecordBuilder();

    @Override
    public List<Record> build(Config config, ResultSet rs) throws SQLException {
        return build(config, rs, null);
    }

    /**
     * 处理h2database JDBC查询结果集到Record与oracle不同，h2database中 BLOB列数据直接getBytes()取数据不需要处理和转换
     *
     * @param config
     * @param rs
     * @param func
     * @return
     * @throws SQLException
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Record> build(Config config, ResultSet rs, Function<Record, Boolean> func) throws SQLException {
        List<Record> result = new ArrayList<Record>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        String[] labelNames = new String[columnCount + 1];
        int[] types = new int[columnCount + 1];
        buildLabelNamesAndTypes(rsmd, labelNames, types);
        while (rs.next()) {
            Record record = new Record();
            CPI.setColumnsMap(record, config.getContainerFactory().getColumnsMap());
            Map<String, Object> columns = record.getColumns();
            for (int i = 1; i <= columnCount; i++) {
                Object value;
                if (types[i] < Types.BLOB) {
                    value = rs.getObject(i);
                } else {
                    if (types[i] == Types.CLOB) {
                        value = rs.getString(i);
                    } else if (types[i] == Types.NCLOB) {
                        value = rs.getString(i);
                    } else if (types[i] == Types.BLOB) {
                        value = rs.getBytes(i);
                    } else {
                        value = rs.getObject(i);
                    }
                }
                columns.put(labelNames[i], value);
            }

            if (func == null) {
                result.add(record);
            } else {
                if (!func.apply(record)) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
        for (int i = 1; i < labelNames.length; i++) {
            // 备忘：getColumnLabel 获取 sql as 子句指定的名称而非字段真实名称
            labelNames[i] = rsmd.getColumnLabel(i);
            types[i] = rsmd.getColumnType(i);
        }
    }
}





