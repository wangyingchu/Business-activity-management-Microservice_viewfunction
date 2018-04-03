package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "FileListQueryVO")
public class FileListQueryVO {
	private String queryContent;
	private boolean queryDocumentName;
	private boolean queryDocumentTag;
	private boolean queryDocumentContent;
	private String activitySpaceName;
	private String participantName;
	private String roleName;
	private String documentsOwnerType;
	public String getQueryContent() {
		return queryContent;
	}
	public void setQueryContent(String queryContent) {
		this.queryContent = queryContent;
	}
	public boolean getQueryDocumentName() {
		return queryDocumentName;
	}
	public void setQueryDocumentName(boolean queryDocumentName) {
		this.queryDocumentName = queryDocumentName;
	}
	public boolean getQueryDocumentTag() {
		return queryDocumentTag;
	}
	public void setQueryDocumentTag(boolean queryDocumentTag) {
		this.queryDocumentTag = queryDocumentTag;
	}
	public boolean getQueryDocumentContent() {
		return queryDocumentContent;
	}
	public void setQueryDocumentContent(boolean queryDocumentContent) {
		this.queryDocumentContent = queryDocumentContent;
	}
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
	public String getDocumentsOwnerType() {
		return documentsOwnerType;
	}
	public void setDocumentsOwnerType(String documentsOwnerType) {
		this.documentsOwnerType = documentsOwnerType;
	}
}