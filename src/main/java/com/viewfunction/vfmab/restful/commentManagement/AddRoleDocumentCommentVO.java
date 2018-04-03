package com.viewfunction.vfmab.restful.commentManagement;

import javax.xml.bind.annotation.XmlRootElement;

import com.viewfunction.vfmab.restful.contentManagement.RoleFileVO;

@XmlRootElement(name = "AddRoleDocumentCommentVO")
public class AddRoleDocumentCommentVO {
	private RoleFileVO roleDocument;
	private AddCommentVO newComment;
	public RoleFileVO getRoleDocument() {
		return roleDocument;
	}
	public void setRoleDocument(RoleFileVO roleDocument) {
		this.roleDocument = roleDocument;
	}
	public AddCommentVO getNewComment() {
		return newComment;
	}
	public void setNewComment(AddCommentVO newComment) {
		this.newComment = newComment;
	}
}