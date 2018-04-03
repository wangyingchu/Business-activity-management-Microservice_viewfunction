package com.viewfunction.vfmab.restful.commentManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;

@XmlRootElement(name = "CommentContentVO")
public class CommentContentVO {
	
	private long commentCreateDate;
	private String commentContent;	
	private ParticipantDetailInfoVO commentAuthor;
	private String commentUUID;
	private String parentCommentUUID;	
	private List<CommentContentVO> subComments;
	
	public long getCommentCreateDate() {
		return commentCreateDate;
	}
	public void setCommentCreateDate(long commentCreateDate) {
		this.commentCreateDate = commentCreateDate;
	}
	public String getCommentContent() {
		return commentContent;
	}
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
	public ParticipantDetailInfoVO getCommentAuthor() {
		return commentAuthor;
	}
	public void setCommentAuthor(ParticipantDetailInfoVO commentAuthor) {
		this.commentAuthor = commentAuthor;
	}
	public String getCommentUUID() {
		return commentUUID;
	}
	public void setCommentUUID(String commentUUID) {
		this.commentUUID = commentUUID;
	}
	public String getParentCommentUUID() {
		return parentCommentUUID;
	}
	public void setParentCommentUUID(String parentCommentUUID) {
		this.parentCommentUUID = parentCommentUUID;
	}
	public List<CommentContentVO> getSubComments() {
		return subComments;
	}
	public void setSubComments(List<CommentContentVO> subComments) {
		this.subComments = subComments;
	}
}