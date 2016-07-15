package test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sk.stuba.fiit.reputator.plugin.core.JsonConverter;
import sk.stuba.fiit.reputator.plugin.model.jsonpojo.Repo;

public class JsonConverterTest {

	private final static String jsonString = "{\"Results\":{\"org.eclipse.mylyn.docs\":{\"Matches\":[{\"Filename\":\"org.eclipse.mylyn.docs.epub.core/src/org/eclipse/mylyn/docs/epub/core/OPSPublication.java\",\"Matches\":[{\"Line\":\"public class OPSPublication extends Publication {\",\"LineNumber\":64,\"Before\":[\" * @see http://www.idpf.org/doc_library/epub/OPS_2.0.1_draft.htm\",\" */\"],\"After\":[\"\",\"\t/** MIME type for NCX documents */\"]}]}],\"FilesWithMatch\":1,\"Revision\":\"5b8cd5b22989454ce1450b627c7709ddb6479bd4\"}},\"Stats\":{\"FilesOpened\":4,\"Duration\":7}}";
	private JsonConverter converter;
	
	@Before
	public void setUp() {
		converter = new JsonConverter();
	}
	
	@Test
	public void convertJsonToPojoTest() {
		List<Repo> repos = converter.convertJsonStringToPojo(Repo.class, jsonString);
		
		assertEquals(repos.size(), 1);
	}
	
}
