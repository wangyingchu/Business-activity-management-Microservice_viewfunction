package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityDataFieldValueVO")
public class ActivityDataFieldValueVO {
	private String singleDataFieldValue;
	private String[] arrayDataFieldValue;
	private ActivityDataDefinitionVO activityDataDefinition;
	public String getSingleDataFieldValue() {
		return singleDataFieldValue;
	}
	public void setSingleDataFieldValue(String singleDataFieldValue) {
		this.singleDataFieldValue = singleDataFieldValue;
	}
	public String[] getArrayDataFieldValue() {
		return arrayDataFieldValue;
	}
	public void setArrayDataFieldValue(String[] arrayDataFieldValue) {
		this.arrayDataFieldValue = arrayDataFieldValue;
	}
	public ActivityDataDefinitionVO getActivityDataDefinition() {
		return activityDataDefinition;
	}
	public void setActivityDataDefinition(ActivityDataDefinitionVO activityDataDefinitionVO) {
		activityDataDefinition = activityDataDefinitionVO;
	}
}