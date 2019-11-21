package cn.novelweb.pojo.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>地区信息</p>
 * <p>2019-11-21 14:25</p>
 *
 * @author Dai Yuanchuan
 **/
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ApiModel(value = "地区、区域信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Region {

    @ApiModelProperty(value = "国家")
    private String country;

    @ApiModelProperty(value = "省份")
    private String province;

    @ApiModelProperty(value = "城市")
    private String city;

    @ApiModelProperty(value = "互联网服务提供商")
    private String isp;

}
