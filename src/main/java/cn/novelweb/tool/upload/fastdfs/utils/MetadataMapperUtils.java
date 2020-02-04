package cn.novelweb.tool.upload.fastdfs.utils;

import cn.novelweb.tool.upload.fastdfs.constant.OtherConstants;
import cn.novelweb.tool.upload.fastdfs.model.MateData;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>文件标签（元数据）映射对象</p>
 * <p>2020-02-03 15:36</p>
 *
 * @author LiZW
 **/
public class MetadataMapperUtils {

    private MetadataMapperUtils() {
    }

    /**
     * 将元数据映射为byte
     */
    public static byte[] toByte(Set<MateData> metadataSet, Charset charset) {
        if (null == metadataSet || metadataSet.isEmpty()) {
            return new byte[0];
        }
        StringBuilder sb = new StringBuilder(32 * metadataSet.size());
        for (MateData md : metadataSet) {
            sb.append(md.getName()).append(OtherConstants.DFS_FIELD_SEPARATOR).append(md.getValue());
            sb.append(OtherConstants.DFS_RECORD_SEPARATOR);
        }
        // 去除最后一个分隔符
        sb.delete(sb.length() - OtherConstants.DFS_RECORD_SEPARATOR.length(), sb.length());
        return sb.toString().getBytes(charset);
    }

    /**
     * 将byte映射为对象
     */
    public static Set<MateData> fromByte(byte[] content, Charset charset) {
        Set<MateData> mdSet = new HashSet<>();
        if (null == content) {
            return mdSet;
        }
        String metaBuff = new String(content, charset);
        String[] rows = metaBuff.split(OtherConstants.DFS_RECORD_SEPARATOR);
        for (String row : rows) {
            String[] cols = row.split(OtherConstants.DFS_FIELD_SEPARATOR, 2);
            MateData md = new MateData(cols[0]);
            if (cols.length == 2) {
                md.setValue(cols[1]);
            }
            mdSet.add(md);
        }
        return mdSet;
    }

}
