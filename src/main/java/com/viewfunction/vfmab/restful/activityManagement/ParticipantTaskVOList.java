package com.viewfunction.vfmab.restful.activityManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ParticipantTaskVOList")
public class ParticipantTaskVOList {
	private List<ParticipantTaskVO> participantTasksVOList;

	public List<ParticipantTaskVO> getParticipantTasksVOList() {
		return participantTasksVOList;
	}

	public void setParticipantTasksVOList(List<ParticipantTaskVO> participantTasksVOList) {
		this.participantTasksVOList = participantTasksVOList;
	}
}