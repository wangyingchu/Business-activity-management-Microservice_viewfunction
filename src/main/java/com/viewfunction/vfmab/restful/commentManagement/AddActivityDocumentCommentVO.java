package com.viewfunction.vfmab.restful.commentManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.vfmab.restful.contentManagement.ActivityTypeFileVO;

@XmlRootElement(name = "AddActivityDocumentCommentVO")
public class AddActivityDocumentCommentVO {
	private ActivityTypeFileVO activityDocument;	
	private AddCommentVO newComment;
	public ActivityTypeFileVO getActivityDocument() {
		return activityDocument;
	}
	public void setActivityDocument(ActivityTypeFileVO activityDocument) {
		this.activityDocument = activityDocument;
	}
	public AddCommentVO getNewComment() {
		return newComment;
	}
	public void setNewComment(AddCommentVO newComment) {
		this.newComment = newComment;
	}
}