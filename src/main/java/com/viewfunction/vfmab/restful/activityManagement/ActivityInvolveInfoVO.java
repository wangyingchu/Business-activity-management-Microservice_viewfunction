package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;

@XmlRootElement(name = "ActivityInvolveInfoVO")
public class ActivityInvolveInfoVO {
	private ParticipantDetailInfoVO ownerInvolver;
	private ParticipantDetailInfoVO initInvolver;
	private ParticipantDetailInfoVO assigneeInvolver;
	private String involveAction;
	private long initTime;
	private long startTime;
	private long endTime;
	private boolean isChildActivityStep;
	public ParticipantDetailInfoVO getOwnerInvolver() {
		return ownerInvolver;
	}
	public void setOwnerInvolver(ParticipantDetailInfoVO ownerInvolver) {
		this.ownerInvolver = ownerInvolver;
	}
	public ParticipantDetailInfoVO getInitInvolver() {
		return initInvolver;
	}
	public void setInitInvolver(ParticipantDetailInfoVO initInvolver) {
		this.initInvolver = initInvolver;
	}
	public ParticipantDetailInfoVO getAssigneeInvolver() {
		return assigneeInvolver;
	}
	public void setAssigneeInvolver(ParticipantDetailInfoVO assigneeInvolver) {
		this.assigneeInvolver = assigneeInvolver;
	}
	public String getInvolveAction() {
		return involveAction;
	}
	public void setInvolveAction(String involveAction) {
		this.involveAction = involveAction;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getInitTime() {
		return initTime;
	}
	public void setInitTime(long initTime) {
		this.initTime = initTime;
	}
	public boolean getIsChildActivityStep() {
		return isChildActivityStep;
	}
	public void setIsChildActivityStep(boolean isChildActivityStep) {
		this.isChildActivityStep = isChildActivityStep;
	}	
}