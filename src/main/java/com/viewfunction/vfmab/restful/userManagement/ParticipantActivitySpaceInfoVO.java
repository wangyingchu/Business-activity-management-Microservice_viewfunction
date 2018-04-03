package com.viewfunction.vfmab.restful.userManagement;

import com.viewfunction.vfmab.restful.activityManagement.RoleVO;

public class ParticipantActivitySpaceInfoVO {
	private RoleVO[] participantRoles;
	private String participantId;
	private String participantName;	
	public String getParticipantId() {
		return participantId;
	}
	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}
	public String getParticipantName() {
		return participantName;
	}
	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
	public RoleVO[] getParticipantRoles() {
		return participantRoles;
	}
	public void setParticipantRoles(RoleVO[] participantRoles) {
		this.participantRoles = participantRoles;
	}
}