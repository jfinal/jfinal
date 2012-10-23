/**
 * Copyright (c) 2011-2012, James Zhan 詹波 (jfinal@126.com).
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
import javax.servlet.ServletContext;
import static com.jfinal.core.Const.DEFAULT_FILE_CONTENT_TYPE;
import com.jfinal.util.PathUtil;

/**
 * FileRender.
 */
@SuppressWarnings("serial")
class FileRender extends Render {
	
	private File file;
	private String fileName;
	private static String fileDownloadPath;
	private static ServletContext servletContext;
	private static String webRootPath;
	
	public FileRender(File file) {
		this.file = file;
	}
	
	public FileRender(String fileName) {
		this.fileName = fileName;
	}
	
	static void init(String fileDownloadPath, ServletContext servletContext) {
		FileRender.fileDownloadPath = fileDownloadPath;
		FileRender.servletContext = servletContext;
		webRootPath = PathUtil.getWebRootPath();
	}
	
	public void render() {
		if (fileName != null) {
			if (fileName.startsWith("/"))
				file = new File(webRootPath + fileName);
			else
				file = new File(fileDownloadPath + fileName);
		}
		
		if (file == null || !file.isFile() || file.length() > Integer.MAX_VALUE) {
            // response.sendError(HttpServletResponse.SC_NOT_FOUND);
            // return;
			
			// throw new RenderException("File not found!");
			RenderFactory.me().getError404Render().setContext(request, response).render();
			return ;
        }
		
		response.addHeader("Content-disposition", "attachment; filename=" + file.getName());
        String contentType = servletContext.getMimeType(file.getName());
        if (contentType == null) {
        	contentType = DEFAULT_FILE_CONTENT_TYPE;		// "application/octet-stream";
        }
        
        response.setContentType(contentType);
        response.setContentLength((int)file.length());
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            for (int n = -1; (n = inputStream.read(buffer)) != -1;) {
                outputStream.write(buffer, 0, n);
            }
            outputStream.flush();
        }
        catch (Exception e) {
        	throw new RenderException(e);
        }
        finally {
            if (inputStream != null) {
                try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            if (outputStream != null) {
            	try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }
	}
}


