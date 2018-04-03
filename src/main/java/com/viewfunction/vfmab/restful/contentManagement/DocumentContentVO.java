package com.viewfunction.vfmab.restful.contentManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;

@XmlRootElement(name = "DocumentContentVO")
public class DocumentContentVO {
	private String documentName;
	private String documentFolderPath;
	private long documentCreateDate;
	private long documentLastUpdateDate;
	private ParticipantDetailInfoVO documentCreator;
	private ParticipantDetailInfoVO documentLastUpdatePerson;
	private ParticipantDetailInfoVO documentLocker;
	private String documentType;	
	private boolean isFolder;
	private String version;
	private long documentSize;
	private long childDocumentNumber;	
	private boolean locked;
	private String lockedBy;
	private boolean linked;	
	private String[] documentTags;
	private List<ContentPermissionVO> contentPermissions;
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	public String getDocumentFolderPath() {
		return documentFolderPath;
	}
	public void setDocumentFolderPath(String documentFolderPath) {
		this.documentFolderPath = documentFolderPath;
	}
	public long getDocumentCreateDate() {
		return documentCreateDate;
	}
	public void setDocumentCreateDate(long documentCreateDate) {
		this.documentCreateDate = documentCreateDate;
	}
	public long getDocumentLastUpdateDate() {
		return documentLastUpdateDate;
	}
	public void setDocumentLastUpdateDate(long documentLastUpdateDate) {
		this.documentLastUpdateDate = documentLastUpdateDate;
	}	
	public String getDocumentType() {
		return documentType;
	}
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}
	public boolean isFolder() {
		return isFolder;
	}
	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public long getDocumentSize() {
		return documentSize;
	}
	public void setDocumentSize(long documentSize) {
		this.documentSize = documentSize;
	}
	public long getChildDocumentNumber() {
		return childDocumentNumber;
	}
	public void setChildDocumentNumber(long childDocumentNumber) {
		this.childDocumentNumber = childDocumentNumber;
	}
	public ParticipantDetailInfoVO getDocumentCreator() {
		return documentCreator;
	}
	public void setDocumentCreator(ParticipantDetailInfoVO documentCreator) {
		this.documentCreator = documentCreator;
	}
	public ParticipantDetailInfoVO getDocumentLastUpdatePerson() {
		return documentLastUpdatePerson;
	}
	public void setDocumentLastUpdatePerson(ParticipantDetailInfoVO documentLastUpdatePerson) {
		this.documentLastUpdatePerson = documentLastUpdatePerson;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public boolean isLinked() {
		return linked;
	}
	public void setLinked(boolean linked) {
		this.linked = linked;
	}
	public String[] getDocumentTags() {
		return documentTags;
	}
	public void setDocumentTags(String[] documentTags) {
		this.documentTags = documentTags;
	}
	public String getLockedBy() {
		return lockedBy;
	}
	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}
	public ParticipantDetailInfoVO getDocumentLocker() {
		return documentLocker;
	}
	public void setDocumentLocker(ParticipantDetailInfoVO documentLocker) {
		this.documentLocker = documentLocker;
	}
	public List<ContentPermissionVO> getContentPermissions() {
		return contentPermissions;
	}
	public void setContentPermissions(List<ContentPermissionVO> contentPermissions) {
		this.contentPermissions = contentPermissions;
	}	
}