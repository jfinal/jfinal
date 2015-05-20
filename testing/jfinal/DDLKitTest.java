/**
 * 
 */
package jfinal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jfinal.ext.kit.DDLKit;
import com.jfinal.ext.kit.DDLKit.Column;

/**
 * @author 朱丛启  2015年5月20日 上午10:11:06
 *
 */
public class DDLKitTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void test() {
		String ddl = DDLKit.createTable("tabename", "tablecomment").addColumn(new Column("name", DDLKit.VARCHAR, "名字", 32,"1", false, true)).ddl();
		System.out.println(ddl);
	}

}
