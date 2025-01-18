/**
 * Copyright (c) 2011-2025, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.plugin.activerecord;

import com.jfinal.log.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Transaction 支持新版本事务方法 transaction(...)，独立于原有事务方法 tx(...)
 */
public class Transaction<R> {

    static final Log log = Log.getLog(Transaction.class);

    boolean shouldRollback = false;

    List<Runnable> onAfterCommitList;       // 事务提交之后回调
    Function<Exception, R> onException;     // 异常产生之后回调

    /**
     * 回滚事务
     */
    public void rollback() {
        shouldRollback = true;
    }

    /**
     * 是否回滚事务
     */
    boolean shouldRollback() {
        return shouldRollback;
    }

    /**
     * 设置异常处理函数，函数返回值将成为 transaction(...) 的返回值
     */
    public void onException(Function<Exception, R> onException) {
        this.onException = onException;
    }

    Function<Exception, R> getOnException() {
        return onException;
    }

    Function<Exception, R> removeOnException() {
        Function<Exception, R> ret = onException;
        onException = null;
        return ret;
    }

    /**
     * 设置当前事务提交后的回调函数
     * 警告：回调发生异常不会向外抛出，如需处理异常情况需在回调中自行 try catch
     */
    public void onAfterCommit(Runnable onAfterCommit) {
        if (onAfterCommit != null) {
            if (onAfterCommitList == null) {
                onAfterCommitList = new ArrayList<>(3);
            }
            onAfterCommitList.add(onAfterCommit);
        }
    }

    /**
     * 事务提交后回调 onAfterCommit
     *
     * 注意，此回调不向外抛出异常
     * 1：调用方需要在 onAfterCommit 函数中自行 try catch 捕获异常进行适当处理
     * 2：此回调发生在事务提交之后，抛出异常无法回滚事务
     * 3：此回调异常不向外传播，保障事务提交成功后的主线流程不受影响
     * 4：此回调通常用于在事务提交后进行异步操作，例如更新缓存、发送通知等等
     */
    void executeOnAfterCommit() {
        if (onAfterCommitList != null) {
            for (int i = onAfterCommitList.size() - 1; i >= 0; i--) {
                try {
                    onAfterCommitList.get(i).run();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}


