package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityTypeFileVO")
public class ActivityTypeFileVO {
	private String activitySpaceName;
	private String activityName;
	private String activityId;
	private String parentFolderPath;
	private String fileName;
	private String participantName;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getActivityName() {
		return activityName;
	}
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
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
	public String getParticipantName() {
		return participantName;
	}
	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
}