package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;

@XmlRootElement(name = "ActivityStepVO")
public class ActivityStepVO {
	private String activityType;	
	private String activityStepName;
	private String activityStepDefinitionKey;
	private String activityId;
	private RoleVO relatedRole;
	private ActivityDataFieldValueVOList activityDataFieldValueList;	
	private long createTime;	
	private String stepAssignee;
	private ParticipantDetailInfoVO stepAssigneeParticipant;
	private String stepDescription;
	private String stepOwner;
	private ParticipantDetailInfoVO stepOwnerParticipant;
	private long finishTime;
	private String dueStatus;
	private String[] stepResponse;
	private long dueDate;	
	private boolean hasParentActivityStep;
	private boolean hasChildActivityStep;
	private String parentActivityStepName;	
	private int stepPriority;
	private boolean isSuspendedStep;
	private boolean isDelegatedStep;
	private String stepProcessEditor;
	
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
	public String getActivityStepDefinitionKey() {
		return activityStepDefinitionKey;
	}
	public void setActivityStepDefinitionKey(String activityStepDefinitionKey) {
		this.activityStepDefinitionKey = activityStepDefinitionKey;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getStepAssignee() {
		return stepAssignee;
	}
	public void setStepAssignee(String stepAssignee) {
		this.stepAssignee = stepAssignee;
	}
	public String getStepDescription() {
		return stepDescription;
	}
	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}
	public String getStepOwner() {
		return stepOwner;
	}
	public void setStepOwner(String stepOwner) {
		this.stepOwner = stepOwner;
	}	
	public RoleVO getRelatedRole() {
		return relatedRole;
	}
	public void setRelatedRole(RoleVO relatedRole) {
		this.relatedRole = relatedRole;
	}
	public ActivityDataFieldValueVOList getActivityDataFieldValueList() {
		return activityDataFieldValueList;
	}
	public void setActivityDataFieldValueList(ActivityDataFieldValueVOList activityDataFieldValueList) {
		this.activityDataFieldValueList = activityDataFieldValueList;
	}
	public String getDueStatus() {
		return dueStatus;
	}
	public void setDueStatus(String dueStatus) {
		this.dueStatus = dueStatus;
	}
	public String[] getStepResponse() {
		return stepResponse;
	}
	public void setStepResponse(String[] stepResponse) {
		this.stepResponse = stepResponse;
	}
	public long getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	public long getDueDate() {
		return dueDate;
	}
	public void setDueDate(long dueDate) {
		this.dueDate = dueDate;
	}
	public ParticipantDetailInfoVO getStepAssigneeParticipant() {
		return stepAssigneeParticipant;
	}
	public void setStepAssigneeParticipant(ParticipantDetailInfoVO stepAssigneeParticipant) {
		this.stepAssigneeParticipant = stepAssigneeParticipant;
	}
	public ParticipantDetailInfoVO getStepOwnerParticipant() {
		return stepOwnerParticipant;
	}
	public void setStepOwnerParticipant(ParticipantDetailInfoVO stepOwnerParticipant) {
		this.stepOwnerParticipant = stepOwnerParticipant;
	}
	public boolean getHasParentActivityStep() {
		return hasParentActivityStep;
	}
	public void setHasParentActivityStep(boolean hasParentActivityStep) {
		this.hasParentActivityStep = hasParentActivityStep;
	}
	public boolean getHasChildActivityStep() {
		return hasChildActivityStep;
	}
	public void setHasChildActivityStep(boolean hasChildActivityStep) {
		this.hasChildActivityStep = hasChildActivityStep;
	}
	public String getParentActivityStepName() {
		return parentActivityStepName;
	}
	public void setParentActivityStepName(String parentActivityStepName) {
		this.parentActivityStepName = parentActivityStepName;
	}
	public int getStepPriority() {
		return stepPriority;
	}
	public void setStepPriority(int stepPriority) {
		this.stepPriority = stepPriority;
	}
	public boolean getIsSuspendedStep() {
		return isSuspendedStep;
	}
	public void setIsSuspendedStep(boolean isSuspendedStep) {
		this.isSuspendedStep = isSuspendedStep;
	}
	public boolean getIsDelegatedStep() {
		return isDelegatedStep;
	}
	public void setIsDelegatedStep(boolean isDelegatedStep) {
		this.isDelegatedStep = isDelegatedStep;
	}
	public String getStepProcessEditor() {
		return stepProcessEditor;
	}
	public void setStepProcessEditor(String stepProcessEditor) {
		this.stepProcessEditor = stepProcessEditor;
	}	
}