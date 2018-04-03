package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityCommentOperationVO")
public class ActivityCommentOperationVO {
	private String activitySpaceName;
	private String activityType;	
	private String activityStepName;	
	private String activityId;	
	private String commentType;
	private String commentWriter;
	private String commentContent;
	private String commentWriterRoleName;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getActivityStepName() {
		return activityStepName;
	}
	public void setActivityStepName(String activityStepName) {
		this.activityStepName = activityStepName;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public String getCommentType() {
		return commentType;
	}
	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}
	public String getCommentWriter() {
		return commentWriter;
	}
	public void setCommentWriter(String commentWriter) {
		this.commentWriter = commentWriter;
	}
	public String getCommentContent() {
		return commentContent;
	}
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
	public String getCommentWriterRoleName() {
		return commentWriterRoleName;
	}
	public void setCommentWriterRoleName(String commentWriterRoleName) {
		this.commentWriterRoleName = commentWriterRoleName;
	}
}