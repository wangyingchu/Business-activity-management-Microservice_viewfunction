package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PreviewTempFileGenerateVO")
public class PreviewTempFileGenerateVO {
	private String activitySpaceName;
	private String documentsOwnerType;
	private String tempFileName;
	private ParticipantFileVO participantFileInfo;
	private ActivityTypeFileVO activityTypeFileInfo;
	private ApplicationSpaceFileVO applicationSpaceFileInfo;
	private RoleFileVO roleFileInfo;	
	private boolean needDocumentConvert;
	private String convertOperation;
	public String getDocumentsOwnerType() {
		return documentsOwnerType;
	}
	public void setDocumentsOwnerType(String documentsOwnerType) {
		this.documentsOwnerType = documentsOwnerType;
	}
	public String getTempFileName() {
		return tempFileName;
	}
	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}	
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
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
	public boolean isNeedDocumentConvert() {
		return needDocumentConvert;
	}
	public void setNeedDocumentConvert(boolean needDocumentConvert) {
		this.needDocumentConvert = needDocumentConvert;
	}
	public String getConvertOperation() {
		return convertOperation;
	}
	public void setConvertOperation(String convertOperation) {
		this.convertOperation = convertOperation;
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
}