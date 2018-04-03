package com.viewfunction.vfmab.restful.activityManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ActivityDataDefinitionVO")
public class ActivityDataDefinitionVO {
	private String description;
	private String displayName;
	private String fieldName;
	private String fieldType;
	private boolean arrayField;
	private boolean mandatoryField;
	private boolean systemField;
	private boolean readableField;
	private boolean writeableField;	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public boolean isArrayField() {
		return arrayField;
	}
	public void setArrayField(boolean isArrayField) {
		this.arrayField = isArrayField;
	}
	public boolean isMandatoryField() {
		return mandatoryField;
	}
	public void setMandatoryField(boolean isMandatoryField) {
		this.mandatoryField = isMandatoryField;
	}
	public boolean isSystemField() {
		return systemField;
	}
	public void setSystemField(boolean isSystemField) {
		this.systemField = isSystemField;
	}
	public boolean isReadableField() {
		return readableField;
	}
	public void setReadableField(boolean readableField) {
		this.readableField = readableField;
	}
	public boolean isWriteableField() {
		return writeableField;
	}
	public void setWriteableField(boolean writeableField) {
		this.writeableField = writeableField;
	}
}
