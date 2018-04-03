package com.viewfunction.vfmab.restful.contentManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityStepOperationResultVO")
public class ContentFolderVO {
	private String folderPath;
	private String parentFolderPath;
	private String folderName;	
	private List<DocumentContentVO> childContentList;
	private boolean folderLocked;
	private List<ContentPermissionVO> folderPermissions;
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
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
	public List<DocumentContentVO> getChildContentList() {
		return childContentList;
	}
	public void setChildContentList(List<DocumentContentVO> childContentList) {
		this.childContentList = childContentList;
	}
	public boolean isFolderLocked() {
		return folderLocked;
	}
	public void setFolderLocked(boolean folderLocked) {
		this.folderLocked = folderLocked;
	}
	public List<ContentPermissionVO> getFolderPermissions() {
		return folderPermissions;
	}
	public void setFolderPermissions(List<ContentPermissionVO> folderPermissions) {
		this.folderPermissions = folderPermissions;
	}	
}