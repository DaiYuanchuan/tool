package cn.novelweb.tool.upload.qcloud.callback;

import com.qcloud.cos.model.COSObjectSummary;

import java.util.List;

/**
 * <p>查询对象列表完成后的回调接口</p>
 * <p>2019-12-06 16:37</p>
 *
 * @author Dai Yuanchuan
 **/
public interface ListObjectCompletionHandler {

    /**
     * 查询对象列表处理完成后回调该接口
     *
     * @param commonPrefixes     被delimiter截断的路径,如delimiter设置为/,common prefix则表示所有子目录的路径
     * @param cosObjectSummaries object summary表示所有列出的object列表
     */
    void complete(List<String> commonPrefixes, List<COSObjectSummary> cosObjectSummaries);

}
