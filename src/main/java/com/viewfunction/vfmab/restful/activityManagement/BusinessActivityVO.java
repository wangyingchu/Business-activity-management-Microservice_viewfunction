package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BusinessActivityVO")
public class BusinessActivityVO {
	private String activityType;
	private String activityId;
	private String rosterName;
	private boolean finished;
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
	public String getRosterName() {
		return rosterName;
	}
	public void setRosterName(String rosterName) {
		this.rosterName = rosterName;
	}
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public String getActivityType() {
		return activityType;
	}
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	} 
}