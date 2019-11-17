package cn.novelweb.pojo.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>上传的文件信息</p>
 * 2019-10-28 14:55
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "上传的文件存储信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Files {

    @ApiModelProperty(value = "文件的哈希值,或者MD5值", required = true)
    private String hash;

    @ApiModelProperty(value = "文件的名称", required = true)
    private String name;

    @ApiModelProperty(value = "文件类型", required = true)
    private String type;

    @ApiModelProperty(value = "文件上传路径", required = true)
    private String path;

    @ApiModelProperty(value = "文件创建时间", example = "0", required = true)
    private long createTime;

}
