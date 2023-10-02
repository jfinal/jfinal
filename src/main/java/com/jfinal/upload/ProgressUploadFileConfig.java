package com.jfinal.upload;

import com.jfinal.kit.StrKit;

import java.io.File;

/**
 * 进度上传文件配置
 * @author 山东小木
 */
public class ProgressUploadFileConfig {
    /**
     * 进度上传文件 rename 策略
     * 给了默认实现
     */
    private static ProgressUploadFileRenameFunc renameFunc = new DefaultProgressUploadFileRenameFunc();

    /**
     * 设置自己的策略
     * @param func
     */
    public static void setRenameFunc(ProgressUploadFileRenameFunc func){
        renameFunc = func;
    }

    /**
     * 设置UUID rename策略
     */
    public static void setUUIDRenameFunc(){
        renameFunc = new UUIDProgressUploadFileRenameFunc();
    }

    /**
     * 获取当前策略
     * @return
     */
    public static ProgressUploadFileRenameFunc getRenameFunc(){
        return renameFunc==null?new DefaultProgressUploadFileRenameFunc():renameFunc;
    }

    /**
     * 默认策略实现
     */
    static class DefaultProgressUploadFileRenameFunc implements ProgressUploadFileRenameFunc {
        @Override
        public String call(String directory,String originFileName) {
            File file = new File(directory+"/"+originFileName);
            int count = 1;
            String newFilename = originFileName;
            while (file.exists()) {
                int dotIndex = originFileName.lastIndexOf(".");
                String extension = "";
                if (dotIndex != -1) {
                    extension = originFileName.substring(dotIndex);
                    newFilename = originFileName.substring(0, dotIndex) + "_" + count + extension;
                } else {
                    newFilename = originFileName + "_" + count;
                }
                file = new File(directory + newFilename);
                count++;
            }
            return newFilename;
        }
    }


    /**
     * UUID 内置实现
     */
    static class UUIDProgressUploadFileRenameFunc implements ProgressUploadFileRenameFunc {
        @Override
        public String call(String directory,String originFileName) {
            int dotIndex = originFileName.lastIndexOf(".");
            String extension = "";
            if (dotIndex != -1) {
                extension = originFileName.substring(dotIndex);
            }
            return StrKit.getRandomUUID()+extension;
        }
    }
}
