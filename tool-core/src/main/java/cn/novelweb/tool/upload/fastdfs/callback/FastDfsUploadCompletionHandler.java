package cn.novelweb.tool.upload.fastdfs.callback;

import cn.novelweb.tool.upload.fastdfs.model.StorePath;

/**
 * <p>FastDfs 异步上传完成后的回调接口</p>
 * <p>2020-02-03 22:39</p>
 *
 * @author Dai Yuanchuan
 **/
public interface FastDfsUploadCompletionHandler {

    /**
     * 异步上传完成后的回调方法
     *
     * @param storePath 存储文件的路径信息
     */
    void complete(StorePath storePath);

}
