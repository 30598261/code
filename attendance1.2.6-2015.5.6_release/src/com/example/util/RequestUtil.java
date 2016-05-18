package com.example.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class RequestUtil {

	static int timeoutConnection = 12000;
	static int timeoutSocket = 8000;
	static int DEFAULT_SOCKET_BUFFER_SIZE = 1024;
	static final String TAG = RequestUtil.class.getCanonicalName();
	static String CHARSET = "UTF-8";

	public static synchronized String doPost(String url, List<NameValuePair> params) {
		HttpPost httpRequest = new HttpPost(url);
		String strResult = null;
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = getHttpClient().execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {

				strResult = EntityUtils.toString(httpResponse.getEntity());
			} else {
				strResult = null;
			}
		} catch (Exception ex) {
			strResult = null;
		}
		return strResult;
	}

	public static synchronized String doPostTo(String url, List<NameValuePair> params) {
		/* 寤虹珛HTTPPost瀵硅薄 */
		HttpPost httpRequest = new HttpPost(url);
		String strResult = null;
		HttpClient httpClient = null;
		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			/* 娣诲姞璇锋眰鍙傛暟鍒拌姹傚锟� */
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			/* 鍙戯拷?璇锋眰骞剁瓑寰呭搷锟� */
			httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			/* 鑻ョ姸鎬佺爜锟�00 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 璇昏繑鍥炴暟锟� */
				strResult = EntityUtils.toString(httpResponse.getEntity());
			} else {
				strResult = null;
			}
		} catch (Exception ex) {
			strResult = null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return strResult;
	}

	/**
	 * 鍙栨暟鎹�
	 * 
	 * @param url
	 * @return
	 */
	public static String doGet(String url) {

		synchronized (url) {
//			System.out.println("璇锋眰锛� + url);
			String result = "";
			try {
				HttpGet httpGet = new HttpGet(url);
				HttpResponse response;
				response = new DefaultHttpClient().execute(httpGet);
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					result = EntityUtils.toString(entity, HTTP.UTF_8);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
	}	

	/**
	 * 浼犳暟鎹�
	 * 
	 * @param url
	 * @param txt
	 * @return
	 */
	static public String dopost(String url, String txt) {
		String result = null;
		BufferedReader reader = null;
		JSONObject jsonObject = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost();
			post.setURI(new URI(url));
			jsonObject = new JSONObject();
			jsonObject.put("name", txt);
			StringEntity entity = new StringEntity(jsonObject.toString());
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			StringBuffer strBuffer = new StringBuffer("");
			String line = null;
			while ((line = reader.readLine()) != null) {
				strBuffer.append(line);
			}
			result = strBuffer.toString();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					reader = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 浼犳枃浠�
	 * 
	 * @param url
	 * @param file
	 * @return
	 */
	public static Boolean uploadFile(String url, File file) {
		String BOUNDARY = UUID.randomUUID().toString(); // 杈圭晫鏍囪瘑 闅忔満鐢熸垚
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 鍐呭绫诲瀷
		String RequestURL = url;
		try {
			URL requesturl = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) requesturl
					.openConnection();
			conn.setReadTimeout(timeoutConnection);
			conn.setConnectTimeout(timeoutConnection);
			conn.setDoInput(true); // 鍏佽杈撳叆娴�
			conn.setDoOutput(true); // 鍏佽杈撳嚭娴�
			conn.setUseCaches(false); // 涓嶅厑璁镐娇鐢ㄧ紦瀛�
			conn.setRequestMethod("POST"); // 璇锋眰鏂瑰紡
			conn.setRequestProperty("Charset", CHARSET); // 璁剧疆缂栫爜
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			if (file != null) {
				/**
				 * 褰撴枃浠朵笉涓虹┖锛屾妸鏂囦欢鍖呰骞朵笖涓婁紶
				 */
				OutputStream outputSteam = conn.getOutputStream();

				DataOutputStream dos = new DataOutputStream(outputSteam);
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				/**
				 * 杩欓噷閲嶇偣娉ㄦ剰锛�name閲岄潰鐨勫�涓烘湇鍔″櫒绔渶瑕乲ey 鍙湁杩欎釜key 鎵嶅彲浠ュ緱鍒板搴旂殑鏂囦欢
				 * filename鏄枃浠剁殑鍚嶅瓧锛屽寘鍚悗缂�悕鐨�姣斿:abc.png
				 */

				sb.append("Content-Disposition: form-data; name=\"img\"; filename=\""
						+ file.getName() + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset="
						+ CHARSET + LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * 鑾峰彇鍝嶅簲鐮�200=鎴愬姛 褰撳搷搴旀垚鍔燂紝鑾峰彇鍝嶅簲鐨勬祦
				 */
				int res = conn.getResponseCode();
				if (res == 200) {
					return true;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static String doGetTo(String url, List<NameValuePair> params) {
		HttpGet httpRequest = new HttpGet(url);
		String strResult = null;
		HttpClient httpClient = null;
		try {
			HttpParams httpParameters = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

			HttpConnectionParams.setSoTimeout(httpParameters, timeoutConnection);

			httpClient = new DefaultHttpClient(httpParameters);
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				strResult = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strResult;
	}

	/**
	 * 闁俺绻冮幏鍏煎复閻ㄥ嫭鏌熷蹇旂�闁姾顕Ч鍌氬敶鐎圭櫢绱濈�鐐靛箛閸欏倹鏆熸导鐘虹翻娴犮儱寮烽弬鍥︽娴肩姾绶�
	 * 
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */

	public static String upload(String url, ArrayList<File> files) {
		String BOUNDARY = "------WebKitFormBoundary"; // 閺佺増宓侀崚鍡涙缁撅拷

		String endline = "--" + BOUNDARY + "--\r\n";// 閺佺増宓佺紒鎾存将閺嶅洤绻�
		StringBuilder sb = new StringBuilder();
		HttpURLConnection http = null;
		BufferedReader in = null;
		DataOutputStream out = null;
		try {
			URL u = new URL(url);
			http = (HttpURLConnection) u.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Charset", "UTF-8");
			http.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
			http.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

			// http.setConnectTimeout("ConnectTimeout");
			// http.setReadTimeout("ReadTimeout");
			http.connect();

			out = new DataOutputStream(http.getOutputStream());
			for (File file : files) {
				String fileBody = endline + "Content-Disposition: form-data;name=\"Filedata\";filename=\""
						+ file.getName() + "\"\r\n";
				fileBody += "Content-Type: image/jpeg\r\n\r\n";
				out.write(fileBody.getBytes());
				FileInputStream fis = new FileInputStream(file);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = fis.read(buffer, 0, 1024)) != -1) {
					out.write(buffer, 0, len);
				}
				fis.close();
				out.write("\r\n".getBytes());
			}
			String postBody = endline + "Content-Disposition: form-data;name=\"test\"\r\n\r\n11\r\n";
			out.write(postBody.getBytes());
			out.write(endline.getBytes());
			out.flush();
			out.close();
			in = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));// charset;
			String tmp;
			while ((tmp = in.readLine()) != null) {
				sb.append(tmp).append("\n");
			}

		} catch (Exception ex) {
			Log.d(TAG, String.format("Level.SEVERE, null, ex %s", ex.getMessage()));
		} finally {
			// close(http, in, out);

			try {
				if (in != null)
					in.close();

				if (http != null)
					http.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return sb.toString();
	}

	public static HttpClient httpClient = null;

	private static synchronized HttpClient getHttpClient() {
		if (httpClient == null) {
			final HttpParams httpParams = new BasicHttpParams();

			// timeout: get connections from connection pool
			ConnManagerParams.setTimeout(httpParams, 1000);
			// timeout: connect to the server
			HttpConnectionParams.setConnectionTimeout(httpParams, timeoutConnection);
			// timeout: transfer data from server
			HttpConnectionParams.setSoTimeout(httpParams, timeoutConnection);

			// // set max connections per host
			// ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new
			// ConnPerRouteBean(DEFAULT_HOST_CONNECTIONS));
			// // set max total connections
			// ConnManagerParams.setMaxTotalConnections(httpParams,
			// DEFAULT_MAX_CONNECTIONS);

			// use expect-continue handshake
			HttpProtocolParams.setUseExpectContinue(httpParams, true);
			// disable stale check
			HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);

			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

			HttpClientParams.setRedirecting(httpParams, false);

			// set user agent
			String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
			HttpProtocolParams.setUserAgent(httpParams, userAgent);

			// disable Nagle algorithm
			HttpConnectionParams.setTcpNoDelay(httpParams, true);

			HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

			// scheme: http and https
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

			ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
			httpClient = new DefaultHttpClient(manager, httpParams);
		}

		return httpClient;
	}
}
