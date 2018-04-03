package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;

@XmlRootElement(name = "ActivityCommentVO")
public class ActivityCommentVO {
	private String commentContent;
	private long createdDate;
	private RoleVO creatorRole;
	private ParticipantDetailInfoVO creatorParticipant;
	public String getCommentContent() {
		return commentContent;
	}
	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}
	public long getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}
	public RoleVO getCreatorRole() {
		return creatorRole;
	}
	public void setCreatorRole(RoleVO creatorRole) {
		this.creatorRole = creatorRole;
	}
	public ParticipantDetailInfoVO getCreatorParticipant() {
		return creatorParticipant;
	}
	public void setCreatorParticipant(ParticipantDetailInfoVO creatorParticipant) {
		this.creatorParticipant = creatorParticipant;
	}
}