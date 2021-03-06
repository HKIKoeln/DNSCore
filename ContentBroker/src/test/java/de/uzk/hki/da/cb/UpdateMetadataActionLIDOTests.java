package de.uzk.hki.da.cb;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.uzk.hki.da.format.MimeTypeDetectionService;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.User;
import de.uzk.hki.da.test.TESTHelper;
import de.uzk.hki.da.util.Path;
import de.uzk.hki.da.util.RelativePath;

public class UpdateMetadataActionLIDOTests {
	
	private static final String _1_B_REP = "1+b";
	private static final String _1_A_REP = "1+a";
	private static final Namespace LIDO_NS = Namespace.getNamespace("http://www.lido-schema.org");
	private static MimeTypeDetectionService mtds;
	private static final Path workAreaRootPathPath = new RelativePath("src/test/resources/cb/UpdateMetadataActionLIDOTests/");
	private static final UpdateMetadataAction action = new UpdateMetadataAction();
	private Event event1;
	private Event event2;
	private Event event3;
	private Object object;
	private PreservationSystem pSystem;
	private Node n;
	
	@BeforeClass
	public static void mockDca() throws IOException {
		mtds = mock(MimeTypeDetectionService.class);
		when(mtds.identify((File)anyObject())).thenReturn("image/tiff");
	}
	
	@Before
	public void setUp() throws IOException {
		
		pSystem = new PreservationSystem();
		pSystem.setId(1);
		pSystem.setMinRepls(0);
		User psadmin = new User();
		psadmin.setShort_name("TEST_PSADMIN");
		psadmin.setEmailAddress("noreply");
		pSystem.setAdmin(psadmin);
		pSystem.setUrisFile("http://data.danrw.de/file");
		
		n = new Node();
		n.setName("testnode");
		n.setAdmin(psadmin);
		pSystem.getNodes().add(n);
		
		action.setLocalNode(n);
		action.setPSystem(pSystem);
		
		object = TESTHelper.setUpObject("42",workAreaRootPathPath);

		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/LIDO-Testexport2014-07-04-FML-Auswahl.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/42/data",_1_A_REP).toFile());
		DAFile f1 = new DAFile(object.getLatestPackage(),_1_A_REP,"LIDO-Testexport2014-07-04-FML-Auswahl.xml");
		de.uzk.hki.da.model.Document doc1 = new de.uzk.hki.da.model.Document(f1);
		object.addDocument(doc1);
		object.getLatestPackage().getFiles().add(f1);
		
		DAFile f2 = new DAFile(object.getLatestPackage(),_1_A_REP,"LVR_DFG-Alltagskultur_0000050177.tif");
		de.uzk.hki.da.model.Document doc2 = new de.uzk.hki.da.model.Document(f2);
		object.addDocument(doc2);
		
		event1 = new Event();
		event1.setSource_file(f2);
		event1.setTarget_file(new DAFile(object.getLatestPackage(),_1_B_REP,"renamed0000050177.tif"));
		event1.setType("CONVERT");
		
		DAFile f4 = new DAFile(object.getLatestPackage(),_1_A_REP,"LVR_DFG-Alltagskultur_0000050178.tif");
		de.uzk.hki.da.model.Document doc4 = new de.uzk.hki.da.model.Document(f4);
		object.addDocument(doc4);
		
		event2 = new Event();
		event2.setSource_file(new DAFile(object.getLatestPackage(),_1_A_REP,"LVR_DFG-Alltagskultur_0000050178.tif"));
		event2.setTarget_file(new DAFile(object.getLatestPackage(),_1_B_REP,"renamed0000050178.tif"));
		event2.setType("CONVERT");
		
		DAFile f5 = new DAFile(object.getLatestPackage(),_1_A_REP,"Test.tif");
		de.uzk.hki.da.model.Document doc5 = new de.uzk.hki.da.model.Document(f5);
		object.addDocument(doc5);
		
		event3 = new Event();
		event3.setSource_file(new DAFile(object.getLatestPackage(),_1_A_REP,"lvr_dfg-alltagskultur_0000050178"));
		event3.setTarget_file(new DAFile(object.getLatestPackage(),_1_B_REP,"renamed0000050178_1.tif"));
		event3.setType("CONVERT");
		
		object.getLatestPackage().getEvents().add(event1);
		object.getLatestPackage().getEvents().add(event2);
		object.getLatestPackage().getEvents().add(event3);
		
		Job job = new Job(); job.setObject(object); job.setId(1);
		object.setPackage_type("LIDO");
		object.setMetadata_file("LIDO-Testexport2014-07-04-FML-Auswahl.xml");
		
		HashMap<String,String> xpaths = new HashMap<String,String>();
		xpaths.put("LIDO", "//lido:linkResource");
		action.setXpathsToUrls(xpaths);
		HashMap<String, String> nsMap = new HashMap<String,String>();
		nsMap.put("lido", LIDO_NS.getURI());
		action.setNamespaces(nsMap);
		
		
		Map<String, String> dcMappings = new HashMap<String,String>();
		dcMappings.put("LIDO", "conf/xslt/dc/lido_to_dc.xsl");
		action.setDcMappings(dcMappings);
		
		action.setMtds(mtds);
		action.setObject(object);
		action.setJob(job);
		action.setPSystem(pSystem);
	}
	
	@After 
	public void tearDown(){
		Path.makeFile(workAreaRootPathPath,"work/TEST/42/data",_1_A_REP,"LIDO-Testexport2014-07-04-FML-Auswahl.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/42/data",_1_B_REP,"LIDO-Testexport2014-07-04-FML-Auswahl.xml").delete();
	}
	
	@Test
	public void test() throws IOException, JDOMException, ParserConfigurationException, SAXException {
		
		action.setPresMode(true);
		action.implementation();
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/42/data",_1_B_REP,"LIDO-Testexport2014-07-04-FML-Auswahl.xml").toFile()));
		assertEquals("http://data.danrw.de/file/42/renamed0000050177.tif", getLIDOURL(doc));
		
	}
		
	private String getLIDOURL(Document doc){
		
		return doc.getRootElement()
				.getChild("lido", LIDO_NS)
				.getChild("administrativeMetadata", LIDO_NS)
				.getChild("resourceWrap", LIDO_NS)
				.getChild("resourceSet", LIDO_NS)
				.getChild("resourceRepresentation", LIDO_NS)
				.getChild("linkResource", LIDO_NS)
				.getValue();
	}

}

