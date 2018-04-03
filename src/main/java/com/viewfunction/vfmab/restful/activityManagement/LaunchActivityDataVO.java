package com.viewfunction.vfmab.restful.activityManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LaunchActivityDataVO")
public class LaunchActivityDataVO {	
	private ActivityTypeDefinitionVO activityTypeDefinition;	
	private String startUserId;
	private List<ActivityDataFieldValueVO> launchActivityData;
	private String launchDecisionPointChoise;
	private String launchUserIdentity;
	public ActivityTypeDefinitionVO getActivityTypeDefinition() {
		return activityTypeDefinition;
	}
	public void setActivityTypeDefinition(ActivityTypeDefinitionVO activityTypeDefinition) {
		this.activityTypeDefinition = activityTypeDefinition;
	}
	public String getStartUserId() {
		return startUserId;
	}
	public void setStartUserId(String startUserId) {
		this.startUserId = startUserId;
	}
	public List<ActivityDataFieldValueVO> getLaunchActivityData() {
		return launchActivityData;
	}
	public void setLaunchActivityData(List<ActivityDataFieldValueVO> launchActivityData) {
		this.launchActivityData = launchActivityData;
	}
	public String getLaunchDecisionPointChoise() {
		return launchDecisionPointChoise;
	}
	public void setLaunchDecisionPointChoise(String launchDecisionPointChoise) {
		this.launchDecisionPointChoise = launchDecisionPointChoise;
	}
	public String getLaunchUserIdentity() {
		return launchUserIdentity;
	}
	public void setLaunchUserIdentity(String launchUserIdentity) {
		this.launchUserIdentity = launchUserIdentity;
	}	
}