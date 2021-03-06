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
package de.uzk.hki.da.cb;

import static de.uzk.hki.da.core.C.ERROR_MSG_DURING_FILE_FORMAT_IDENTIFICATION;
import static de.uzk.hki.da.core.C.QUEUE_TO_CLIENT;
import static de.uzk.hki.da.core.C.QUEUE_TO_SERVER;
import static de.uzk.hki.da.core.C.WA_DATA;
import static de.uzk.hki.da.utils.StringUtilities.isNotSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.core.IngestGate;
import de.uzk.hki.da.core.SubsystemNotAvailableException;
import de.uzk.hki.da.core.UserException;
import de.uzk.hki.da.format.FileFormatException;
import de.uzk.hki.da.format.FileFormatFacade;
import de.uzk.hki.da.format.FileWithFileFormat;
import de.uzk.hki.da.grid.GridFacade;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.DocumentsGenService;
import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.service.JmsMessage;
import de.uzk.hki.da.util.ConfigurationException;
import de.uzk.hki.da.util.Path;

/**
 * <li>Creates a new Representation and copies the contents of the submission into it.
 * <li>Tests if it is a delta package (detected through orig_name=already existing orig_name of an object).
 * <li>If that's the case, the previous representations of the original packages get loaded, so that all 
 * reps including the new one are accessible under fork/[csn]/[orig_name]/data/[repnames]
 * 
 * @author Daniel M. de Oliveira
 */
public class RestructureAction extends AbstractAction{
	
	private static final String UNDERSCORE = "_";
	
	private FileFormatFacade fileFormatFacade;
	private IngestGate ingestGate;
	private GridFacade gridRoot;
	private DocumentsGenService dgs = new DocumentsGenService();
	
	public RestructureAction(){
		SUPPRESS_OBJECT_CONSISTENCY_CHECK=true;
	}
	
	@Override
	public void checkConfiguration() {
		if (getGridRoot()==null) throw new ConfigurationException("Must not be null: gridRoot");
		if (getFileFormatFacade()==null) throw new ConfigurationException("Must not be null: fileFormatFacade");
	}
	

	@Override
	public void checkPreconditions() {
	}
	
	@Override
	public boolean implementation() throws FileNotFoundException, IOException,
			UserException, RepositoryException, SubsystemNotAvailableException {
		
		RetrievePackagesHelper retrievePackagesHelper = new RetrievePackagesHelper(getGridRoot());
		if (o.isDelta()
				&&(! checkIfOnWorkAreaIsSpaceAvailabeForDeltaPackages(retrievePackagesHelper)))
			return false;
		
		
		j.setRep_name(getNewRepName());
		makeRepOfSIPContent(o.getPath(), o.getDataPath(), j.getRep_name());
		
		
		if (o.isDelta())
			retrieveDeltaPackages(retrievePackagesHelper);
		
		
		o.getLatestPackage().scanRepRecursively(j.getRep_name()+"a");
		o.reattach();
		
		determineFileFormats();
		dgs.addDocumentsToObject(o);
		
		logger.debug("Create new b representation "+j.getRep_name()+"b");
		Path.makeFile(o.getDataPath(), j.getRep_name()+"b").mkdir();
		Path.makeFile(o.getDataPath(),"jhove_temp").mkdirs();
		return true;
	}

	
	
