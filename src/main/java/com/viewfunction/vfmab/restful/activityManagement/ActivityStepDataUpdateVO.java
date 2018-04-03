package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityStepDataUpdateVO")
public class ActivityStepDataUpdateVO {

	private ActivityDataFieldValueVOList activityDataFieldValueList;
	private ActivityStepOperationVO activityStepOperationVO;
	
	public ActivityDataFieldValueVOList getActivityDataFieldValueList() {
		return activityDataFieldValueList;
	}
	public void setActivityDataFieldValueList(ActivityDataFieldValueVOList activityDataFieldValueList) {
		this.activityDataFieldValueList = activityDataFieldValueList;
	}
	public ActivityStepOperationVO getActivityStepOperationVO() {
		return activityStepOperationVO;
	}
	public void setActivityStepOperationVO(ActivityStepOperationVO activityStepOperationVO) {
		this.activityStepOperationVO = activityStepOperationVO;
	}
}