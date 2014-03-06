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

package de.uzk.hki.da.cb;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.io.FileUtils;

import de.uzk.hki.da.core.ConfigurationException;
import de.uzk.hki.da.grid.DistributedConversionAdapter;
import de.uzk.hki.da.metadata.PremisXmlReader;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.PublicationRight.Audience;


/**
 * First moves the dip out of the package to another location.
 * Then evaluates contract rights for publication and trims down bitstreams
 * of PIP that aren't allowed to get published.
 * @author Daniel M. de Oliveira
 */
public class PrepareSendToPresenterAction extends AbstractAction {

	private DistributedConversionAdapter distributedConversionAdapter;
	private File publicDir;
	private File instDir;
	
	@Override
	boolean implementation() throws IOException {
		if (distributedConversionAdapter==null) throw new ConfigurationException("distributedConversionAdapter not set");
		
		String dipName = object.getContractor().getShort_name() + "/" + object.getIdentifier()+"_"+object.getLatestPackage().getId();
		publicDir = new File(localNode.getDipAreaRootPath()+"public/"+dipName);
		instDir = new File(localNode.getDipAreaRootPath()+"institution/"+dipName);
		
		logger.trace("Moving the dip content for presentation purposes out of the archival package.");
		copyPIPSforReplication();
		
		Object premisObject = readRightsFromPREMIS();
		
		if (!premisObject.grantsRight("PUBLICATION")) {
			logger.info("PUBLICATION Right not granted. Will delete datastreams.");
			if (publicDir.exists()) FileUtils.deleteDirectory(publicDir);
			if (instDir.exists()) FileUtils.deleteDirectory(instDir);
		} else
		if (!premisObject.grantsPublicationRight(Audience.PUBLIC)) {
			logger.info("Rights for PUBLIC not granted. Will delete datastreams.");
			if (publicDir.exists()) FileUtils.deleteDirectory(publicDir);
		} else
		if (!premisObject.grantsPublicationRight(Audience.INSTITUTION)) {
			logger.info("Rights for INSTITUTION not granted. Will delete datastreams.");
			if (instDir.exists()) FileUtils.deleteDirectory(instDir);
		}
		
		registerPIPSforReplication(dipName);
		return true;
	}


	/**
	 * @return
	 * @throws IOException
	 */
	private Object readRightsFromPREMIS() throws IOException {
		Object premisObject = null;
		try {
			PremisXmlReader reader = new PremisXmlReader();

			File premisFile = new File(object.getDataPath() +
					object.getNameOfNewestBRep() + "/premis.xml");
			
			premisObject = reader.deserialize(premisFile);
		} catch (ParseException pe){
			throw new RuntimeException("error while parsing PREMIS-file",pe);
		}
		return premisObject;
	}


	/**
	 * @param dipName has the form [csn]/[oid]_[pkgid]
	 */
	private void registerPIPSforReplication(String dipName) {
		
		if (!publicDir.exists())
			distributedConversionAdapter.create("dip/public/"+dipName);
		else
			distributedConversionAdapter.register(
				"dip/public/"+dipName, 
				publicDir.getAbsolutePath()
				);
		
		if (!instDir.exists())
			distributedConversionAdapter.create("dip/institution/"+dipName);
		else
			distributedConversionAdapter.register(
				"dip/institution/"+dipName, 
				instDir.getAbsolutePath()
				);
	}


	/**
	 * The xIP (in case PUBLIC right is set) contains converted files that are 
	 * meant only to be presented in a portal but not to be archived. So to prevent these files
	 * get into the archive we extract the derivate before we build a tar file around the xIP.
	 * 
	 * Moves 
	 * <li>from fork/[csn]/[oid]/data/dip/public -> dip/public/[csn]/[oid]
	 * <li>from fork/[csn]/[oid]/data/dip/institution -> dip/institution/[csn]/[oid]
	 * 
	 * @param dipName has the form urn_pkgid
	 * @throws IOException 
	 */
	private void copyPIPSforReplication() throws IOException {
		
		if (new File(object.getDataPath()+"dip/public").exists()){
			logger.info("Copying public datastreams to " + publicDir.getAbsolutePath());
			if (publicDir.exists()) FileUtils.deleteDirectory(publicDir);
			FileUtils.copyDirectory(
					new File(object.getDataPath()+"dip/public"), 
					publicDir);
		}
		if (new File(object.getDataPath()+"dip/institution").exists()){
			logger.info("Copying institution datastreams to " + instDir);
			if (instDir.exists()) FileUtils.deleteDirectory(instDir);
			FileUtils.copyDirectory(
					new File(object.getDataPath()+"dip/institution"), 
					instDir);
		}
	}
	
	

	@Override
	void rollback() throws Exception {
		if (publicDir.exists()) FileUtils.deleteDirectory(publicDir);
		if (instDir.exists()) FileUtils.deleteDirectory(instDir);
		logger.info("@Admin: You can safely roll back this job to status "+this.getStartStatus()+" now.");
	}


	public DistributedConversionAdapter getDistributedConversionAdapter() {
		return distributedConversionAdapter;
	}


	public void setDistributedConversionAdapter(
			DistributedConversionAdapter distributedConversionAdapter) {
		this.distributedConversionAdapter = distributedConversionAdapter;
	}
}