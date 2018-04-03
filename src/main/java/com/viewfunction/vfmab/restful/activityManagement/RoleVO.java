package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RoleVO")
public class RoleVO {
    private String activitySpaceName;      
    private String roleName;   
    private String description;     
    private String displayName;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}   
}