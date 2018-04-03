package com.viewfunction.vfmab.restful.activityManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ChildActivityStepsInfoVO")
public class ChildActivityStepsInfoVO {
	private boolean allChildStepsFinished;
	private List<ActivityStepVO> childActivitySteps;
	public boolean getAllChildStepsFinished() {
		return allChildStepsFinished;
	}
	public void setAllChildStepsFinished(boolean allChildStepsFinished) {
		this.allChildStepsFinished = allChildStepsFinished;
	}
	public List<ActivityStepVO> getChildActivitySteps() {
		return childActivitySteps;
	}
	public void setChildActivitySteps(List<ActivityStepVO> childActivitySteps) {
		this.childActivitySteps = childActivitySteps;
	}

}
