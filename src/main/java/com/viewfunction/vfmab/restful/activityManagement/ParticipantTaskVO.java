package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ParticipantTaskVO")
public class ParticipantTaskVO {
	private String activityType;
	private String roleName;
	private String activityStepName;
	private String stepDescription;
	private long createTime;
	private long dueDate;
	private String stepAssignee;
	private String stepOwner;
	private ActivityStepVO activityStep;
	private String dueStatus;
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getActivityStepName() {
		return activityStepName;
	}
	public void setActivityStepName(String activityStepName) {
		this.activityStepName = activityStepName;
	}
	public String getStepDescription() {
		return stepDescription;
	}
	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getDueDate() {
		return dueDate;
	}
	public void setDueDate(long dueDate) {
		this.dueDate = dueDate;
	}
	public String getStepAssignee() {
		return stepAssignee;
	}
	public void setStepAssignee(String stepAssignee) {
		this.stepAssignee = stepAssignee;
	}
	public String getStepOwner() {
		return stepOwner;
	}
	public void setStepOwner(String stepOwner) {
		this.stepOwner = stepOwner;
	}
	public ActivityStepVO getActivityStep() {
		return activityStep;
	}
	public void setActivityStep(ActivityStepVO activityStep) {
		this.activityStep = activityStep;
	}
	public String getDueStatus() {
		return dueStatus;
	}
	public void setDueStatus(String dueStatus) {
		this.dueStatus = dueStatus;
	}
}