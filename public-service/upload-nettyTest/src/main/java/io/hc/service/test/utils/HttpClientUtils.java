package io.hc.service.test.utils;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.CharsetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.io.ByteStreams;

/**
 * http Client 请求工具类
 * 
 * @author hechuan
 *
 * @created 2017年4月17日
 *
 * @since UPLOAD-2.0.0
 */
public class HttpClientUtils {

	private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

	// HttpClient
	private static CloseableHttpClient httpClient = HttpClients.createDefault();

	private static int TIMEOUT = 1800000;

	private static RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(TIMEOUT)
			.setSocketTimeout(TIMEOUT).build();

	/**
	 * get方式调用http,返回byte[]
	 * 
	 * @param String
	 *            url
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] getBytes(String url) throws Exception {
		HttpGet request = null;
		try {
			// HTTP请求
			request = new HttpGet(url);
			request.setConfig(requestConfig);
			
			logger.debug(request.getRequestLine().toString());

			// 发送请求，返回响应
			HttpResponse response = httpClient.execute(request);
			// 响应成功
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("response.getStatusCode= {}",statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				return ByteStreams.toByteArray(stream);
			}
		} catch (Exception e) {
			throw new Exception("GET请求失败:[" + url + "]", e);
		} finally {
			if (request != null) {
				try {
					request.releaseConnection();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * get方式调用http,返回String
	 * 
	 * @param String
	 *            url
	 * @param String
	 *            charsetName
	 * @return String
	 * @throws Exception
	 */
	public static String getString(String url, String charsetName) throws Exception {
		byte[] bytes = getBytes(url);
		if (bytes == null || bytes.length <= 0) {
			return "";
		}
		return new String(bytes, charsetName);
	}

	/**
	 * post方式传递json调用http
	 * 
	 * @param String
	 *            url
	 * @param String
	 *            reqJson 请求的json数据
	 * @param String
	 *            charsetName 字符集
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] postJson2Bytes(String url, String reqJson, String charsetName) throws Exception {
		HttpPost request = null;
		try {
			// HTTP请求
			request = new HttpPost(url);
			request.setConfig(requestConfig);
			request.addHeader("Content-Type", "application/json;charset=" + charsetName);

			request.setEntity(new StringEntity(reqJson, charsetName));
			
			logger.debug(request.getRequestLine().toString());
			logger.debug("reqJson:" + reqJson);

			// 发送请求，返回响应
			HttpResponse response = httpClient.execute(request);

			// 响应成功
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("response.getStatusCode= {}",statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				byte[] bis = ByteStreams.toByteArray(stream);
				return bis;
			}
		} catch (Exception e) {
			throw new Exception("POST请求失败:[" + url + "][" + reqJson + "]", e);
		} finally {
			if (request != null) {
				try {
					request.releaseConnection();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * post方式传递json调用http
	 * 
	 * @param String
	 *            url
	 * @param String
	 *            reqJson 请求的json数据
	 * @param String
	 *            charsetName 字符集
	 * @return String
	 * @throws Exception
	 */
	public static String postJson2String(String url, String reqJson, String charsetName) throws Exception {
		byte[] bytes = postJson2Bytes(url, reqJson, charsetName);
		if (bytes == null || bytes.length <= 0) {
			return "";
		}
		return new String(bytes, charsetName);
	}

	/**
	 * post方式传递参数调用http
	 * 
	 * @param String
	 *            url
	 * @param Map<String,Object>
	 *            paramMap 请求的参数数据
	 * @param String
	 *            charsetName 字符集
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] postParam2Bytes(String url, Map<String, Object> paramMap, String charsetName)
			throws Exception {
		HttpPost request = null;
		try {
			// HTTP请求
			request = new HttpPost(url);
			request.setConfig(requestConfig);

			if (paramMap != null && paramMap.size() > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Entry<String, Object> entry : paramMap.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				request.setEntity(new UrlEncodedFormEntity(nvps, charsetName));
			}
			logger.debug(request.getRequestLine().toString());
			logger.debug("params:" + paramMap);

			// 发送请求，返回响应
			HttpResponse response = httpClient.execute(request);

			// 响应成功
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("response.getStatusCode= {}",statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				byte[] bis = ByteStreams.toByteArray(stream);
				return bis;
			}
		} catch (Exception e) {
			throw new Exception("POST请求失败:[" + url + "][" + paramMap + "]", e);
		} finally {
			if (request != null) {
				try {
					request.releaseConnection();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * post方式传递参数调用http
	 * 
	 * @param String
	 *            url
	 * @param Map<String,Object>
	 *            paramMap 请求的参数数据
	 * @param String
	 *            charsetName 字符集
	 * @return String
	 * @throws Exception
	 */
	public static String postParam2String(String url, Map<String, Object> paramMap, String charsetName)
			throws Exception {
		byte[] bytes = postParam2Bytes(url, paramMap, charsetName);
		if (bytes == null || bytes.length <= 0) {
			return "";
		}
		return new String(bytes, charsetName);
	}

