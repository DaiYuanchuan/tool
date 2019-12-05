package cn.novelweb.tool.annotation.log.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>操作日志实体信息</p>
 * <p>2019-12-05 22:39</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@ApiModel(value = "操作信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpLogInfo extends AccessLogInfo{

    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @ApiModelProperty(value = "执行操作的类名称")
    private String className;

    @ApiModelProperty(value = "执行操作的方法名称")
    private String methodName;

    @ApiModelProperty(value = "url参数")
    private String parameter;
}
