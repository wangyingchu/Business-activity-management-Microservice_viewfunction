package com.viewfunction.vfmab.restful.commentManagement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.viewfunction.contentRepository.contentBureau.BaseContentObject;
import com.viewfunction.contentRepository.contentBureau.CommentObject;
import com.viewfunction.contentRepository.contentBureau.ContentSpace;
import com.viewfunction.contentRepository.util.exception.ContentReposityException;
import com.viewfunction.contentRepository.util.factory.ContentComponentFactory;
import com.viewfunction.contentRepository.util.helper.BinaryContent;
import com.viewfunction.contentRepository.util.helper.CommentOperationHelper;
import com.viewfunction.contentRepository.util.helper.ContentOperationHelper;

import com.viewfunction.vfmab.restful.contentManagement.ActivityTypeFileVO;
import com.viewfunction.vfmab.restful.contentManagement.ApplicationSpaceFileVO;
import com.viewfunction.vfmab.restful.contentManagement.ContentManagementService;
import com.viewfunction.vfmab.restful.contentManagement.ParticipantFileVO;
import com.viewfunction.vfmab.restful.contentManagement.RoleFileVO;
import com.viewfunction.vfmab.restful.util.BooleanOperationResultVO;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVOsList;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfosQueryVO;
import com.viewfunction.participantManagement.operation.restfulClient.ParticipantOperationServiceRESTClient;
import org.springframework.stereotype.Service;

@Service
@Path("/commentManagementService")  
@Produces("application/json")
public class CommentManagementService {
	
