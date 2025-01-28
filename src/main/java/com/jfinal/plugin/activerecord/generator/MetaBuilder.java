/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jfinal.plugin.activerecord.generator;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.dialect.Dialect;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import javax.sql.DataSource;

/** MetaBuilder */
public class MetaBuilder {

  protected DataSource dataSource;
  protected Dialect dialect = new MysqlDialect();

  // 白名单 + 黑名单选择过滤的 tableName 集合，白名单优先于黑名单
  protected Set<String> whitelist = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
  protected Set<String> blacklist = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

  protected Predicate<String> tableSkip = null;

  protected Connection conn = null;
  protected DatabaseMetaData dbMeta = null;

  protected String[] removedTableNamePrefixes = null;

  protected TypeMapping typeMapping = new TypeMapping();

  protected boolean generateRemarks = false; // 是否生成备注
  protected boolean generateView = false; // 是否生成 view
  protected boolean fetchFieldAutoIncrement = false; // 是否取出字段的自增属性

  public MetaBuilder(DataSource dataSource) {
    if (dataSource == null) {
      throw new IllegalArgumentException("dataSource can not be null.");
    }
    this.dataSource = dataSource;
  }

  public void setGenerateRemarks(boolean generateRemarks) {
    this.generateRemarks = generateRemarks;
  }

  public void setGenerateView(boolean generateView) {
    this.generateView = generateView;
  }

  /** 配置是否取出字段的自增属性 */
  public void setFetchFieldAutoIncrement(boolean fetchFieldAutoIncrement) {
    this.fetchFieldAutoIncrement = fetchFieldAutoIncrement;
  }

  public void setDialect(Dialect dialect) {
    if (dialect != null) {
      this.dialect = dialect;
    }
  }

  /** 添加要生成的 tableName 到白名单 */
  public void addWhitelist(String... tableNames) {
    if (tableNames != null) {
      for (String table : tableNames) {
        table = table.trim();
        if (this.blacklist.contains(table)) {
          throw new IllegalArgumentException("黑名单中已经存在的 table 不能加入白名单 -> " + table);
        }
        this.whitelist.add(table);
      }
    }
  }

  public void removeWhitelist(String tableName) {
    if (tableName != null) {
      this.whitelist.remove(tableName.trim());
    }
  }

  /** 添加要排除的 tableName 到黑名单 */
  public void addBlacklist(String... tableNames) {
    if (tableNames != null) {
      for (String table : tableNames) {
        table = table.trim();
        if (this.whitelist.contains(table)) {
          throw new IllegalArgumentException("白名单中已经存在的 table 不能加入黑名单 -> " + table);
        }
        this.blacklist.add(table);
      }
    }
  }

  public void removeBlacklist(String tableName) {
    if (tableName != null) {
      this.blacklist.remove(tableName.trim());
    }
  }

  public void addExcludedTable(String... excludedTables) {
    addBlacklist(excludedTables);
  }

  /** 设置需要被移除的表名前缀，仅用于生成 modelName 与 baseModelName 例如表名 "osc_account"，移除前缀 "osc_" 后变为 "account" */
  public void setRemovedTableNamePrefixes(String... removedTableNamePrefixes) {
    this.removedTableNamePrefixes = removedTableNamePrefixes;
  }

  public void setTypeMapping(TypeMapping typeMapping) {
    if (typeMapping != null) {
      this.typeMapping = typeMapping;
    }
  }

