package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityTypeDefinitionVO")
public class ActivityTypeDefinitionVO {	
	private String activityType;
	private String activitySpaceName;
	private boolean enabled;
	private String launchDecisionPointAttributeName;
	private String launchUserIdentityAttributeName;
	private String[] launchDecisionPointChoiseList;
	private String[] launchProcessVariableList;	
	private String[] activityLaunchRoles;
	private String[] activityLaunchParticipants;
	private String activityTypeDesc;
	private ActivityDataFieldValueVO[] activityLaunchData;
	private String[] activityCategories;
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}
	public String getActivitySpaceName() {
		return activitySpaceName;
	}
	public void setActivitySpaceName(String activitySpaceName) {
		this.activitySpaceName = activitySpaceName;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getLaunchDecisionPointAttributeName() {
		return launchDecisionPointAttributeName;
	}
	public void setLaunchDecisionPointAttributeName(
			String launchDecisionPointAttributeName) {
		this.launchDecisionPointAttributeName = launchDecisionPointAttributeName;
	}
	public String getLaunchUserIdentityAttributeName() {
		return launchUserIdentityAttributeName;
	}
	public void setLaunchUserIdentityAttributeName(
			String launchUserIdentityAttributeName) {
		this.launchUserIdentityAttributeName = launchUserIdentityAttributeName;
	}
	public String[] getLaunchDecisionPointChoiseList() {
		return launchDecisionPointChoiseList;
	}
	public void setLaunchDecisionPointChoiseList(
			String[] launchDecisionPointChoiseList) {
		this.launchDecisionPointChoiseList = launchDecisionPointChoiseList;
	}
	public String getActivityTypeDesc() {
		return activityTypeDesc;
	}
	public void setActivityTypeDesc(String activityTypeDesc) {
		this.activityTypeDesc = activityTypeDesc;
	}
	public ActivityDataFieldValueVO[] getActivityLaunchData() {
		return activityLaunchData;
	}
	public void setActivityLaunchData(ActivityDataFieldValueVO[] activityLaunchData) {
		this.activityLaunchData = activityLaunchData;
	}
	public String[] getLaunchProcessVariableList() {
		return launchProcessVariableList;
	}
	public void setLaunchProcessVariableList(String[] launchProcessVariableList) {
		this.launchProcessVariableList = launchProcessVariableList;
	}
	public String[] getActivityLaunchRoles() {
		return activityLaunchRoles;
	}
	public void setActivityLaunchRoles(String[] activityLaunchRoles) {
		this.activityLaunchRoles = activityLaunchRoles;
	}
	public String[] getActivityLaunchParticipants() {
		return activityLaunchParticipants;
	}
	public void setActivityLaunchParticipants(
			String[] activityLaunchParticipants) {
		this.activityLaunchParticipants = activityLaunchParticipants;
	}
	public String[] getActivityCategories() {
		return activityCategories;
	}
	public void setActivityCategories(String[] activityCategories) {
		this.activityCategories = activityCategories;
	}
}