package com.viewfunction.vfmab.restful.activityManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RoleQueueVO")
public class RoleQueueVO {
	private List<RoleVO> relatedRoles;	
	private List<ActivityStepVO> activitySteps;	
	private List<ActivityDataDefinitionVO> exposedDataFields;	
	private String activitySpaceName;
	private String queueName;
	private String description;
	private String displayName;
	public List<RoleVO> getRelatedRoles() {
		return relatedRoles;
	}
	public void setRelatedRoles(List<RoleVO> relatedRoles) {
		this.relatedRoles = relatedRoles;
	}
	public List<ActivityStepVO> getActivitySteps() {
		return activitySteps;
	}
	public void setActivitySteps(List<ActivityStepVO> activitySteps) {
		this.activitySteps = activitySteps;
	}
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public List<ActivityDataDefinitionVO> getExposedDataFields() {
		return exposedDataFields;
	}
	public void setExposedDataFields(List<ActivityDataDefinitionVO> exposedDataFields) {
		this.exposedDataFields = exposedDataFields;
	}
}