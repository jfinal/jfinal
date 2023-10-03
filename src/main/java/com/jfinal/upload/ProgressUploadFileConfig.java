package com.jfinal.upload;

import com.jfinal.kit.StrKit;
import com.jfinal.kit.TimeKit;
import java.io.File;

/**
 * 进度上传文件配置
 *
 * @author 山东小木
 */
public class ProgressUploadFileConfig {
    /**
     * 进度上传文件 rename 策略
     * 给了默认实现
     */
    private static ProgressUploadFileRenameFunc renameFunc = new TimeProgressUploadFileRenameFunc();

    /**
     * 设置自己的策略
     *
     * @param func
     */
    public static void setRenameFunc(ProgressUploadFileRenameFunc func) {
        renameFunc = func;
    }

    /**
     * 设置UUID rename策略
     */
    public static void setUUIDRenameFunc() {
        renameFunc = new UUIDProgressUploadFileRenameFunc();
    }

    /**
     * 设置基于当前时间格式化的 rename策略
     */
    public static void setTimeRenameFunc() {
        renameFunc = new TimeProgressUploadFileRenameFunc();
    }

    /**
     * 设置重名自动计数加1格式化 rename策略
     */
    public static void setCountRenameFunc() {
        renameFunc = new CountProgressUploadFileRenameFunc();
    }

    /**
     * 获取当前策略
     *
     * @return
     */
    public static ProgressUploadFileRenameFunc getRenameFunc() {
        return renameFunc == null ? new TimeProgressUploadFileRenameFunc() : renameFunc;
    }

    /**
     * rename计数策略实现
     */
    static class CountProgressUploadFileRenameFunc implements ProgressUploadFileRenameFunc {
        @Override
        public String call(String directory, String originFileName) {
            if(!directory.endsWith("/")){
                directory = directory + '/';
            }
            File file = new File(directory + originFileName);
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
        public String call(String directory, String originFileName) {
            int dotIndex = originFileName.lastIndexOf(".");
            String extension = "";
            if (dotIndex != -1) {
                extension = originFileName.substring(dotIndex);
            }
            return StrKit.getRandomUUID() + extension;
        }
    }

    /**
     * 内置基于时间的文件名rename策略
     */
    static class TimeProgressUploadFileRenameFunc implements ProgressUploadFileRenameFunc {
        @Override
        public String call(String directory, String originFileName) {
            if(!directory.endsWith("/")){
                directory = directory + '/';
            }
            File file;
            String newFilename = originFileName;
            do {
                int dotIndex = originFileName.lastIndexOf(".");
                newFilename = TimeKit.nowWithMillisecond();
                String extension = "";
                if (dotIndex != -1) {
                    extension = originFileName.substring(dotIndex);
                    newFilename = newFilename + extension;
                }
                file = new File(directory + newFilename);
            } while (file.exists());
            return newFilename;
        }
    }
}
