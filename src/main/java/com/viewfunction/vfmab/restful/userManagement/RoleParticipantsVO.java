package com.viewfunction.vfmab.restful.userManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVOsList;
@XmlRootElement(name = "RoleParticipantsVO")
public class RoleParticipantsVO {
	private String roleName;
	private String roleDisplayName;	
	private ParticipantDetailInfoVOsList roleParticipants;
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getRoleDisplayName() {
		return roleDisplayName;
	}
	public void setRoleDisplayName(String roleDisplayName) {
		this.roleDisplayName = roleDisplayName;
	}
	public ParticipantDetailInfoVOsList getRoleParticipants() {
		return roleParticipants;
	}
	public void setRoleParticipants(ParticipantDetailInfoVOsList roleParticipants) {
		this.roleParticipants = roleParticipants;
	}
}