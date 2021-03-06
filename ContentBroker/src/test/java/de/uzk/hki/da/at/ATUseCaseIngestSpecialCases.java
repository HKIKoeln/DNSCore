/*
 DA-NRW Software Suite | ContentBroker
 Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
 Universität zu Köln

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

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.util.Path;

/**
 * Relates to AK-T/02 Ingest - Sunny Day Szenario (mit besonderen Bedingungen).
 * @author Daniel M. de Oliveira
 */
public class ATUseCaseIngestSpecialCases extends AcceptanceTest{

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		ath.putPackageToIngestArea("ATÜÄÖ","tgz","ATÜÄÖ");
		ath.putPackageToIngestArea("ATSonderzeichen_in_Dateinamen","tgz","ATSonderzeichen_in_Dateinamen");
		ath.putPackageToIngestArea("ATUmlaute_in_Dateinamen","tgz","ATUmlaute_in_Dateinamen");
		ath.putPackageToIngestArea("AT_CON1","tar","AT_CON1");
		ath.putPackageToIngestArea("AT_CON2","tgz","AT_CON2");
		ath.putPackageToIngestArea("AT_CON3","zip","AT_CON3");
		ath.putPackageToIngestArea("AT&Sonderzeichen%in#Paketnamen","tgz","AT&Sonderzeichen%in#Paketnamen");
		ath.putPackageToIngestArea("ATLeerzeichen_in_Dateinamen","tgz","ATLeerzeichen_in_Dateinamen");
	}
	
	@AfterClass
	public static void tearDown(){
		
//		FileUtils.deleteQuietly(Path.make(localNode.getWorkAreaRootPath(),"/work/TEST/"+object.getIdentifier()).toFile());

		Path.make(localNode.getIngestAreaRootPath(),"/TEST/AT_CON1.tar").toFile().delete();
		Path.make(localNode.getIngestAreaRootPath(),"/TEST/AT_CON2.tgz").toFile().delete();
		Path.make(localNode.getIngestAreaRootPath(),"/TEST/AT_CON3.zip").toFile().delete();
		
		
	}
	
	@Test
	public void testUmlautsInPackageName() throws Exception{
		ath.waitForObjectToBeInFinishState("ATÜÄÖ");
	}
	
	@Test
	public void testSpecialCharactersInFileNames() throws Exception{
		ath.waitForObjectToBeInFinishState("ATSonderzeichen_in_Dateinamen");
	}
	
	@Test
	public void testUmlautsInFileNames() throws Exception{
		ath.waitForObjectToBeInFinishState("ATUmlaute_in_Dateinamen");
	}
	
	@Test
	public void testTARContainer() throws Exception{
		ath.waitForObjectToBeInFinishState("AT_CON1");
	}
	
	@Test
	public void testTGZContainer() throws Exception{
		ath.waitForObjectToBeInFinishState("AT_CON2");
	}
	
	@Test
	public void testZIPContainer() throws Exception{
		ath.waitForObjectToBeInFinishState("AT_CON3");
	}
	
	@Test
	public void testSpecialCharsInPackageName() throws Exception{
		ath.waitForObjectToBeInFinishState("AT&Sonderzeichen%in#Paketnamen");
	}
	
	@Test
	public void testWhiteSpacesInFileNames() throws Exception{
		ath.waitForObjectToBeInFinishState("ATLeerzeichen_in_Dateinamen");
	}
}
