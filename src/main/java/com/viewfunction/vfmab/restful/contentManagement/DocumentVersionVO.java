package com.viewfunction.vfmab.restful.contentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DocumentVersionVO")
public class DocumentVersionVO {
	private DocumentContentVO documentContent;
	private long versionCreatedDate;
	private String versionNumber;
	private String[] versionLabels;
	public DocumentContentVO getDocumentContent() {
		return documentContent;
	}
	public void setDocumentContent(DocumentContentVO documentContent) {
		this.documentContent = documentContent;
	}
	public long getVersionCreatedDate() {
		return versionCreatedDate;
	}
	public void setVersionCreatedDate(long versionCreatedDate) {
		this.versionCreatedDate = versionCreatedDate;
	}
	public String getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}
	public String[] getVersionLabels() {
		return versionLabels;
	}
	public void setVersionLabels(String[] versionLabels) {
		this.versionLabels = versionLabels;
	}
}