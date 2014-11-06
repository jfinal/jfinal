/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.token;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import com.jfinal.core.Const;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

/**
 * TokenManager.
 */
public class TokenManager {
	
	private static ITokenCache tokenCache;
	private static Random random = new Random();
	
	private TokenManager() {
		
	}
	
	public static void init(ITokenCache tokenCache) {
		if (tokenCache == null)
			return;
		
		TokenManager.tokenCache = tokenCache;
		
		long halfTimeOut = Const.MIN_SECONDS_OF_TOKEN_TIME_OUT * 1000 / 2;	// Token最小过期时间的一半时间作为任务运行的间隔时间
		new Timer().schedule(new TimerTask() {public void run() {removeTimeOutToken();}},
							 halfTimeOut,
							 halfTimeOut);
	}
	
	/**
	 * Create Token.
	 * @param Controller
	 * @param tokenName token name
	 * @param secondsOfTimeOut seconds of time out, for ITokenCache only.
	 */
	public static void createToken(Controller controller, String tokenName, int secondsOfTimeOut) {
		if (tokenCache == null) {
			String tokenId = String.valueOf(random.nextLong());
			controller.setAttr(tokenName, tokenId);
			controller.setSessionAttr(tokenName, tokenId);
			createTokenHiddenField(controller, tokenName, tokenId);
		}
		else {
			createTokenUseTokenIdGenerator(controller, tokenName, secondsOfTimeOut);
		}
	}
	
	/**
	 * Use ${token!} in view for generate hidden input field.
	 */
	private static void createTokenHiddenField(Controller controller, String tokenName, String tokenId) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input type='hidden' name='").append(tokenName).append("' value='" + tokenId).append("' />");
		controller.setAttr("token", sb.toString());
	}
	
	private static void createTokenUseTokenIdGenerator(Controller controller, String tokenName, int secondsOfTimeOut) {
		if (secondsOfTimeOut < Const.MIN_SECONDS_OF_TOKEN_TIME_OUT)
			secondsOfTimeOut = Const.MIN_SECONDS_OF_TOKEN_TIME_OUT;
		
		String tokenId = null;
		Token token = null;
		int safeCounter = 8;
		do {
			if (safeCounter-- == 0)
				throw new RuntimeException("Can not create tokenId.");
			tokenId = String.valueOf(random.nextLong());
			token = new Token(tokenId, System.currentTimeMillis() + (secondsOfTimeOut * 1000));
		} while(tokenId == null || tokenCache.contains(token));
		
		controller.setAttr(tokenName, tokenId);
		tokenCache.put(token);
		createTokenHiddenField(controller, tokenName, tokenId);
	}
	
	/**
	 * Check token to prevent resubmit.
	 * @param tokenName the token name used in view's form
	 * @return true if token is correct
	 */
	public static synchronized boolean validateToken(Controller controller, String tokenName) {
		String clientTokenId = controller.getPara(tokenName);
		if (tokenCache == null) {
			String serverTokenId = controller.getSessionAttr(tokenName);
			controller.removeSessionAttr(tokenName);		// important!
			return StrKit.notBlank(clientTokenId) && clientTokenId.equals(serverTokenId);
		}
		else {
			Token token = new Token(clientTokenId);
			boolean result = tokenCache.contains(token); 
			tokenCache.remove(token);
			return result;
		}
	}
	
	private static void removeTimeOutToken() {
		List<Token> tokenInCache = tokenCache.getAll();
		if (tokenInCache == null)
			return;
		
		List<Token> timeOutTokens = new ArrayList<Token>();
		long currentTime = System.currentTimeMillis();
		// find and save all time out tokens
		for (Token token : tokenInCache)
			if (token.getExpirationTime() <=  currentTime)
				timeOutTokens.add(token);
		
		// remove all time out tokens
		for (Token token : timeOutTokens)
			tokenCache.remove(token);
	}
}





