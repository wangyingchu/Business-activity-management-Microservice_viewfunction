package com.viewfunction.vfmab.restful.activityManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityDataFieldValueVOList")
public class ActivityDataFieldValueVOList {
	
	private List<ActivityDataFieldValueVO> activityDataFieldValueList;

	public List<ActivityDataFieldValueVO> getActivityDataFieldValueList() {
		return activityDataFieldValueList;
	}

	public void setActivityDataFieldValueList(
			List<ActivityDataFieldValueVO> activityDataFieldValueList) {
		this.activityDataFieldValueList = activityDataFieldValueList;
	}	
}