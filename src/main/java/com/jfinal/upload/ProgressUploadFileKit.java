package com.jfinal.upload;

import com.jfinal.core.JFinal;
import com.jfinal.kit.StrKit;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * 获取上传文件 进度显示工具类
 * @author 山东小木
 */
public class ProgressUploadFileKit {
    public static UploadFile get(HttpServletRequest request,String parameterName, String uploadPath, Consumer<UploadProgress> callback){
        // 检查请求是否包含文件上传
        if (!ServletFileUpload.isMultipartContent(request)) {
            return null;
        }
        UploadFile progressFile = null;
        // 创建文件项工厂
        FileItemFactory factory = new DiskFileItemFactory();
        // 创建上传处理器
        ServletFileUpload upload = new ServletFileUpload(factory);
        // 创建进度监听器
        ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, int items) {
                callback.accept(new UploadProgress(items, contentLength, bytesRead));
            }
        };
        // 将进度监听器添加到上传处理器
        upload.setProgressListener(progressListener);
        try {
            List<FileItem> formItems = upload.parseRequest(request);
            if (formItems != null && !formItems.isEmpty()) {
                FileItem fileItem = null;
                if (StrKit.isBlank(parameterName)) {
                    fileItem = formItems.stream().filter(item -> !item.isFormField()).findFirst().orElse(null);
                } else {
                    fileItem = formItems.stream().filter(item -> (!item.isFormField() && parameterName.equals(item.getFieldName()))).findFirst().orElse(null);
                }
                if (fileItem != null) {
                    // 处理上传的文件
                    String originFileName = fileItem.getName();
                    String finalUploadPath = JFinal.me().getConstants().getBaseUploadPath() + (StrKit.isBlank(uploadPath) ? "" : (File.separator + uploadPath));
                    String newFileName = ProgressUploadFileConfig.getRenameFunc().call(finalUploadPath, originFileName);
                    String filePath = finalUploadPath + File.separator + newFileName;
                    File storeFile = new File(filePath);
                    // 保存文件到硬盘
                    fileItem.write(storeFile);
                    progressFile = new UploadFile(parameterName, finalUploadPath, storeFile.getName(), originFileName, fileItem.getContentType());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return progressFile;
    }
}
