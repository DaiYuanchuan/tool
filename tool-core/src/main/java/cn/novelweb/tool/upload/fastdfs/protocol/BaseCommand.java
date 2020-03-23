package cn.novelweb.tool.upload.fastdfs.protocol;

import cn.novelweb.tool.upload.fastdfs.conn.Connection;

/**
 * <p>FastDFS命令操执行接口</p>
 * <p>2020-02-03 16:37</p>
 *
 * @author Dai Yuanchuan
 **/
public interface BaseCommand<T> {

    /**
     * 执行FastDFS命令
     *
     * @param conn 连接池
     * @return execute
     */
    T execute(Connection conn);

}
