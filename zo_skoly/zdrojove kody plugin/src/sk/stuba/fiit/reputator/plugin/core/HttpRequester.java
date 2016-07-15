package sk.stuba.fiit.reputator.plugin.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpRequester {

    private final static String HOUND_URL = "http://localhost:6080/api/v1/search?stats=fosho&repos=*&rng=%3A20&q=boo&files=foo&i=ignoreCase";
    private final static String UTF_8 = "UTF-8";

    public HttpRequester() {
    }

    public Response makeRequest(String url) {
        OkHttpClient client = new OkHttpClient();
        client = client.newBuilder().readTimeout(30, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(url).build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
        	System.err.println("Error while making http request");
        	System.err.println(e);
        }

        return response;
    }

    public String makeHoundRequest(String query, String filePath, boolean ignoreCase) {
        String url = null;
        
        try {
        	url = HOUND_URL.replace("boo", URLEncoder.encode(query, UTF_8));
			url = url.replace("foo", URLEncoder.encode(filePath, UTF_8));
			url = ignoreCase ? url.replace("ignoreCase", "fosho")
					: url.replace("ignoreCase", "nope");
		} catch (UnsupportedEncodingException e1) {
			System.err.println("Error while encoding parameters");
        	System.err.println(e1);
		}
        
        Response response = makeRequest(url);
        String completeJSON = null;
        try {
            completeJSON = response.body().string();
        } catch (IOException e) {
        	System.err.println("IOException reading response body");
        	System.err.println(e);
        }

        return completeJSON;
    }
}
