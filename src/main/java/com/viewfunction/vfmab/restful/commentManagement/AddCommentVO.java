package com.viewfunction.vfmab.restful.commentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AddCommentVO")
public class AddCommentVO {	
	private String activitySpaceName;
	private String parentCommentUUID;
	private String commentContent;
	private String commentAuthor;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getParentCommentUUID() {
		return parentCommentUUID;
	}
	public void setParentCommentUUID(String parentCommentUUID) {
		this.parentCommentUUID = parentCommentUUID;
	}
	public String getCommentContent() {
		return commentContent;
	}
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
	public String getCommentAuthor() {
		return commentAuthor;
	}
	public void setCommentAuthor(String commentAuthor) {
		this.commentAuthor = commentAuthor;
	}	
}