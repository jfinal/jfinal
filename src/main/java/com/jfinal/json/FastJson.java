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

package com.jfinal.json;

import java.lang.reflect.Type;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.jfinal.plugin.activerecord.Record;

/**
 * Json 转换 fastjson2 实现.
 */
public class FastJson extends Json {

    private static final JSONWriter.Feature REFERENCE_DETECTION = JSONWriter.Feature.ReferenceDetection;

    static {
        // 支持序列化 ActiveRecord 的 Record 类型
        JSON.register(Record.class, new FastJsonRecordSerializer());
    }

    public static FastJson getJson() {
        return new FastJson();
    }

    public String toJson(Object object) {
        // 优先使用对象级的属性 datePattern, 然后才是全局性的 defaultDatePattern
        String dp = datePattern != null ? datePattern : getDefaultDatePattern();
        if (dp == null) {
            return JSON.toJSONString(object, REFERENCE_DETECTION);
        } else {
            return JSON.toJSONString(object, dp, REFERENCE_DETECTION);
        }
    }

    /**
     * 支持传入更多 JSONWriter.Feature
     *
     * 例如：
     *    JSONWriter.Feature.WriteMapNullValue 支持对 null 值字段的转换
     */
    public String toJson(Object object, JSONWriter.Feature... features) {
        features = addReferenceDetection(features);
        String dp = datePattern != null ? datePattern : getDefaultDatePattern();
        if (dp == null) {
            return JSON.toJSONString(object, features);
        } else {
            return JSON.toJSONString(object, dp, features);
        }
    }

    public <T> T parse(String jsonString, Class<T> type) {
        return JSON.parseObject(jsonString, type, JSONReader.Feature.SupportSmartMatch);
    }

    private static JSONWriter.Feature[] addReferenceDetection(JSONWriter.Feature[] features) {
        int len = features != null ? features.length : 0;
        JSONWriter.Feature[] ret = new JSONWriter.Feature[len + 1];
        ret[0] = REFERENCE_DETECTION;
        if (len > 0) {
            System.arraycopy(features, 0, ret, 1, len);
        }
        return ret;
    }

    public static void addSerializer(Type type, ObjectWriter<?> value) {
        JSON.register(type, value);
    }
}

