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

package de.uzk.hki.da.model;
import javax.persistence.*;

import de.uzk.hki.da.utils.Utilities;

import java.lang.Object;


/**
 * The Class Node.
 */
@Entity
@Table(name="nodes")
public class Node{
	
	/** The id. */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	/** The name. */
	private String name;

	/** The urn_index. */
	private int urn_index=-1;
	
	/** The repl_destinations. */
	@Transient private String repl_destinations;
	
	/** The dip area root path. */
	@Transient private String dipAreaRootPath;
	
	/** The work area root path. */
	@Transient private String workAreaRootPath;
	
	/** The user area root path. */
	@Transient private String userAreaRootPath;
	
	/** The ingest area root path. */
	@Transient private String ingestAreaRootPath;
	
	/** The grid cache area root path. */
	@Transient private String gridCacheAreaRootPath;
	
	/** The admin email. */
	@Transient private String adminEmail;
	
	/** The working resource. */
	@Transient private String workingResource;
	
	/** The dip resource. */
	@Transient private String dipResource;

	/**
	 * Instantiates a new node.
	 */
	public Node(){}
	
	/**
	 * Instantiates a new node.
	 *
	 * @param name the name
	 * @param urn_index the urn_index
	 */
	public Node(String name,int urn_index){
		this.urn_index=urn_index;
		this.name=name;
	}
	
	/**
	 * Only for Testing purposes.
	 *
	 * @param id the id
	 * @param name the name
	 */
	public Node(int id,String name){
		this.id=id; this.name=name;
	}
	
	
	/**
	 * Instantiates a new node.
	 *
	 * @param name the name
	 * @param workingResource the working resource
	 */
	public Node(String name,String workingResource){
		this.name=name;
		this.workingResource=workingResource;
	}
	
	
	/**
	 * Instantiates a new node.
	 *
	 * @param name the name
	 */
	public Node(String name){
		this.name=name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Sets the working resource.
	 *
	 * @param working_resource the new working resource
	 */
	public void setWorkingResource(String working_resource) {
		this.workingResource = working_resource;
	}
	
	/**
	 * Gets the working resource.
	 *
	 * @return the working resource
	 */
	public String getWorkingResource() {
		return workingResource;
	}
	
	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return "Node["+name+","+workingResource+"]";
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override 
	public boolean equals(Object o){
		if (o == this) return true;

		if (o instanceof String){
			if (((String) o).equals(this.name)) return true;
			return false;
		}
		
		if (!(o instanceof Node)) return false;
		
		Node no= (Node) o;
		
		if (no.getName().equals(this.getName())) return true;
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override 
	public int hashCode(){
		
		return 31+name.hashCode();
	}

	/**
	 * Gets the urn_index.
	 *
	 * @return the urn_index
	 */
	public int getUrn_index() {
		return urn_index;
	}

	/**
	 * Sets the urn_index.
	 *
	 * @param urn_index the new urn_index
	 */
	public void setUrn_index(int urn_index) {
		this.urn_index = urn_index;
	}

	/**
	 * Sets the repl_destinations.
	 *
	 * @param replicas the new repl_destinations
	 */
	public void setReplDestinations(String replicas) {
		this.repl_destinations = replicas;
	}


	/**
	 * Gets the repl_destinations.
	 *
	 * @return the replica destinations
	 */
	public String getReplDestinations() {
		return repl_destinations;
	}

	/**
	 * Gets the work area root path.
	 *
	 * @return the work area root path
	 */
	public String getWorkAreaRootPath() {
		return workAreaRootPath;
	}


	/**
	 * Sets the work area root path.
	 *
	 * @param workAreaRootPath the new work area root path
	 */
	public void setWorkAreaRootPath(String workAreaRootPath) {
		this.workAreaRootPath = Utilities.slashize(workAreaRootPath);
	}

	/**
	 * Gets the dip area root path.
	 *
	 * @return the dip area root path
	 */
	public String getDipAreaRootPath() {
		return dipAreaRootPath;
	}


	/**
	 * Sets the dip area root path.
	 *
	 * @param dipAreaRootPath the new dip area root path
	 */
	public void setDipAreaRootPath(String dipAreaRootPath) {
		this.dipAreaRootPath = Utilities.slashize(dipAreaRootPath);
	}


	/**
	 * Gets the user area root path.
	 *
	 * @return the user area root path
	 */
	public String getUserAreaRootPath() {
		return userAreaRootPath;
	}


	/**
	 * Sets the user area root path.
	 *
	 * @param transferAreaRootPath the new user area root path
	 */
	public void setUserAreaRootPath(String transferAreaRootPath) {
		this.userAreaRootPath = Utilities.slashize(transferAreaRootPath);
	}


	/**
	 * Gets the ingest area root path.
	 *
	 * @return the ingest area root path
	 */
	public String getIngestAreaRootPath() {
		return ingestAreaRootPath;
	}


	/**
	 * Sets the ingest area root path.
	 *
	 * @param ingestAreaRootPath the new ingest area root path
	 */
	public void setIngestAreaRootPath(String ingestAreaRootPath) {
		this.ingestAreaRootPath = Utilities.slashize(ingestAreaRootPath);
	}
	
	/**
	 * Sets the admin email.
	 *
	 * @param adminEmail the new admin email
	 */
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}
	
	/**
	 * Gets the admin email.
	 *
	 * @return the admin email
	 */
	public String getAdminEmail() {
		return adminEmail;
	}


	/**
	 * Sets the grid cache area root path.
	 *
	 * @param gridCacheAreaRootPath the gridCacheAreaRootPath to set
	 */
	public void setGridCacheAreaRootPath(String gridCacheAreaRootPath) {
		this.gridCacheAreaRootPath = Utilities.slashize(gridCacheAreaRootPath);
	}


	/**
	 * Gets the grid cache area root path.
	 *
	 * @return the gridCacheAreaRootPath
	 */
	public String getGridCacheAreaRootPath() {
		return gridCacheAreaRootPath;
	}


	/**
	 * Gets the dip resource.
	 *
	 * @return the dip resource
	 */
	public String getDipResource() {
		return dipResource;
	}


	/**
	 * Sets the dip resource.
	 *
	 * @param dipResource the new dip resource
	 */
	public void setDipResource(String dipResource) {
		this.dipResource = dipResource;
	}
	
}