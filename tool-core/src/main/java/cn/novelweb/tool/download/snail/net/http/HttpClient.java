package cn.novelweb.tool.download.snail.net.http;

import cn.novelweb.tool.download.snail.config.SystemConfig;
import cn.novelweb.tool.download.snail.context.exception.NetException;
import cn.novelweb.tool.download.snail.pojo.wrapper.HttpHeaderWrapper;
import cn.novelweb.tool.download.snail.utils.IoUtils;
import cn.novelweb.tool.download.snail.utils.MapUtils;
import cn.novelweb.tool.download.snail.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>HTTP客户端</p>
 * <p>配置参考：https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html</p>
 * <p>推荐直接使用HTTP协议下载：HTTPS下载CPU占用较高</p>
 * 
 * @author acgist
 */
public final class HttpClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
	
	/**
	 * <p>HTTP状态码</p>
	 * <p>协议链接：https://www.ietf.org/rfc/rfc2616</p>
	 * 
	 * @author acgist
	 */
	public enum StatusCode {
		
		/**
		 * <p>成功</p>
		 */
		OK(200),
		/**
		 * <p>部分内容</p>
		 * <p>断点续传</p>
		 */
		PARTIAL_CONTENT(206),
		/**
		 * <p>无法满足请求范围</p>
		 */
		REQUESTED_RANGE_NOT_SATISFIABLE(416),
		/**
		 * <p>服务器错误</p>
		 */
		INTERNAL_SERVER_ERROR(500);
		
		/**
		 * <p>状态码</p>
		 */
		private final int code;
		
		/**
		 * @param code 状态码
		 */
		private StatusCode(int code) {
			this.code = code;
		}
		
		/**
		 * <p>获取状态码</p>
		 * 
		 * @return 状态码
		 */
		public final int code() {
			return this.code;
		}
		
		/**
		 * <p>判断状态码是否相等</p>
		 * 
		 * @param code 状态码
		 * 
		 * @return 是否相等
		 */
		public final boolean verifyCode(int code) {
			return this.code == code;
		}
		
	}
	
	/**
	 * <p>请求方式</p>
	 * 
	 * @author acgist
	 */
	public enum Method {
		
		/**
		 * <p>GET请求</p>
		 */
		GET,
		/**
		 * <p>HEAD请求</p>
		 */
		HEAD,
		/**
		 * <p>POST请求</p>
		 */
		POST;
		
	}
	
	/**
	 * <p>HTTP客户端信息（User-Agent）</p>
	 */
	private static final String USER_AGENT;
	
	static {
		final StringBuilder userAgentBuilder = new StringBuilder();
		userAgentBuilder
			.append("Mozilla/5.0")
			.append(" ")
			.append("(compatible; ")
			.append(SystemConfig.getNameEn())
			.append("/")
			.append(SystemConfig.getVersion())
			.append("; +")
			.append(SystemConfig.getSupport())
			.append(")");
		USER_AGENT = userAgentBuilder.toString();
		LOGGER.debug("HTTP客户端信息（User-Agent）：{}", USER_AGENT);
		// 配置HTTPS
		final SSLContext sslContext = buildSSLContext();
		if(sslContext != null) {
			HttpsURLConnection.setDefaultHostnameVerifier(SnailHostnameVerifier.INSTANCE);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}
	}
	
	/**
	 * <p>请求地址</p>
	 */
	private final String url;
	/**
	 * <p>请求连接</p>
	 */
	private final HttpURLConnection httpURLConnection;
	/**
	 * <p>状态码</p>
	 */
	private int code;
	/**
	 * <p>响应头</p>
	 */
	private HttpHeaderWrapper httpHeaderWrapper;
	
	/**
	 * @param url 请求地址
	 * @param connectTimeout 连接超时时间（单位：毫秒）
	 * @param receiveTimeout 响应超时时间（单位：毫秒）
	 * 
	 * @throws NetException 网络异常
	 */
	private HttpClient(String url, int connectTimeout, int receiveTimeout) throws NetException {
		this.url = url;
		this.httpURLConnection = this.buildHttpURLConnection(connectTimeout, receiveTimeout);
		this.buildDefaultHeader();
	}
	
	/**
	 * <p>新建下载HTTP客户端</p>
	 * 
	 * @param url 请求地址
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public static final HttpClient newDownloader(String url) throws NetException {
		return newInstance(url, SystemConfig.CONNECT_TIMEOUT_MILLIS, SystemConfig.DOWNLOAD_TIMEOUT_MILLIS);
	}
	
	/**
	 * <p>新建HTTP客户端</p>
	 * 
	 * @param url 请求地址
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public static final HttpClient newInstance(String url) throws NetException {
		return newInstance(url, SystemConfig.CONNECT_TIMEOUT_MILLIS, SystemConfig.RECEIVE_TIMEOUT_MILLIS);
	}
	
	/**
	 * <p>新建HTTP客户端</p>
	 * 
	 * @param url 请求地址
	 * @param connectTimeout 连接超时时间（单位：毫秒）
	 * @param receiveTimeout 响应超时时间（单位：毫秒）
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public static final HttpClient newInstance(String url, int connectTimeout, int receiveTimeout) throws NetException {
		return new HttpClient(url, connectTimeout, receiveTimeout);
	}
	
	/**
	 * <p>使用缓存</p>
	 * 
	 * @return {@link HttpClient}
	 */
	public HttpClient cache() {
		this.httpURLConnection.setUseCaches(true);
		return this;
	}
	
	/**
	 * <p>设置请求头</p>
	 * 
	 * @param key 请求头名称
	 * @param value 请求头值
	 * 
	 * @return {@link HttpClient}
	 */
	public HttpClient header(String key, String value) {
		this.httpURLConnection.setRequestProperty(key, value);
		return this;
	}
	
	/**
	 * <p>启用长连接</p>
	 * <p>系统默认使用长连接</p>
	 * 
	 * @return {@link HttpClient}
	 */
	public HttpClient keepAlive() {
		return this.header("Connection", "keep-alive");
	}
	
	/**
	 * <p>禁用长连接</p>
	 * 
	 * @return {@link HttpClient}
	 */
	public HttpClient disableKeepAlive() {
		return this.header("Connection", "close");
	}
	
	/**
	 * <p>设置请求范围</p>
	 * 
	 * @param pos 开始位置
	 * 
	 * @return {@link HttpClient}
	 */
	public HttpClient range(long pos) {
		return this.header(HttpHeaderWrapper.HEADER_RANGE, "bytes=" + pos + "-");
	}
	
	/**
	 * <p>执行GET请求</p>
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpClient get() throws NetException {
		return this.execute(Method.GET, null);
	}
	
	/**
	 * <p>执行HEAD请求</p>
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpClient head() throws NetException {
		return this.execute(Method.HEAD, null);
	}
	
	/**
	 * <p>执行POST请求</p>
	 * 
	 * @param data 请求数据
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpClient post(String data) throws NetException {
		this.header(HttpHeaderWrapper.HEADER_CONTENT_TYPE, "application/json");
		return this.execute(Method.POST, data);
	}
	
	/**
	 * <p>执行POST表单请求</p>
	 * 
	 * @param data 请求数据
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpClient post(Map<String, String> data) throws NetException {
		// 设置表单请求
		this.header(HttpHeaderWrapper.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
		if(MapUtils.isEmpty(data)) {
			return this.execute(Method.POST, null);
		} else {
			// 请求表单数据
			final String body = data.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + UrlUtils.encode(entry.getValue()))
				.collect(Collectors.joining("&"));
			return this.execute(Method.POST, body);
		}
	}
	
	/**
	 * <p>执行请求</p>
	 * 
	 * @param method 请求方法
	 * @param body 请求数据
	 * 
	 * @return {@link HttpClient}
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpClient execute(Method method, String body) throws NetException {
		OutputStream output = null;
		try {
			// 设置请求方式
			this.httpURLConnection.setRequestMethod(method.name());
			if(method == Method.GET) {
				// 是否写出：GET不要写出
				this.httpURLConnection.setDoOutput(false);
			} else if(method == Method.HEAD) {
				// 是否写出：HEAD不要写出
				this.httpURLConnection.setDoOutput(false);
			} else if(method == Method.POST) {
				// 是否写出：POST需要写出
				this.httpURLConnection.setDoOutput(true);
			} else {
				throw new NetException("不支持的请求方式：" + method);
			}
			// 发起连接
			this.httpURLConnection.connect();
			// 发送请求参数
			if(body != null) {
				output = this.httpURLConnection.getOutputStream();
				output.write(body.getBytes());
			}
			// 设置状态码
			this.code = this.httpURLConnection.getResponseCode();
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			IoUtils.close(output);
		}
		return this;
	}
	
	/**
	 * <p>获取状态码</p>
	 * 
	 * @return 状态码
	 */
	public int code() {
		return this.code;
	}
	
	/**
	 * <p>判断状态码是否成功</p>
	 * 
	 * @return 是否成功
	 */
	public boolean ok() {
		return StatusCode.OK.verifyCode(this.code);
	}
	
	/**
	 * <p>判断状态码是否部分内容</p>
	 * 
	 * @return 是否部分内容
	 */
	public boolean partialContent() {
		return StatusCode.PARTIAL_CONTENT.verifyCode(this.code);
	}
	
	/**
	 * <p>判断状态码是否无法满足请求范围</p>
	 * 
	 * @return 是否无法满足请求范围
	 */
	public boolean requestedRangeNotSatisfiable() {
		return StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE.verifyCode(this.code);
	}
	
	/**
	 * <p>判断状态码是否服务器错误</p>
	 * 
	 * @return 是否服务器错误
	 */
	public boolean internalServerError() {
		return StatusCode.INTERNAL_SERVER_ERROR.verifyCode(this.code);
	}
	
	/**
	 * <p>判断是否可以下载</p>
	 * 
	 * @return 是否可以下载
	 * 
	 * @see #ok()
	 * @see #partialContent()
	 */
	public boolean downloadable() {
		return this.ok() || this.partialContent();
	}
	
	/**
	 * <p>获取响应数据流</p>
	 * <p>使用完成需要关闭（归还连接）：下次相同地址端口继续使用</p>
	 * 
	 * @return 响应数据流
	 * 
	 * @throws NetException 网络异常
	 */
	public InputStream response() throws NetException {
		try {
			return this.httpURLConnection.getInputStream();
		} catch (IOException e) {
			throw new NetException(e);
		}
	}

	/**
	 * <p>获取响应字节数组</p>
	 * 
	 * @return 响应字节数组
	 * 
	 * @throws NetException 网络异常
	 */
	public byte[] responseToBytes() throws NetException {
		final InputStream input = this.response();
		try {
			final int size = input.available();
			final byte[] bytes = new byte[size];
			final int length = input.read(bytes);
			if(length == size) {
				return bytes;
			} else {
				return Arrays.copyOf(bytes, length);
			}
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			IoUtils.close(input);
		}
	}
	
	/**
	 * <p>获取响应文本</p>
	 * 
	 * @return 响应文本
	 * 
	 * @throws NetException 网络异常
	 */
	public String responseToString() throws NetException {
		int length;
		final InputStream input = this.response();
		final byte[] bytes = new byte[SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH];
		final StringBuilder builder = new StringBuilder();
		try {
			while((length = input.read(bytes)) >= 0) {
				builder.append(new String(bytes, 0, length));
			}
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			IoUtils.close(input);
		}
		return builder.toString();
	}
	
	/**
	 * <p>获取响应头</p>
	 * 
	 * @return 响应头
	 */
	public HttpHeaderWrapper responseHeader() {
		if(this.httpHeaderWrapper == null) {
			this.httpHeaderWrapper = HttpHeaderWrapper.newInstance(this.httpURLConnection.getHeaderFields());
		}
		return this.httpHeaderWrapper;
	}
	
	/**
	 * <p>关闭连接</p>
	 * <p>管理连接和底层Socket：不能保持长连接</p>
	 * 
	 * @return {@link HttpClient}
	 */
	public HttpClient shutdown() {
		this.httpURLConnection.disconnect();
		return this;
	}
	
	/**
	 * <p>创建请求连接</p>
	 * 
	 * @param connectTimeout 连接超时时间（单位：毫秒）
	 * @param receiveTimeout 响应超时时间（单位：毫秒）
	 * 
	 * @return 请求连接
	 * 
	 * @throws NetException 网络异常
	 */
	private HttpURLConnection buildHttpURLConnection(int connectTimeout, int receiveTimeout) throws NetException {
		try {
			final URL requestUrl = new URL(this.url);
			final HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
			// 是否读取
			connection.setDoInput(true);
			// 是否缓存
			connection.setUseCaches(false);
			// 响应超时时间
			connection.setReadTimeout(receiveTimeout);
			// 连接超时时间
			connection.setConnectTimeout(connectTimeout);
			// 是否自动重定向
			connection.setInstanceFollowRedirects(true);
			return connection;
		} catch (IOException e) {
			throw new NetException(e);
		}
	}
	
	/**
	 * <p>设置默认请求头</p>
	 */
	private void buildDefaultHeader() {
		// 接收所有类型参数
		this.header("Accept", "*/*");
		// 设置客户端信息
		this.header(HttpHeaderWrapper.HEADER_USER_AGENT, USER_AGENT);
	}

	/**
	 * <p>新建SSLContext</p>
	 * 
	 * @return SSLContext
	 */
	private static final SSLContext buildSSLContext() {
		try {
			// SSL协议：SSL、SSLv2、SSLv3、TLS、TLSv1、TLSv1.1、TLSv1.2、TLSv1.3
			final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, new X509TrustManager[] { SnailTrustManager.INSTANCE }, SecureRandom.getInstanceStrong());
			return sslContext;
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			LOGGER.error("新建SSLContext异常", e);
		}
		try {
			return SSLContext.getDefault();
		} catch (NoSuchAlgorithmException ex) {
			LOGGER.error("新建SSLContext异常", ex);
		}
		return null;
	}

	/**
	 * <p>域名验证</p>
	 * 
	 * @author acgist
	 */
	public static class SnailHostnameVerifier implements HostnameVerifier {

		private static final SnailHostnameVerifier INSTANCE = new SnailHostnameVerifier();
		
		/**
		 * <p>禁止创建实例</p>
		 */
		private SnailHostnameVerifier() {
		}
		
		@Override
		public boolean verify(String requestHost, SSLSession remoteSslSession) {
			// 证书域名必须匹配
			return requestHost.equalsIgnoreCase(remoteSslSession.getPeerHost());
		}
		
	}
	
	/**
	 * <p>证书验证</p>
	 * 
	 * @author acgist
	 */
	public static class SnailTrustManager implements X509TrustManager {

		private static final SnailTrustManager INSTANCE = new SnailTrustManager();

		/**
		 * <p>禁止创建实例</p>
		 */
		private SnailTrustManager() {
		}
		
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			if(chain == null) {
				throw new CertificateException("证书验证失败");
			}
		}
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			if(chain == null) {
				throw new CertificateException("证书验证失败");
			}
		}
		
	}
	
}
