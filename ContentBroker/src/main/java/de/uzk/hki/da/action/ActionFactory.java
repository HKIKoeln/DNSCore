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

package de.uzk.hki.da.action;


import static de.uzk.hki.da.utils.StringUtilities.isNotSet;
import static de.uzk.hki.da.core.C.*;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.uzk.hki.da.core.PreconditionsNotMetException;
import de.uzk.hki.da.core.UserExceptionManager;
import de.uzk.hki.da.format.FileFormatFacade;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.JobNamedQueryDAO;
import de.uzk.hki.da.model.Node;
import de.uzk.hki.da.model.PreservationSystem;
import de.uzk.hki.da.model.SubformatIdentificationStrategyPuidMapping;
import de.uzk.hki.da.model.WorkArea;
import de.uzk.hki.da.service.HibernateUtil;
import de.uzk.hki.da.service.JmsMessageServiceHandler;
import de.uzk.hki.da.util.ConfigurationException;


/**
 * A factory for creating Action objects.
 *
 * @author Sebastian Cuy
 * @author Daniel M. de Oliveira
 */
public class ActionFactory implements ApplicationContextAware {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger("de.uzk.hki.da.core");
	
	/** The action registry. */
	private ActionRegistry actionRegistry;
	
	private FileFormatFacade fileFormatFacade;
	
	/** The local node. */
	private Node localNode; // Node the ContentBroker actually runs on
	
	private JobNamedQueryDAO qc;
	
	/** The user exception manager. */
	private UserExceptionManager userExceptionManager;
	
	/** The context. */
	private ApplicationContext context;
	
	/** The on halt. */
	private boolean onHalt = false;
	
	/** *The JmsMessageService */
	private JmsMessageServiceHandler jmsMessageServiceHandler;

	private PreservationSystem preservationSystem;

	private List<String> availableJobTypes;

	
	public void init(){
		setPreservationSystem(new PreservationSystem()); getPreservationSystem().setId(1);
		
		// attach policies to preservation system
		Session session = HibernateUtil.openSession();
		session.beginTransaction();
		session.refresh(preservationSystem);
		// replace proxies by real objects if loading lazily.
		Hibernate.initialize(getPreservationSystem().getConversion_policies());

		for (SubformatIdentificationStrategyPuidMapping sfiP:getSecondStageScanPolicies(session)) {
			fileFormatFacade.registerSubformatIdentificationStrategyPuidMapping(sfiP.getSubformatIdentificationStrategyName(),sfiP.getFormatPuid());
		}
		session.close();
		
		
	
	}
	
	private void injectProperties(AbstractAction action, Job job){
		action.setUserExceptionManager(userExceptionManager);
		action.setJmsMessageServiceHandler(jmsMessageServiceHandler);
		action.setLocalNode(localNode);
		job.getObject().setTransientNodeRef(localNode);
		action.setObject(job.getObject());
		action.setActionMap(getActionRegistry());
		action.setActionFactory(this);
		action.setJob(job);
		action.setPSystem(getPreservationSystem());
		
	}
	
	
	
	private void checkPreservationSystemNode() throws IllegalStateException {
		StringBuilder msg = new StringBuilder();
		
		if (getActionRegistry() == null) msg.append("Unable to build action. Action map has not been set.\n");
		if (getLocalNode()==null) msg.append("Unable to build action. Node not set.\n");
		if (getUserExceptionManager()==null) msg.append("User exception manager not set.\n");
		if (getPreservationSystem()==null) msg.append("PreservationSystem not set.\n");
		else {
			if (getPreservationSystem().getMinRepls()==null) msg.append("Min repls not set.\n");
			if (preservationSystem.getMinRepls()==0) msg.append("MinNodes, 0 is not allowed!\n");
			if (getPreservationSystem().getAdmin()==null) msg.append("Node admin not set.\n");
			if (isNotSet(preservationSystem.getAdmin().getEmailAddress()))  
				msg.append("Not set: systemFromEmailAdress.\n");
			if (isNotSet(preservationSystem.getUrnNameSpace())) 
				msg.append("Not set: URN NameSpace parameter.\n");
			if (isNotSet(preservationSystem.getUrisCho())) 
				msg.append("Not set: choBaseUri.\n");
			if (isNotSet(preservationSystem.getUrisAggr())) 
				msg.append("Not set: aggrBaseUri.\n");
			if (isNotSet(preservationSystem.getUrisLocal())) 
				msg.append("Not set: localBaseUri.\n");
			if (getPreservationSystem().getMinRepls()<3) logger.warn("min_repls lower than 3 not recommended for lta");
		}
		
		if (! msg.toString().isEmpty())
			throw new IllegalStateException(msg.toString());
	}
	
	
	
