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

package com.jfinal.ext.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import com.jfinal.core.Controller;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.render.Render;

/**
 * CaptchaRender.本验证码实现已被 Deprecated 不建使用.
 * 建议使用新版本的 Controller.renderCaptcha() 既简单又美观，并且还提供了 
 * Controller.validateCaptcha(para)与 Validator.validateCaptcha(para)支持
 */
@Deprecated
public class CaptchaRender extends Render {
	
	private static final int WIDTH = 80, HEIGHT = 26;
	private static final String[] strArr = {"3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y"};
	
	private String captchaName;
	
	public CaptchaRender(String captchaName) {
		if (StrKit.isBlank(captchaName)) {
			throw new IllegalArgumentException("captchaName can not be blank");
		}
		this.captchaName = captchaName;
	}
	
	public void render() {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		String vCode = drawGraphic(image);
		vCode = vCode.toUpperCase();
		vCode = HashKit.md5(vCode);
		Cookie cookie = new Cookie(captchaName, vCode);
		cookie.setMaxAge(-1);
		cookie.setPath("/");
		// cookie.setHttpOnly(true);
		response.addCookie(cookie);
		response.setHeader("Pragma","no-cache");
        response.setHeader("Cache-Control","no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        
        ServletOutputStream sos = null;
        try {
			sos = response.getOutputStream();
			ImageIO.write(image, "jpeg",sos);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (sos != null) {
				try {sos.close();} catch (IOException e) {LogKit.logNothing(e);}
			}
		}
	}

	private String drawGraphic(BufferedImage image) {
		// 获取图形上下文
		Graphics g = image.createGraphics();
		// 生成随机类
		Random random = new Random();
		// 设定背景色
		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		// 设定字体
		g.setFont(new Font("Times New Roman", Font.PLAIN, 18));

		// 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
		g.setColor(getRandColor(160, 200));
		for (int i = 0; i < 155; i++) {
			int x = random.nextInt(WIDTH);
			int y = random.nextInt(HEIGHT);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}

		// 取随机产生的认证码(4位数字)
		String sRand = "";
		for (int i = 0; i < 4; i++) {
			String rand = String.valueOf(strArr[random.nextInt(strArr.length)]);
			sRand += rand;
			// 将认证码显示到图象中
			g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
			// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, 16 * i + 11, 19);
		}

		// 图象生效
		g.dispose();
		
		return sRand;
	}
	
	/*
	 * 给定范围获得随机颜色
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
	
	public static boolean validate(Controller controller, String userInputCaptcha, String captchaName) {
		if (StrKit.isBlank(userInputCaptcha)) {
			return false;
		}
		
		userInputCaptcha = userInputCaptcha.toUpperCase();
		userInputCaptcha = HashKit.md5(userInputCaptcha);
		boolean result = userInputCaptcha.equals(controller.getCookie(captchaName));
		if (result == true) {
			controller.removeCookie(captchaName);
		}
		return result;
	}
}


