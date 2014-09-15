/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2014 LVRInfoKom
  Landschaftsverband Rheinland

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.uzk.hki.da.at;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.path.Path;
import de.uzk.hki.da.test.TESTHelper;

/**
 * @author Polina Gubaidullina
 */
public class ATUseCaseIngestLIDO extends Base{

	private static final String DATA_DANRW_DE = "http://data.danrw.de";
	private static final String origName = "ATUseCaseUpdateMetadataLZA_LIDO";
	private static Object object;
	private static final Namespace LIDO_NS = Namespace.getNamespace("http://www.lido-schema.org");
	
	@BeforeClass
	public static void setUp() throws IOException{
		setUpBase();
		object = ingest(origName);
	}
	
	@AfterClass
	public static void tearDown(){
		try{
			new File("/tmp/"+object.getIdentifier()+".pack_1.tar").delete();
			FileUtils.deleteDirectory(new File("/tmp/"+object.getIdentifier()+".pack_1"));
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		
		TESTHelper.clearDB();
		cleanStorage();
	}
	
	@Test
	public void testLZA() throws FileNotFoundException, JDOMException, IOException {
		
		Object lzaObject = retrievePackage(origName,"1");
		System.out.println("object identifier: "+lzaObject.getIdentifier());
		
		Path tmpObjectDirPath = Path.make("tmp", lzaObject.getIdentifier()+".pack_1", "data");	
		File[] tmpObjectSubDirs = new File (Path.make("tmp", lzaObject.getIdentifier()+".pack_1", "data").toString()).listFiles();
		String bRep = "";
		
		for (int i=0; i<tmpObjectSubDirs.length; i++) {
			if(tmpObjectSubDirs[i].getName().contains("+b")) {
				bRep = tmpObjectSubDirs[i].getName();
			}
		}
		
		SAXBuilder builder = new SAXBuilder();
		String LidoFileName = "LIDO-Testexport2014-07-04-FML-Auswahl.xml";
		Document doc = builder.build
				(new FileReader(Path.make(tmpObjectDirPath, bRep, LidoFileName).toFile()));
		assertTrue(getLIDOURL(doc).equals("Picture2.tif"));
	}
	
	@Test
	public void testPres() throws FileNotFoundException, JDOMException, IOException{
		object = retrievePackage(origName,"1");
		System.out.println("object identifier: "+object.getIdentifier());
		
		String packageType = object.getPackage_type();
		System.out.println("package type: "+packageType);
		
		SAXBuilder builder = new SAXBuilder();
		
		Document doc = builder.build
				(new FileReader(Path.make(localNode.getWorkAreaRootPath(),"pips", "public", "TEST", object.getIdentifier(), object.getPackage_type()+".xml").toFile()));
		assertTrue(getLIDOURL(doc).contains(DATA_DANRW_DE));
	}
	
	@Test
	public void testIndex() {
		assertTrue(repositoryFacade.getIndexedMetadata("portal_ci_test", "Inventarnummer").contains("\"edm:provider\":\"DA-NRW - Digitales Archiv Nordrhein-Westfalen\""));
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
	
