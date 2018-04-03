package com.viewfunction.vfmab.restful.userManagement;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CustomStructureVO")
public class CustomStructureVO {
	private String structureName;
	private String structureId;
	private List<CustomStructureVO> subCustomStructures;
	private List<CustomAttributeVO> subCustomAttributes;
	public String getStructureName() {
		return structureName;
	}
	public void setStructureName(String structureName) {
		this.structureName = structureName;
	}	
	public String getStructureId() {
		return structureId;
	}
	public void setStructureId(String structureId) {
		this.structureId = structureId;
	}
	public List<CustomStructureVO> getSubCustomStructures() {
		return subCustomStructures;
	}
	public void setSubCustomStructures(List<CustomStructureVO> subCustomStructures) {
		this.subCustomStructures = subCustomStructures;
	}
	public List<CustomAttributeVO> getSubCustomAttributes() {
		return subCustomAttributes;
	}
	public void setSubCustomAttributes(List<CustomAttributeVO> subCustomAttributes) {
		this.subCustomAttributes = subCustomAttributes;
	}	
}