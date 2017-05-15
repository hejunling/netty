package io.hc.service.nettyTask.utils;

import com.google.common.base.Strings;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HmacSHA1加密工具类
 * 
 * @author hechuan
 *
 * @created 2017年5月11日
 *
 * @since UPLOAD-3.0.0
 */
public class HmacSha1Util {

	private static final String ALGORITHM = "HmacSHA1";

	public static byte[] sign(String data, String key, String charsetName)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {

		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(charsetName), ALGORITHM);
		Mac mac = Mac.getInstance(ALGORITHM);
		mac.init(signingKey);

		return mac.doFinal(data.getBytes(charsetName));
	}

	public static String signToString(String data, String key, String charsetName)
			throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {

		return Strings.nullToEmpty(Base64.encodeBase64String(sign(data, key, charsetName))).trim();
	}

}
