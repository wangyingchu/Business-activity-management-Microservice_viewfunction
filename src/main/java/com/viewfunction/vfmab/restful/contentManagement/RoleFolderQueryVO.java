package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RoleFolderQueryVO")
public class RoleFolderQueryVO {
	private String activitySpaceName;
	private String roleName;
	private String parentFolderPath;
	private String folderName;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}	
	public String getParentFolderPath() {
		return parentFolderPath;
	}
	public void setParentFolderPath(String parentFolderPath) {
		this.parentFolderPath = parentFolderPath;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}