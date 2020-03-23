package cn.novelweb.tool.upload.fastdfs.pool;

import cn.novelweb.tool.upload.fastdfs.conn.Connection;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.net.InetSocketAddress;

/**
 * 定义FastDFS连接池对象<br/>
 * 定义了对象池要实现的功能,对一个地址进行池化Map Pool<br/>
 * <p>2020-02-03 17:22</p>
 *
 * @author LiZW
 **/
public class ConnectionPool extends GenericKeyedObjectPool<InetSocketAddress, Connection> {
    /**
     * 默认构造函数
     */
    public ConnectionPool(KeyedPooledObjectFactory<InetSocketAddress, Connection> factory, GenericKeyedObjectPoolConfig config) {
        super(factory, config);
    }

    /**
     * 默认构造函数
     */
    public ConnectionPool(KeyedPooledObjectFactory<InetSocketAddress, Connection> factory) {
        super(factory);
    }
}