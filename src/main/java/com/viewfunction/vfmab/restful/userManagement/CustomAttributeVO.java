package com.viewfunction.vfmab.restful.userManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CustomAttributeVO")
public class CustomAttributeVO {
	private String attributeName;
	private String attributeType;
	private boolean arrayAttribute;
	private String[] attributeRowValue;	
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeType() {
		return attributeType;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public boolean getArrayAttribute() {
		return arrayAttribute;
	}
	public void setArrayAttribute(boolean arrayAttribute) {
		this.arrayAttribute = arrayAttribute;
	}
	public String[] getAttributeRowValue() {
		return attributeRowValue;
	}
	public void setAttributeRowValue(String[] attributeRowValue) {
		this.attributeRowValue = attributeRowValue;
	}	
}