	/**
	 * post方式传递可序列化对象调用http
	 * 
	 * @param String
	 *            url
	 * @param Serializable
	 *            obj 可序列化对象
	 * @param String
	 *            charsetName 字符集
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] postObject2Bytes(String url, Serializable obj, String charsetName) throws Exception {
		HttpPost request = null;
		try {
			// HTTP请求
			request = new HttpPost(url);
			request.setConfig(requestConfig);

			request.setEntity(new SerializableEntity(obj));
			logger.debug(request.getRequestLine().toString());

			// 发送请求，返回响应
			HttpResponse response = httpClient.execute(request);

			// 响应成功
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("response.getStatusCode= {}",statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				byte[] bis = ByteStreams.toByteArray(stream);
				return bis;
			}
		} catch (Exception e) {
			throw new Exception("POST请求失败:[" + url + "]", e);
		} finally {
			if (request != null) {
				try {
					request.releaseConnection();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * post方式传递可序列化对象调用http
	 * 
	 * @param String
	 *            url
	 * @param Serializable
	 *            obj 可序列化对象
	 * @param String
	 *            charsetName 字符集
	 * @return String
	 * @throws Exception
	 */
	public static String postObject2String(String url, Serializable obj, String charsetName) throws Exception {
		byte[] bytes = postObject2Bytes(url, obj, charsetName);
		if (bytes == null || bytes.length <= 0) {
			return "";
		}
		return new String(bytes, charsetName);
	}

	/**
	 * post方式传递文件调用http
	 *
	 * @param url
	 * @param file
	 * @param paramMap
	 * @param charsetName
	 * @return
	 * @throws Exception
	 */
	public static byte[] postFile2Bytes(String url, File file, Map<String, Object> paramMap, String charsetName)
			throws Exception {
		HttpPost request = null;
		try {
			// HTTP请求
			request = new HttpPost(url);
			request.setConfig(requestConfig);

			// 把文件转换成流对象FileBody
			FileBody bin = new FileBody(file);
			// 以浏览器兼容模式运行，防止文件名乱码。
			MultipartEntityBuilder reqEntityBuilder = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE).addPart("file", bin);

			// 设置请求参数
			if (paramMap != null && paramMap.size() > 0) {
				for (Entry<String, Object> entry : paramMap.entrySet()) {
					StringBody value = new StringBody(entry.getValue().toString(),
							ContentType.create("text/plain", charsetName));
					reqEntityBuilder.addPart(entry.getKey(), value);
				}
			}
			HttpEntity reqEntity = reqEntityBuilder.setCharset(CharsetUtils.get(charsetName)).build();
			request.setEntity(reqEntity);
			
			logger.debug(request.getRequestLine().toString());
			logger.debug("params:" + paramMap);

			// 发送请求，返回响应
			HttpResponse response = httpClient.execute(request);

			// 响应成功
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("response.getStatusCode= {}",statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				byte[] bis = ByteStreams.toByteArray(stream);
				return bis;
			} else {
				throw new Exception("POST请求失败:[" + url + "],statusCode:" + statusCode);
			}
		} catch (Exception e) {
			throw new Exception("POST请求失败:[" + url + "]", e);
		} finally {
			if (request != null) {
				try {
					request.releaseConnection();
				} catch (Exception e) {
				}
			}
		}
		// return null;
	}

	/**
	 * post方式传递文件调用http
	 *
	 * @param url
	 * @param file
	 * @param paramMap
	 * @param charsetName
	 * @return
	 * @throws Exception
	 */
	public static String postFile2String(String url, File file, Map<String, Object> paramMap, String charsetName)
			throws Exception {
		byte[] bytes = postFile2Bytes(url, file, paramMap, charsetName);
		if (bytes == null || bytes.length <= 0) {
			return "";
		}
		return new String(bytes, charsetName);
	}

	public static String getParam2String(String url, Map<String, Object> paramMap, String charsetName)
			throws Exception {
		byte[] bytes = getParam2Bytes(url, paramMap, charsetName);
		if (bytes == null || bytes.length <= 0) {
			return "";
		}
		return new String(bytes, charsetName);
	}

	public static byte[] getParam2Bytes(String url, Map<String, Object> paramMap, String charsetName) throws Exception {
		HttpGet request = null;
		String param = null;
		try {
			if (paramMap != null && paramMap.size() > 0) {
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (Entry<String, Object> entry : paramMap.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				}
				// 对参数编码
				param = URLEncodedUtils.format(nvps, "UTF-8");
			}
			// HTTP请求
			if (!StringUtils.isEmpty(param)) {
				url = url + "?" + param;
			}
			request = new HttpGet(url);
			request.setConfig(requestConfig);
			
			logger.debug(request.getRequestLine().toString());
			logger.debug("params:" + paramMap);

			// 发送请求，返回响应
			HttpResponse response = httpClient.execute(request);

			// 响应成功
			int statusCode = response.getStatusLine().getStatusCode();
			
			logger.debug("response.getStatusCode= {}",statusCode);
			
			if (statusCode == HttpStatus.SC_OK) {
				InputStream stream = response.getEntity().getContent();
				byte[] bis = ByteStreams.toByteArray(stream);
				return bis;
			}
		} catch (Exception e) {
			throw new Exception("get请求失败:[" + url + "][" + paramMap + "]", e);
		} finally {
			if (request != null) {
				try {
					request.releaseConnection();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
}
