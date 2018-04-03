package com.viewfunction.vfmab.restful.contentManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "SetActivityFolderPermissionsVO")
public class SetActivityFolderPermissionsVO {
	private ActivityFolderQueryVO activityFolder;
	private List<ContentPermissionVO> permissionsList;
	public List<ContentPermissionVO> getPermissionsList() {
		return permissionsList;
	}
	public void setPermissionsList(List<ContentPermissionVO> permissionsList) {
		this.permissionsList = permissionsList;
	}
	public ActivityFolderQueryVO getActivityFolder() {
		return activityFolder;
	}
	public void setActivityFolder(ActivityFolderQueryVO activityFolder) {
		this.activityFolder = activityFolder;
	}
}
