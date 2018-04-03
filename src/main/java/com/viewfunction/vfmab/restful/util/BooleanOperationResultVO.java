package com.viewfunction.vfmab.restful.util;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "OperationBooleanResultVO")
public class BooleanOperationResultVO {
	private long tiemStamp;
	private boolean operationResult;
	public long getTiemStamp() {
		return tiemStamp;
	}
	public void setTiemStamp(long tiemStamp) {
		this.tiemStamp = tiemStamp;
	}
	public boolean isOperationResult() {
		return operationResult;
	}
	public void setOperationResult(boolean operationResult) {
		this.operationResult = operationResult;
	}
}