package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ApplicationSpaceFolderQueryVO")
public class ApplicationSpaceFolderQueryVO {
	private String activitySpaceName;	
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
}