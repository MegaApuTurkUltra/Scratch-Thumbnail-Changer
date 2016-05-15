/**
 * 
 */
package apu.scratch.hax;

import java.io.File;
import java.io.InputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONObject;

import apu.scratch.hax.ProgressFileEntity.ProgressCallback;

/**
 * Backend to handle HTTP
 * 
 * @author "MegaApuTurkUltra"
 */
public class GuiHackerBackend {
	static RequestConfig globalConfig;
	static CookieStore cookieStore;
	static CloseableHttpClient httpClient;
	static CloseableHttpResponse resp;
	static String csrfToken;

	private static boolean useProxy = false;

	public static void reset() {
		try {
			csrfToken = null;
			if (cookieStore != null)
				cookieStore.clear();
			if (resp != null)
				resp.close();
			if (httpClient != null)
				httpClient.close();
		} catch (Exception e) {
			e.printStackTrace();
			// ignore. HttpClient was probably in the middle of something
			// or not fully initialized. We can let GC handle this
		}
		System.gc();
	}

	/**
	 * DO NOT USE SUPER DANGEROUS
	 */
	public static SSLContext bypassSSLCertCheck() throws Exception {
		/*SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} }, new SecureRandom());
		return sslContext;*/ return null;
	}

	public static void init() throws Exception {
		RequestConfig.Builder configBuilder = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY)
				.setSocketTimeout(0).setConnectionRequestTimeout(0)
				.setConnectTimeout(0);
		if (useProxy) {
			configBuilder.setProxy(new HttpHost("localhost", 8888));
		}

		globalConfig = configBuilder.build();

		if (!useProxy) {
			cookieStore = new BasicCookieStore();
		}
		BasicClientCookie lang = new BasicClientCookie("scratchlanguage", "en");
		lang.setDomain(".scratch.mit.edu");
		lang.setPath("/");
		cookieStore.addCookie(lang);
		// hacks activated in 3...2...1..

		HttpClientBuilder clientBuilder = HttpClients
				.custom()
				.setDefaultRequestConfig(globalConfig)
				.setUserAgent(
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
				.setDefaultCookieStore(cookieStore);
		if (useProxy) {
			clientBuilder.setSslcontext(bypassSSLCertCheck());
		}
		httpClient = clientBuilder.build();
	}

	public static void login(String username, char[] password) throws Exception {
		System.out.print("Requesting index page...");
		HttpUriRequest index = RequestBuilder.get()
				.setUri("https://scratch.mit.edu/")
				.addHeader("Accept", "text/html")
				.addHeader("Referer", "https://scratch.mit.edu").build();
		resp = httpClient.execute(index);
		System.out.println(resp.getStatusLine());
		resp.close();

		System.out.print("Requesting CSRF token...");
		HttpUriRequest csrf = RequestBuilder.get()
				.setUri("https://scratch.mit.edu/csrf_token/")
				.addHeader("Accept", "*/*")
				.addHeader("Referer", "https://scratch.mit.edu")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();
		resp = httpClient.execute(csrf);
		System.out.println(resp.getStatusLine());
		resp.close();

		for (Cookie c : cookieStore.getCookies()) {
			if (c.getName().equals("scratchcsrftoken")) {
				csrfToken = c.getValue();
			}
		}
		System.out.println("Found CSRF: " + csrfToken);

		System.out.print("Logging in...");
		JSONObject loginObj = new JSONObject();
		loginObj.put("username", username);
		loginObj.put("password", new String(password));
		loginObj.put("captcha_challenge", "");
		loginObj.put("captcha_response", "");
		loginObj.put("embed_captcha", false);
		loginObj.put("timezone", "America/New_York");
		loginObj.put("csrfmiddlewaretoken", csrfToken);
		HttpUriRequest login = RequestBuilder
				.post()
				.setUri("https://scratch.mit.edu/login/")
				.addHeader("Accept",
						"application/json, text/javascript, */*; q=0.01")
				.addHeader("Referer", "https://scratch.mit.edu")
				.addHeader("Origin", "https://scratch.mit.edu")
				.addHeader("Content-Type", "application/json")
				.addHeader("X-Requested-With", "XMLHttpRequest")
				.addHeader("X-CSRFToken", csrfToken)
				.setEntity(new StringEntity(loginObj.toString())).build();
		resp = httpClient.execute(login);
		System.out.println(resp.getStatusLine());
		StringBuffer loginResp = new StringBuffer();
		InputStream in = resp.getEntity().getContent();
		int i;
		while ((i = in.read()) != -1)
			loginResp.append((char) i);
		in.close();
		resp.close();
		System.out.println(loginResp.toString());

		JSONArray response = new JSONArray(loginResp.toString());
		JSONObject obj = response.getJSONObject(0);
		String loginMessage = "Unknown";
		if (obj.has("msg"))
			loginMessage = obj.getString("msg");
		if (obj.getInt("success") != 1) {
			throw new Exception(
					"Login might have failed\nScratch returned the message:\n\""
							+ loginMessage + "\"");
		}
	}

