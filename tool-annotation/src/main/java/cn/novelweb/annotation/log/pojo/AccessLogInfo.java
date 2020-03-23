package cn.novelweb.annotation.log.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * <p>访问日志实体信息</p>
 * <p>2019-12-03 22:15</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "访问信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessLogInfo {

    @ApiModelProperty(value = "当前访问的ip地址")
    private String ip;

    @ApiModelProperty(value = "ip地址的实际地理位置")
    private String location;

    @ApiModelProperty(value = "浏览器内核类型")
    private String browser;

    @ApiModelProperty(value = "浏览器内核版本")
    private String browserVersion;

    @ApiModelProperty(value = "浏览器的解析引擎类型")
    private String browserEngine;

    @ApiModelProperty(value = "浏览器的解析引擎版本")
    private String browserEngineVersion;

    @ApiModelProperty(value = "是否为移动平台")
    private Boolean isMobile;

    @ApiModelProperty(value = "操作系统类型")
    private String os;

    @ApiModelProperty(value = "操作平台类型")
    private String platform;

    @ApiModelProperty(value = "爬虫的类型(如果有)")
    private String spider;

    @ApiModelProperty(value = "访问的URL获取除去host部分的路径")
    private String requestUri;

    @ApiModelProperty(value = "访问出现错误时获取到的异常原因")
    private String errorCause;

    @ApiModelProperty(value = "访问出现错误时获取到的异常信息")
    private String errorMsg;

    @ApiModelProperty(value = "模块名称")
    private String title;

    @ApiModelProperty(value = "访问的状态(0:正常,1:不正常)", example = "0")
    private Integer status;

    @ApiModelProperty(value = "访问的时间")
    private Date createTime;

}
