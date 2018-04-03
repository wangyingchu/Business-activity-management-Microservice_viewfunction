package com.viewfunction.vfmab.restful.contentManagement;

public class ContentPermissionVO {	
	private boolean displayContentPermission;
	private boolean addContentPermission;
	private boolean addSubFolderPermission;
	private boolean deleteContentPermission;
	private boolean deleteSubFolderPermission;
	private boolean editContentPermission;
	private boolean configPermissionPermission;	
	private String permissionParticipant;
	private String permissionScope;	
	
	public String getPermissionParticipant() {
		return permissionParticipant;
	}	
	
	public void setPermissionParticipant(String permissionParticipant) {
		this.permissionParticipant = permissionParticipant;
	}	
	
	public String getPermissionScope() {
		return permissionScope;
	}	
	
	public void setPermissionScope(String permissionScope) {
		this.permissionScope = permissionScope;
	}

	public boolean getDisplayContentPermission() {
		return displayContentPermission;
	}

	public void setDisplayContentPermission(boolean displayContentPermission) {
		this.displayContentPermission = displayContentPermission;
	}

	public boolean getAddContentPermission() {
		return addContentPermission;
	}

	public void setAddContentPermission(boolean addContentPermission) {
		this.addContentPermission = addContentPermission;
	}

	public boolean getDeleteContentPermission() {
		return deleteContentPermission;
	}

	public void setDeleteContentPermission(boolean deleteContentPermission) {
		this.deleteContentPermission = deleteContentPermission;
	}

	public boolean getEditContentPermission() {
		return editContentPermission;
	}

	public void setEditContentPermission(boolean editContentPermission) {
		this.editContentPermission = editContentPermission;
	}

	public boolean getConfigPermissionPermission() {
		return configPermissionPermission;
	}

	public void setConfigPermissionPermission(boolean configPermissionPermission) {
		this.configPermissionPermission = configPermissionPermission;
	}

	public boolean getAddSubFolderPermission() {
		return addSubFolderPermission;
	}

	public void setAddSubFolderPermission(boolean addSubFolderPermission) {
		this.addSubFolderPermission = addSubFolderPermission;
	}

	public boolean getDeleteSubFolderPermission() {
		return deleteSubFolderPermission;
	}

	public void setDeleteSubFolderPermission(boolean deleteSubFolderPermission) {
		this.deleteSubFolderPermission = deleteSubFolderPermission;
	}	
}