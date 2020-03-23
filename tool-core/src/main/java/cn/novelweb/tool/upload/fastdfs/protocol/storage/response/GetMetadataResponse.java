package cn.novelweb.tool.upload.fastdfs.protocol.storage.response;

import cn.novelweb.tool.upload.fastdfs.model.MateData;
import cn.novelweb.tool.upload.fastdfs.protocol.BaseResponse;
import cn.novelweb.tool.upload.fastdfs.utils.MetadataMapperUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * <p></p>
 * <p>2020-02-03 17:01</p>
 *
 * @author Dai Yuanchuan
 **/
public class GetMetadataResponse extends BaseResponse<Set<MateData>> {

    /**
     * 解析反馈内容
     */
    @Override
    public Set<MateData> decodeContent(InputStream in, Charset charset) throws IOException {
        // 解析报文内容
        byte[] bytes = new byte[(int) getContentLength()];
        int contentSize = in.read(bytes);
        if (contentSize != getContentLength()) {
            throw new IOException("读取到的数据长度与协议长度不符");
        }
        return MetadataMapperUtils.fromByte(bytes, charset);
    }

}
