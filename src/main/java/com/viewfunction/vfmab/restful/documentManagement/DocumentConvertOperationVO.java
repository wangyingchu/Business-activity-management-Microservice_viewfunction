package com.viewfunction.vfmab.restful.documentManagement;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DocumentConvertOperationVO")
public class DocumentConvertOperationVO {
	private String sourceDocumentPath;
	private String targetDocumentPath;
	public String getSourceDocumentPath() {
		return sourceDocumentPath;
	}
	public void setSourceDocumentPath(String sourceDocumentPath) {
		this.sourceDocumentPath = sourceDocumentPath;
	}
	public String getTargetDocumentPath() {
		return targetDocumentPath;
	}
	public void setTargetDocumentPath(String targetDocumentPath) {
		this.targetDocumentPath = targetDocumentPath;
	}
}