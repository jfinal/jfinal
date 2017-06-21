package com.jfinal.kit;

import java.nio.charset.Charset;

public class Base64Kit {
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	private Base64Kit() {}
	private static IBase64 delegate;
	static{
		IBase64 delegateToUse = null;
		if (isPresent("java.util.Base64", Base64Kit.class.getClassLoader())) {
            delegateToUse = new Java8Base64();
        }else{
        	delegateToUse = new Java67Base64();
        }
		delegate = delegateToUse;
	}
	/**
     * 编码
     * @param value byte数组
     * @return {String}
     */
    public static String encode(byte[] value) {
        return delegate.encode(value);
    }

    /**
     * 编码
     * @param value 字符串
     * @return {String}
     */
    public static String encode(String value) {
        byte[] val = value.getBytes(UTF_8);
        return delegate.encode(val);
    }

    /**
     * 编码
     * @param value 字符串
     * @param charsetName charSet
     * @return {String}
     */
    public static String encode(String value, String charsetName) {
        byte[] val = value.getBytes(Charset.forName(charsetName));
        return delegate.encode(val);
    }

    /**
     * 解码
     * @param value 字符串
     * @return {byte[]}
     */
    public static byte[] decode(String value) {
        return delegate.decode(value);
    }

    /**
     * 解码
     * @param value 字符串
     * @return {String}
     */
    public static String decodeToStr(String value) {
        byte[] decodedValue = delegate.decode(value);
        return new String(decodedValue, UTF_8);
    }

    /**
     * 解码
     * @param value 字符串
     * @param charsetName 字符集
     * @return {String}
     */
    public static String decodeToStr(String value, String charsetName) {
        byte[] decodedValue = delegate.decode(value);
        return new String(decodedValue, Charset.forName(charsetName));
    }
    
    private  static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, true, classLoader);
            return true;
        }catch (Throwable ex) {
            return false;
        }
    }
    
    static interface IBase64 {
    	public String encode(byte[] value);
    	public byte[] decode(String value);
    }

    static class Java8Base64 implements IBase64{
    	@Override
    	public String encode(byte[] value) {
    		return java.util.Base64.getEncoder().encodeToString(value);  
    	}

    	@Override
    	public byte[] decode(String value) {
    		return java.util.Base64.getDecoder().decode(value);
    	}
    }

    static class Java67Base64 implements IBase64{
    	public String encode(byte[] data) {
            return javax.xml.bind.DatatypeConverter.printBase64Binary( data );
        }
        public byte[] decode(String base64){
            return javax.xml.bind.DatatypeConverter.parseBase64Binary( base64 );
        }
    }
}