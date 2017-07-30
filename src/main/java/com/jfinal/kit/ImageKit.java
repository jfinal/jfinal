/**
 * Copyright (c) 2011-2017, 玛雅牛 (myaniu AT gmail dot com).
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

package com.jfinal.kit;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageKit {
    private ImageKit() {
        
    }
    
    /**
     * 生成形如data:image/jpeg;base64,iVBORw0KGgoA……的字符串，将图片文件Data URI化
     *
     * @param imageFilePath 图片文件路径
     * @return 形如data:image/jpeg;base64,iVBORw0KGgoA……的字符串
     * @throws IOException
     */
    public static String encodeDataUri(String imageFilePath) throws IOException{
        return encodeDataUri(new File(imageFilePath));
    }
    
    /**
     * 生成形如data:image/jpeg;base64,iVBORw0KGgoA……的字符串，将图片文件Data URI化
     *
     * @param imageFile 图片文件对象
     * @return 形如data:image/jpeg;base64,iVBORw0KGgoA……的字符串
     * @throws IOException
     */
    public static String encodeDataUri(File imageFile) throws IOException{
        String type = FileKit.getFileExtension(imageFile).toLowerCase();
        if("jpg".equals(type)) {
            type = "jpeg";
        }
        return "data:image/"+type+";base64," + encodeBase64(imageFile);
    }
    
    /**
     * 将文件编码成base64格式
     *
     * @param imageFilePath 图片文件路径
     * @return base64编码格式的字符串
     * @throws IOException
     */
    public static String encodeBase64(String imageFilePath) throws IOException{
        return encodeBase64(new File(imageFilePath));
    }
    
    /**
     * 将文件编码成base64格式
     *
     * @param imageFile 图片文件对象
     * @return base64编码格式的字符串
     * @throws IOException
     */
    public static String encodeBase64(File imageFile) throws IOException{
        BufferedImage bi = ImageIO.read(imageFile);
        String type = FileKit.getFileExtension(imageFile).toLowerCase();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi,type, baos);
        return Base64Kit.encode(baos.toByteArray());
    }
}
