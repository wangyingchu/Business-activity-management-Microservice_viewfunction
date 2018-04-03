package com.viewfunction.vfmab.restful.commentManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.vfmab.restful.contentManagement.ParticipantFileVO;

@XmlRootElement(name = "AddParticipantentDocumentCommentVO")
public class AddParticipantentDocumentCommentVO {
	private ParticipantFileVO participantDocument;	
	private AddCommentVO newComment;
	public ParticipantFileVO getParticipantDocument() {
		return participantDocument;
	}
	public void setParticipantDocument(ParticipantFileVO participantDocument) {
		this.participantDocument = participantDocument;
	}
	public AddCommentVO getNewComment() {
		return newComment;
	}
	public void setNewComment(AddCommentVO newComment) {
		this.newComment = newComment;
	}
}