	private void checkJobActionContractorObject(AbstractAction action) {
		StringBuilder msg = new StringBuilder();
		
		if (action.getJob()==null) msg.append("Not set: job.\n");
		if (action.getObject()==null) {
			msg.append("Not set: object\n");
		}else {
			if (action.getObject().getLatestPackage().getContainerName()==null) msg.append("Not set: containerName of latest package.\n");
			if (action.getObject().getIdentifier()==null) 
				msg.append("Not set: object identifier not set.\n");
			if (action.getObject().getContractor()==null) {
				msg.append("Not set: contractor\n");
			}else {
				if (action.getObject().getContractor().getShort_name()==null) 
					msg.append("Not set: contractor short name.\n");
				if (action.getObject().getContractor().getEmailAddress()==null||action.getObject().getContractor().getEmailAddress().isEmpty()) 
					msg.append("Not set: user email.\n");
			}
			action.getObject().getLatestPackage();
		}
		
		// TODO check if folders exist
		
		if (! msg.toString().isEmpty())
			throw new IllegalStateException(msg.toString());
	}
	
	
	/**
	 * Following the defined priorities (context) of
	 * job types this method tests for every job type 
	 * if there are jobs for this type found in the database 
	 * which have to be done.
	 * 
	 * @return the first job found or
	 * null if Factory is on halt or no job of any type found
	 * that can be started.
	 */
	public AbstractAction buildNextAction() {		
		if (context == null) throw new ConfigurationException("Unable to build action. Application context has not been set.");
		try{
			checkPreservationSystemNode();
		} catch (IllegalStateException e) {
			logger.error(e.getMessage());
			onHalt=true;
			return null;
		}
		
		if (onHalt){
			logger.info("ActionFactory is on halt. Waiting to resume work ...");
			return null;
		}
		
		return selectActionToExecute();
		
		
	}
	
