package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RoleFileVO")
public class RoleFileVO {
	private String activitySpaceName;
	private String participantName;
	private String roleName;
	private String parentFolderPath;
	private String fileName;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getParticipantName() {
		return participantName;
	}
	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getParentFolderPath() {
		return parentFolderPath;
	}
	public void setParentFolderPath(String parentFolderPath) {
		this.parentFolderPath = parentFolderPath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}	
}