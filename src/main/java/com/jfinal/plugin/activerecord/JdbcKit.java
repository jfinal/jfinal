/**
 * 
 */
package com.jfinal.plugin.activerecord;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author tanyaowu
 *
 */
public class JdbcKit {

	public static Byte getByte(ResultSet rs, int i) throws SQLException {
		Object value = rs.getObject(i);
		if (value != null) {
			value = Byte.parseByte(value + "");
			return (Byte) value;
		} else {
			return null;
		}
	}
	
	public static Short getShort(ResultSet rs, int i) throws SQLException {
		Object value = rs.getObject(i);
		if (value != null) {
			value = Short.parseShort(value + "");
			return (Short) value;
		} else {
			return null;
		}
	}

	/**
	 * 
	 */
	public JdbcKit() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
