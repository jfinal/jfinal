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

package com.jfinal.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;

/**
 * FileRender.
 */
public class FileRender extends Render {
	
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	
	private File file;
	private static String baseDownloadPath;
	private static ServletContext servletContext;
	private String downloadFileName = null;
	
	public FileRender(File file) {
		if (file == null) {
			throw new IllegalArgumentException("file can not be null.");
		}
		this.file = file;
	}
	
	public FileRender(File file, String downloadFileName) {
		this(file);
		
		if (StrKit.isBlank(downloadFileName)) {
			throw new IllegalArgumentException("downloadFileName can not be blank.");
		}
		this.downloadFileName = downloadFileName;
	}
	
	public FileRender(String fileName) {
		if (StrKit.isBlank(fileName)) {
			throw new IllegalArgumentException("fileName can not be blank.");
		}
		
		String fullFileName;
		fileName = fileName.trim();
		if (fileName.startsWith("/") || fileName.startsWith("\\")) {
			if (baseDownloadPath.equals("/")) {
				fullFileName = fileName;
			} else {
				fullFileName = baseDownloadPath + fileName;	
			}
		} else {
			fullFileName = baseDownloadPath + File.separator + fileName;
		}
		
		this.file = new File(fullFileName);
	}
	
	public FileRender(String fileName, String downloadFileName) {
		this(fileName);
		
		if (StrKit.isBlank(downloadFileName)) {
			throw new IllegalArgumentException("downloadFileName can not be blank.");
		}
		this.downloadFileName = downloadFileName;
	}
	
	static void init(String baseDownloadPath, ServletContext servletContext) {
		FileRender.baseDownloadPath = baseDownloadPath;
		FileRender.servletContext = servletContext;
	}
	
	public void render() {
		if (file == null || !file.isFile()) {
			RenderManager.me().getRenderFactory().getErrorRender(404).setContext(request, response).render();
			return ;
        }
		
		// ---------
		response.setHeader("Accept-Ranges", "bytes");
		String fn = downloadFileName == null ? file.getName() : downloadFileName;
		response.setHeader("Content-disposition", "attachment; " + encodeFileName(request, fn));
        String contentType = servletContext.getMimeType(file.getName());
        response.setContentType(contentType != null ? contentType : DEFAULT_CONTENT_TYPE);
        
        // ---------
        if (StrKit.isBlank(request.getHeader("Range"))) {
        	normalRender();
        } else {
        	rangeRender();
        }
	}
	
	protected String encodeFileName(String fileName) {
		try {
			// return new String(fileName.getBytes("GBK"), "ISO8859-1");
			return new String(fileName.getBytes(getEncoding()), "ISO8859-1");
		} catch (UnsupportedEncodingException e) {
			return fileName;
		}
	}
	
