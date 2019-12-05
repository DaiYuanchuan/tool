package cn.novelweb.tool.annotation.log.callback;

import cn.novelweb.tool.annotation.log.pojo.OpLogInfo;

/**
 * <p>操作日志处理完成后的回调接口</p>
 * <p>2019-12-06 00:04</p>
 *
 * @author Dai Yuanchuan
 **/
public interface OpLogCompletionHandler {

    /**
     * 操作日志信息处理完成后自动回调该接口
     * 此接口主要用于用户将日志信息存入MySQL、Redis等等
     *
     * @param opLogInfo 处理完成后的 操作日志实体信息
     */
    void complete(OpLogInfo opLogInfo);

}
