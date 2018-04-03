package com.viewfunction.vfmab.restful.commentManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.vfmab.restful.contentManagement.ApplicationSpaceFileVO;

@XmlRootElement(name = "AddApplicationSpaceDocumentCommentVO")
public class AddApplicationSpaceDocumentCommentVO {
	private ApplicationSpaceFileVO applicationSpaceDocument;
	private AddCommentVO newComment;
	public ApplicationSpaceFileVO getApplicationSpaceDocument() {
		return applicationSpaceDocument;
	}
	public void setApplicationSpaceDocument(ApplicationSpaceFileVO applicationSpaceDocument) {
		this.applicationSpaceDocument = applicationSpaceDocument;
	}
	public AddCommentVO getNewComment() {
		return newComment;
	}
	public void setNewComment(AddCommentVO newComment) {
		this.newComment = newComment;
	}
}
