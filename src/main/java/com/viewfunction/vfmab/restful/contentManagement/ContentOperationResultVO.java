package com.viewfunction.vfmab.restful.contentManagement;

public class ContentOperationResultVO {
	private boolean operationResult;
	private String resultReason;
	public boolean isOperationResult() {
		return operationResult;
	}
	public void setOperationResult(boolean operationResult) {
		this.operationResult = operationResult;
	}
	public String getResultReason() {
		return resultReason;
	}
	public void setResultReason(String resultReason) {
		this.resultReason = resultReason;
	}
}
