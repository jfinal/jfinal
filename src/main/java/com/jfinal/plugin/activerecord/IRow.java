package com.jfinal.plugin.activerecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * IRow 支持统一的方式来处理 Model 和 Record
 */
public interface IRow<M> {

    public Map<String, Object> toMap();

    public M put(Map<String, Object> map);

    public M set(String attr, Object value);

    public <T> T get(String column);

    /**
     * Get column of any mysql type. Returns defaultValue if null.
     */
    public <T> T get(String column, Object defaultValue);

    /**
     * Get column of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
     */
    public String getStr(String column);

    /**
     * Get column of mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
     */
    public Integer getInt(String column);

    /**
     * Get column of mysql type: bigint
     */
    public Long getLong(String column);

    /**
     * Get column of mysql type: unsigned bigint
     */
    public java.math.BigInteger getBigInteger(String column);

    /**
     * Get column of mysql type: date, year
     */
    public java.util.Date getDate(String column) ;

    public LocalDateTime getLocalDateTime(String column);

    /**
     * Get column of mysql type: time
     */
    public java.sql.Time getTime(String column);

    /**
     * Get column of mysql type: timestamp, datetime
     */
    public java.sql.Timestamp getTimestamp(String column);

    /**
     * Get column of mysql type: real, double
     */
    public Double getDouble(String column);

    /**
     * Get column of mysql type: float
     */
    public Float getFloat(String column);

    public Short getShort(String column);

    public Byte getByte(String column);

    /**
     * Get column of mysql type: bit, tinyint(1)
     */
    public Boolean getBoolean(String column);

    /**
     * Get column of mysql type: decimal, numeric
     */
    public BigDecimal getBigDecimal(String column);

    /**
     * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
     * I have not finished the test.
     */
    public byte[] getBytes(String column);

    /**
     * Get column of any type that extends from Number
     */
    public Number getNumber(String column);

    /**
     * Return json string of this record.
     */
    public String toJson();
}

