/**
 * 
 */
package com.jfinal.ext.kit;

import java.util.Random;
import java.util.UUID;

/**
 * @author 朱丛启  2015年5月2日 下午6:53:32
 *
 */
public class RandomKit {

	public enum SMSAuthCodeType{
		Numbers,
		CharAndNumbers,
	}
	
	/**
	 * 短信验证码,纯数字
	 * @param codeLen 验证码长度
	 * @return
	 */
	public static String smsAuthCode(int codeLen){
		return smsAuthCode(codeLen, SMSAuthCodeType.Numbers);
	}
	
	/**
	 * sms验证码
	 * @param codeLen
	 * @param type
	 * @return
	 */
	public static String smsAuthCode(int codeLen, SMSAuthCodeType type){
		String randomCode = "";
		String strTable = type == SMSAuthCodeType.Numbers ? "1234567890"
				: "1234567890abcdefghijkmnpqrstuvwxyz";
		int len = strTable.length();
		boolean bDone = true;
		do {
			randomCode = "";
			int count = 0;
			for (int i = 0; i < codeLen; i++) {
				double dblR = Math.random() * len;
				int intR = (int) Math.floor(dblR);
				char c = strTable.charAt(intR);
				if (('0' <= c) && (c <= '9')) {
					count++;
				}
				randomCode += strTable.charAt(intR);
			}
			if (count >= 2) {
				bDone = false;
			}
		} while (bDone);

		return randomCode.toUpperCase();
	}
	
	/**
	 * 随机范围内的数
	 * @param min
	 * @param max
	 * @return
	 */
	public static int random(int min, int max){
		Random random = new Random();
		return random.nextInt(max)%(max-min+1) + min;
	}
	
	/**
	 * 随机字符串：UUID方式
	 * @return
	 */
	public static String randomStr(){
		return UUID.randomUUID().toString();
	}
	
	/**
	 *  随机字符串再 md5：UUID方式
	 * @return
	 */
	public static String randomMD5Str(){
		return PasswordKit.MD5(randomStr());
	}
}
