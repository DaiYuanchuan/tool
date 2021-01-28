package cn.novelweb.tool.download.snail.net.upnp;

import cn.novelweb.tool.download.snail.format.XML;
import cn.novelweb.tool.download.snail.utils.StringUtils;

/**
 * <p>UPNP响应</p>
 * 
 * @author acgist
 */
public final class UpnpResponse {

	/**
	 * <p>外网IP地址</p>
	 * <pre>
	 * <xmp>
	 * <?xml version="1.0"?>
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	 * 	<s:Body>
	 * 		<u:GetExternalIPAddressResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
	 * 			<NewExternalIPAddress>114.114.114.114</NewExternalIPAddress>
	 * 		</u:GetExternalIPAddressResponse>
	 * 	</s:Body>
	 * </s:Envelope>
	 * </xmp>
	 * </pre>
	 * 
	 * @param body XML响应内容
	 * 
	 * @return 外网IP地址
	 */
	public static String parseGetExternalIPAddress(String body) {
		if(StringUtils.isEmpty(body)) {
			return null;
		}
		final XML xml = XML.load(body);
		return xml.elementValue("NewExternalIPAddress");
	}
	
	/**
	 * <p>端口映射信息</p>
	 * <pre>
	 * <xmp>
	 * <?xml version="1.0"?>
	 * <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
	 * 	<s:Body>
	 * 		<u:GetSpecificPortMappingEntryResponse xmlns:u="urn:schemas-upnp-org:service:WANIPConnection:1">
	 * 			<NewInternalPort>17888</NewInternalPort>
	 * 			<NewInternalClient>192.168.1.100</NewInternalClient>
	 * 			<NewEnabled>1</NewEnabled>
	 * 			<NewPortMappingDescription>Snail</NewPortMappingDescription>
	 * 			<NewLeaseDuration>0</NewLeaseDuration>
	 * 		</u:GetSpecificPortMappingEntryResponse>
	 * 	</s:Body>
	 * </s:Envelope>
	 * </xmp>
	 * </pre>
	 * 
	 * @param body XML响应内容
	 * 
	 * @return 端口映射的内网IP地址
	 */
	public static String parseGetSpecificPortMappingEntry(String body) {
		if(StringUtils.isEmpty(body)) {
			return null;
		}
		final XML xml = XML.load(body);
		return xml.elementValue("NewInternalClient");
	}

}
