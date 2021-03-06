package cn.novelweb.tool.upload.local.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>上传文件需要用的基本参数(包含分片数据)</p>
 * 2019-10-29 22:23
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "上传文件需要用的基本参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadFileParam {

    @ApiModelProperty(value = "任务ID", required = true)
    private String id;

    @ApiModelProperty(value = "总分片数量", example = "0", required = true)
    private int chunks;

    @ApiModelProperty(value = "当前为第几块分片(第一个块是 0，注意不是从 1 开始的)", example = "0", required = true)
    private int chunk;

    @ApiModelProperty(value = "当前分片大小", example = "0", required = true)
    private long size = 0L;

    @ApiModelProperty(value = "当前文件名称", required = true)
    private String name;

    @ApiModelProperty(value = "当前文件的分片对象", required = true)
    private MultipartFile file;

    @ApiModelProperty(value = "当前文件的MD5,不是分片的", required = true)
    private String md5;

}
