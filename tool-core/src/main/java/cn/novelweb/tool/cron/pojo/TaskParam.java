package cn.novelweb.tool.cron.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>执行定时任务时的各项参数</p>
 * <p>2020-03-27 11:07</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel(value = "执行任务参数")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskParam {

    /**
     * 如:cn.novelweb.tool.cron.pojo.TaskParam
     */
    @ApiModelProperty(value = "需要执行定时任务的类路径[包含类名]")
    private String classPath;

    @ApiModelProperty(value = "需要执行定时任务的方法名称")
    private String methodName;

    @ApiModelProperty(value = "需要执行任务的方法的中的参数，没有为空")
    private Object[] param;

    @ApiModelProperty(value = "cron表达式")
    private String cron;

    public void setParam(Object... param) {
        this.param = param;
    }

}
