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

package com.jfinal.kit;

import com.jfinal.core.Const;
import com.jfinal.log.Log;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * sse 实现 chat gpt 打字机效果
 */
public class SseEmitter {

    private final HttpServletResponse response;
    private boolean complete;

    public SseEmitter(HttpServletResponse response) {
        this.response = response;
        this.complete = false;
        this.initResponse();
    }

    private void initResponse() {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding(Const.DEFAULT_ENCODING);
        response.setHeader("Content-Type", "text/event-stream; charset:utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
    }

    /**
     * sse 等待中
     * @param sleepMillis 睡觉毫秒时长
     */
    public void waiting(int sleepMillis) {
        if (sleepMillis <= 0) {
            sleepMillis = 100;
        }
        while (!complete) {
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Waiting finished");
    }

    /**
     * 响应 sse 自动转 json
     * @param data 推送给客户端的数据
     */
    public void sendMessage(Object data) {
        sendMessage(data == null ? null : JsonKit.toJson(data));
    }

    /**
     * 响应 sse 自动转 json
     * @param data        推送给客户端的文本
     * @param retryMillis 多长毫秒时间让客户端重试
     */
    public void sendMessage(Object data, int retryMillis) {
        sendMessage(data == null ? null : JsonKit.toJson(data), retryMillis);
    }

    /**
     * 响应 sse
     * @param data 推送给客户端的文本
     */
    public void sendMessage(String data) {
        sendMessage(data, 0);
    }

    /**
     * 响应 sse
     * @param data        推送给客户端的文本
     * @param retryMillis 多长毫秒时间让客户端重试
     */
    public void sendMessage(String data, int retryMillis) {
        try {
            PrintWriter writer = response.getWriter();
            String dataStr = retryMillis > 0 ? "retry: %s\ndata: %s\n\n" : "data: %s\n\n";
            if (data == null) {
                data = "";
            }
            writer.write(retryMillis > 0 ? String.format(dataStr, retryMillis, data) : String.format(dataStr, data));
            writer.flush();
        } catch (IOException e) {
            Log.getLog(getClass()).error(e.getMessage(), e);
        }
    }

    /**
     * 结束 sse
     */
    public void complete() {
        try (PrintWriter writer = response.getWriter()) {
            writer.write("event:complete\ndata:\n\n");
            writer.flush();
        } catch (IOException e) {
            Log.getLog(getClass()).error(e.getMessage(), e);
        } finally {
            complete = true;
        }
    }
}
