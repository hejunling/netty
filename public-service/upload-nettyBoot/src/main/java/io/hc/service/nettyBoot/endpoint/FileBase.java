package io.hc.service.nettyBoot.endpoint;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * 文件工具类
 * 
 * @author hechuan
 *
 * @created 2017年5月12日
 *
 * @since UPLOAD-3.0.0
 */
public abstract class FileBase {

	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	/** 临时文件夹 */
	private final String tempDir;

	public FileBase(String tempDir) {
		this.tempDir = tempDir;
	}

	/**
	 * 解析请求路径成本地文件路径
	 *
	 * @param uri
	 *            请求路径
	 * @return 本地文件路径
	 */
	protected String sanitizeUri(String uri) {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}

		if (uri.isEmpty() || uri.charAt(0) != '/') {
			return null;
		}

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);

		// Simplistic dumb security check.
		// You will have to do something serious in the production environment.
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.charAt(0) == '.'
				|| uri.charAt(uri.length() - 1) == '.' || INSECURE_URI.matcher(uri).matches()) {
			return null;
		}

		// Convert to absolute path.
		return this.tempDir + File.separator + uri;
	}

}
