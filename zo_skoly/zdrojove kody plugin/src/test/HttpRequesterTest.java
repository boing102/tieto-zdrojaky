package test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import okhttp3.Response;
import sk.stuba.fiit.reputator.plugin.core.HttpRequester;

public class HttpRequesterTest {
	private HttpRequester httpRequester;
	
	@Before
	public void setUp() {
		httpRequester = new HttpRequester();
	}
	
	@Test
	public void requestTest() {
		Response response = httpRequester.makeRequest("http://www.google.com");
		assertTrue(response.isSuccessful());
	}
	
	@Test
	/**
	 * Will only work when hound is running.
	 */
	public void houndRequestTest() {
		String bodyResponse = httpRequester.makeHoundRequest("hello", "", false);
		assertTrue(!"".equals(bodyResponse));
	}

}
