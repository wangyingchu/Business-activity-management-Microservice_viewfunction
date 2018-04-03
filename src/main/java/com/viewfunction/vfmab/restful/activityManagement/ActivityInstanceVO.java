package com.viewfunction.vfmab.restful.activityManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;

@XmlRootElement(name = "ActivityInstanceVO")
public class ActivityInstanceVO {
	private ActivityTypeDefinitionVO activityTypeDefinition;	
	private String activityId;
	private boolean isFinished;	
	private long activityDuration;
	private long activityStartTime;
	private long activityEndTime;
	private String activityStartUserId;	
	private ParticipantDetailInfoVO activityStartUserParticipant;
	private List<ActivityStepVO> currentActivitySteps;	
	private List<String> nextActivitySteps;	
	private List<ActivityStepVO> finishedActivitySteps;
	private boolean isSuspended;
	
	public ActivityTypeDefinitionVO getActivityTypeDefinition() {
		return activityTypeDefinition;
	}
	public void setActivityTypeDefinition(ActivityTypeDefinitionVO activityTypeDefinition) {
		this.activityTypeDefinition = activityTypeDefinition;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public boolean getIsFinished() {
		return isFinished;
	}
	public void setIsFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	public long getActivityDuration() {
		return activityDuration;
	}
	public void setActivityDuration(long activityDuration) {
		this.activityDuration = activityDuration;
	}
	public long getActivityStartTime() {
		return activityStartTime;
	}
	public void setActivityStartTime(long activityStartTime) {
		this.activityStartTime = activityStartTime;
	}
	public long getActivityEndTime() {
		return activityEndTime;
	}
	public void setActivityEndTime(long activityEndTime) {
		this.activityEndTime = activityEndTime;
	}
	public String getActivityStartUserId() {
		return activityStartUserId;
	}
	public void setActivityStartUserId(String activityStartUserId) {
		this.activityStartUserId = activityStartUserId;
	}
	public List<ActivityStepVO> getCurrentActivitySteps() {
		return currentActivitySteps;
	}
	public void setCurrentActivitySteps(List<ActivityStepVO> currentActivitySteps) {
		this.currentActivitySteps = currentActivitySteps;
	}
	public List<String> getNextActivitySteps() {
		return nextActivitySteps;
	}
	public void setNextActivitySteps(List<String> nextActivitySteps) {
		this.nextActivitySteps = nextActivitySteps;
	}
	public List<ActivityStepVO> getFinishedActivitySteps() {
		return finishedActivitySteps;
	}
	public void setFinishedActivitySteps(List<ActivityStepVO> finishedActivitySteps) {
		this.finishedActivitySteps = finishedActivitySteps;
	}
	public ParticipantDetailInfoVO getActivityStartUserParticipant() {
		return activityStartUserParticipant;
	}
	public void setActivityStartUserParticipant(
			ParticipantDetailInfoVO activityStartUserParticipant) {
		this.activityStartUserParticipant = activityStartUserParticipant;
	}
	public boolean getIsSuspended() {
		return isSuspended;
	}
	public void setIsSuspended(boolean isSuspendedActivity) {
		this.isSuspended = isSuspendedActivity;
	}
}
