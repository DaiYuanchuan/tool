package cn.novelweb.tool.upload.fastdfs.conn;

import cn.novelweb.tool.upload.fastdfs.protocol.storage.AbstractStorageCommand;
import cn.novelweb.tool.upload.fastdfs.protocol.tracker.AbstractTrackerCommand;

import java.net.InetSocketAddress;

/**
 * <p>FastDFS命令执行器</p>
 * <p>2020-02-03 16:38</p>
 *
 * @author LiZW
 **/
public interface CommandExecutor {

    /**
     * 在Tracker Server上执行命令
     *
     * @param command Tracker Server命令
     * @param <T>     返回数据类型
     * @return 返回数据
     */
    <T> T execute(AbstractTrackerCommand<T> command);

    /**
     * 在Storage Server上执行命令
     *
     * @param address Storage Server地址
     * @param command Storage Server命令
     * @param <T>     返回数据类型
     * @return 返回数据
     */
    <T> T execute(InetSocketAddress address, AbstractStorageCommand<T> command);

}
