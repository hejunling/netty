package io.hc.service.test.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

public class StreamUtils {
	

	public static void closeInputStream(InputStream inputStream) {

		if(inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			inputStream = null;
		}
	}

	public static void closeOutputStream(OutputStream outputStream) {

		if(outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			outputStream = null;
		}
	}

	public static void closeCipherInputStream(CipherInputStream cipherInputStream) {

		if(cipherInputStream != null) {
			try {
				cipherInputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			cipherInputStream = null;
		}
	}

	public static void closeBufferedInputStream(BufferedInputStream bufferedInputStream) {

		if(bufferedInputStream != null) {
			try {
				bufferedInputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			bufferedInputStream = null;
		}
	}

	public static void closeCipherOutputStream(CipherOutputStream cipherOutputStream) {

		if(cipherOutputStream != null) {
			try {
				cipherOutputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			cipherOutputStream = null;
		}
	}

	public static void closeBufferedOutputStream(BufferedOutputStream bufferedOutputStream) {

		if(bufferedOutputStream != null) {
			try {
				bufferedOutputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			bufferedOutputStream = null;
		}
	}

	public static void closeFileOutputStream(FileOutputStream fileOutputStream) {

		if(fileOutputStream != null) {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			fileOutputStream = null;
		}
	}

	public static void closeFileInputStream(FileInputStream fileInputStream) {

		if(fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			fileInputStream = null;
		}
	}

	public static void closeGZIPInputStream(GZIPInputStream gZIPInputStream) {

		if(gZIPInputStream != null) {
			try {
				gZIPInputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			gZIPInputStream = null;
		}
	}

	public static void closeZipOutputStream(ZipOutputStream zipOutputStream) {

		if(zipOutputStream != null) {
			try {
				zipOutputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			zipOutputStream = null;
		}
	}

	public static void closeWriter(Writer writer) {

		if(writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			writer = null;
		}
	}

	public static void flush(OutputStream outputStream) {

		try {
			outputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void flush(BufferedOutputStream bufferedOutputStream) {

		try {
			bufferedOutputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void flush(CipherOutputStream cipherOutputStream) {

		try {
			cipherOutputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void flush(FileOutputStream fileOutputStream) {

		try {
			fileOutputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
//	public static void flushBuffer(HttpServletResponse response) {
//
//		try {
//			response.flushBuffer();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}

}