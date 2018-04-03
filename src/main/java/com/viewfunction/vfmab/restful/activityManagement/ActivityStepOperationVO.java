package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityStepOperationVO")
public class ActivityStepOperationVO {
	private String activitySpaceName;
	private String activityType;	
	private String activityStepName;	
	private String activityId;
	private String currentStepOwner;
	private String newStepOwner;
	private String activityStepRelatedRole;
	private String activityStepRelatedRoleQueue;
	private String activityStepResponse;
	private long activityStepDueDate;
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getActivityStepName() {
		return activityStepName;
	}
	public void setActivityStepName(String activityStepName) {
		this.activityStepName = activityStepName;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public String getCurrentStepOwner() {
		return currentStepOwner;
	}
	public void setCurrentStepOwner(String currentStepOwner) {
		this.currentStepOwner = currentStepOwner;
	}
	public String getNewStepOwner() {
		return newStepOwner;
	}
	public void setNewStepOwner(String newStepOwner) {
		this.newStepOwner = newStepOwner;
	}
	public String getActivityStepRelatedRole() {
		return activityStepRelatedRole;
	}
	public void setActivityStepRelatedRole(String activityStepRelatedRole) {
		this.activityStepRelatedRole = activityStepRelatedRole;
	}
	public String getActivityStepRelatedRoleQueue() {
		return activityStepRelatedRoleQueue;
	}
	public void setActivityStepRelatedRoleQueue(
			String activityStepRelatedRoleQueue) {
		this.activityStepRelatedRoleQueue = activityStepRelatedRoleQueue;
	}
	public String getActivityStepResponse() {
		return activityStepResponse;
	}
	public void setActivityStepResponse(String activityStepResponse) {
		this.activityStepResponse = activityStepResponse;
	}
	public long getActivityStepDueDate() {
		return activityStepDueDate;
	}
	public void setActivityStepDueDate(long activityStepDueDate) {
		this.activityStepDueDate = activityStepDueDate;
	}
}