	public static JSONArray getProjects() throws Exception {
		System.out.print("Loading projects...");
		HttpUriRequest projects = RequestBuilder.get()
				.setUri("https://scratch.mit.edu/site-api/projects/all/")
				.addHeader("Accept", "*/*")
				.addHeader("Referer", "https://scratch.mit.edu")
				.addHeader("X-Requested-With", "XMLHttpRequest").build();
		resp = httpClient.execute(projects);
		System.out.println(resp.getStatusLine());
		resp.close();
		StringBuffer projectsResp = new StringBuffer();
		InputStream in = resp.getEntity().getContent();
		int i;
		while ((i = in.read()) != -1)
			projectsResp.append((char) i);
		in.close();
		resp.close();
		return new JSONArray(projectsResp.toString());
	}

	public static void hackThumbnail(int projectId, File thumbnail,
			String mimeType) throws Exception {
		System.out.print("Uploading thumbnail...");

		// debug only, do not enable
		// useProxy = true;
		// init();

		// the ST is sneaky :P
		BasicClientCookie pid = new BasicClientCookie("projectId",
				Integer.toString(projectId));
		pid.setDomain(".scratch.mit.edu");
		pid.setPath("/");
		cookieStore.addCookie(pid);

		// even more sneaky...
		System.out.print("Refinding CSRF...");
		for (Cookie c : cookieStore.getCookies()) {
			if (c.getName().equals("scratchcsrftoken")) {
				csrfToken = c.getValue();
			}
		}
		System.out.println("Found CSRF: " + csrfToken);

		HttpUriRequest thumb = RequestBuilder
				.post()
				.setUri("https://scratch.mit.edu/internalapi/project"
						+ "/thumbnail/" + projectId + "/set/?v=v440.3&_rnd="
						+ Math.random())
				.addHeader(
						"Referer",
						"https://cdn.scratch.mit.edu/scratchr2/static/__61560e08176805f6f84b8d1dd737d59b__/Scratch.swf")
				.addHeader("Content-Type", mimeType)
				.addHeader("X-CSRFToken", csrfToken)
				.addHeader("X-Requested-With", "ShockwaveFlash/19.0.0.226")
				.addHeader("Origin", "https://cdn.scratch.mit.edu")
				.addHeader("Accept", "*/*")
				.addHeader("Accept-Language", "en-US,en;q=0.8")
				.setEntity(
						new ProgressFileEntity(thumbnail, "image/png",
								new ProgressCallback() {
									@Override
									public void updateProgress(int progress) {
										if(ThumbnailGuiHacker.INSTANCE == null){
											System.out.print("\rUpload: " + progress + "%");
											return;
										}
										ThumbnailGuiHacker.INSTANCE
												.setProgress(progress);
									}
								})).build();
		resp = httpClient.execute(thumb);
		System.out.println(resp.getStatusLine());
		int statusCode = resp.getStatusLine().getStatusCode();
		InputStream in = resp.getEntity().getContent();
		int i;
		while ((i = in.read()) != -1) {
			System.out.print((char) i);
		}
		resp.close();
		if (statusCode != 200) {
			throw new IllegalStateException("Response status is " + statusCode);
		}
	}
}
