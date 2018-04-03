package com.viewfunction.vfmab.restful.userManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "UserBasicInfoVOList")
public class UserBasicInfoVOList {
	private List<UserBasicInfoVO> userBasicInfoVOList;

	public List<UserBasicInfoVO> getUserBasicInfoVOList() {
		return userBasicInfoVOList;
	}

	public void setUserBasicInfoVOList(List<UserBasicInfoVO> userBasicInfoVOList) {
		this.userBasicInfoVOList = userBasicInfoVOList;
	}
}