	/**
	 * 依据浏览器判断编码规则
	 */
	public String encodeFileName(HttpServletRequest request, String fileName) {
		String userAgent = request.getHeader("User-Agent");
		try {
			String encodedFileName = URLEncoder.encode(fileName, "UTF8");
			// 如果没有UA，则默认使用IE的方式进行编码
			if (userAgent == null) {
				return "filename=\"" + encodedFileName + "\"";
			}
			
			userAgent = userAgent.toLowerCase();
			// IE浏览器，只能采用URLEncoder编码
			if (userAgent.indexOf("msie") != -1) {
				return "filename=\"" + encodedFileName + "\"";
			}
			
			// Opera浏览器只能采用filename*
			if (userAgent.indexOf("opera") != -1) {
				return "filename*=UTF-8''" + encodedFileName;
			}
			
			// Safari浏览器，只能采用ISO编码的中文输出,Chrome浏览器，只能采用MimeUtility编码或ISO编码的中文输出
			if (userAgent.indexOf("safari") != -1 || userAgent.indexOf("applewebkit") != -1 || userAgent.indexOf("chrome") != -1) {
				return "filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO8859-1") + "\"";
			}
			
			// FireFox浏览器，可以使用MimeUtility或filename*或ISO编码的中文输出
			if (userAgent.indexOf("mozilla") != -1) {
				return "filename*=UTF-8''" + encodedFileName;
			}
			
			return "filename=\"" + encodedFileName + "\"";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void normalRender() {
		response.setHeader("Content-Length", String.valueOf(file.length()));
		InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            for (int len = -1; (len = inputStream.read(buffer)) != -1;) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
        	if (getDevMode()) {
        		throw new RenderException(e);
        	}
        } catch (Exception e) {
        	throw new RenderException(e);
        } finally {
            if (inputStream != null)
                try {inputStream.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
            if (outputStream != null)
            	try {outputStream.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
        }
	}
	
	private void rangeRender() {
		Long[] range = {null, null};
		processRange(range);
		
		String contentLength = String.valueOf(range[1].longValue() - range[0].longValue() + 1);
		response.setHeader("Content-Length", contentLength);
		response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);	// status = 206
		
		// Content-Range: bytes 0-499/10000
		StringBuilder contentRange = new StringBuilder("bytes ").append(String.valueOf(range[0])).append("-").append(String.valueOf(range[1])).append("/").append(String.valueOf(file.length()));
		response.setHeader("Content-Range", contentRange.toString());
		
		InputStream inputStream = null;
		OutputStream outputStream = null;
        try {
        	long start = range[0];
        	long end = range[1];
            inputStream = new BufferedInputStream(new FileInputStream(file));
            if (inputStream.skip(start) != start)
                	throw new RuntimeException("File skip error");
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            long position = start;
            for (int len; position <= end && (len = inputStream.read(buffer)) != -1;) {
            	if (position + len <= end) {
            		outputStream.write(buffer, 0, len);
            		position += len;
            	}
            	else {
            		for (int i=0; i<len && position <= end; i++) {
            			outputStream.write(buffer[i]);
                    	position++;
            		}
            	}
            }
            outputStream.flush();
        }
        catch (IOException e) {
        	if (getDevMode())	throw new RenderException(e);
        }
        catch (Exception e) {
        	throw new RenderException(e);
        }
        finally {
            if (inputStream != null)
                try {inputStream.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
            if (outputStream != null)
            	try {outputStream.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
        }
	}
	
	/**
	 * Examples of byte-ranges-specifier values (assuming an entity-body of length 10000):
	 * The first 500 bytes (byte offsets 0-499, inclusive): bytes=0-499
	 * The second 500 bytes (byte offsets 500-999, inclusive): bytes=500-999
	 * The final 500 bytes (byte offsets 9500-9999, inclusive): bytes=-500
	 * 															Or bytes=9500-
	 */
	private void processRange(Long[] range) {
		String rangeStr = request.getHeader("Range");
		int index = rangeStr.indexOf(',');
		if (index != -1)
			rangeStr = rangeStr.substring(0, index);
		rangeStr = rangeStr.replace("bytes=", "");
		
		String[] arr = rangeStr.split("-", 2);
		if (arr.length < 2)
			throw new RuntimeException("Range error");
		
		long fileLength = file.length();
		for (int i=0; i<range.length; i++) {
			if (StrKit.notBlank(arr[i])) {
				range[i] = Long.parseLong(arr[i].trim());
				if (range[i] >= fileLength)
					range[i] = fileLength - 1;
			}
		}
		
		// Range format like: 9500-
		if (range[0] != null && range[1] == null) {
			range[1] = fileLength - 1;
		}
		// Range format like: -500
		else if (range[0] == null && range[1] != null) {
			range[0] = fileLength - range[1];
			range[1] = fileLength - 1;
		}
		
		// check final range
		if (range[0] == null || range[1] == null || range[0].longValue() > range[1].longValue())
			throw new RuntimeException("Range error");
	}
}

