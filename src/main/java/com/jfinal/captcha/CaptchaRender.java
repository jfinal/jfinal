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

package com.jfinal.captcha;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;
import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

/**
 * CaptchaRender.
 */
public class CaptchaRender extends Render {
	
	protected static String captchaName = "_jfinal_captcha";
	protected static Random random = new Random(System.nanoTime());
	
	// 默认的验证码大小
	protected static final int WIDTH = 108, HEIGHT = 40;
	// 验证码随机字符数组
	protected static final char[] charArray = "3456789ABCDEFGHJKMNPQRSTUVWXY".toCharArray();
	// 验证码字体
	protected static final Font[] RANDOM_FONT = new Font[] {
		new Font("nyala", Font.BOLD, 38),
		new Font("Arial", Font.BOLD, 32),
		new Font("Bell MT", Font.BOLD, 32),
		new Font("Credit valley", Font.BOLD, 34),
		new Font("Impact", Font.BOLD, 32),
		new Font(Font.MONOSPACED, Font.BOLD, 40)
	};
	
	/**
	 * 设置 captchaName
	 */
	public static void setCaptchaName(String captchaName) {
		if (StrKit.isBlank(captchaName)) {
			throw new IllegalArgumentException("captchaName can not be blank.");
		}
		CaptchaRender.captchaName = captchaName;
	}
	
	/**
	 * 生成验证码
	 */
	public void render() {
		Captcha captcha = createCaptcha();
		CaptchaManager.me().getCaptchaCache().put(captcha);
		
		Cookie cookie = new Cookie(captchaName, captcha.getKey());
		cookie.setMaxAge(-1);
		cookie.setPath("/");
		response.addCookie(cookie);
		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		
		ServletOutputStream sos = null;
		try {
			BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			drawGraphic(captcha.getValue(), image);
			
			sos = response.getOutputStream();
			ImageIO.write(image, "jpeg", sos);
		} catch (IOException e) {
			if (getDevMode()) {
				throw new RenderException(e);
			}
		} catch (Exception e) {
			throw new RenderException(e);
		} finally {
			if (sos != null) {
				try {sos.close();} catch (IOException e) {LogKit.logNothing(e);}
			}
		}
	}
	
	protected Captcha createCaptcha() {
		String captchaKey = getCaptchaKeyFromCookie();
		if (StrKit.isBlank(captchaKey)) {
			captchaKey = StrKit.getRandomUUID();
		}
		return new Captcha(captchaKey, getRandomString(), Captcha.DEFAULT_EXPIRE_TIME);
	}
	
	protected String getCaptchaKeyFromCookie() {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(captchaName)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	
	protected String getRandomString() {
		char[] randomChars = new char[4];
		for (int i=0; i<randomChars.length; i++) {
			randomChars[i] = charArray[random.nextInt(charArray.length)];
		}
		return String.valueOf(randomChars);
	}
	
	protected void drawGraphic(String randomString, BufferedImage image){
		// 获取图形上下文
		Graphics2D g = image.createGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		// 图形抗锯齿
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// 字体抗锯齿
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// 设定背景色
		g.setColor(getRandColor(210, 250));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		//绘制小字符背景
		Color color = null;
		for(int i = 0; i < 20; i++){
			color = getRandColor(120, 200);
			g.setColor(color);
			String rand = String.valueOf(charArray[random.nextInt(charArray.length)]);
			g.drawString(rand, random.nextInt(WIDTH), random.nextInt(HEIGHT));
			color = null;
		}
		
		//设定字体
		g.setFont(RANDOM_FONT[random.nextInt(RANDOM_FONT.length)]);
		// 绘制验证码
		for (int i = 0; i < randomString.length(); i++){
			//旋转度数 最好小于45度
			int degree = random.nextInt(28);
			if (i % 2 == 0) {
				degree = degree * (-1);
			}
			//定义坐标
			int x = 22 * i, y = 21;
			//旋转区域
			g.rotate(Math.toRadians(degree), x, y);
			//设定字体颜色
			color = getRandColor(20, 130);
			g.setColor(color);
			//将认证码显示到图象中
			g.drawString(String.valueOf(randomString.charAt(i)), x + 8, y + 10);
			//旋转之后，必须旋转回来
			g.rotate(-Math.toRadians(degree), x, y);
		}
		//图片中间曲线，使用上面缓存的color
		g.setColor(color);
		//width是线宽,float型
		BasicStroke bs = new BasicStroke(3);
		g.setStroke(bs);
		//画出曲线
		QuadCurve2D.Double curve = new QuadCurve2D.Double(0d, random.nextInt(HEIGHT - 8) + 4, WIDTH / 2, HEIGHT / 2, WIDTH, random.nextInt(HEIGHT - 8) + 4);
		g.draw(curve);
		// 销毁图像
		g.dispose();
	}
	
	/*
	 * 给定范围获得随机颜色
	 */
	protected Color getRandColor(int fc, int bc) {
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
	
	/**
	 * 校验用户输入的验证码是否正确
	 * @param controller 控制器
	 * @param userInputString 用户输入的字符串
	 * @return 验证通过返回 true, 否则返回 false
	 */
	public static boolean validate(Controller controller, String userInputString) {
		String captchaKey = controller.getCookie(captchaName);
		if (validate(captchaKey, userInputString)) {
			controller.removeCookie(captchaName);
			return true;
		}
		return false;
	}
	
	/**
	 * 校验用户输入的验证码是否正确
	 * @param captchaKey 验证码 key，在不支持 cookie 的情况下可通过传参给服务端
	 * @param userInputString 用户输入的字符串
	 * @return 验证通过返回 true, 否则返回 false
	 */
	public static boolean validate(String captchaKey, String userInputString) {
		ICaptchaCache captchaCache = CaptchaManager.me().getCaptchaCache();
		Captcha captcha = captchaCache.get(captchaKey);
		if (captcha != null && captcha.notExpired() && captcha.getValue().equalsIgnoreCase(userInputString)) {
			captchaCache.remove(captcha.getKey());
			return true;
		}
		return false;
	}
}