  public List<TableMeta> build() {
    System.out.println("Build TableMeta ...");
    try {
      conn = dataSource.getConnection();
      dbMeta = conn.getMetaData();

      List<TableMeta> ret = new ArrayList<TableMeta>();
      buildTableNames(ret);
      for (TableMeta tableMeta : ret) {
        buildPrimaryKey(tableMeta);
        buildColumnMetas(tableMeta);
      }
      removeNoPrimaryKeyTable(ret);
      return ret;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  // 移除没有主键的 table
  protected void removeNoPrimaryKeyTable(List<TableMeta> ret) {
    for (java.util.Iterator<TableMeta> it = ret.iterator(); it.hasNext(); ) {
      TableMeta tm = it.next();
      if (StrKit.isBlank(tm.primaryKey)) {
        if (generateView) {
          tm.primaryKey = dialect.getDefaultPrimaryKey();
          System.out.println("Set primaryKey \"" + tm.primaryKey + "\" for " + tm.name);
        } else {
          it.remove();
          System.err.println("Skip table " + tm.name + " because there is no primary key");
        }
      }
    }
  }

  /**
   * 通过继承并覆盖此方法，跳过一些不希望处理的 table，定制更加灵活的 table 过滤规则
   *
   * @return 返回 true 时将跳过当前 tableName 的处理
   */
  protected boolean isSkipTable(String tableName) {
    return false;
  }

  /**
   * 跳过不需要生成器处理的 table
   *
   * <p>由于 setMetaBuilder 将置换掉 MetaBuilder，所以 Generator.addExcludedTable(...) 需要放在 setMetaBuilder
   * 之后调用，否则 addExcludedTable 将无效
   *
   * <p>示例： Generator gen = new Generator(...); gen.setMetaBuilder(new MetaBuilder(dataSource).skip(
   * tableName -> { return tableName.startsWith("SYS_"); }) ); gen.addExcludedTable("error_log"); //
   * 注意这行代码要放在上面的之后调用 gen.generate();
   */
  public MetaBuilder skip(Predicate<String> tableSkip) {
    this.tableSkip = tableSkip;
    return this;
  }

  /**
   * 构造 modelName，mysql 的 tableName 建议使用小写字母，多单词表名使用下划线分隔，不建议使用驼峰命名 oracle 之下的 tableName
   * 建议使用下划线分隔多单词名，无论 mysql还是 oralce，tableName 都不建议使用驼峰命名
   */
  protected String buildModelName(String tableName) {
    // 移除表名前缀仅用于生成 modelName、baseModelName，而 tableMeta.name 表名自身不能受影响
    if (removedTableNamePrefixes != null) {
      for (String prefix : removedTableNamePrefixes) {
        if (tableName.startsWith(prefix)) {
          tableName = tableName.replaceFirst(prefix, "");
          break;
        }
      }
    }

    // 将 oralce 大写的 tableName 转成小写，再生成 modelName
    if (dialect.isOracle()) {
      tableName = tableName.toLowerCase();
    }

    return StrKit.firstCharToUpperCase(StrKit.toCamelCase(tableName));
  }

  /** 使用 modelName 构建 baseModelName */
  protected String buildBaseModelName(String modelName) {
    return "Base" + modelName;
  }

  /**
   * 不同数据库 dbMeta.getTables(...) 的 schemaPattern 参数意义不同 1：oracle 数据库这个参数代表 dbMeta.getUserName()
   * 2：postgresql 数据库中需要在 jdbcUrl中配置 schemaPatter，例如：
   * jdbc:postgresql://localhost:15432/djpt?currentSchema=public,sys,app
   * 最后的参数就是搜索schema的顺序，DruidPlugin 下测试成功 3：开发者若在其它库中发现工作不正常，可通过继承 MetaBuilder并覆盖此方法来实现功能
   */
  protected ResultSet getTablesResultSet() throws SQLException {
    String schemaPattern = dialect.isOracle() ? dbMeta.getUserName() : null;
    if (generateView) {
      return dbMeta.getTables(
          conn.getCatalog(), schemaPattern, null, new String[] {"TABLE", "VIEW"});
    } else {
      return dbMeta.getTables(
          conn.getCatalog(), schemaPattern, null, new String[] {"TABLE"}); // 不支持 view 生成
    }
  }

  protected void buildTableNames(List<TableMeta> ret) throws SQLException {
    ResultSet rs = getTablesResultSet();
    while (rs.next()) {
      String tableName = rs.getString("TABLE_NAME");

      // 如果使用白名单（size>0），则不在白名单之中的都将被过滤
      if (whitelist.size() > 0 && !whitelist.contains(tableName)) {
        System.out.println("Skip table :" + tableName);
        continue;
      }
      // 如果使用黑名单（size>0），则处在黑名单之中的都将被过滤
      if (blacklist.size() > 0 && blacklist.contains(tableName)) {
        System.out.println("Skip table :" + tableName);
        continue;
      }

      // isSkipTable 为最早期的过滤机制，建议使用白名单、黑名单过滤
      if (isSkipTable(tableName)) {
        System.out.println("Skip table :" + tableName);
        continue;
      }

      // jfinal 4.3 新增过滤 table 机制
      if (tableSkip != null && tableSkip.test(tableName)) {
        System.out.println("Skip table :" + tableName);
        continue;
      }

      TableMeta tableMeta = new TableMeta();
      tableMeta.name = tableName;
      tableMeta.remarks = rs.getString("REMARKS");

      tableMeta.modelName = buildModelName(tableName);
      tableMeta.baseModelName = buildBaseModelName(tableMeta.modelName);
      ret.add(tableMeta);
    }
    rs.close();
  }

  protected void buildPrimaryKey(TableMeta tableMeta) throws SQLException {
    ResultSet rs = dbMeta.getPrimaryKeys(conn.getCatalog(), null, tableMeta.name);

    StringBuilder primaryKey = new StringBuilder();
    int index = 0;
    while (rs.next()) {
      String cn = rs.getString("COLUMN_NAME");

      // 避免 oracle 驱动的 bug 生成重复主键，如：ID,ID
      if (primaryKey.toString().equals(cn)) {
        continue;
      }

      if (index++ > 0) {
        primaryKey.append(",");
      }
      primaryKey.append(cn);
    }

    // 无主键的 table 将在后续的 removeNoPrimaryKeyTable() 中被移除，不再抛出异常
    // if (StrKit.isBlank(primaryKey)) {
    // throw new RuntimeException("primaryKey of table \"" + tableMeta.name + "\" required by active
    // record pattern");
    // }

    tableMeta.primaryKey = primaryKey.toString();
    rs.close();
  }

  /**
   * 文档参考： http://dev.mysql.com/doc/connector-j/en/connector-j-reference-type-conversions.html
   *
   * <p>JDBC 与时间有关类型转换规则，mysql 类型到 java 类型如下对应关系： DATE java.sql.Date DATETIME java.sql.Timestamp
   * TIMESTAMP[(M)] java.sql.Timestamp TIME java.sql.Time
   *
   * <p>对数据库的 DATE、DATETIME、TIMESTAMP、TIME 四种类型注入 new java.util.Date()对象保存到库以后可以达到“秒精度”
   * 为了便捷性，getter、setter 方法中对上述四种字段类型采用 java.util.Date，可通过定制 TypeMapping 改变此映射规则
   */
  protected void buildColumnMetas(TableMeta tableMeta) throws SQLException {
    String sql = dialect.forTableBuilderDoBuild(tableMeta.name);
    Statement stm = conn.createStatement();
    ResultSet rs = stm.executeQuery(sql);
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();

    Map<String, ColumnMeta> columnMetaMap = new HashMap<>();
    if (generateRemarks) {
      ResultSet colMetaRs = null;
      try {
        colMetaRs = dbMeta.getColumns(conn.getCatalog(), null, tableMeta.name, null);
        while (colMetaRs.next()) {
          ColumnMeta columnMeta = new ColumnMeta();
          columnMeta.name = colMetaRs.getString("COLUMN_NAME");
          columnMeta.remarks = colMetaRs.getString("REMARKS");
          columnMetaMap.put(columnMeta.name, columnMeta);
        }
      } catch (Exception e) {
        System.out.println("无法生成 REMARKS");
      } finally {
        if (colMetaRs != null) {
          colMetaRs.close();
        }
      }
    }

    for (int i = 1; i <= columnCount; i++) {
      ColumnMeta cm = new ColumnMeta();
      // 备忘：getColumnName 获取字段真实名称而非 sql as 子句指定的名称
      cm.name = rsmd.getColumnName(i);

      String typeStr = null;
      if (dialect.isKeepByteAndShort()) {
        int type = rsmd.getColumnType(i);
        if (type == Types.TINYINT) {
          typeStr = "java.lang.Byte";
        } else if (type == Types.SMALLINT) {
          typeStr = "java.lang.Short";
        }
      }

      if (typeStr == null) {
        String colClassName = rsmd.getColumnClassName(i);
        typeStr = typeMapping.getType(colClassName);
      }

      if (typeStr == null) {
        int type = rsmd.getColumnType(i);
        if (type == Types.BINARY
            || type == Types.VARBINARY
            || type == Types.LONGVARBINARY
            || type == Types.BLOB) {
          typeStr = "byte[]";
        } else if (type == Types.CLOB || type == Types.NCLOB) {
          typeStr = "java.lang.String";
        }
        // 支持 oracle 的 TIMESTAMP、DATE 字段类型，其中 Types.DATE 值并不会出现
        // 保留对 Types.DATE 的判断，一是为了逻辑上的正确性、完备性，二是其它类型的数据库可能用得着
        else if (type == Types.TIMESTAMP || type == Types.DATE) {
          typeStr = "java.util.Date";
        }
        // 支持 PostgreSql 的 jsonb json
        else if (type == Types.OTHER) {
          typeStr = "java.lang.Object";
        } else {
          typeStr = "java.lang.String";
        }
      }

      typeStr = handleJavaType(typeStr, rsmd, i);

      cm.javaType = typeStr;

      // 构造字段对应的属性名 attrName
      cm.attrName = buildAttrName(cm.name);

      // 备注字段赋值
      if (generateRemarks && columnMetaMap.containsKey(cm.name)) {
        cm.remarks = columnMetaMap.get(cm.name).remarks;
      }

      // 是否取出字段的自增属性
      if (fetchFieldAutoIncrement) {
        cm.isAutoIncrement = rsmd.isAutoIncrement(i);
      }

      // 移除包名前缀 java.lang.
      // if (cm.javaType != null && cm.javaType.startsWith("java.lang.")) {
      // 	cm.javaType = cm.javaType.replaceFirst("java.lang.", "");
      // }
      tableMeta.columnMetas.add(cm);
    }

    rs.close();
    stm.close();
  }

  /**
   * handleJavaType(...) 方法是用于处理 java 类型的回调方法，当 jfinal 默认 处理规则无法满足需求时，用户可以通过继承 MetaBuilder
   * 并覆盖此方法定制自己的 类型转换规则
   *
   * <p>当前实现只处理了 Oracle 数据库的 NUMBER 类型，根据精度与小数位数转换成 Integer、 Long、BigDecimal。其它数据库直接返回原值 typeStr
   *
   * <p>Oracle 数据库 number 类型对应 java 类型： 1：如果不指定number的长度，或指定长度 n > 18 number 对应 java.math.BigDecimal
   * 2：如果number的长度在10 <= n <= 18 number(n) 对应 java.lang.Long 3：如果number的长度在1 <= n <= 9 number(n) 对应
   * java.lang.Integer 类型
   *
   * <p>社区分享：《Oracle NUMBER 类型映射改进》https://jfinal.com/share/1145
   */
  protected String handleJavaType(String typeStr, ResultSetMetaData rsmd, int column)
      throws SQLException {
    // 当前实现只处理 Oracle
    if (!dialect.isOracle()) {
      return typeStr;
    }

    // 默认实现只处理 BigDecimal 类型
    if ("java.math.BigDecimal".equals(typeStr)) {
      int scale = rsmd.getScale(column); // 小数点右边的位数，值为 0 表示整数
      int precision = rsmd.getPrecision(column); // 最大精度
      if (scale == 0) {
        if (precision <= 9) {
          typeStr = "java.lang.Integer";
        } else if (precision <= 18) {
          typeStr = "java.lang.Long";
        } else {
          typeStr = "java.math.BigDecimal";
        }
      } else {
        // 非整数都采用 BigDecimal 类型，需要转成 double 的可以覆盖并改写下面的代码
        typeStr = "java.math.BigDecimal";
      }
    }

    return typeStr;
  }

  /**
   * 构造 colName 所对应的 attrName，mysql 数据库建议使用小写字段名或者驼峰字段名 Oralce 反射将得到大写字段名，所以不建议使用驼峰命名，建议使用下划线分隔单词命名法
   */
  protected String buildAttrName(String colName) {
    if (dialect.isOracle()) {
      colName = colName.toLowerCase();
    }
    return StrKit.toCamelCase(colName);
  }
}
