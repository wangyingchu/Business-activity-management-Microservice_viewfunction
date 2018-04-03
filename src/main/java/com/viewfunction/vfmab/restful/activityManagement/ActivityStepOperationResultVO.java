package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityStepOperationResultVO")
public class ActivityStepOperationResultVO {
	private boolean operationResult;

	public boolean isOperationResult() {
		return operationResult;
	}

	public void setOperationResult(boolean operationResult) {
		this.operationResult = operationResult;
	}
}