	@POST
    @Path("/activityDocumentComments/")	
	@Produces("application/json")
	public List<CommentContentVO> getActivityDocumentComments(ActivityTypeFileVO activityTypeFileVO){
		String activitySpace=activityTypeFileVO.getActivitySpaceName();
		String parentFolderPath=activityTypeFileVO.getParentFolderPath();
		String fileName=activityTypeFileVO.getFileName();
		String activityType=activityTypeFileVO.getActivityName();
		String activityId=activityTypeFileVO.getActivityId();		
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ContentManagementService.ActivityInstance_attachment;				
		String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;		
		List<CommentContentVO> activityDocumentCommentsList=new ArrayList<CommentContentVO>(); 		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);		
							List<CommentObject> commentList=targetBinaryContent.getComments();
							for(CommentObject currentCommentObject:commentList){
								activityDocumentCommentsList.add(generateCommentContentVO(activitySpace,currentCommentObject));								
							}		
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return activityDocumentCommentsList;
	}
	
	@POST
    @Path("/participantDocumentComments/")	
	@Produces("application/json")
	public List<CommentContentVO> getParticipantDocumentComments(ParticipantFileVO participantFileVO){
		String activitySpace=participantFileVO.getActivitySpaceName();
		String parentFolderPath=participantFileVO.getParentFolderPath();
		String fileName=participantFileVO.getFileName();
		String participantName=participantFileVO.getParticipantName();			
		String currentFolderFullPath="/"+ContentManagementService.ActivitySpace_ContentStore+"/"+ContentManagementService.Participant_ContentStore+"/"+participantName+parentFolderPath;				
		List<CommentContentVO> documentCommentsList=new ArrayList<CommentContentVO>(); 		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);		
							List<CommentObject> commentList=targetBinaryContent.getComments();
							for(CommentObject currentCommentObject:commentList){
								documentCommentsList.add(generateCommentContentVO(activitySpace,currentCommentObject));								
							}		
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return documentCommentsList;
	}
	
	@POST
    @Path("/applicationSpaceDocumentComments/")	
	@Produces("application/json")
	public List<CommentContentVO> getApplicationSpaceDocumentComments(ApplicationSpaceFileVO applicationSpaceFileVO){
		String activitySpace=applicationSpaceFileVO.getActivitySpaceName();
		String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
		String fileName=applicationSpaceFileVO.getFileName();		
		String spaceFolderRootAbsPath="/"+ContentManagementService.ActivitySpace_ContentStore+"/"+ContentManagementService.Space_ContentStore;		
		String currentFolderFullPath=spaceFolderRootAbsPath+parentFolderPath;		
		List<CommentContentVO> documentCommentsList=new ArrayList<CommentContentVO>(); 		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);		
							List<CommentObject> commentList=targetBinaryContent.getComments();
							for(CommentObject currentCommentObject:commentList){
								documentCommentsList.add(generateCommentContentVO(activitySpace,currentCommentObject));								
							}		
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return documentCommentsList;
	}
	
	@POST
    @Path("/roleDocumentComments/")	
	@Produces("application/json")
	public List<CommentContentVO> getRoleDocumentComments(RoleFileVO roleFileVO){
		String activitySpace=roleFileVO.getActivitySpaceName();
		String parentFolderPath=roleFileVO.getParentFolderPath();
		String fileName=roleFileVO.getFileName();	
		String roleName=roleFileVO.getRoleName();							
		String currentFolderFullPath="/"+ContentManagementService.ActivitySpace_ContentStore+"/"+ContentManagementService.Role_ContentStore+"/"+roleName+parentFolderPath;
		List<CommentContentVO> documentCommentsList=new ArrayList<CommentContentVO>(); 		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);		
							List<CommentObject> commentList=targetBinaryContent.getComments();
							for(CommentObject currentCommentObject:commentList){
								documentCommentsList.add(generateCommentContentVO(activitySpace,currentCommentObject));								
							}		
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return documentCommentsList;
	}
	
	@POST
    @Path("/addActivityDocumentComment/")	
	@Produces("application/json")
	public CommentContentVO addActivityDocumentComment(AddActivityDocumentCommentVO addActivityDocumentCommentVO){
		String activitySpace=addActivityDocumentCommentVO.getActivityDocument().getActivitySpaceName();
		String parentFolderPath=addActivityDocumentCommentVO.getActivityDocument().getParentFolderPath();
		String fileName=addActivityDocumentCommentVO.getActivityDocument().getFileName();
		String activityType=addActivityDocumentCommentVO.getActivityDocument().getActivityName();
		String activityId=addActivityDocumentCommentVO.getActivityDocument().getActivityId();		
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ContentManagementService.ActivityInstance_attachment;				
		String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;				
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);								
							CommentObject newCommentObject=ContentComponentFactory.createCommentObject();			
							newCommentObject.setCommentAuthor(addActivityDocumentCommentVO.getNewComment().getCommentAuthor());
							newCommentObject.setCommentContent(addActivityDocumentCommentVO.getNewComment().getCommentContent());
							newCommentObject.setCommentCreateDate(new Date().getTime());							
							boolean result=targetBinaryContent.addComment(newCommentObject);
							if(result){								
								CommentContentVO commentContentVO=new CommentContentVO();				
								List<String> userList=new ArrayList<String>();
								userList.add(newCommentObject.getCommentAuthor());			
								ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
								participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
								participantDetailInfosQueryVO.setParticipantScope(activitySpace);				
								ParticipantDetailInfoVOsList participantDetailInfoVOsList=
										ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
								if(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().size()>0){
									commentContentVO.setCommentAuthor(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().get(0));				
								}						
								commentContentVO.setCommentContent(newCommentObject.getCommentContent());
								commentContentVO.setCommentCreateDate(newCommentObject.getCommentCreateDate());								
								commentContentVO.setSubComments(new ArrayList<CommentContentVO>());
								return commentContentVO;								
							}else{
								return null;
							}							
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return null;
	}	
	
	@POST
    @Path("/addParticipantDocumentComment/")	
	@Produces("application/json")
	public CommentContentVO addParticipantDocumentComment(AddParticipantentDocumentCommentVO addParticipantentDocumentCommentVO){		
		ParticipantFileVO participantFileVO=addParticipantentDocumentCommentVO.getParticipantDocument();
		String activitySpace=participantFileVO.getActivitySpaceName();
		String parentFolderPath=participantFileVO.getParentFolderPath();
		String fileName=participantFileVO.getFileName();
		String participantName=participantFileVO.getParticipantName();			
		String currentFolderFullPath="/"+ContentManagementService.ActivitySpace_ContentStore+"/"+ContentManagementService.Participant_ContentStore+"/"+participantName+parentFolderPath;		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);								
							CommentObject newCommentObject=ContentComponentFactory.createCommentObject();			
							newCommentObject.setCommentAuthor(addParticipantentDocumentCommentVO.getNewComment().getCommentAuthor());
							newCommentObject.setCommentContent(addParticipantentDocumentCommentVO.getNewComment().getCommentContent());
							newCommentObject.setCommentCreateDate(new Date().getTime());							
							boolean result=targetBinaryContent.addComment(newCommentObject);
							if(result){								
								CommentContentVO commentContentVO=new CommentContentVO();				
								List<String> userList=new ArrayList<String>();
								userList.add(newCommentObject.getCommentAuthor());			
								ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
								participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
								participantDetailInfosQueryVO.setParticipantScope(activitySpace);				
								ParticipantDetailInfoVOsList participantDetailInfoVOsList=
										ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
								if(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().size()>0){
									commentContentVO.setCommentAuthor(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().get(0));				
								}						
								commentContentVO.setCommentContent(newCommentObject.getCommentContent());
								commentContentVO.setCommentCreateDate(newCommentObject.getCommentCreateDate());								
								commentContentVO.setSubComments(new ArrayList<CommentContentVO>());
								return commentContentVO;								
							}else{
								return null;
							}							
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return null;
	}
	
	@POST
    @Path("/addApplicationSpaceDocumentComment/")	
	@Produces("application/json")
	public CommentContentVO addApplicationSpaceDocumentComment(AddApplicationSpaceDocumentCommentVO addApplicationSpaceDocumentCommentVO){		
		ApplicationSpaceFileVO applicationSpaceFileVO=addApplicationSpaceDocumentCommentVO.getApplicationSpaceDocument();
		String activitySpace=applicationSpaceFileVO.getActivitySpaceName();
		String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
		String fileName=applicationSpaceFileVO.getFileName();		
		String spaceFolderRootAbsPath="/"+ContentManagementService.ActivitySpace_ContentStore+"/"+ContentManagementService.Space_ContentStore;		
		String currentFolderFullPath=spaceFolderRootAbsPath+parentFolderPath;			
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);								
							CommentObject newCommentObject=ContentComponentFactory.createCommentObject();			
							newCommentObject.setCommentAuthor(addApplicationSpaceDocumentCommentVO.getNewComment().getCommentAuthor());
							newCommentObject.setCommentContent(addApplicationSpaceDocumentCommentVO.getNewComment().getCommentContent());
							newCommentObject.setCommentCreateDate(new Date().getTime());							
							boolean result=targetBinaryContent.addComment(newCommentObject);
							if(result){								
								CommentContentVO commentContentVO=new CommentContentVO();				
								List<String> userList=new ArrayList<String>();
								userList.add(newCommentObject.getCommentAuthor());			
								ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
								participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
								participantDetailInfosQueryVO.setParticipantScope(activitySpace);				
								ParticipantDetailInfoVOsList participantDetailInfoVOsList=
										ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
								if(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().size()>0){
									commentContentVO.setCommentAuthor(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().get(0));				
								}						
								commentContentVO.setCommentContent(newCommentObject.getCommentContent());
								commentContentVO.setCommentCreateDate(newCommentObject.getCommentCreateDate());								
								commentContentVO.setSubComments(new ArrayList<CommentContentVO>());
								return commentContentVO;								
							}else{
								return null;
							}							
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return null;
	}
	
	@POST
    @Path("/addRoleDocumentComment/")	
	@Produces("application/json")
	public CommentContentVO addRoleDocumentComment(AddRoleDocumentCommentVO addRoleDocumentCommentVO){	
		RoleFileVO roleFileVO=addRoleDocumentCommentVO.getRoleDocument();		
		String activitySpace=roleFileVO.getActivitySpaceName();
		String parentFolderPath=roleFileVO.getParentFolderPath();
		String fileName=roleFileVO.getFileName();	
		String roleName=roleFileVO.getRoleName();							
		String currentFolderFullPath="/"+ContentManagementService.ActivitySpace_ContentStore+"/"+ContentManagementService.Role_ContentStore+"/"+roleName+parentFolderPath;		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject!=null){
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){							
						}else{							
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);								
							CommentObject newCommentObject=ContentComponentFactory.createCommentObject();			
							newCommentObject.setCommentAuthor(addRoleDocumentCommentVO.getNewComment().getCommentAuthor());
							newCommentObject.setCommentContent(addRoleDocumentCommentVO.getNewComment().getCommentContent());
							newCommentObject.setCommentCreateDate(new Date().getTime());							
							boolean result=targetBinaryContent.addComment(newCommentObject);
							if(result){								
								CommentContentVO commentContentVO=new CommentContentVO();				
								List<String> userList=new ArrayList<String>();
								userList.add(newCommentObject.getCommentAuthor());			
								ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
								participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
								participantDetailInfosQueryVO.setParticipantScope(activitySpace);				
								ParticipantDetailInfoVOsList participantDetailInfoVOsList=
										ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
								if(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().size()>0){
									commentContentVO.setCommentAuthor(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().get(0));				
								}						
								commentContentVO.setCommentContent(newCommentObject.getCommentContent());
								commentContentVO.setCommentCreateDate(newCommentObject.getCommentCreateDate());								
								commentContentVO.setSubComments(new ArrayList<CommentContentVO>());
								return commentContentVO;								
							}else{
								return null;
							}							
						}
					}	
				}					
			} catch (ContentReposityException e) {				
				e.printStackTrace();
			}finally{
				if(activityContentSpace!=null){
					activityContentSpace.closeContentSpace();
				}				
			}			
		return null;
	}	
	
	@POST
    @Path("/addSubComment/")	
	@Produces("application/json")
	public CommentContentVO addSubComment(AddCommentVO addCommentVO){
		String activitySpace=addCommentVO.getActivitySpaceName();
		String parentCommentUUID=addCommentVO.getParentCommentUUID();
		ContentSpace activityContentSpace = null;
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			CommentOperationHelper commentOh=ContentComponentFactory.getCommentOperationHelper();			
			CommentObject parentComment=commentOh.getCommentByCommentUUID(activityContentSpace, parentCommentUUID);			
			CommentObject newCommentObject=ContentComponentFactory.createCommentObject();			
			newCommentObject.setCommentAuthor(addCommentVO.getCommentAuthor());
			newCommentObject.setCommentContent(addCommentVO.getCommentContent());
			newCommentObject.setCommentCreateDate(new Date().getTime());			
			parentComment.addSubComment(newCommentObject);
			
			CommentContentVO commentContentVO=new CommentContentVO();				
			List<String> userList=new ArrayList<String>();
			userList.add(newCommentObject.getCommentAuthor());			
			ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
			participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
			participantDetailInfosQueryVO.setParticipantScope(activitySpace);				
			ParticipantDetailInfoVOsList participantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
			if(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().size()>0){
				commentContentVO.setCommentAuthor(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().get(0));				
			}						
			commentContentVO.setCommentContent(newCommentObject.getCommentContent());
			commentContentVO.setCommentCreateDate(newCommentObject.getCommentCreateDate());
			commentContentVO.setCommentUUID(newCommentObject.getCommentUUID());
			commentContentVO.setParentCommentUUID(newCommentObject.getParentComment().getCommentUUID());
			commentContentVO.setSubComments(new ArrayList<CommentContentVO>());
			
			return commentContentVO;
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}		
		return null;
	}	
	
	@DELETE
    @Path("/deleteComment/")	
	@Produces("application/json")
	public BooleanOperationResultVO deleteComment(DeleteCommentVO commentToDeleteVO){		
		BooleanOperationResultVO operationResultVO=new BooleanOperationResultVO();
		operationResultVO.setTiemStamp(new Date().getTime());	
		operationResultVO.setOperationResult(false);
		String activitySpace=commentToDeleteVO.getActivitySpaceName();
		String commentUUID=commentToDeleteVO.getCommentUUID();
		String operatorId=commentToDeleteVO.getOperatorId();
		ContentSpace activityContentSpace = null;
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			CommentOperationHelper commentOh=ContentComponentFactory.getCommentOperationHelper();				
			boolean deleteResult=commentOh.deleteCommentByCommentUUID(activityContentSpace, commentUUID);			
			operationResultVO.setOperationResult(deleteResult);	
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}		
		return null;
	}		
	
	private static CommentContentVO generateCommentContentVO(String activitySpace,CommentObject commentObject){
		CommentContentVO commentContentVO=new CommentContentVO();
		try {						
			List<String> userList=new ArrayList<String>();
			userList.add(commentObject.getCommentAuthor());			
			ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
			participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
			participantDetailInfosQueryVO.setParticipantScope(activitySpace);				
			ParticipantDetailInfoVOsList participantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
			if(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().size()>0){
				commentContentVO.setCommentAuthor(participantDetailInfoVOsList.getParticipantDetailInfoVOsList().get(0));				
			}			
			commentContentVO.setCommentContent(commentObject.getCommentContent());
			commentContentVO.setCommentCreateDate(commentObject.getCommentCreateDate());
			commentContentVO.setCommentUUID(commentObject.getCommentUUID());
			if(commentObject.getParentComment()!=null){
				commentContentVO.setParentCommentUUID(commentObject.getParentComment().getCommentUUID());				
			}			
			List<CommentContentVO> commentContentList=new ArrayList<CommentContentVO>();			
			List<CommentObject> commentObjectList=commentObject.getSubComments();
			for(CommentObject currentCommentObject:commentObjectList){				
				CommentContentVO currentCommentContentVO=generateCommentContentVO(activitySpace,currentCommentObject);				
				commentContentList.add(currentCommentContentVO);				
			}			
			commentContentVO.setSubComments(commentContentList);		
		} catch (ContentReposityException e) {			
			e.printStackTrace();
		}		
		return commentContentVO;
	}
}