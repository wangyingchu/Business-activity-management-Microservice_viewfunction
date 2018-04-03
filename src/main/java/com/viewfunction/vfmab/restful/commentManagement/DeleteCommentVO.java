package com.viewfunction.vfmab.restful.commentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeleteCommentVO")
public class DeleteCommentVO {
	private String activitySpaceName;
	private String commentUUID;	
	private String operatorId;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}	
	public String getCommentUUID() {
		return commentUUID;
	}
	public void setCommentUUID(String commentUUID) {
		this.commentUUID = commentUUID;
	}
	public String getOperatorId() {
		return operatorId;
	}
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}	
}
