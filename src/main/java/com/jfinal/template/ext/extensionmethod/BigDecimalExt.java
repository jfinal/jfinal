/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.template.ext.extensionmethod;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 针对 java.math.BigDecimal 的扩展方法
 *
 * 用法：
 * #if(value.toInt() == 123)
 */
public class BigDecimalExt {
    public Boolean toBoolean(BigDecimal self) {
        return self.compareTo(BigDecimal.ZERO) != 0;
    }

    public Integer toInt(BigDecimal self) {
        return self.intValue();
    }

    public Long toLong(BigDecimal self) {
        return self.longValue();
    }

    public Float toFloat(BigDecimal self) {
        return self.floatValue();
    }

    public Double toDouble(BigDecimal self) {
        return self.doubleValue();
    }

    public Short toShort(BigDecimal self) {
        return self.shortValue();
    }

    public Byte toByte(BigDecimal self) {
        return self.byteValue();
    }

    // BigDecimal.toBigInteger() 已存在
    // public BigInteger toBigInteger(BigDecimal self) {
    //     return self.toBigInteger();
    // }

    public BigDecimal toBigDecimal(BigDecimal self) {
        return self;
    }

    /**
     * 四舍五入
     * @param self BigDecimal 对象自身
     * @param newScale 设置返回值的小数位数
     */
    public BigDecimal halfUp(BigDecimal self, int newScale) {
        return self.setScale(newScale, RoundingMode.HALF_UP);
    }
}
