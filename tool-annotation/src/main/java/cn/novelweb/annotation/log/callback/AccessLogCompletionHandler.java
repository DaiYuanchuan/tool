package cn.novelweb.annotation.log.callback;

import cn.novelweb.annotation.log.pojo.AccessLogInfo;

/**
 * <p>系统访问日志处理完成后的回调接口</p>
 * <p>2019-12-04 00:47</p>
 *
 * @author Dai Yuanchuan
 **/
public interface AccessLogCompletionHandler {

    /**
     * 访问日志信息处理完成后自动回调该接口
     * 此接口主要用于用户将日志信息存入MySQL、Redis等等
     *
     * @param accessLogInfo 处理完成后的 访问日志实体信息
     */
    void complete(AccessLogInfo accessLogInfo);

}
