package com.example.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 * 字符串操作辅助类
 * 
 * 
 */
public class StringUtils {

	
	private static final String timeFormat = "%02d:%02d:%02d";
	private static final String emptyTime = "00:00:00";

	private static final String tv2telFormat = "2123[0-9]{6}";
	private static final String mobileFormat = "1(3|5|6|8)[0-9]{9}";

	public static final String regexContact = "^#contact#([0-9]{10,12})#(.{0,30})#$";

	public static final String MIME_VIDEO = "video/.*"; // 视频MIME正则
	public static final String MIME_AUDIO = "audio/.*"; // 音频MIME正则
	public static final String MIME_IMAGE = "image/.*"; // 图片MIME正则

	public final static String SERVER = "10622162802";
	public final static String SERVER_PHONE = "10659200046001";
	public final static String SERVER_MONITOR = "10659200046002";
	public final static String SERVER_CANCEL = "10622162";
	public final static String SMS_PHONE = "S";
	public final static String SMS_MONITOR = "K";
	public final static String SMS_CANCEL_PHONE = "QXSPTH";
	public final static String SMS_CANCEL_MONITOR = "QXSPJK";

	/**
	 * 字符串加密，用于将密码写入配置文件
	 * 
	 * @param s
	 *            原字符串
	 * @param key
	 *            密钥
	 * @return 加密后的字符串
	 */
	public static String encrypt(String s, long key) {
		byte[] bytes = s.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] += key;
		}
		return new String(bytes);
	}

	/**
	 * 解密字符串，用于将配置文件中读出的密码还原
	 * 
	 * @param s
	 *            加密字符串
	 * @param key
	 *            密钥
	 * @return 原字符串
	 */
	public static String decrypt(String s, long key) {
		byte[] bytes = s.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] -= key;
		}
		return new String(bytes);
	}

	/**
	 * 字符串加密，用于将密码写入配置文件
	 * 
	 * @param s
	 *            原字符串
	 * @return 加密后的字符串
	 */
	public static String encrypt(String s) {
		return encrypt(s, 3810);
	}

	/**
	 * 解密字符串，用于将配置文件中读出的密码还原
	 * 
	 * @param s
	 *            加密字符串
	 * @return 原字符串
	 */
	public static String decrypt(String s) {
		return decrypt(s, 3810);
	}

	public static boolean tryCryptKey(String s, int key) {
		if (s == null)
			return true;
		return decrypt(encrypt(s, key), key).equals(s);
	}

	/**
	 * 安全地将字符串转换为整数，如果字符串不是整数则返回0
	 * 
	 * @param s
	 *            输入的字符串
	 * @param radix
	 *            进制(2-16)
	 * @return 转换后的整数
	 */
	public static int parseIntSafe(String s, int radix) {
		int i = 0;
		try {
			i = Integer.parseInt(s, radix);
		} catch (Exception e) {
			i = 0;
		}
		return i;
	}

	/**
	 * 安全地将字符串转换为整数，如果字符串不是整数则返回默认值
	 * 
	 * @param s
	 *            输入的字符串
	 * @param radix
	 *            进制(2-16)
	 * @param defaultVal
	 *            默认返回值
	 * @return 转换后的整数
	 */
	public static int parseIntSafe(String s, int radix, int defaultVal) {
		int i = 0;
		try {
			i = Integer.parseInt(s, radix);
		} catch (Exception e) {
			i = defaultVal;
		}
		return i;
	}

	/**
	 * 整数转为字符串
	 * 
	 * @param i
	 *            整数
	 * @param radix
	 *            进制(2-16)
	 * @param len
	 *            长度
	 * @return 转换结果
	 */
	public static String int2Str(int i, int radix, int len) {
		if (radix < 2 || radix > 16)
			return null;
		char[] arr = new char[len];
		Arrays.fill(arr, '0');
		int pos = len - 1;
		while (pos >= 0 && i != 0) {
			int temp = i % radix;
			temp = temp < 0 ? temp + radix : temp;
			arr[pos--] = (char) (temp < 10 ? temp + '0' : temp - 10 + 'a');
			i /= radix;
		}
		return new String(arr);
	}

	/**
	 * 安全地将字符串转换为长整数，如果字符串不是长整数则返回0
	 * 
	 * @param s
	 *            输入的字符串*
	 * @param radix
	 *            进制(2-16)
	 * @return 转换后的长整数
	 */
	public static long parseLongSafe(String s, int radix) {
		long i = 0;
		try {
			i = Long.parseLong(s, radix);
		} catch (Exception e) {
			i = 0;
		}
		return i;
	}

	/**
	 * 安全地将字符串转换为长整数，如果字符串不是长整数则返回默认值
	 * 
	 * @param s
	 *            输入的字符串
	 * @param radix
	 *            进制(2-16)
	 * @param defaultVal
	 *            默认返回值
	 * @return 转换后的长整数
	 */
	public static long parseLongSafe(String s, int radix, long defaultVal) {
		long i = 0;
		try {
			i = Long.parseLong(s, radix);
		} catch (Exception e) {
			i = defaultVal;
		}
		return i;
	}

	/**
	 * 长整数转为字符串
	 * 
	 * @param i
	 *            长整数
	 * @param radix
	 *            进制(2-16)
	 * @param len
	 *            长度
	 * @return 转换结果
	 */
	public static String long2Str(long i, int radix, int len) {
		if (radix < 2 || radix > 16)
			return null;
		char[] arr = new char[len];
		Arrays.fill(arr, '0');
		int pos = len - 1;
		while (pos >= 0 && i != 0) {
			long temp = i % radix;
			temp = temp < 0 ? temp + radix : temp;
			arr[pos--] = (char) (temp < 10 ? temp + '0' : temp - 10 + 'a');
			i /= radix;
		}
		return new String(arr);
	}

	/**
	 * 秒数转换为时分秒形式
	 * 
	 * @param sec
	 *            秒数
	 * @return 结果字符串，HH:MM:SS形式
	 */
	public static String secToHms(long sec) {
		if (sec <= 0)
			return emptyTime;
		long h = 0, m = 0, s = 0;
		if (sec >= 3600) {
			h = sec / 3600;
			sec -= h * 3600;
		}
		if (sec >= 60) {
			m = sec / 60;
			sec -= m * 60;
		}
		s = sec;
		return String.format(timeFormat, h, m, s);
	}

	/**
	 * 将DOM的xml转换为字符串
	 * 
	 * @param root
	 *            根节点
	 * @return 字符串
	 * @throws IOException
	 */
	public static String getStringFromNode(Node root) throws IOException {
		StringBuilder result = new StringBuilder();
		getStringFromNodeHelper(root, result);
		return result.toString();
	}

	private static String getStringFromNodeHelper(Node root,
			StringBuilder result) throws IOException {
		if (root.getNodeType() == 3)
			result.append(root.getNodeValue().replaceAll("\\s", ""));
		else {
			boolean hasChildNodes = root.hasChildNodes();
			if (root.getNodeType() != 9) {
				result.append("<").append(root.getNodeName());
				NamedNodeMap map = root.getAttributes();
				int len = map.getLength();
				for (int i = 0; i < len; ++i) {
					Node item = map.item(i);
					result.append(" ").append(item.getNodeName()).append("=\"")
							.append(item.getNodeValue()).append("\"");
				}
				if (hasChildNodes)
					result.append(">");
				else
					result.append(" />");
			} else {
				result.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			}
			if (hasChildNodes) {
				NodeList nodes = root.getChildNodes();
				int len = nodes.getLength();
				for (int i = 0; i < len; ++i) {
					Node node = nodes.item(i);
					getStringFromNodeHelper(node, result);
				}

				if (root.getNodeType() != 9) {
					result.append("</").append(root.getNodeName()).append(">");
				}
			}
		}
		return result.toString();
	}

	public static String getPathFromContentUri(Context context, Uri uri) {
		String path = null;
		if (uri != null) {
			String s = uri.toString();
			if (s.startsWith("file://")) {
				path = uri.getEncodedPath();
			} else {
				ContentResolver cr = context.getContentResolver();
				Cursor cursor = cr.query(uri, new String[] { "_data" }, null,
						null, null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						path = cursor.getString(0);
					}
					cursor.close();
				}
			}
		}
		return path;
	}

	public static String getFileExtention(String filename) {
		int index = filename.lastIndexOf('.');
		if (index < 0)
			return "";
		String ext = filename.substring(index + 1).toLowerCase();
		return ext;
	}

	public static String getFileExtention(File f) {
		return getFileExtention(f.getName());
	}

	public static boolean isValidTv2Tel(String s) {
		return s.matches(tv2telFormat);
	}

	public static boolean isValidMobile(String s) {
		return s.matches(mobileFormat);
	}

	public static String decryptCtValidNum(String validNum) {
		if (validNum.length() == 9)
			return new StringBuilder(validNum).deleteCharAt(8).deleteCharAt(4)
					.deleteCharAt(0).reverse().toString();
		if (validNum.length() == 6)
			return new StringBuilder().append(validNum.charAt(1))
					.append(validNum.charAt(3)).append(validNum.charAt(0))
					.append(validNum.charAt(5)).append(validNum.charAt(4))
					.append(validNum.charAt(2)).toString();
		return null;
	}

}
