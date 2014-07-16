package de.uzk.hki.da.cb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.RelativePath;
import de.uzk.hki.da.utils.TESTHelper;

public class UpdateMetadataRheinlaender {
	
	private static final Namespace METS_NS = Namespace.getNamespace("http://www.loc.gov/METS/");
	private static final Namespace XLINK_NS = Namespace.getNamespace("http://www.w3.org/1999/xlink");
	private static final Path workAreaRootPathPath = new RelativePath("src/test/resources/cb/UpdateMetadataRheinlaender/");
	private static final UpdateMetadataAction action = new UpdateMetadataAction();
	private Event event2;
	private Event event3;
	private Event event4;
	private Event event5;
	private Event event6;
	private Object object;
	private Boolean isTest;
	
	
	@Before
	public void setUp() throws IOException{
		
		object = TESTHelper.setUpObject("43",workAreaRootPathPath);
		
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/mets_2_32044.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/mets_2_32045.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/mets_2_32046.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/mets_2_32047.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/mets_2_32048.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/alvr_nr_4547_aufn_002.tif").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/alvr_nr_4547_aufn_003.tif").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/alvr_nr_4547_aufn_004.tif").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/alvr_nr_4547_aufn_005.tif").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/alvr_nr_4547_aufn_006.tif").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/EAD_Export.XML").toFile(), Path.make(workAreaRootPathPath,"work/TEST/43/data/a/").toFile());
		
		DAFile f2 = new DAFile(object.getLatestPackage(),"a","mets_2_32044.xml");
		object.getLatestPackage().getFiles().add(f2);
		DAFile f3 = new DAFile(object.getLatestPackage(),"a","mets_2_32045.xml");
		object.getLatestPackage().getFiles().add(f3);
		DAFile f4 = new DAFile(object.getLatestPackage(),"a","mets_2_32046.xml");
		object.getLatestPackage().getFiles().add(f4);
		DAFile f5 = new DAFile(object.getLatestPackage(),"a","mets_2_32047.xml");
		object.getLatestPackage().getFiles().add(f5);
		DAFile f6 = new DAFile(object.getLatestPackage(),"a","mets_2_32048.xml");
		object.getLatestPackage().getFiles().add(f6);
		DAFile f7 = new DAFile(object.getLatestPackage(),"a","EAD_Export.XML");
		object.getLatestPackage().getFiles().add(f7);
		
		event2 = new Event();
		event2.setSource_file(new DAFile(object.getLatestPackage(),"a","alvr_nr_4547_aufn_002.tif"));
		event2.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed002.tif"));
		event2.setType("CONVERT");
		object.getLatestPackage().getEvents().add(event2);
		
		event3 = new Event();
		event3.setSource_file(new DAFile(object.getLatestPackage(),"a","alvr_nr_4547_aufn_003.tif"));
		event3.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed003.tif"));
		event3.setType("CONVERT");
		object.getLatestPackage().getEvents().add(event3);
		
		event4 = new Event();
		event4.setSource_file(new DAFile(object.getLatestPackage(),"a","alvr_nr_4547_aufn_004.tif"));
		event4.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed004.tif"));
		event4.setType("CONVERT");
		object.getLatestPackage().getEvents().add(event4);
		
		event5 = new Event();
		event5.setSource_file(new DAFile(object.getLatestPackage(),"a","alvr_nr_4547_aufn_005.tif"));
		event5.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed005.tif"));
		event5.setType("CONVERT");
		object.getLatestPackage().getEvents().add(event5);
		
		event6 = new Event();
		event6.setSource_file(new DAFile(object.getLatestPackage(),"a","alvr_nr_4547_aufn_006.tif"));
		event6.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed006.tif"));
		event6.setType("CONVERT");
		object.getLatestPackage().getEvents().add(event6);
		
		Job job = new Job(); 
		job.setObject(object); 
		job.setId(1);
		object.setPackage_type("EAD");
		object.setMetadata_file("EAD_Export.XML");

		HashMap<String,String> xpaths = new HashMap<String,String>();
		xpaths.put("METS", "//mets:FLocat/@xlink:href");
		xpaths.put("EAD", "//daoloc/@href");
		action.setXpathsToUrls(xpaths);
		HashMap<String, String> nsMap = new HashMap<String,String>();
		nsMap.put("mets", METS_NS.getURI());
		nsMap.put("xlink", XLINK_NS.getURI());
		action.setNamespaces(nsMap);
		action.setAbsUrlPrefix("http://data.danrw.de/file");
		Map<String, String> dcMappings = new HashMap<String,String>();
		dcMappings.put("EAD", "conf/xslt/dc/ead_to_dc.xsl");
		action.setDcMappings(dcMappings);
		
		action.setObject(object);
		action.setJob(job);
	}

	@After 
	public void tearDown(){
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/alvr_nr_4547_aufn_002.tif").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/alvr_nr_4547_aufn_003.tif").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/alvr_nr_4547_aufn_004.tif").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/alvr_nr_4547_aufn_005.tif").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/alvr_nr_4547_aufn_006.tif").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/mets_2_32044.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/mets_2_32045.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/mets_2_32046.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/mets_2_32047.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/mets_2_32048.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/a/EAD_Export.XML").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32044.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32045.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32046.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32047.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32048.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/43/data/b/EAD_Export.XML").delete();
	}

	@Test
	public void test() throws IOException, JDOMException {
		
		action.implementation();
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32044.xml").toFile()));
		assertEquals("http://data.danrw.de/file/43/renamed002.tif", getURL(doc));
		
		Document doc3 = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32045.xml").toFile()));
		assertEquals("http://data.danrw.de/file/43/renamed003.tif", getURL(doc3));
		
		Document doc4 = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32046.xml").toFile()));
		assertEquals("http://data.danrw.de/file/43/renamed004.tif", getURL(doc4));
		
		Document doc5 = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32047.xml").toFile()));
		assertEquals("http://data.danrw.de/file/43/renamed005.tif", getURL(doc5));
		
		Document doc6 = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/43/data/b/mets_2_32048.xml").toFile()));
		assertEquals("http://data.danrw.de/file/43/renamed006.tif", getURL(doc6));
	}
	
	private String getURL(Document doc){
		
		return doc.getRootElement()
				.getChild("fileSec", METS_NS)
				.getChild("fileGrp", METS_NS)
				.getChild("file", METS_NS)
				.getChild("FLocat", METS_NS)
				.getAttributeValue("href", XLINK_NS);
	}
}