	private AbstractAction selectActionToExecute() {
		availableJobTypes = actionRegistry.getAvailableJobTypes();
		logger.trace("available job types: " + availableJobTypes);
		
		for (String jobType : availableJobTypes) {

			AbstractAction action = (AbstractAction) context.getBean(jobType);
			
			
			Job jobCandidate = qc.fetchJobFromQueue(action.getStartStatus(), status(action,
					WORKFLOW_STATUS_DIGIT_WORKING)
					, localNode);
			if (jobCandidate == null) {
				jobCandidate = qc.fetchJobFromQueue(status(action,
						WORKFLOW_STATUS_DIGIT_UP_TO_ROLLBACK), WORKFLOW_STATUS_DIGIT_WORKING, localNode);
				if (jobCandidate == null) {
					logger.trace("No job for type {}, checking for types with lower priority", jobType);
					continue;
				}
				action.setROLLBACKONLY(true);
			}
			logger.info("fetched job: {}", jobCandidate);

			
			injectProperties(action,jobCandidate);
			try {
				checkJobActionContractorObject(action);
			}catch(IllegalStateException e) {
				logger.error(e.getMessage());
				qc.updateJobStatus(jobCandidate, status(action,WORKFLOW_STATUS_DIGIT_ERROR_MODEL_INCONSISTENT));
				continue;
			}
			try {
				action.checkConfiguration();
			}catch(ConfigurationException e) {
				logger.error("Regarding job for object ["+jobCandidate.getObject().getIdentifier()+"]. Bad configuration of action. "+e.getMessage());
				qc.updateJobStatus(jobCandidate, status(action,WORKFLOW_STATUS_DIGIT_ERROR_BAD_CONFIGURATION));
				continue;
			}
			try {
				action.setWorkArea(new WorkArea(localNode,jobCandidate.getObject()));
				action.synchronizeObjectDatabaseAndFileSystemState();
				action.checkPreconditions();
			}catch(PreconditionsNotMetException e) {
				logger.error("Regarding job for object ["+jobCandidate.getObject().getIdentifier()+"]. Preconfigurations not met for action. "+e.getMessage());
				qc.updateJobStatus(jobCandidate, status(action,WORKFLOW_STATUS_DIGIT_ERROR_PRECONDITIONS_NOT_MET));
				continue;
			}
			
			actionRegistry.registerAction(action);
			return action;
		}
		logger.info("(for local node) No jobs in queue, nothing to do, shoobidoowoo, ...");
		return null;
	}
	
	
	private String status(AbstractAction action,String digit) {
		return action.getStartStatus().substring(0,action.getStartStatus().length()-1) + digit;
	}
	
	
	public JmsMessageServiceHandler getJmsMessageService() {
		return jmsMessageServiceHandler;
	}

	public void setJmsMessageServiceHandler(JmsMessageServiceHandler jmsMessageService) {
		this.jmsMessageServiceHandler = jmsMessageService;
	}

	/**
	 * Gets the second stage scan policies.
	 *
	 * @return the second stage scan policies
	 */
	private List<SubformatIdentificationStrategyPuidMapping> getSecondStageScanPolicies(Session session) {
		@SuppressWarnings("unchecked")
		List<SubformatIdentificationStrategyPuidMapping> l = session
				.createQuery("from SubformatIdentificationStrategyPuidMapping").list();

		return l;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.context = context;		
	}

	/**
	 * Gets the action registry.
	 *
	 * @return the action registry
	 */
	public ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	/**
	 * Sets the action registry.
	 *
	 * @param actionRegistry the new action registry
	 */
	public void setActionRegistry(ActionRegistry actionRegistry) {
		this.actionRegistry = actionRegistry;
	}
	
	
	/**
	 * Gets the user exception manager.
	 *
	 * @return the user exception manager
	 */
	public UserExceptionManager getUserExceptionManager() {
		return userExceptionManager;
	}
	
	/**
	 * Sets the user exception manager.
	 *
	 * @param userExceptionManager the new user exception manager
	 */
	public void setUserExceptionManager(UserExceptionManager userExceptionManager) {
		this.userExceptionManager = userExceptionManager;
	}

	/**
	 * Pause.
	 *
	 * @param b the b
	 */
	public void pause(boolean b) {
		onHalt  = b;
	}
	
	public boolean paused() {
		return onHalt;
	}


	/**
	 * Gets the local node.
	 *
	 * @return the local node
	 */
	public Node getLocalNode() {
		return localNode;
	}

	/**
	 * Sets the local node.
	 *
	 * @param localNode the new local node
	 */
	public void setLocalNode(Node localNode) {
		this.localNode = localNode;
	}

	public PreservationSystem getPreservationSystem() {
		return preservationSystem;
	}

	public void setPreservationSystem(PreservationSystem preservationSystem) {
		this.preservationSystem = preservationSystem;
	}

	public JobNamedQueryDAO getQueueConnector() {
		return qc;
	}

	public void setQueueConnector(JobNamedQueryDAO qc) {
		this.qc = qc;
	}

	public FileFormatFacade getFileFormatFacade() {
		return fileFormatFacade;
	}

	public void setFileFormatFacade(FileFormatFacade fileFormatFacade) {
		this.fileFormatFacade = fileFormatFacade;
	}
}
