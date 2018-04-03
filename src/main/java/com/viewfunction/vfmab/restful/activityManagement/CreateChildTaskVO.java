package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CreateChildTaskVO")
public class CreateChildTaskVO {	
	private String activitySpaceName;
	private String activityType;	
	private String activityStepName;	
	private String activityId;
	private String currentStepOwner;	
	private String childTaskName;
	private String childTaskDescription;
	private String childTaskStepAssignee;
	private long childTaskDueDate;
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
	public String getChildTaskName() {
		return childTaskName;
	}
	public void setChildTaskName(String childTaskName) {
		this.childTaskName = childTaskName;
	}
	public String getChildTaskDescription() {
		return childTaskDescription;
	}
	public void setChildTaskDescription(String childTaskDescription) {
		this.childTaskDescription = childTaskDescription;
	}
	public String getChildTaskStepAssignee() {
		return childTaskStepAssignee;
	}
	public void setChildTaskStepAssignee(String childTaskStepAssignee) {
		this.childTaskStepAssignee = childTaskStepAssignee;
	}
	public long getChildTaskDueDate() {
		return childTaskDueDate;
	}
	public void setChildTaskDueDate(long childTaskDueDate) {
		this.childTaskDueDate = childTaskDueDate;
	}
}
