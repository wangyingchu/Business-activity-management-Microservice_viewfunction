package com.viewfunction.vfmab.restful.contentManagement;

public class PreviewFileGenerateResultVO {
	private boolean generateResult;
	private String previewFileLocation;
	private String previewFileName;
	public boolean isGenerateResult() {
		return generateResult;
	}
	public void setGenerateResult(boolean generateResult) {
		this.generateResult = generateResult;
	}
	public String getPreviewFileLocation() {
		return previewFileLocation;
	}
	public void setPreviewFileLocation(String previewFileLocation) {
		this.previewFileLocation = previewFileLocation;
	}
	public String getPreviewFileName() {
		return previewFileName;
	}
	public void setPreviewFileName(String previewFileRoot) {
		this.previewFileName = previewFileRoot;
	}
}