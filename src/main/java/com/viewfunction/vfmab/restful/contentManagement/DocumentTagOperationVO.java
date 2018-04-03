package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DocumentTagOperationVO")
public class DocumentTagOperationVO {
	private String activitySpaceName;
	private String documentsOwnerType;
	private String tagValue;
	private ParticipantFileVO participantFileInfo;
	private ActivityTypeFileVO activityTypeFileInfo;
	private ApplicationSpaceFileVO applicationSpaceFileInfo;
	private RoleFileVO roleFileInfo;
	private String[] tagValues;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getDocumentsOwnerType() {
		return documentsOwnerType;
	}
	public void setDocumentsOwnerType(String documentsOwnerType) {
		this.documentsOwnerType = documentsOwnerType;
	}
	public String getTagValue() {
		return tagValue;
	}
	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}
	public ParticipantFileVO getParticipantFileInfo() {
		return participantFileInfo;
	}
	public void setParticipantFileInfo(ParticipantFileVO participantFileInfo) {
		this.participantFileInfo = participantFileInfo;
	}
	public ActivityTypeFileVO getActivityTypeFileInfo() {
		return activityTypeFileInfo;
	}
	public void setActivityTypeFileInfo(ActivityTypeFileVO activityTypeFileInfo) {
		this.activityTypeFileInfo = activityTypeFileInfo;
	}
	public ApplicationSpaceFileVO getApplicationSpaceFileInfo() {
		return applicationSpaceFileInfo;
	}
	public void setApplicationSpaceFileInfo(ApplicationSpaceFileVO applicationSpaceFileInfo) {
		this.applicationSpaceFileInfo = applicationSpaceFileInfo;
	}
	public RoleFileVO getRoleFileInfo() {
		return roleFileInfo;
	}
	public void setRoleFileInfo(RoleFileVO roleFileInfo) {
		this.roleFileInfo = roleFileInfo;
	}
	public String[] getTagValues() {
		return tagValues;
	}
	public void setTagValues(String[] tagValues) {
		this.tagValues = tagValues;
	}
}