	private boolean checkIfOnWorkAreaIsSpaceAvailabeForDeltaPackages(RetrievePackagesHelper retrievePackagesHelper) {
		try {
			if (!getIngestGate().canHandle(retrievePackagesHelper.getObjectSize(o, j ))){
				JmsMessage jms = new JmsMessage(QUEUE_TO_CLIENT,QUEUE_TO_SERVER,o.getIdentifier() 
						+ " - Please check WorkArea space limitations: " + ingestGate.getFreeDiskSpacePercent() +" % free needed " );
				super.getJmsMessageServiceHandler().sendJMSMessage(jms);	
				logger.info("no disk space available at working resource. will not fetch new data.");
				return false;
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to determine object size for object " + o.getIdentifier(), e);
		}
		return true;
	}
	
	private void retrieveDeltaPackages(RetrievePackagesHelper retrievePackagesHelper) {
		
		logger.info("object already exists. Moving existing packages to work area.");
		try {
			retrievePackagesHelper.loadPackages(o, false);
			logger.info("Packages of object \""+o.getIdentifier()+
					"\" are now available on cache resource at: " + Path.make(o.getPath(),"existingAIPs"));
			FileUtils.copyFile(Path.makeFile(o.getPath("newest"),"premis.xml"),
					Path.makeFile(o.getDataPath(),"premis_old.xml"));
		} catch (IOException e) {
			throw new RuntimeException("error while trying to get existing packages from lza area",e);
		}
	}
	
	
	
	
	private void determineFileFormats() throws FileNotFoundException, SubsystemNotAvailableException {
		List<FileWithFileFormat> scannedFiles = null;
		try {
			List<DAFile> dafiles = o.getNewestFilesFromAllRepresentations(preservationSystem.getSidecarExtensions());
			scannedFiles = fileFormatFacade.identify(dafiles);
		} catch (FileFormatException e) {
			throw new RuntimeException(ERROR_MSG_DURING_FILE_FORMAT_IDENTIFICATION,e);
		} catch (IOException e) {
			throw new SubsystemNotAvailableException(e);
		}
		for (FileWithFileFormat f:scannedFiles){
			logger.info(f+":"+f.getFormatPUID()+":"+f.getSubformatIdentifier());
		}
	}
	
	@Override
	public void rollback() throws Exception {
		if (! isNotSet(j.getRep_name())) { // since we know that the SIP content has been moved successfully when rep_name is set.
			revertToSIPContent(o.getPath(),o.getDataPath(),j.getRep_name());
		} else 
			throw new RuntimeException("REP NAME WAS NOT SET YET. ROLLBACK IS NOT POSSIBLE. MANUAL CLEANUP REQUIRED.");
	}

	
	static void revertToSIPContent(Path objectPath, Path dataPath, String repName) throws IOException {
		final String A = "a";
		final String DATA_TMP = WA_DATA+UNDERSCORE;
		if (isNotSet(repName)) throw new IllegalArgumentException("rep name not set");
		if (isNotSet(dataPath)) throw new IllegalArgumentException("data path not set");
		if (isNotSet(objectPath)) throw new IllegalArgumentException("object path not set");
		
		FileUtils.moveDirectory(
			Path.makeFile( dataPath, repName + A ), 
			Path.makeFile( objectPath, DATA_TMP ));
			
		FileUtils.deleteDirectory( dataPath.toFile() );
			
		FileUtils.moveDirectory(
			Path.makeFile( objectPath, DATA_TMP ), 
			Path.makeFile( dataPath ));
	}
	
	static void makeRepOfSIPContent(Path objectPath, Path dataPath, String repName) throws IOException {
		final String A = "a";
		final String DATA_TMP = WA_DATA+UNDERSCORE;
		if (isNotSet(repName)) throw new IllegalArgumentException("rep name not set");
		if (isNotSet(dataPath)) throw new IllegalArgumentException("data path not set");
		if (isNotSet(objectPath)) throw new IllegalArgumentException("object path not set");
		
		FileUtils.moveDirectory(dataPath.toFile(), 
				Path.makeFile(objectPath,DATA_TMP));
		
		dataPath.toFile().mkdirs();
		
		FileUtils.moveDirectory(Path.makeFile(objectPath,DATA_TMP), 
	    		Path.makeFile(dataPath, repName + A));
	}
	
	
	
	
	/**
	 * @param j
	 * @param physicalPathToAIP
	 * @return the representations
	 * @throws IOException 
	 */
	public String getNewRepName() throws IOException{
		
		Date dNow = new Date( );
	    SimpleDateFormat ft = new SimpleDateFormat ("yyyy'_'MM'_'dd'+'HH'_'mm'_'ss'+'");
	    String repName = ft.format(dNow);
	    
	    return repName;
	}

	public IngestGate getIngestGate() {
		return ingestGate;
	}

	public void setIngestGate(IngestGate ingestGate) {
		this.ingestGate = ingestGate;
	}

	public GridFacade getGridRoot() {
		return gridRoot;
	}
 
	public void setGridRoot(GridFacade gridRoot) {
		this.gridRoot = gridRoot;
	}
	
	public FileFormatFacade getFileFormatFacade() {
		return fileFormatFacade;
	}

	public void setFileFormatFacade(FileFormatFacade fileFormatFacade) {
		this.fileFormatFacade = fileFormatFacade;
	}
}
