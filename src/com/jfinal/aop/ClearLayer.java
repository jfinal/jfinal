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

package com.jfinal.aop;

/**
 * ClearLayer indicates ClearIntercptor which layer of interceptor should be cleared.
 * The JFinal interceptor has 3 layers, there are Global, Controller and Action.
 */
public enum ClearLayer {
	
	/**
	 * clear the interceptor of upper layer
	 */
	UPPER,
	
	/**
	 * clear the interceptor of all layers
	 */
	ALL;
}


