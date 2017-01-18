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

package com.jfinal.validate;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;

/**
 * Validator.
 */
public abstract class Validator implements Interceptor {
	
	protected Controller controller;
	protected Invocation invocation;
	protected boolean shortCircuit = false;
	protected boolean invalid = false;
	protected String datePattern = null;
	
	// TODO set the DEFAULT_DATE_PATTERN in Const and config it in Constants. TypeConverter do the same thing.
	protected static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	protected static final String emailAddressPattern = "\\b(^['_A-Za-z0-9-]+(\\.['_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";
	
	protected void setShortCircuit(boolean shortCircuit) {
		this.shortCircuit = shortCircuit;
	}
	
	protected void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
	
	protected String getDatePattern() {
		return (datePattern != null ? datePattern : DEFAULT_DATE_PATTERN);
	}
	
	final public void intercept(Invocation invocation) {
		Validator validator = null;
		try {
			validator = getClass().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		validator.controller = invocation.getController();
		validator.invocation = invocation;
		
		try {
			validator.validate(validator.controller);
		} catch (ValidateException e) {
			// should not be throw, short circuit validate need this
			LogKit.logNothing(e);
		}
		
		if (validator.invalid) {
			validator.handleError(validator.controller);
		} else {
			invocation.invoke();
		}
	}
	
	/**
	 * Use validateXxx method to validate the parameters of this action.
	 */
	protected abstract void validate(Controller c);
	
	/**
	 * Handle the validate error.
	 * Example:<br>
	 * controller.keepPara();<br>
	 * controller.render("register.html");
	 */
	protected abstract void handleError(Controller c);
	
	/**
	 * Add message when validate failure.
	 */
	protected void addError(String errorKey, String errorMessage) {
		invalid = true;
		controller.setAttr(errorKey, errorMessage);
		if (shortCircuit) {
			throw new ValidateException();
		}
	}
	
	/**
	 * Return the controller of this action.
	 */
	protected Controller getController() {
		return controller;
	}
	
	/**
	 * Return the action key of this action.
	 */
	protected String getActionKey() {
		return invocation.getActionKey();
	}
	
	/**
	 * Return the controller key of this action.
	 */
	protected String getControllerKey() {
		return invocation.getControllerKey();
	}
	
	/**
	 * Return the method of this action.
	 */
	protected Method getActionMethod() {
		return invocation.getMethod();
	}
	
	/**
	 * Return the method name of this action.
	 */
	protected String getActionMethodName() {
		return invocation.getMethodName();
	}
	
	/**
	 * Return view path of this controller.
	 */
	protected String getViewPath() {
		return invocation.getViewPath();
	}
	
	/**
	 * Validate Required. Allow space characters.
	 */
	protected void validateRequired(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (value == null || "".equals(value)) {	// 经测试,form表单域无输入时值为"",跳格键值为"\t",输入空格则为空格" "
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate Required for urlPara.
	 */
	protected void validateRequired(int index, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value == null /* || "".equals(value) */) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate required string.
	 */
	protected void validateRequiredString(String field, String errorKey, String errorMessage) {
		if (StrKit.isBlank(controller.getPara(field))) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate required string for urlPara.
	 */
	protected void validateRequiredString(int index, String errorKey, String errorMessage) {
		if (StrKit.isBlank(controller.getPara(index))) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate integer.
	 */
	protected void validateInteger(String field, int min, int max, String errorKey, String errorMessage) {
		validateIntegerValue(controller.getPara(field), min, max, errorKey, errorMessage);
	}
	
	/**
	 * Validate integer for urlPara.
	 */
	protected void validateInteger(int index, int min, int max, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n"))) {
			value = "-" + value.substring(1);
		}
		validateIntegerValue(value, min, max, errorKey, errorMessage);
	}
	
	private void validateIntegerValue(String value, int min, int max, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			int temp = Integer.parseInt(value.trim());
			if (temp < min || temp > max) {
				addError(errorKey, errorMessage);
			}
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate integer.
	 */
	protected void validateInteger(String field, String errorKey, String errorMessage) {
		validateIntegerValue(controller.getPara(field), errorKey, errorMessage);
	}
	
	/**
	 * Validate integer for urlPara.
	 */
	protected void validateInteger(int index, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n"))) {
			value = "-" + value.substring(1);
		}
		validateIntegerValue(value, errorKey, errorMessage);
	}
	
	private void validateIntegerValue(String value, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Integer.parseInt(value.trim());
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate long.
	 */
	protected void validateLong(String field, long min, long max, String errorKey, String errorMessage) {
		validateLongValue(controller.getPara(field), min, max, errorKey, errorMessage);
	}
	
	/**
	 * Validate long for urlPara.
	 */
	protected void validateLong(int index, long min, long max, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n"))) {
			value = "-" + value.substring(1);
		}
		validateLongValue(value, min, max, errorKey, errorMessage);
	}
	
	private void validateLongValue(String value, long min, long max, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			long temp = Long.parseLong(value.trim());
			if (temp < min || temp > max) {
				addError(errorKey, errorMessage);
			}
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate long.
	 */
	protected void validateLong(String field, String errorKey, String errorMessage) {
		validateLongValue(controller.getPara(field), errorKey, errorMessage);
	}
	
	/**
	 * Validate long for urlPara.
	 */
	protected void validateLong(int index, String errorKey, String errorMessage) {
		String value = controller.getPara(index);
		if (value != null && (value.startsWith("N") || value.startsWith("n"))) {
			value = "-" + value.substring(1);
		}
		validateLongValue(value, errorKey, errorMessage);
	}
	
	private void validateLongValue(String value, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Long.parseLong(value.trim());
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate double.
	 */
	protected void validateDouble(String field, double min, double max, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			double temp = Double.parseDouble(value.trim());
			if (temp < min || temp > max) {
				addError(errorKey, errorMessage);
			}
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate double.
	 */
	protected void validateDouble(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Double.parseDouble(value.trim());
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate date. Date formate: yyyy-MM-dd
	 */
	protected void validateDate(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			new SimpleDateFormat(getDatePattern()).parse(value.trim());	// Date temp = Date.valueOf(value); 为了兼容 64位 JDK
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate date.
	 */
	protected void validateDate(String field, Date min, Date max, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Date temp = new SimpleDateFormat(getDatePattern()).parse(value.trim());	// Date temp = Date.valueOf(value); 为了兼容 64位 JDK
			if (temp.before(min) || temp.after(max)) {
				addError(errorKey, errorMessage);
			}
		}
		catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate date. Date formate: yyyy-MM-dd
	 */
	protected void validateDate(String field, String min, String max, String errorKey, String errorMessage) {
		// validateDate(field, Date.valueOf(min), Date.valueOf(max), errorKey, errorMessage);  为了兼容 64位 JDK
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(getDatePattern());
			validateDate(field, sdf.parse(min.trim()), sdf.parse(max.trim()), errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate equal field. Usually validate password and password again
	 */
	protected void validateEqualField(String field_1, String field_2, String errorKey, String errorMessage) {
		String value_1 = controller.getPara(field_1);
		String value_2 = controller.getPara(field_2);
		if (value_1 == null || value_2 == null || (! value_1.equals(value_2))) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate equal string.
	 */
	protected void validateEqualString(String s1, String s2, String errorKey, String errorMessage) {
		if (s1 == null || s2 == null || (! s1.equals(s2))) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate equal integer.
	 */
	protected void validateEqualInteger(Integer i1, Integer i2, String errorKey, String errorMessage) {
		if (i1 == null || i2 == null || (i1.intValue() != i2.intValue())) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate email.
	 */
	protected void validateEmail(String field, String errorKey, String errorMessage) {
		validateRegex(field, emailAddressPattern, false, errorKey, errorMessage);
	}
	
	/**
	 * Validate URL.
	 */
	protected void validateUrl(String field, String errorKey, String errorMessage) {
		String value = controller.getPara(field);
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			value = value.trim();
			if (value.startsWith("https://")) {
				value = "http://" + value.substring(8); // URL doesn't understand the https protocol, hack it
			}
			new URL(value);
		} catch (MalformedURLException e) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate regular expression.
	 */
	protected void validateRegex(String field, String regExpression, boolean isCaseSensitive, String errorKey, String errorMessage) {
        String value = controller.getPara(field);
        if (value == null) {
        	addError(errorKey, errorMessage);
        	return ;
        }
        Pattern pattern = isCaseSensitive ? Pattern.compile(regExpression) : Pattern.compile(regExpression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
        	addError(errorKey, errorMessage);
        }
	}
	
	/**
	 * Validate regular expression and case sensitive.
	 */
	protected void validateRegex(String field, String regExpression, String errorKey, String errorMessage) {
		validateRegex(field, regExpression, true, errorKey, errorMessage);
	}
	
	/**
	 * Validate string.
	 */
	protected void validateString(String field, int minLen, int maxLen, String errorKey, String errorMessage) {
		validateStringValue(controller.getPara(field), minLen, maxLen, errorKey, errorMessage);
	}
	
	/**
	 * Validate string for urlPara
	 */
	protected void validateString(int index, int minLen, int maxLen, String errorKey, String errorMessage) {
		validateStringValue(controller.getPara(index), minLen, maxLen, errorKey, errorMessage);
	}
	
	private void validateStringValue(String value, int minLen, int maxLen, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		if (value.length() < minLen || value.length() > maxLen) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate token created by Controller.createToken(String).
	 */
	protected void validateToken(String tokenName, String errorKey, String errorMessage) {
		if (controller.validateToken(tokenName) == false) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * Validate token created by Controller.createToken().
	 */
	protected void validateToken(String errorKey, String errorMessage) {
		if (controller.validateToken() == false) {
			addError(errorKey, errorMessage);
		}
	}
	
	/**
	 * validate boolean.
	 */
	protected void validateBoolean(String field, String errorKey, String errorMessage) {
		validateBooleanValue(controller.getPara(field), errorKey, errorMessage);
	}
	
	/**
	 * validate boolean for urlPara.
	 */
	protected void validateBoolean(int index, String errorKey, String errorMessage) {
		validateBooleanValue(controller.getPara(index), errorKey, errorMessage);
	}
	
	private void validateBooleanValue(String value, String errorKey, String errorMessage) {
		if (StrKit.isBlank(value)) {
			addError(errorKey, errorMessage);
			return ;
		}
		value = value.trim().toLowerCase();
		if ("1".equals(value) || "true".equals(value)) {
			return ;
		} else if ("0".equals(value) || "false".equals(value)) {
			return ;
		}
		addError(errorKey, errorMessage);
	}
	
	protected void validateCaptcha(String field, String errorKey, String errorMessage) {
		if (getController().validateCaptcha(field) == false) {
			addError(errorKey, errorMessage);
		}
	}
}



