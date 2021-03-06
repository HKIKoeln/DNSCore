package de.uzk.hki.da.convert;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uzk.hki.da.core.C;
import de.uzk.hki.da.model.ConversionInstruction;
import de.uzk.hki.da.model.ConversionRoutine;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.test.TESTHelper;
import de.uzk.hki.da.util.Path;
import de.uzk.hki.da.util.RelativePath;
import de.uzk.hki.da.utils.CommandLineConnector;
import de.uzk.hki.da.utils.ProcessInformation;

public class PublishImageMultipageTIFFTests {

	Path workAreaRootPathPath= Path.make(TC.TEST_ROOT_CONVERT,"PublishImageMultipageTiffTests");
	
	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMultipage() throws IOException {
		PublishImageConversionStrategy cs = new PublishImageConversionStrategy();
		Object o = TESTHelper.setUpObject("123",new RelativePath(workAreaRootPathPath));
		ProcessInformation pi = new ProcessInformation();
		pi.setExitValue(0);
		CommandLineConnector cli = mock ( CommandLineConnector.class );
		DAFile sourceFile = new DAFile(o.getLatestPackage(),"source","ALVR{}_Nr_4557_Aufn_249.tif");
		
		String cmdPublic[] = new String[]{
				"convert",
				new File(workAreaRootPathPath+"/work/TEST/123/data/source/ALVR{}_Nr_4557_Aufn_249.tif").getAbsolutePath(),
				new File(workAreaRootPathPath+"/work/TEST/123/data/"+C.WA_DIP+"/public/ALVR{}_Nr_4557_Aufn_249.jpg").getAbsolutePath()
		};
		when(cli.runCmdSynchronously(cmdPublic)).thenReturn(pi);
		
		String cmdInstitution[] = new String[]{
				"convert",
				new File(workAreaRootPathPath+"/work/TEST/123/data/source/ALVR{}_Nr_4557_Aufn_249.tif").getAbsolutePath(),
				new File(workAreaRootPathPath+"/work/TEST/123/data/"+C.WA_DIP+"/institution/ALVR{}_Nr_4557_Aufn_249.jpg").getAbsolutePath()
		};
		when(cli.runCmdSynchronously(cmdInstitution)).thenReturn(pi);
		
		
		cs.setCLIConnector(cli);
		
		ConversionInstruction ci = new ConversionInstruction();
		ci.setSource_file(sourceFile);
		ci.setTarget_folder("");
		
		ConversionRoutine cr = new ConversionRoutine();
		cr.setTarget_suffix("jpg");
		ci.setConversion_routine(cr);
		
		cs.setObject(o);
		List<Event> events = cs.convertFile(ci);
		assertEquals(4, events.size());
	}
	
}
