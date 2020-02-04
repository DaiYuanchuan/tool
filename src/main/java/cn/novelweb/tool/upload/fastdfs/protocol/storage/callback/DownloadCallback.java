package cn.novelweb.tool.upload.fastdfs.protocol.storage.callback;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>文件下载回调接口</p>
 * <p>2020-02-03 16:48</p>
 *
 * @author LiZW
 **/
public interface DownloadCallback<T> {

    /**
     * 注意不能直接返回入参的InputStream，因为此方法返回后将关闭原输入流<br/>
     * 不能关闭inputStream? TODO 验证是否可以关闭
     *
     * @param inputStream 返回数据输入流
     * @return receive
     * @throws IOException IO异常
     */
    T receive(InputStream inputStream) throws IOException;

}
