package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityCommentOperationResultVO")
public class ActivityCommentOperationResultVO {
	private boolean operationResult;

	public boolean isOperationResult() {
		return operationResult;
	}

	public void setOperationResult(boolean operationResult) {
		this.operationResult = operationResult;
	}
}