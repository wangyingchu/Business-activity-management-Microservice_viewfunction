package com.viewfunction.vfmab.restful.userManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "UserBasicInfoVO")
public class UserBasicInfoVO {
	private String userId;
	private String userDisplayName;
	private String userType;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserDisplayName() {
		return userDisplayName;
	}
	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
}