package com.viewfunction.vfmab.restful.contentManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.artofsolving.jodconverter.OfficeDocumentConverter;

import com.viewfunction.contentRepository.contentBureau.BaseContentObject;
import com.viewfunction.contentRepository.contentBureau.ContentSpace;
import com.viewfunction.contentRepository.contentBureau.PermissionObject;
import com.viewfunction.contentRepository.util.RuntimeEnvironmentHandler;
import com.viewfunction.contentRepository.util.exception.ContentReposityException;
import com.viewfunction.contentRepository.util.factory.ContentComponentFactory;
import com.viewfunction.contentRepository.util.helper.BinaryContent;
import com.viewfunction.contentRepository.util.helper.BinaryContentVersionObject;
import com.viewfunction.contentRepository.util.helper.ContentOperationHelper;
import com.viewfunction.contentRepository.util.helper.ContentQueryHelper;
import com.viewfunction.contentRepository.util.helper.PropertyQueryHelper;
import com.viewfunction.contentRepository.util.helper.SecurityOperationHelper;

import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVOsList;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfosQueryVO;
import com.viewfunction.participantManagement.operation.restfulClient.ParticipantOperationServiceRESTClient;

import com.viewfunction.vfmab.restful.util.BooleanOperationResultVO;
import com.viewfunction.vfmab.restful.util.ServiceResourceHolder;
import org.springframework.stereotype.Service;

@Service
@Path("/contentManagementService")  
@Produces("application/json")
public class ContentManagementService {
	
	public static final String ActivitySpace_ContentStore="ActivitySpace_ContentStore";
	public static final String Participant_ContentStore ="Participant_ContentStore";		
	public static final String Space_ContentStore="Space_ContentStore";	
	public static final String Role_ContentStore="Role_ContentStore";	
	public static final String ActivityInstance_attachment ="ActivityInstance_attachment";	
	private static final String  DocumentsOwnerType_participant="PARTICIPANT";
	private static final String  DocumentsOwnerType_activity="ACTIVITY";
	private static final String  DocumentsOwnerType_applicationSpace="APPLICATIONSPACE";
	private static final String  DocumentsOwnerType_role="ROLE";
	
	private static String applicationContext;	
	private static String previewFileDownloadURL="/PreviewFileDownload?fileName=";
	private static String document_convertOperation_MSOFFICE_To_PDF="MSOFFICE->PDF";	
	private static String document_convertOperation_AddPOSTFIX_ODT="ADDPOSTFIX->.odt";
	private static String document_convertOperation_AddPOSTFIX_ODS="ADDPOSTFIX->.ods";
	private static String document_convertOperation_AddPOSTFIX_ODP="ADDPOSTFIX->.odp";
	
	@POST
    @Path("/participantPersonalFolder/")
	@Produces("application/json")
	public ContentFolderVO participantFolder(ParticipantFolderQueryVO participantFolderQueryVO){
		String activitySpaceName=participantFolderQueryVO.getActivitySpaceName();
		String participantName=participantFolderQueryVO.getParticipantName();
		
		String parentFolderPath=participantFolderQueryVO.getParentFolderPath()!=null?participantFolderQueryVO.getParentFolderPath():"";
		String currentFolderName=participantFolderQueryVO.getFolderName()!=null?participantFolderQueryVO.getFolderName():"";			
		
		if(!parentFolderPath.startsWith("/")){
			parentFolderPath="/"+parentFolderPath;
		}		
		String currentFolderAbsPath=null;
		if(parentFolderPath.endsWith("/")){
			currentFolderAbsPath=parentFolderPath+currentFolderName;				
		}else{
			currentFolderAbsPath=parentFolderPath+"/"+currentFolderName;
		}		
		
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+currentFolderAbsPath;			
		ContentFolderVO contentFolderVO=new ContentFolderVO();
		contentFolderVO.setFolderName(currentFolderName);		
		contentFolderVO.setFolderPath(currentFolderAbsPath);		
		contentFolderVO.setParentFolderPath(parentFolderPath);	
		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);
			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				targetParticipantFolderRoot=participantFolderRootContentObject.addSubContentObject(participantName, null, false);
			}
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			contentFolderVO.setFolderLocked(currentFolderContentObject.isLocked());			
			List<DocumentContentVO> childContentList=buildDocumentContentList(currentFolderContentObject,currentFolderAbsPath,activitySpaceName);
			contentFolderVO.setChildContentList(childContentList);
			return contentFolderVO;						
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
	@Path("/addParticipantPersonalFolder/")
	@Produces("application/json")
	public static ContentOperationResultVO addParticipantFolder(AddParticipantFolderVO addParticipantFolderVO){
		String activitySpaceName=addParticipantFolderVO.getActivitySpaceName();
		String participantName=addParticipantFolderVO.getParticipantName();
		String currentFolderAbsPath=addParticipantFolderVO.getParentFolderPath()!=null?addParticipantFolderVO.getParentFolderPath():"";
		if(!currentFolderAbsPath.startsWith("/")){
			currentFolderAbsPath="/"+currentFolderAbsPath;				
		}			
		String folderName=addParticipantFolderVO.getFolderName();
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";	
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName;
		if(currentFolderAbsPath!=null){
			currentFolderFullPath=currentFolderFullPath+currentFolderAbsPath;
		}		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				if(!currentFolderAbsPath.equals("/")){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Participant root folder does not exist.");					
				}else{
					targetParticipantFolderRoot=participantFolderRootContentObject.addSubContentObject(participantName, null, false);			
					targetParticipantFolderRoot.addSubContentObject(folderName, null, false);
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("Add Participant folder successed.");					
				}		
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Participant folder path does not exist.");					
				}else{
					BaseContentObject subFolderContentObject=currentFolderContentObject.addSubContentObject(folderName, null, false);
					if(subFolderContentObject!=null){
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("Add Participant folder successed.");
					}else{
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("Add Participant folder failed.");
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
		return contentOperationResultVO;		
	}		
	/*Common json response service for upload file
	@POST
	@Path("/participantPersonalFile/addFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public ContentOperationResultVO addParticipantFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){			
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		
		if(newFileAbsPath==null){
			contentOperationResultVO.setOperationResult(false);
			contentOperationResultVO.setResultReason("Participant folder path does not provided.");
			return contentOperationResultVO;
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		String fileType=fileAttachment.getContentType().toString();	
		
		DataHandler dataHandler=fileAttachment.getDataHandler();		
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PARTICIPANT_TEMP/";		
		String participantTempFileFullPath=tempFileRootPath+activitySpace+"/"+"PARTICIPANTS/"+participantName;		
		boolean createTempFolder=(new File(participantTempFileFullPath)).mkdirs();		
		String participantTemFolderFullPath=participantTempFileFullPath+"/";		
		
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";	
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+newFileAbsPath;		
		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				if(!newFileAbsPath.equals("/")){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Participant root folder does not exist.");					
				}else{
					targetParticipantFolderRoot=participantFolderRootContentObject.addSubContentObject(participantName, null, false);	
					String fileURI=participantTemFolderFullPath+newFileName;					
					File tempFile=new File(fileURI);
					try {
						InputStream fileInputStream=dataHandler.getInputStream();									
						OutputStream out=new FileOutputStream(tempFile);
						byte buf[]=new byte[1024];
						int len;
						while((len=fileInputStream.read(buf))>0){
							out.write(buf,0,len);
						}
						out.close();			
						fileInputStream.close();							
					} catch (IOException e) {			
						e.printStackTrace();			
					}							
					boolean addFileResult=coh.addBinaryContent(targetParticipantFolderRoot, tempFile, "", true);					
					if(addFileResult){
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("Add Participant file successed.");
					}else{
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("Add Participant file failed.");
					}
					tempFile.delete();										
				}		
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Participant folder path does not exist.");					
				}else{	
					String fileURI=participantTemFolderFullPath+newFileName;					
					File tempFile=new File(fileURI);
					try {
						InputStream fileInputStream=dataHandler.getInputStream();									
						OutputStream out=new FileOutputStream(tempFile);
						byte buf[]=new byte[1024];
						int len;
						while((len=fileInputStream.read(buf))>0){
							out.write(buf,0,len);
						}
						out.close();			
						fileInputStream.close();							
					} catch (IOException e) {			
						e.printStackTrace();			
					}							
					boolean addFileResult=coh.addBinaryContent(currentFolderContentObject, tempFile, "", true);					
					if(addFileResult){
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("Add Participant file successed.");
					}else{
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("Add Participant file failed.");
					}
					tempFile.delete();
				}				
			}			
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}	
	*/
	
	@POST
	@Path("/participantPersonalFile/addFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response addParticipantFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){			
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		/*
		String perWarpText="<html><textarea>";		
		String postWarpText="</textarea></html>";	
		String successfulResponse="{'operationResult':true,'resultReason':'Add participant file successed.'}";		
		String failedResponse="{'operationResult':false,'resultReason':'Add Participant file failed.'}";		
		String rootFolderNotExistResponse="{'operationResult':false,'resultReason':'Participant root folder does not exist.'}";
		String parentFolderNotExistResponse="{'operationResult':false,'resultReason':'Participant folder path does not exist.'}";		
		String parentFolderNotProvideredResponse="{'operationResult':false,'resultReason':'Participant folder path does not provided.'}";
		*/
		if(newFileAbsPath==null){			
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		String fileType=fileAttachment.getContentType().toString();	
		
		DataHandler dataHandler=fileAttachment.getDataHandler();		
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PARTICIPANT_TEMP/";		
		String participantTempFileFullPath=tempFileRootPath+activitySpace+"/"+"PARTICIPANTS/"+participantName;		
		boolean createTempFolder=(new File(participantTempFileFullPath)).mkdirs();		
		String participantTemFolderFullPath=participantTempFileFullPath+"/";		
		
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";	
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+newFileAbsPath;		
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				if(!newFileAbsPath.equals("/")){	
					response=Response.serverError().build();					
				}else{
					targetParticipantFolderRoot=participantFolderRootContentObject.addSubContentObject(participantName, null, false);	
					String fileURI=participantTemFolderFullPath+newFileName;					
					File tempFile=new File(fileURI);
					try {
						InputStream fileInputStream=dataHandler.getInputStream();									
						OutputStream out=new FileOutputStream(tempFile);
						byte buf[]=new byte[1024];
						int len;
						while((len=fileInputStream.read(buf))>0){
							out.write(buf,0,len);
						}
						out.close();			
						fileInputStream.close();							
					} catch (IOException e) {			
						e.printStackTrace();			
					}	
					boolean addFileResult=coh.addBinaryContent(targetParticipantFolderRoot, tempFile, "", participantName,true);					
					if(addFileResult){
						response=Response.ok().build();
					}else{						
						
						response=Response.serverError().build();
					}
					tempFile.delete();										
				}		
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject==null){		
					response=Response.serverError().build();
				}else{	
					String fileURI=participantTemFolderFullPath+newFileName;					
					File tempFile=new File(fileURI);
					try {
						InputStream fileInputStream=dataHandler.getInputStream();									
						OutputStream out=new FileOutputStream(tempFile);
						byte buf[]=new byte[1024];
						int len;
						while((len=fileInputStream.read(buf))>0){
							out.write(buf,0,len);
						}
						out.close();			
						fileInputStream.close();							
					} catch (IOException e) {			
						e.printStackTrace();			
					}						
					boolean addFileResult=coh.addBinaryContent(currentFolderContentObject, tempFile, "", participantName,true);					
					if(addFileResult){
						response=Response.ok().build();
					}else{		
						response=Response.serverError().build();
					}
					tempFile.delete();
				}				
			}			
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}		
	
	@POST
	@Path("/participantPersonalFile/updateFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response updateParticipantPersonalFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){				
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		if(newFileAbsPath==null){
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");		
		DataHandler dataHandler=fileAttachment.getDataHandler();			
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PARTICIPANT_TEMP/";		
		String participantTempFileFullPath=tempFileRootPath+activitySpace+"/"+"PARTICIPANTS/"+participantName;		
		boolean createTempFolder=(new File(participantTempFileFullPath)).mkdirs();		
		String participantTemFolderFullPath=participantTempFileFullPath+"/";
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+newFileAbsPath;
		
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject activityInstanceCurrentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityInstanceCurrentFolderContentObject==null){				
				response=Response.serverError().build();
			}else{	
				String fileURI=participantTemFolderFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}	
				boolean addFileResult=coh.updateBinaryContent(activityInstanceCurrentFolderContentObject, newFileName,tempFile, "", participantName, true);	
				if(addFileResult){					
					response=Response.ok().build();
				}else{					
					response=Response.serverError().build();
				}
				tempFile.delete();
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}	
	
	@POST
	@Path("/participantPersonalFile/deleteFile/")
	@Produces("application/json")
	public ContentOperationResultVO deleteParticipantFile(ParticipantFileVO participantFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		
		String activitySpace=participantFileVO.getActivitySpaceName();
		String parentFolderPath=participantFileVO.getParentFolderPath();
		String fileName=participantFileVO.getFileName();
		String participantName=participantFileVO.getParticipantName();		
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;		
		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("participant file folder doesn't exist");				
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){
					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("participant file doesn't exist");
					}else{
						String fileType=coh.getContentObjectType(fileContentObject);
						boolean operationResult=false;
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_BINARTCONTENT)){
							operationResult=coh.removeBinaryContent(currentFolderContentObject, fileName, true);
						}
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_TEXTBINARY)){
							operationResult=coh.removeTextContent(currentFolderContentObject, fileName, true);				
						}
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)){
							operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
						}
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
						}	
						contentOperationResultVO.setOperationResult(operationResult);
						
						if(operationResult){
							contentOperationResultVO.setResultReason("delete participant file successed");
						}else{
							contentOperationResultVO.setResultReason("delete participant file failed");
						}
					}						
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("participant file folder doesn't exist");
				}					
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}			
	
	@POST
	@Path("/participantPersonalFile/lockFile/")
	@Produces("application/json")
	public ContentOperationResultVO lockParticipantFile(ParticipantFileVO participantFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("participant file not locked");
		
		String activitySpace=participantFileVO.getActivitySpaceName();
		String parentFolderPath=participantFileVO.getParentFolderPath();
		String fileName=participantFileVO.getFileName();
		String participantName=participantFileVO.getParticipantName();		
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;		
		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("participant file folder doesn't exist");				
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("participant file doesn't exist");
					}else{						
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							documentContentObject.lock(false,participantFileVO.getParticipantName());
						}else{
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
							targetBinaryContent.lock(false,participantFileVO.getParticipantName());								
						}												
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("participant file locked");
					}	
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("participant file folder doesn't exist");
				}					
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}		
	
	@POST
	@Path("/participantPersonalFile/unlockFile/")
	@Produces("application/json")
	public ContentOperationResultVO unlockParticipantFile(ParticipantFileVO participantFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("participant file not unlocked");
		
		String activitySpace=participantFileVO.getActivitySpaceName();
		String parentFolderPath=participantFileVO.getParentFolderPath();
		String fileName=participantFileVO.getFileName();
		String participantName=participantFileVO.getParticipantName();		
		String participantFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;		
		
		ContentSpace activityContentSpace = null;				
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject participantFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(participantFolderRootAbsPath);			
			BaseContentObject targetParticipantFolderRoot=participantFolderRootContentObject.getSubContentObject(participantName);
			if(targetParticipantFolderRoot==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("participant file folder doesn't exist");				
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);					
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("participant file doesn't exist");
					}else{						
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							documentContentObject.unlock(participantFileVO.getParticipantName());
						}else{
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
							targetBinaryContent.unlock(participantFileVO.getParticipantName());							
						}												
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("participant file unlocked");
					}	
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("participant file folder doesn't exist");
				}			
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}	
	
	@Path("/participantPersonalFile/downloadFile/")
	@GET
	public javax.ws.rs.core.Response getFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("participantName") String participantName,@QueryParam("browserType") String browserType){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;		
		String currentDocumentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentDocumentFolderFullPath);		
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);			
			
			String fileTempName=activitySpaceName+currentDocumentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(documentContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String downloadFileName="";		
			String formatedFileName=fileName.replaceAll(" ", "_");
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(formatedFileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(formatedFileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String(formatedFileName.getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}			
	
	@POST
	@Path("/participantPersonalFile/getFileVersionHistory/")
	@Produces("application/json")
	public List<DocumentVersionVO> getParticipantPersonalFileFileVersionHistory(ParticipantFileVO participantFileVO){		
		List<DocumentVersionVO> documentVersionList=new ArrayList<DocumentVersionVO>();	
		String activitySpace=participantFileVO.getActivitySpaceName();
		String parentFolderPath=participantFileVO.getParentFolderPath();
		String fileName=participantFileVO.getFileName();
		String participantName=participantFileVO.getParticipantName();						
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;		
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
							List<BinaryContentVersionObject> versionObjectList=targetBinaryContent.getAllLinearVersions();								
							for(BinaryContentVersionObject currentVersionObject:versionObjectList){								
								DocumentVersionVO currentDocumentVersionVO=new DocumentVersionVO();
								currentDocumentVersionVO.setVersionCreatedDate(currentVersionObject.getCurrentVersionCreatedDate().getTimeInMillis());
								currentDocumentVersionVO.setVersionLabels(currentVersionObject.getCurrentVersionLabels());
								currentDocumentVersionVO.setVersionNumber(currentVersionObject.getCurrentVersionNumber());									
								
								DocumentContentVO currentDocumentContentVO=new DocumentContentVO();	
								currentDocumentContentVO.setFolder(false);
								BinaryContent bco=currentVersionObject.getBinaryContent();
													
								currentDocumentContentVO.setDocumentFolderPath(currentFolderFullPath);								
								currentDocumentContentVO.setDocumentLastUpdateDate(bco.getLastModified().getTimeInMillis());					
								currentDocumentContentVO.setDocumentName(bco.getContentName());
								currentDocumentContentVO.setDocumentSize(bco.getContentSize());
								currentDocumentContentVO.setDocumentType(bco.getMimeType());
								currentDocumentContentVO.setVersion(currentVersionObject.getCurrentVersionNumber());
								//if these four properties needed for version history?
								currentDocumentContentVO.setLinked(false);
								currentDocumentContentVO.setLocked(false);	
								currentDocumentContentVO.setLockedBy(null);
								currentDocumentContentVO.setDocumentTags(null);		
								
								if(bco.getCreated()!=null){
									currentDocumentContentVO.setDocumentCreateDate(bco.getCreated().getTimeInMillis());
								}	
								List<String> userList=new ArrayList<String>();
								if(bco.getCreatedBy()!=null){												
									userList.add(bco.getCreatedBy());
								}
								if(bco.getLastModifiedBy()!=null){						
									userList.add(bco.getLastModifiedBy());
								}
								/*
								if(bco.getLocker()!=null){						
									userList.add(bco.getLocker());	
								}
								*/						
								if(userList.size()>0){
									ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
									participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
									participantDetailInfosQueryVO.setParticipantScope(activitySpace);						
									ParticipantDetailInfoVOsList participantDetailInfoVOsList=
											ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
									List<ParticipantDetailInfoVO> commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();						
									if(commentParticipantsList!=null&&commentParticipantsList.size()>0){
										if(commentParticipantsList.size()>=1){
											currentDocumentContentVO.setDocumentCreator(commentParticipantsList.get(0));
										}
										if(commentParticipantsList.size()>=2){
											currentDocumentContentVO.setDocumentLastUpdatePerson(commentParticipantsList.get(1));
										}							
										if(commentParticipantsList.size()>=3){
											currentDocumentContentVO.setDocumentLocker(commentParticipantsList.get(2));
										}							
									}
								}	
								currentDocumentVersionVO.setDocumentContent(currentDocumentContentVO);
								documentVersionList.add(currentDocumentVersionVO);
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
		return documentVersionList;
	}			
	
	@Path("/participantPersonalFile/downloadHistoryFile/{historyVersion}/")
	@GET
	public javax.ws.rs.core.Response getParticipantPersonalHistoryFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("participantName") String participantName,@QueryParam("browserType") String browserType,@PathParam("historyVersion") String historyVersion){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;			
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
			BinaryContent targetVersionBinaryContent=documentContent;
			List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
			for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
				if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
					targetVersionBinaryContent=currentVersionObject.getBinaryContent();
					break;					
				}					
			}		
			
			String fileTempName=historyVersion+activitySpaceName+currentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(targetVersionBinaryContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String versionPerFix="V"+historyVersion+"_";
			String downloadFileName=versionPerFix+fileName;			
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String((versionPerFix+fileName).getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}	
	
	@POST
    @Path("/businessActivityFolder/")
	@Produces("application/json")
	public ContentFolderVO activityFolder(ActivityFolderQueryVO activityFolderQueryVO){
		String activitySpaceName=activityFolderQueryVO.getActivitySpaceName();
		String activityName=activityFolderQueryVO.getActivityName();
		String activityId=activityFolderQueryVO.getActivityId();		
		String parentFolderPath=activityFolderQueryVO.getParentFolderPath()!=null?activityFolderQueryVO.getParentFolderPath():"";
		String currentFolderName=activityFolderQueryVO.getFolderName()!=null?activityFolderQueryVO.getFolderName():"";					
		if(!parentFolderPath.startsWith("/")){
			parentFolderPath="/"+parentFolderPath;
		}		
		String currentFolderAbsPath=null;
		if(parentFolderPath.endsWith("/")){
			currentFolderAbsPath=parentFolderPath+currentFolderName;				
		}else{
			currentFolderAbsPath=parentFolderPath+"/"+currentFolderName;
		}	
		
		String activityTypeFolderRootAbsPath="/"+activityName+"/";
		String currentFolderFullPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment+currentFolderAbsPath;	
		
		ContentFolderVO contentFolderVO=new ContentFolderVO();
		contentFolderVO.setFolderName(currentFolderName);		
		contentFolderVO.setFolderPath(currentFolderAbsPath);		
		contentFolderVO.setParentFolderPath(parentFolderPath);	
		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);				
			BaseContentObject activityTypeFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);							
			if(activityTypeFolderContentObject==null){
				return null;
			}else{
				contentFolderVO.setFolderLocked(activityTypeFolderContentObject.isLocked());
				List<DocumentContentVO> childContentList=buildDocumentContentList(activityTypeFolderContentObject,currentFolderAbsPath,activitySpaceName);				
				contentFolderVO.setChildContentList(childContentList);					
				
				SecurityOperationHelper soh=ContentComponentFactory.getSecurityOperationHelper();			
				List<PermissionObject> permissionObjectList=soh.getContentPermissions(activityTypeFolderContentObject);				
				List<ContentPermissionVO> contentPermissionList=new ArrayList<ContentPermissionVO>();					
				for(PermissionObject permissionObject:permissionObjectList){						
					ContentPermissionVO currentContentPermissionVO=new ContentPermissionVO();	
					currentContentPermissionVO.setDisplayContentPermission(permissionObject.getDisplayContentPermission());
					currentContentPermissionVO.setAddContentPermission(permissionObject.getAddContentPermission());
					currentContentPermissionVO.setAddSubFolderPermission(permissionObject.getAddSubFolderPermission());
					currentContentPermissionVO.setDeleteContentPermission(permissionObject.getDeleteContentPermission());
					currentContentPermissionVO.setDeleteSubFolderPermission(permissionObject.getDeleteSubFolderPermission());					
					currentContentPermissionVO.setEditContentPermission(permissionObject.getEditContentPermission());
					currentContentPermissionVO.setConfigPermissionPermission(permissionObject.getConfigPermissionPermission());
					currentContentPermissionVO.setPermissionScope(permissionObject.getPermissionScope());
					currentContentPermissionVO.setPermissionParticipant(permissionObject.getPermissionParticipant());						
					contentPermissionList.add(currentContentPermissionVO);		
				}			
				contentFolderVO.setFolderPermissions(contentPermissionList);				
				return contentFolderVO;
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
    @Path("/setBusinessActivityFolderPermissions/")
	@Produces("application/json")
	public BooleanOperationResultVO setActivityFolderPermissions(SetActivityFolderPermissionsVO setActivityFolderPermissionsVO){		
		ActivityFolderQueryVO activityFolderQueryVO=setActivityFolderPermissionsVO.getActivityFolder();
		String activitySpaceName=activityFolderQueryVO.getActivitySpaceName();
		String activityName=activityFolderQueryVO.getActivityName();
		String activityId=activityFolderQueryVO.getActivityId();		
		String parentFolderPath=activityFolderQueryVO.getParentFolderPath()!=null?activityFolderQueryVO.getParentFolderPath():"";
		String currentFolderName=activityFolderQueryVO.getFolderName()!=null?activityFolderQueryVO.getFolderName():"";					
		if(!parentFolderPath.startsWith("/")){
			parentFolderPath="/"+parentFolderPath;
		}		
		String currentFolderAbsPath=null;
		if(parentFolderPath.endsWith("/")){
			currentFolderAbsPath=parentFolderPath+currentFolderName;				
		}else{
			currentFolderAbsPath=parentFolderPath+"/"+currentFolderName;
		}			
		String activityTypeFolderRootAbsPath="/"+activityName+"/";
		String currentFolderFullPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment+currentFolderAbsPath;			
		BooleanOperationResultVO booleanOperationResultVO=new BooleanOperationResultVO();		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject activityTypeFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityTypeFolderContentObject==null){
				return null;
			}else{
				SecurityOperationHelper soh=ContentComponentFactory.getSecurityOperationHelper();				
				List<ContentPermissionVO> contentPermissionVOList=setActivityFolderPermissionsVO.getPermissionsList();				
				List<PermissionObject> permissionList=new ArrayList<PermissionObject>();				
				for(ContentPermissionVO contentPermissionVO:contentPermissionVOList){
					PermissionObject currentPermissionObject=ContentComponentFactory.createPermissionObject();	
					currentPermissionObject.setDisplayContentPermission(contentPermissionVO.getDisplayContentPermission());
					currentPermissionObject.setAddContentPermission(contentPermissionVO.getAddContentPermission());
					currentPermissionObject.setAddSubFolderPermission(contentPermissionVO.getAddSubFolderPermission());
					currentPermissionObject.setDeleteContentPermission(contentPermissionVO.getDeleteContentPermission());
					currentPermissionObject.setDeleteSubFolderPermission(contentPermissionVO.getDeleteSubFolderPermission());
					currentPermissionObject.setEditContentPermission(contentPermissionVO.getEditContentPermission());
					currentPermissionObject.setConfigPermissionPermission(contentPermissionVO.getConfigPermissionPermission());					
					currentPermissionObject.setPermissionScope(contentPermissionVO.getPermissionScope());
					currentPermissionObject.setPermissionParticipant(contentPermissionVO.getPermissionParticipant());					
					permissionList.add(currentPermissionObject);					
				}							
				boolean result=soh.setContentPermissions(activityTypeFolderContentObject, permissionList);				
				booleanOperationResultVO.setTiemStamp(new Date().getTime());
				booleanOperationResultVO.setOperationResult(result);		
				return booleanOperationResultVO;
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
	@Path("/addBusinessActivityFolder/")
	@Produces("application/json")
	public static ContentOperationResultVO addBusinessActivityFolder(AddActivityFolderVO addActivityFolderVO){
		String activitySpaceName=addActivityFolderVO.getActivitySpaceName();
		String activityName=addActivityFolderVO.getActivityName();
		String activityId=addActivityFolderVO.getActivityId();
		String currentFolderAbsPath=addActivityFolderVO.getParentFolderPath()!=null?addActivityFolderVO.getParentFolderPath():"";
		if(!currentFolderAbsPath.startsWith("/")){
			currentFolderAbsPath="/"+currentFolderAbsPath;				
		}			
		String folderName=addActivityFolderVO.getFolderName();
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String activityTypeFolderRootAbsPath="/"+activityName+"/";		
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;		
		String currentFolderFullPath=activityInstanceFolderRootPath+currentFolderAbsPath;		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject activityInstanceFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(activityInstanceFolderRootContentObject==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("activity instance folder path does not exist.");					
			}else{
				BaseContentObject subFolderContentObject=activityInstanceFolderRootContentObject.addSubContentObject(folderName, null, false);
				if(addActivityFolderVO.getFolderCreator()!=null){
					subFolderContentObject.addProperty("vfcr:creator", addActivityFolderVO.getFolderCreator(), false);
				}
				if(subFolderContentObject!=null){
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("activity instance folder successed.");
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("activity instance folder failed.");
				}					
			}			
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}		
		return contentOperationResultVO;		
	}	
	
	/*Common json response service for upload file
	@POST
	@Path("/businessActivityFile/addFile/{activitySpace}/")
	@Consumes("multipart/form-data")
	public ContentOperationResultVO addActivityInstanceFile(@PathParam("activitySpace") String activitySpace,MultipartBody body){			
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		String activityType=body.getAttachmentObject("activityType",String.class);
		String activityId=body.getAttachmentObject("activityId",String.class);
		
		if(newFileAbsPath==null){
			contentOperationResultVO.setOperationResult(false);
			contentOperationResultVO.setResultReason("activity instance folder path does not provided.");
			return contentOperationResultVO;
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		String fileType=fileAttachment.getContentType().toString();		
		
		DataHandler dataHandler=fileAttachment.getDataHandler();			
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/ACTIVITY_TEMP/";		
		String activityTempFileFullPath=tempFileRootPath+activitySpace+"/"+"ACTIVITYS/"+activityType;		
		boolean createTempFolder=(new File(activityTempFileFullPath)).mkdirs();		
		String activityTypeTemFolderFullPath=activityTempFileFullPath+"/";				
		String activityTypeFolderRootAbsPath="/"+activityType+"/";		
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;	
		String currentFolderFullPath=activityInstanceFolderRootPath+newFileAbsPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject activityInstanceCurrentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityInstanceCurrentFolderContentObject==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("Activity instance folder path does not exist.");					
			}else{	
				String fileURI=activityTypeTemFolderFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}							
				boolean addFileResult=coh.addBinaryContent(activityInstanceCurrentFolderContentObject, tempFile, "", true);					
				if(addFileResult){
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("Add activity instance file successed.");
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Add activity instance file failed.");
				}
				tempFile.delete();
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}	
	*/
	
	@POST
	@Path("/businessActivityFile/addFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response addActivityInstanceFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){			
		//ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		String activityType=body.getAttachmentObject("activityType",String.class);
		String activityId=body.getAttachmentObject("activityId",String.class);
		
		if(newFileAbsPath==null){
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		String fileType=fileAttachment.getContentType().toString();		
		
		DataHandler dataHandler=fileAttachment.getDataHandler();			
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/ACTIVITY_TEMP/";		
		String activityTempFileFullPath=tempFileRootPath+activitySpace+"/"+"ACTIVITYS/"+activityType;		
		boolean createTempFolder=(new File(activityTempFileFullPath)).mkdirs();		
		String activityTypeTemFolderFullPath=activityTempFileFullPath+"/";				
		String activityTypeFolderRootAbsPath="/"+activityType+"/";		
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;	
		String currentFolderFullPath=activityInstanceFolderRootPath+newFileAbsPath;	
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject activityInstanceCurrentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityInstanceCurrentFolderContentObject==null){				
				response=Response.serverError().build();
			}else{	
				String fileURI=activityTypeTemFolderFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}							
				boolean addFileResult=coh.addBinaryContent(activityInstanceCurrentFolderContentObject, tempFile, "",participantName, true);					
				if(addFileResult){					
					response=Response.ok().build();
				}else{					
					response=Response.serverError().build();
				}
				tempFile.delete();
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}	
	
	@POST
	@Path("/businessActivityFile/updateFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response updateActivityInstanceFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){				
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		String activityType=body.getAttachmentObject("activityType",String.class);
		String activityId=body.getAttachmentObject("activityId",String.class);
		
		if(newFileAbsPath==null){
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		//String fileType=fileAttachment.getContentType().toString();		
		
		DataHandler dataHandler=fileAttachment.getDataHandler();			
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/ACTIVITY_TEMP/";		
		String activityTempFileFullPath=tempFileRootPath+activitySpace+"/"+"ACTIVITYS/"+activityType;		
		//boolean createTempFolder=(new File(activityTempFileFullPath)).mkdirs();		
		String activityTypeTemFolderFullPath=activityTempFileFullPath+"/";				
		String activityTypeFolderRootAbsPath="/"+activityType+"/";		
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;	
		String currentFolderFullPath=activityInstanceFolderRootPath+newFileAbsPath;	
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject activityInstanceCurrentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityInstanceCurrentFolderContentObject==null){				
				response=Response.serverError().build();
			}else{	
				String fileURI=activityTypeTemFolderFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}	
				boolean addFileResult=coh.updateBinaryContent(activityInstanceCurrentFolderContentObject, newFileName,tempFile, "", participantName, true);	
				if(addFileResult){					
					response=Response.ok().build();
				}else{					
					response=Response.serverError().build();
				}
				tempFile.delete();
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}	
	
	@POST
	@Path("/businessActivityFile/deleteFile/")
	@Produces("application/json")
	public ContentOperationResultVO deleteBusinessActivityFile(ActivityTypeFileVO activityTypeFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		
		String activitySpace=activityTypeFileVO.getActivitySpaceName();
		String parentFolderPath=activityTypeFileVO.getParentFolderPath();
		String fileName=activityTypeFileVO.getFileName();
		String activityType=activityTypeFileVO.getActivityName();
		String activityId=activityTypeFileVO.getActivityId();		
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
		String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;
		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);	
			
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(currentFolderContentObject!=null){
				
				BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
				if(fileContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("activity instance file doesn't exist");
				}else{
					String fileType=coh.getContentObjectType(fileContentObject);
					boolean operationResult=false;
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_BINARTCONTENT)){
						operationResult=coh.removeBinaryContent(currentFolderContentObject, fileName, true);
					}
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_TEXTBINARY)){
						operationResult=coh.removeTextContent(currentFolderContentObject, fileName, true);				
					}
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)){
						operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
					}
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
						operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
					}	
					contentOperationResultVO.setOperationResult(operationResult);
					
					if(operationResult){
						contentOperationResultVO.setResultReason("delete activity instance file successed");
					}else{
						contentOperationResultVO.setResultReason("delete activity instance file failed");
					}
				}						
			}else{
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("activity instance file folder doesn't exist");
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}		
	
	@Path("/businessActivityFile/downloadFile/")
	@GET
	public javax.ws.rs.core.Response getActivityInstanceFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("activityType") String activityType,@QueryParam("activityId") String activityId,@QueryParam("browserType") String browserType){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;			
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
		String currentDocumentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentDocumentFolderFullPath);	
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
			
			String fileTempName=activitySpaceName+currentDocumentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(documentContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String downloadFileName=fileName;			
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(fileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(fileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(fileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(fileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(fileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}	
	
	@Path("/businessActivityFile/downloadHistoryFile/{historyVersion}/")
	@GET
	public javax.ws.rs.core.Response getActivityInstanceHistoryFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("activityType") String activityType,@QueryParam("activityId") String activityId,@QueryParam("browserType") String browserType,@PathParam("historyVersion") String historyVersion){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;			
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
		String currentDocumentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentDocumentFolderFullPath);	
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
			BinaryContent targetVersionBinaryContent=documentContent;
			List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
			for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
				if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
					targetVersionBinaryContent=currentVersionObject.getBinaryContent();
					break;					
				}					
			}		
			
			String fileTempName=historyVersion+activitySpaceName+currentDocumentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(targetVersionBinaryContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String versionPerFix="V"+historyVersion+"_";
			String downloadFileName=versionPerFix+fileName;			
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String((versionPerFix+fileName).getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}	
	
	@POST
	@Path("/businessActivityFile/lockFile/")
	@Produces("application/json")
	public ContentOperationResultVO lockActivityFile(ActivityTypeFileVO activityTypeFileVO){		
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("activity instance file not locked");
		
		String activitySpace=activityTypeFileVO.getActivitySpaceName();
		String parentFolderPath=activityTypeFileVO.getParentFolderPath();
		String fileName=activityTypeFileVO.getFileName();
		String activityType=activityTypeFileVO.getActivityName();
		String activityId=activityTypeFileVO.getActivityId();		
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
		String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
		
		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("activity instance file doesn't exist");
					}else{						
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							documentContentObject.lock(false,activityTypeFileVO.getParticipantName());
						}else{
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
							targetBinaryContent.lock(false,activityTypeFileVO.getParticipantName());								
						}												
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("activity instance file locked");
					}	
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("activity instance file folder doesn't exist");
				}					
				
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}	
	
	@POST
	@Path("/businessActivityFile/getFileVersionHistory/")
	@Produces("application/json")
	public List<DocumentVersionVO> getFileVersionHistory(ActivityTypeFileVO activityTypeFileVO){		
		List<DocumentVersionVO> documentVersionList=new ArrayList<DocumentVersionVO>();	
		
		String activitySpace=activityTypeFileVO.getActivitySpaceName();
		String parentFolderPath=activityTypeFileVO.getParentFolderPath();
		String fileName=activityTypeFileVO.getFileName();
		String activityType=activityTypeFileVO.getActivityName();
		String activityId=activityTypeFileVO.getActivityId();		
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
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
							List<BinaryContentVersionObject> versionObjectList=targetBinaryContent.getAllLinearVersions();								
							for(BinaryContentVersionObject currentVersionObject:versionObjectList){								
								DocumentVersionVO currentDocumentVersionVO=new DocumentVersionVO();
								currentDocumentVersionVO.setVersionCreatedDate(currentVersionObject.getCurrentVersionCreatedDate().getTimeInMillis());
								currentDocumentVersionVO.setVersionLabels(currentVersionObject.getCurrentVersionLabels());
								currentDocumentVersionVO.setVersionNumber(currentVersionObject.getCurrentVersionNumber());									
								
								DocumentContentVO currentDocumentContentVO=new DocumentContentVO();	
								currentDocumentContentVO.setFolder(false);
								BinaryContent bco=currentVersionObject.getBinaryContent();
													
								currentDocumentContentVO.setDocumentFolderPath(currentFolderFullPath);								
								currentDocumentContentVO.setDocumentLastUpdateDate(bco.getLastModified().getTimeInMillis());					
								currentDocumentContentVO.setDocumentName(bco.getContentName());
								currentDocumentContentVO.setDocumentSize(bco.getContentSize());
								currentDocumentContentVO.setDocumentType(bco.getMimeType());
								currentDocumentContentVO.setVersion(currentVersionObject.getCurrentVersionNumber());
								//if these four properties needed for version history?
								currentDocumentContentVO.setLinked(false);
								currentDocumentContentVO.setLocked(false);	
								currentDocumentContentVO.setLockedBy(null);
								currentDocumentContentVO.setDocumentTags(null);		
								
								if(bco.getCreated()!=null){
									currentDocumentContentVO.setDocumentCreateDate(bco.getCreated().getTimeInMillis());
								}	
								List<String> userList=new ArrayList<String>();
								if(bco.getCreatedBy()!=null){												
									userList.add(bco.getCreatedBy());
								}
								if(bco.getLastModifiedBy()!=null){						
									userList.add(bco.getLastModifiedBy());
								}
								/*
								if(bco.getLocker()!=null){						
									userList.add(bco.getLocker());	
								}
								*/						
								if(userList.size()>0){
									ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
									participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
									participantDetailInfosQueryVO.setParticipantScope(activitySpace);						
									ParticipantDetailInfoVOsList participantDetailInfoVOsList=
											ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
									List<ParticipantDetailInfoVO> commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();						
									if(commentParticipantsList!=null&&commentParticipantsList.size()>0){
										if(commentParticipantsList.size()>=1){
											currentDocumentContentVO.setDocumentCreator(commentParticipantsList.get(0));
										}
										if(commentParticipantsList.size()>=2){
											currentDocumentContentVO.setDocumentLastUpdatePerson(commentParticipantsList.get(1));
										}							
										if(commentParticipantsList.size()>=3){
											currentDocumentContentVO.setDocumentLocker(commentParticipantsList.get(2));
										}							
									}
								}	
								currentDocumentVersionVO.setDocumentContent(currentDocumentContentVO);
								documentVersionList.add(currentDocumentVersionVO);
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
		return documentVersionList;
	}			
	
	@POST
	@Path("/businessActivityFile/unlockFile/")
	@Produces("application/json")
	public ContentOperationResultVO unlockActivityFile(ActivityTypeFileVO activityTypeFileVO){		
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("activity instance file not unlocked");
		
		String activitySpace=activityTypeFileVO.getActivitySpaceName();
		String parentFolderPath=activityTypeFileVO.getParentFolderPath();
		String fileName=activityTypeFileVO.getFileName();
		String activityType=activityTypeFileVO.getActivityName();
		String activityId=activityTypeFileVO.getActivityId();		
		String activityTypeFolderRootAbsPath="/"+activityType+"/";
		String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
		String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			

		ContentSpace activityContentSpace = null;	
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("activity instance file doesn't exist");
					}else{						
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							documentContentObject.unlock(activityTypeFileVO.getParticipantName());
						}else{
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
							targetBinaryContent.unlock(activityTypeFileVO.getParticipantName());								
						}												
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("activity instance file unlocked");
					}	
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("activity instance file folder doesn't exist");
				}					
				
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}			
	
	@POST
    @Path("/applicationSpaceFolder/")
	@Produces("application/json")
	public ContentFolderVO applicationSpaceFolder(ApplicationSpaceFolderQueryVO applicationSpaceFolderQueryVO){
		String activitySpaceName=applicationSpaceFolderQueryVO.getActivitySpaceName();		
		
		String parentFolderPath=applicationSpaceFolderQueryVO.getParentFolderPath()!=null?applicationSpaceFolderQueryVO.getParentFolderPath():"";
		String currentFolderName=applicationSpaceFolderQueryVO.getFolderName()!=null?applicationSpaceFolderQueryVO.getFolderName():"";			
		
		if(!parentFolderPath.startsWith("/")){
			parentFolderPath="/"+parentFolderPath;
		}		
		String currentFolderAbsPath=null;
		if(parentFolderPath.endsWith("/")){
			currentFolderAbsPath=parentFolderPath+currentFolderName;				
		}else{
			currentFolderAbsPath=parentFolderPath+"/"+currentFolderName;
		}		
		
		String activitySpaceFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;		
		String currentFolderFullPath=activitySpaceFolderRootAbsPath+currentFolderAbsPath;
		
		ContentFolderVO contentFolderVO=new ContentFolderVO();
		contentFolderVO.setFolderName(currentFolderName);		
		contentFolderVO.setFolderPath(currentFolderAbsPath);		
		contentFolderVO.setParentFolderPath(parentFolderPath);	
		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(currentFolderContentObject==null){
				return null;
			}else{							
				contentFolderVO.setFolderLocked(currentFolderContentObject.isLocked());
				List<DocumentContentVO> childContentList=buildDocumentContentList(currentFolderContentObject,currentFolderAbsPath,activitySpaceName);
				contentFolderVO.setChildContentList(childContentList);
				return contentFolderVO;
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
	@Path("/addApplicationSpaceFolder/")
	@Produces("application/json")
	public static ContentOperationResultVO addApplicationSpaceFolder(AddApplicationSpaceFolderVO addApplicationSpaceFolderVO){
		String activitySpaceName=addApplicationSpaceFolderVO.getActivitySpaceName();		
		String currentFolderAbsPath=addApplicationSpaceFolderVO.getParentFolderPath()!=null?addApplicationSpaceFolderVO.getParentFolderPath():"";
		if(!currentFolderAbsPath.startsWith("/")){
			currentFolderAbsPath="/"+currentFolderAbsPath;				
		}			
		String folderName=addApplicationSpaceFolderVO.getFolderName();
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;	
		if(currentFolderAbsPath!=null){
			currentFolderFullPath=currentFolderFullPath+currentFolderAbsPath;
		}		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(currentFolderContentObject==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("ApplicationSpace folder path does not exist.");					
			}else{
				BaseContentObject subFolderContentObject=currentFolderContentObject.addSubContentObject(folderName, null, false);
				if(subFolderContentObject!=null){
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("Add ApplicationSpace folder successed.");
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Add ApplicationSpace folder failed.");
				}					
			}				
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}		
		return contentOperationResultVO;		
	}				
	
	@POST
	@Path("/applicationSpaceFile/addFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response addApplicationSpaceFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){			
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);		
		if(newFileAbsPath==null){			
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		String fileType=fileAttachment.getContentType().toString();			
		DataHandler dataHandler=fileAttachment.getDataHandler();		
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/APPLICATIONSPACE_TEMP/";		
		String applicationSpaceTempFileFullPath=tempFileRootPath+activitySpace;		
		boolean createTempFolder=(new File(applicationSpaceTempFileFullPath)).mkdirs();		
		String participantTemFolderFullPath=applicationSpaceTempFileFullPath+"/";		
		
		String applicationSpaceFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;	
		String currentFolderFullPath=applicationSpaceFolderRootAbsPath+newFileAbsPath;		
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);	
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(currentFolderContentObject==null){		
				response=Response.serverError().build();
			}else{	
				String fileURI=participantTemFolderFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}						
				boolean addFileResult=coh.addBinaryContent(currentFolderContentObject, tempFile, "", participantName,true);					
				if(addFileResult){
					response=Response.ok().build();
				}else{		
					response=Response.serverError().build();
				}
				tempFile.delete();
			}	
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}	
	
	@POST
	@Path("/applicationSpaceFile/updateFile/{activitySpace}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response updateApplicationSpaceFile(@PathParam("activitySpace") String activitySpace,@PathParam("userUid") String participantName,MultipartBody body){				
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		if(newFileAbsPath==null){
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");		
		DataHandler dataHandler=fileAttachment.getDataHandler();			
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/APPLICATIONSPACE_TEMP/";		
		String applicationSpaceTempFileFullPath=tempFileRootPath+activitySpace;		
		boolean createTempFolder=(new File(applicationSpaceTempFileFullPath)).mkdirs();		
		String participantTemFolderFullPath=applicationSpaceTempFileFullPath+"/";			
		String applicationSpaceFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;	
		String currentFolderFullPath=applicationSpaceFolderRootAbsPath+newFileAbsPath;		
		
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject activityInstanceCurrentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityInstanceCurrentFolderContentObject==null){				
				response=Response.serverError().build();
			}else{	
				String fileURI=participantTemFolderFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}	
				boolean addFileResult=coh.updateBinaryContent(activityInstanceCurrentFolderContentObject, newFileName,tempFile, "", participantName, true);	
				if(addFileResult){					
					response=Response.ok().build();
				}else{					
					response=Response.serverError().build();
				}
				tempFile.delete();
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}	
	
	@POST
	@Path("/applicationSpaceFile/getFileVersionHistory/")
	@Produces("application/json")
	public List<DocumentVersionVO> getApplicationSpaceFileVersionHistory(ApplicationSpaceFileVO applicationSpaceFileVO){		
		List<DocumentVersionVO> documentVersionList=new ArrayList<DocumentVersionVO>();	
		String activitySpace=applicationSpaceFileVO.getActivitySpaceName();
		String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
		String fileName=applicationSpaceFileVO.getFileName();		
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;		
		String currentFolderFullPath=roleFolderRootAbsPath+parentFolderPath;
		
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
							List<BinaryContentVersionObject> versionObjectList=targetBinaryContent.getAllLinearVersions();								
							for(BinaryContentVersionObject currentVersionObject:versionObjectList){								
								DocumentVersionVO currentDocumentVersionVO=new DocumentVersionVO();
								currentDocumentVersionVO.setVersionCreatedDate(currentVersionObject.getCurrentVersionCreatedDate().getTimeInMillis());
								currentDocumentVersionVO.setVersionLabels(currentVersionObject.getCurrentVersionLabels());
								currentDocumentVersionVO.setVersionNumber(currentVersionObject.getCurrentVersionNumber());									
								
								DocumentContentVO currentDocumentContentVO=new DocumentContentVO();	
								currentDocumentContentVO.setFolder(false);
								BinaryContent bco=currentVersionObject.getBinaryContent();
													
								currentDocumentContentVO.setDocumentFolderPath(currentFolderFullPath);								
								currentDocumentContentVO.setDocumentLastUpdateDate(bco.getLastModified().getTimeInMillis());					
								currentDocumentContentVO.setDocumentName(bco.getContentName());
								currentDocumentContentVO.setDocumentSize(bco.getContentSize());
								currentDocumentContentVO.setDocumentType(bco.getMimeType());
								currentDocumentContentVO.setVersion(currentVersionObject.getCurrentVersionNumber());
								//if these four properties needed for version history?
								currentDocumentContentVO.setLinked(false);
								currentDocumentContentVO.setLocked(false);	
								currentDocumentContentVO.setLockedBy(null);
								currentDocumentContentVO.setDocumentTags(null);		
								
								if(bco.getCreated()!=null){
									currentDocumentContentVO.setDocumentCreateDate(bco.getCreated().getTimeInMillis());
								}	
								List<String> userList=new ArrayList<String>();
								if(bco.getCreatedBy()!=null){												
									userList.add(bco.getCreatedBy());
								}
								if(bco.getLastModifiedBy()!=null){						
									userList.add(bco.getLastModifiedBy());
								}
								/*
								if(bco.getLocker()!=null){						
									userList.add(bco.getLocker());	
								}
								*/						
								if(userList.size()>0){
									ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
									participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
									participantDetailInfosQueryVO.setParticipantScope(activitySpace);						
									ParticipantDetailInfoVOsList participantDetailInfoVOsList=
											ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
									List<ParticipantDetailInfoVO> commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();						
									if(commentParticipantsList!=null&&commentParticipantsList.size()>0){
										if(commentParticipantsList.size()>=1){
											currentDocumentContentVO.setDocumentCreator(commentParticipantsList.get(0));
										}
										if(commentParticipantsList.size()>=2){
											currentDocumentContentVO.setDocumentLastUpdatePerson(commentParticipantsList.get(1));
										}							
										if(commentParticipantsList.size()>=3){
											currentDocumentContentVO.setDocumentLocker(commentParticipantsList.get(2));
										}							
									}
								}	
								currentDocumentVersionVO.setDocumentContent(currentDocumentContentVO);
								documentVersionList.add(currentDocumentVersionVO);
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
		return documentVersionList;
	}			
	
	@Path("/applicationSpaceFile/downloadHistoryFile/{historyVersion}/")
	@GET
	public javax.ws.rs.core.Response getApplicationSpaceHistoryFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("participantName") String participantName,@QueryParam("browserType") String browserType,@PathParam("historyVersion") String historyVersion){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;				
		String applicationSpaceFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;		
		String currentFolderFullPath=applicationSpaceFolderRootAbsPath+parentFolderPath;					
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
			BinaryContent targetVersionBinaryContent=documentContent;
			List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
			for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
				if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
					targetVersionBinaryContent=currentVersionObject.getBinaryContent();
					break;					
				}					
			}		
			
			String fileTempName=historyVersion+activitySpaceName+currentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(targetVersionBinaryContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String versionPerFix="V"+historyVersion+"_";
			String downloadFileName=versionPerFix+fileName;			
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String((versionPerFix+fileName).getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}	
	
	@POST
	@Path("/applicationSpaceFile/deleteFile/")
	@Produces("application/json")
	public ContentOperationResultVO deleteApplicationSpaceFile(ApplicationSpaceFileVO applicationSpaceFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String activitySpace=applicationSpaceFileVO.getActivitySpaceName();
		String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
		String fileName=applicationSpaceFileVO.getFileName();		
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;		
		String currentFolderFullPath=roleFolderRootAbsPath+parentFolderPath;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(currentFolderContentObject!=null){				
				BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
				if(fileContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("applicationSpace file doesn't exist");
				}else{
					String fileType=coh.getContentObjectType(fileContentObject);
					boolean operationResult=false;
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_BINARTCONTENT)){
						operationResult=coh.removeBinaryContent(currentFolderContentObject, fileName, true);
					}
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_TEXTBINARY)){
						operationResult=coh.removeTextContent(currentFolderContentObject, fileName, true);				
					}
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)){
						operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
					}
					if(fileType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
						operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
					}	
					contentOperationResultVO.setOperationResult(operationResult);
					
					if(operationResult){
						contentOperationResultVO.setResultReason("delete applicationSpace file successed");
					}else{
						contentOperationResultVO.setResultReason("delete applicationSpace file failed");
					}
				}						
			}else{
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("applicationSpace file folder doesn't exist");
			}			
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}	
		
	@POST
	@Path("/applicationSpaceFile/lockFile/")
	@Produces("application/json")
	public ContentOperationResultVO lockApplicationSpaceFile(ApplicationSpaceFileVO applicationSpaceFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("applicationSpace file not locked");		
		
		String activitySpace=applicationSpaceFileVO.getActivitySpaceName();
		String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
		String fileName=applicationSpaceFileVO.getFileName();		
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;		
		String currentFolderFullPath=roleFolderRootAbsPath+parentFolderPath;
		ContentSpace activityContentSpace = null;				
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);	
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(currentFolderContentObject!=null){						
				BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
				if(documentContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("applicationSpaceapplicationSpace file doesn't exist");
				}else{						
					String cType = coh.getContentObjectType(documentContentObject);	
					if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
						documentContentObject.lock(false,applicationSpaceFileVO.getParticipantName());
					}else{
						BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						targetBinaryContent.lock(false,applicationSpaceFileVO.getParticipantName());							
					}												
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("applicationSpace file locked");
				}	
			}else{
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("applicationSpace file folder doesn't exist");
			}	
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}		
	
	@POST
	@Path("/applicationSpaceFile/unlockFile/")
	@Produces("application/json")
	public ContentOperationResultVO unlockApplicationSpaceFile(ApplicationSpaceFileVO applicationSpaceFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("applicationSpace file not unlocked");		
		
		String activitySpace=applicationSpaceFileVO.getActivitySpaceName();
		String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
		String fileName=applicationSpaceFileVO.getFileName();		
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;		
		String currentFolderFullPath=roleFolderRootAbsPath+parentFolderPath;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			if(currentFolderContentObject!=null){						
				BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
				if(documentContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("applicationSpaceapplicationSpace file doesn't exist");
				}else{						
					String cType = coh.getContentObjectType(documentContentObject);	
					if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
						documentContentObject.unlock(applicationSpaceFileVO.getParticipantName());
					}else{
						BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						targetBinaryContent.unlock(applicationSpaceFileVO.getParticipantName());							
					}												
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("applicationSpace file unlocked");
				}	
			}else{
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("applicationSpace file folder doesn't exist");
			}	
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}			
	
	@Path("/applicationSpaceFile/downloadFile/")
	@GET
	public javax.ws.rs.core.Response getApplicationFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("browserType") String browserType){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;		
		String currentDocumentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentDocumentFolderFullPath);			
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
			
			String fileTempName=activitySpaceName+currentDocumentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(documentContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String downloadFileName="";		
			String formatedFileName=fileName.replaceAll(" ", "_");
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(formatedFileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(formatedFileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String(formatedFileName.getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}
	
	@POST
    @Path("/roleFolder/")
	@Produces("application/json")
	public ContentFolderVO roleFolder(RoleFolderQueryVO roleFolderQueryVO){
		String activitySpaceName=roleFolderQueryVO.getActivitySpaceName();
		String roleName=roleFolderQueryVO.getRoleName();		
		String parentFolderPath=roleFolderQueryVO.getParentFolderPath()!=null?roleFolderQueryVO.getParentFolderPath():"";
		String currentFolderName=roleFolderQueryVO.getFolderName()!=null?roleFolderQueryVO.getFolderName():"";			
		
		if(!parentFolderPath.startsWith("/")){
			parentFolderPath="/"+parentFolderPath;
		}		
		String currentFolderAbsPath=null;
		if(parentFolderPath.endsWith("/")){
			currentFolderAbsPath=parentFolderPath+currentFolderName;				
		}else{
			currentFolderAbsPath=parentFolderPath+"/"+currentFolderName;
		}		
		
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+currentFolderAbsPath;			
		ContentFolderVO contentFolderVO=new ContentFolderVO();
		contentFolderVO.setFolderName(currentFolderName);		
		contentFolderVO.setFolderPath(currentFolderAbsPath);		
		contentFolderVO.setParentFolderPath(parentFolderPath);			
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject roleFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(roleFolderRootAbsPath);
			
			BaseContentObject targetRoleFolderRoot=roleFolderRootContentObject.getSubContentObject(roleName);
			if(targetRoleFolderRoot==null){				
				targetRoleFolderRoot=roleFolderRootContentObject.addSubContentObject(roleName, null, false);
			}
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			contentFolderVO.setFolderLocked(currentFolderContentObject.isLocked());
			List<DocumentContentVO> childContentList=buildDocumentContentList(currentFolderContentObject,currentFolderAbsPath,activitySpaceName);
			contentFolderVO.setChildContentList(childContentList);
			return contentFolderVO;		
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
	@Path("/addRoleFolder/")
	@Produces("application/json")
	public static ContentOperationResultVO addRoleFolder(AddRoleFolderVO addRoleFolderVO){
		String activitySpaceName=addRoleFolderVO.getActivitySpaceName();
		String roleName=addRoleFolderVO.getRoleName();
		String currentFolderAbsPath=addRoleFolderVO.getParentFolderPath()!=null?addRoleFolderVO.getParentFolderPath():"";
		if(!currentFolderAbsPath.startsWith("/")){
			currentFolderAbsPath="/"+currentFolderAbsPath;				
		}			
		String folderName=addRoleFolderVO.getFolderName();
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/";	
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName;
		if(currentFolderAbsPath!=null){
			currentFolderFullPath=currentFolderFullPath+currentFolderAbsPath;
		}		
		ContentSpace activityContentSpace = null;		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);			
			BaseContentObject roleFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(roleFolderRootAbsPath);			
			BaseContentObject targetRoleFolderRoot=roleFolderRootContentObject.getSubContentObject(roleName);
			if(targetRoleFolderRoot==null){
				if(!currentFolderAbsPath.equals("/")){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Role root folder does not exist.");					
				}else{
					targetRoleFolderRoot=roleFolderRootContentObject.addSubContentObject(roleName, null, false);			
					targetRoleFolderRoot.addSubContentObject(folderName, null, false);
					contentOperationResultVO.setOperationResult(true);
					contentOperationResultVO.setResultReason("Add Role folder successed.");					
				}		
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject==null){
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("Role folder path does not exist.");					
				}else{
					BaseContentObject subFolderContentObject=currentFolderContentObject.addSubContentObject(folderName, null, false);
					if(subFolderContentObject!=null){
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("Add Role folder successed.");
					}else{
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("Add Role folder failed.");
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
		return contentOperationResultVO;		
	}		
	
	@POST
	@Path("/roleFile/addFile/{activitySpace}/{roleName}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response addRoleFile(@PathParam("activitySpace") String activitySpace,@PathParam("roleName") String roleName,@PathParam("userUid") String participantName,MultipartBody body){			
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);		
		if(newFileAbsPath==null){			
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");
		String fileType=fileAttachment.getContentType().toString();	
		
		DataHandler dataHandler=fileAttachment.getDataHandler();		
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/ROLE_TEMP/";		
		String rolempFileFullPath=tempFileRootPath+activitySpace+"/"+"ROLES/"+roleName;		
		boolean createTempFolder=(new File(rolempFileFullPath)).mkdirs();		
		String roleTemFolderFullPath=rolempFileFullPath+"/";		
		
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/";	
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+newFileAbsPath;		
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject roleFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(roleFolderRootAbsPath);			
			BaseContentObject targetRoleFolderRoot=roleFolderRootContentObject.getSubContentObject(roleName);
			if(targetRoleFolderRoot==null){
				if(!newFileAbsPath.equals("/")){	
					response=Response.serverError().build();					
				}else{
					targetRoleFolderRoot=roleFolderRootContentObject.addSubContentObject(roleName, null, false);	
					String fileURI=roleTemFolderFullPath+newFileName;					
					File tempFile=new File(fileURI);
					try {
						InputStream fileInputStream=dataHandler.getInputStream();									
						OutputStream out=new FileOutputStream(tempFile);
						byte buf[]=new byte[1024];
						int len;
						while((len=fileInputStream.read(buf))>0){
							out.write(buf,0,len);
						}
						out.close();			
						fileInputStream.close();							
					} catch (IOException e) {			
						e.printStackTrace();			
					}	
					boolean addFileResult=coh.addBinaryContent(targetRoleFolderRoot, tempFile, "", participantName,true);					
					if(addFileResult){
						response=Response.ok().build();
					}else{						
						
						response=Response.serverError().build();
					}
					tempFile.delete();										
				}		
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject==null){		
					response=Response.serverError().build();
				}else{	
					String fileURI=roleTemFolderFullPath+newFileName;					
					File tempFile=new File(fileURI);
					try {
						InputStream fileInputStream=dataHandler.getInputStream();									
						OutputStream out=new FileOutputStream(tempFile);
						byte buf[]=new byte[1024];
						int len;
						while((len=fileInputStream.read(buf))>0){
							out.write(buf,0,len);
						}
						out.close();			
						fileInputStream.close();							
					} catch (IOException e) {			
						e.printStackTrace();			
					}						
					boolean addFileResult=coh.addBinaryContent(currentFolderContentObject, tempFile, "", participantName,true);					
					if(addFileResult){
						response=Response.ok().build();
					}else{		
						response=Response.serverError().build();
					}
					tempFile.delete();
				}				
			}			
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}		
	
	@POST
	@Path("/roleFile/updateFile/{activitySpace}/{roleName}/{userUid}/")
	@Consumes("multipart/form-data")
	public Response updateRoleFile(@PathParam("activitySpace") String activitySpace,@PathParam("roleName") String roleName,@PathParam("userUid") String participantName,MultipartBody body){				
		String newFileAbsPath=body.getAttachmentObject("fileFolderPath",String.class);
		String newFileName=body.getAttachmentObject("fileName",String.class);
		if(newFileAbsPath==null){
			return Response.serverError().build();
		}
		if(!newFileAbsPath.startsWith("/")){
			newFileAbsPath="/"+newFileAbsPath;			
		}				
		Attachment fileAttachment=body.getAttachment("uploadedfile");		
		DataHandler dataHandler=fileAttachment.getDataHandler();			
		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/ROLE_TEMP/";		
		String rolempFileFullPath=tempFileRootPath+activitySpace+"/"+"ROLES/"+roleName;		
		boolean createTempFolder=(new File(rolempFileFullPath)).mkdirs();
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+newFileAbsPath;	
		
		Response response=null;
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject activityInstanceCurrentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);			
			if(activityInstanceCurrentFolderContentObject==null){				
				response=Response.serverError().build();
			}else{	
				String fileURI=rolempFileFullPath+newFileName;					
				File tempFile=new File(fileURI);
				try {
					InputStream fileInputStream=dataHandler.getInputStream();									
					OutputStream out=new FileOutputStream(tempFile);
					byte buf[]=new byte[1024];
					int len;
					while((len=fileInputStream.read(buf))>0){
						out.write(buf,0,len);
					}
					out.close();			
					fileInputStream.close();							
				} catch (IOException e) {			
					e.printStackTrace();			
				}	
				boolean addFileResult=coh.updateBinaryContent(activityInstanceCurrentFolderContentObject, newFileName,tempFile, "", participantName, true);	
				if(addFileResult){					
					response=Response.ok().build();
				}else{					
					response=Response.serverError().build();
				}
				tempFile.delete();
			}					
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return response;
	}	
	
	@POST
	@Path("/roleFile/getFileVersionHistory/")
	@Produces("application/json")
	public List<DocumentVersionVO> getRoleFileVersionHistory(RoleFileVO roleFileVO){		
		List<DocumentVersionVO> documentVersionList=new ArrayList<DocumentVersionVO>();
		
		String activitySpace=roleFileVO.getActivitySpaceName();
		String parentFolderPath=roleFileVO.getParentFolderPath();
		String fileName=roleFileVO.getFileName();			
		String roleName=roleFileVO.getRoleName();					
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;
		
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
							List<BinaryContentVersionObject> versionObjectList=targetBinaryContent.getAllLinearVersions();								
							for(BinaryContentVersionObject currentVersionObject:versionObjectList){								
								DocumentVersionVO currentDocumentVersionVO=new DocumentVersionVO();
								currentDocumentVersionVO.setVersionCreatedDate(currentVersionObject.getCurrentVersionCreatedDate().getTimeInMillis());
								currentDocumentVersionVO.setVersionLabels(currentVersionObject.getCurrentVersionLabels());
								currentDocumentVersionVO.setVersionNumber(currentVersionObject.getCurrentVersionNumber());									
								
								DocumentContentVO currentDocumentContentVO=new DocumentContentVO();	
								currentDocumentContentVO.setFolder(false);
								BinaryContent bco=currentVersionObject.getBinaryContent();
													
								currentDocumentContentVO.setDocumentFolderPath(currentFolderFullPath);								
								currentDocumentContentVO.setDocumentLastUpdateDate(bco.getLastModified().getTimeInMillis());					
								currentDocumentContentVO.setDocumentName(bco.getContentName());
								currentDocumentContentVO.setDocumentSize(bco.getContentSize());
								currentDocumentContentVO.setDocumentType(bco.getMimeType());
								currentDocumentContentVO.setVersion(currentVersionObject.getCurrentVersionNumber());
								//if these four properties needed for version history?
								currentDocumentContentVO.setLinked(false);
								currentDocumentContentVO.setLocked(false);	
								currentDocumentContentVO.setLockedBy(null);
								currentDocumentContentVO.setDocumentTags(null);		
								
								if(bco.getCreated()!=null){
									currentDocumentContentVO.setDocumentCreateDate(bco.getCreated().getTimeInMillis());
								}	
								List<String> userList=new ArrayList<String>();
								if(bco.getCreatedBy()!=null){												
									userList.add(bco.getCreatedBy());
								}
								if(bco.getLastModifiedBy()!=null){						
									userList.add(bco.getLastModifiedBy());
								}
								/*
								if(bco.getLocker()!=null){						
									userList.add(bco.getLocker());	
								}
								*/						
								if(userList.size()>0){
									ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
									participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
									participantDetailInfosQueryVO.setParticipantScope(activitySpace);						
									ParticipantDetailInfoVOsList participantDetailInfoVOsList=
											ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
									List<ParticipantDetailInfoVO> commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();						
									if(commentParticipantsList!=null&&commentParticipantsList.size()>0){
										if(commentParticipantsList.size()>=1){
											currentDocumentContentVO.setDocumentCreator(commentParticipantsList.get(0));
										}
										if(commentParticipantsList.size()>=2){
											currentDocumentContentVO.setDocumentLastUpdatePerson(commentParticipantsList.get(1));
										}							
										if(commentParticipantsList.size()>=3){
											currentDocumentContentVO.setDocumentLocker(commentParticipantsList.get(2));
										}							
									}
								}	
								currentDocumentVersionVO.setDocumentContent(currentDocumentContentVO);
								documentVersionList.add(currentDocumentVersionVO);
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
		return documentVersionList;
	}			
	
	@Path("/roleFile/downloadHistoryFile/{historyVersion}/")
	@GET
	public javax.ws.rs.core.Response getRoleHistoryFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("participantName") String participantName,@QueryParam("roleName") String roleName,@QueryParam("browserType") String browserType,@PathParam("historyVersion") String historyVersion){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;								
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
			BinaryContent targetVersionBinaryContent=documentContent;
			List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
			for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
				if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
					targetVersionBinaryContent=currentVersionObject.getBinaryContent();
					break;					
				}					
			}		
			
			String fileTempName=historyVersion+activitySpaceName+currentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(targetVersionBinaryContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String versionPerFix="V"+historyVersion+"_";
			String downloadFileName=versionPerFix+fileName;			
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(versionPerFix+fileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String((versionPerFix+fileName).getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(versionPerFix+fileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}	
	
	@POST
	@Path("/roleFile/deleteFile/")
	@Produces("application/json")
	public ContentOperationResultVO deleteRoleFile(RoleFileVO roleFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();		
		String activitySpace=roleFileVO.getActivitySpaceName();
		String parentFolderPath=roleFileVO.getParentFolderPath();
		String fileName=roleFileVO.getFileName();			
		String roleName=roleFileVO.getRoleName();	
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;		
		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject roleFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(roleFolderRootAbsPath);			
			BaseContentObject targetRoleFolderRoot=roleFolderRootContentObject.getSubContentObject(roleName);
			if(targetRoleFolderRoot==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("role file folder doesn't exist");				
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){
					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("role file doesn't exist");
					}else{
						String fileType=coh.getContentObjectType(fileContentObject);
						boolean operationResult=false;
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_BINARTCONTENT)){
							operationResult=coh.removeBinaryContent(currentFolderContentObject, fileName, true);
						}
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_TEXTBINARY)){
							operationResult=coh.removeTextContent(currentFolderContentObject, fileName, true);				
						}
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)){
							operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
						}
						if(fileType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							operationResult=currentFolderContentObject.removeSubContentObject(fileName, true);
						}	
						contentOperationResultVO.setOperationResult(operationResult);
						
						if(operationResult){
							contentOperationResultVO.setResultReason("delete role file successed");
						}else{
							contentOperationResultVO.setResultReason("delete role file failed");
						}
					}						
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("role file folder doesn't exist");
				}					
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}			
	
	@POST
	@Path("/roleFile/lockFile/")
	@Produces("application/json")
	public ContentOperationResultVO lockRoleFile(RoleFileVO roleFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();	
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("role file not locked");	
		String activitySpace=roleFileVO.getActivitySpaceName();
		String parentFolderPath=roleFileVO.getParentFolderPath();
		String fileName=roleFileVO.getFileName();			
		String roleName=roleFileVO.getRoleName();	
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;		
		
		ContentSpace activityContentSpace = null;			
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject roleFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(roleFolderRootAbsPath);			
			BaseContentObject targetRoleFolderRoot=roleFolderRootContentObject.getSubContentObject(roleName);
			if(targetRoleFolderRoot==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("role file folder doesn't exist");				
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);	
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("role file doesn't exist");
					}else{						
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							documentContentObject.lock(false,roleFileVO.getParticipantName());
						}else{
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);
							targetBinaryContent.lock(false,roleFileVO.getParticipantName());								
						}												
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("role file locked");
					}	
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("role file folder doesn't exist");
				}	
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}			
	
	@POST
	@Path("/roleFile/unlockFile/")
	@Produces("application/json")
	public ContentOperationResultVO unlockRoleFile(RoleFileVO roleFileVO){
		ContentOperationResultVO contentOperationResultVO=new ContentOperationResultVO();	
		contentOperationResultVO.setOperationResult(false);
		contentOperationResultVO.setResultReason("role file not unlocked");	
		String activitySpace=roleFileVO.getActivitySpaceName();
		String parentFolderPath=roleFileVO.getParentFolderPath();
		String fileName=roleFileVO.getFileName();			
		String roleName=roleFileVO.getRoleName();	
		String roleFolderRootAbsPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/";		
		String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;		
		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject roleFolderRootContentObject=activityContentSpace.getContentObjectByAbsPath(roleFolderRootAbsPath);			
			BaseContentObject targetRoleFolderRoot=roleFolderRootContentObject.getSubContentObject(roleName);
			if(targetRoleFolderRoot==null){
				contentOperationResultVO.setOperationResult(false);
				contentOperationResultVO.setResultReason("role file folder doesn't exist");				
			}else{
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);					
				if(currentFolderContentObject!=null){						
					BaseContentObject documentContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(documentContentObject==null){
						contentOperationResultVO.setOperationResult(false);
						contentOperationResultVO.setResultReason("role file doesn't exist");
					}else{						
						String cType = coh.getContentObjectType(documentContentObject);	
						if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
							documentContentObject.unlock(roleFileVO.getParticipantName());
						}else{
							BinaryContent targetBinaryContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
							targetBinaryContent.unlock(roleFileVO.getParticipantName());							
						}												
						contentOperationResultVO.setOperationResult(true);
						contentOperationResultVO.setResultReason("role file unlocked");
					}	
				}else{
					contentOperationResultVO.setOperationResult(false);
					contentOperationResultVO.setResultReason("role file folder doesn't exist");
				}	
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return contentOperationResultVO;
	}			
	
	@Path("/roleFile/downloadFile/")
	@GET
	public javax.ws.rs.core.Response getRoleFile(@HeaderParam("User-Agent")final String userAgent,
			@QueryParam("documentFolderPath") String documentFolderPath,@QueryParam("documentName") String documentName,@QueryParam("activitySpaceName") String activitySpaceName,
			@QueryParam("roleName") String roleName,@QueryParam("browserType") String browserType){				
		String parentFolderPath=documentFolderPath;			
		String fileName=documentName;		
		String currentDocumentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();		
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);
			BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentDocumentFolderFullPath);			
			BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
			
			String fileTempName=activitySpaceName+currentDocumentFolderFullPath+"_"+fileName;
			String tempFileFullName=fileTempName.replaceAll("/", "").replaceAll(ActivitySpace_ContentStore, "");
			String tempFileLocation=generateDownloadTempFile(documentContent.getContentInputStream(),tempFileFullName);
			AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(new FileInputStream(new File(tempFileLocation)));
			
			String downloadFileName="";		
			String formatedFileName=fileName.replaceAll(" ", "_");
			String attachmentFileNameStr="attachment; filename =";			
			try {				
				if(browserType.toUpperCase().equals("FIREFOX")){
					downloadFileName=MimeUtility.encodeText(formatedFileName,"UTF8", "B");					
				}
				if(browserType.toUpperCase().equals("IE")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8"); 
				}
				if(browserType.toUpperCase().equals("CHROME")){
					downloadFileName=MimeUtility.encodeText(formatedFileName,"UTF8", "B"); 
				}
				if(browserType.toUpperCase().equals("SAFARI")){
					downloadFileName=new String(formatedFileName.getBytes("UTF-8"),"ISO8859-1") ;				
				}
				if(browserType.toUpperCase().equals("MOZILLA")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8");					
				}
				if(browserType.toUpperCase().equals("OPERA")){
					downloadFileName=URLEncoder.encode(formatedFileName, "UTF8");
					attachmentFileNameStr="attachment; filename*=UTF-8''";
				}				
			} catch (UnsupportedEncodingException e) {				
				e.printStackTrace();
			}
			Response documentResponse=Response.ok(autoCloseDocumentInputStream, MediaType.APPLICATION_OCTET_STREAM).			
				header("content-disposition", attachmentFileNameStr + downloadFileName).build();			
			return documentResponse;						
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}			
		}	
		return Response.serverError().build();			
	}			
	
	@POST
	@Path("/generatePerviewFile/")
	@Produces("application/json")	
	public PreviewFileGenerateResultVO generatePerviewFile(PreviewTempFileGenerateVO previewTempFileGenerateVO){		
		PreviewFileGenerateResultVO previewFileGenerateResultVO=new PreviewFileGenerateResultVO();		
		String activitySpace=previewTempFileGenerateVO.getActivitySpaceName();		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_participant)){
				ParticipantFileVO participantFileVO=previewTempFileGenerateVO.getParticipantFileInfo();				
				String parentFolderPath=participantFileVO.getParentFolderPath();
				String fileName=participantFileVO.getFileName();					
				String participantName=participantFileVO.getParticipantName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());						
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}				
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_activity)){
				ActivityTypeFileVO activityTypeFileVO=previewTempFileGenerateVO.getActivityTypeFileInfo();
				String parentFolderPath=activityTypeFileVO.getParentFolderPath();
				String fileName=activityTypeFileVO.getFileName();
				String activityType=activityTypeFileVO.getActivityName();
				String activityId=activityTypeFileVO.getActivityId();		
				String activityTypeFolderRootAbsPath="/"+activityType+"/";
				String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
				String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());						
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}					
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_applicationSpace)){
				ApplicationSpaceFileVO applicationSpaceFileVO=previewTempFileGenerateVO.getApplicationSpaceFileInfo();
				String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
				String fileName=applicationSpaceFileVO.getFileName();				
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());						
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}								
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_role)){
				RoleFileVO roleFileVO=previewTempFileGenerateVO.getRoleFileInfo();				
				String parentFolderPath=roleFileVO.getParentFolderPath();
				String fileName=roleFileVO.getFileName();						
				String roleName=roleFileVO.getRoleName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());						
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}				
			}else{
				previewFileGenerateResultVO.setGenerateResult(false);				
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return previewFileGenerateResultVO;		
	}	
	
	@POST
	@Path("/generateHistoryPerviewFile/{historyVersion}/")
	@Produces("application/json")	
	public PreviewFileGenerateResultVO generateHistoryPerviewFile(PreviewTempFileGenerateVO previewTempFileGenerateVO,@PathParam("historyVersion") String historyVersion){		
		PreviewFileGenerateResultVO previewFileGenerateResultVO=new PreviewFileGenerateResultVO();		
		String activitySpace=previewTempFileGenerateVO.getActivitySpaceName();		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_participant)){
				ParticipantFileVO participantFileVO=previewTempFileGenerateVO.getParticipantFileInfo();				
				String parentFolderPath=participantFileVO.getParentFolderPath();
				String fileName=participantFileVO.getFileName();					
				String participantName=participantFileVO.getParticipantName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						BinaryContent targetVersionBinaryContent=documentContent;
						List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
						for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
							if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
								targetVersionBinaryContent=currentVersionObject.getBinaryContent();
								break;					
							}					
						}							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(targetVersionBinaryContent.getContentInputStream());
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}				
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_activity)){
				ActivityTypeFileVO activityTypeFileVO=previewTempFileGenerateVO.getActivityTypeFileInfo();
				String parentFolderPath=activityTypeFileVO.getParentFolderPath();
				String fileName=activityTypeFileVO.getFileName();
				String activityType=activityTypeFileVO.getActivityName();
				String activityId=activityTypeFileVO.getActivityId();		
				String activityTypeFolderRootAbsPath="/"+activityType+"/";
				String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
				String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						BinaryContent targetVersionBinaryContent=documentContent;
						List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
						for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
							if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
								targetVersionBinaryContent=currentVersionObject.getBinaryContent();
								break;					
							}					
						}							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(targetVersionBinaryContent.getContentInputStream());
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}					
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_applicationSpace)){
				ApplicationSpaceFileVO applicationSpaceFileVO=previewTempFileGenerateVO.getApplicationSpaceFileInfo();
				String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
				String fileName=applicationSpaceFileVO.getFileName();				
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						BinaryContent targetVersionBinaryContent=documentContent;
						List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
						for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
							if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
								targetVersionBinaryContent=currentVersionObject.getBinaryContent();
								break;					
							}					
						}							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(targetVersionBinaryContent.getContentInputStream());						
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}								
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_role)){
				RoleFileVO roleFileVO=previewTempFileGenerateVO.getRoleFileInfo();				
				String parentFolderPath=roleFileVO.getParentFolderPath();
				String fileName=roleFileVO.getFileName();						
				String roleName=roleFileVO.getRoleName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						BinaryContent targetVersionBinaryContent=documentContent;
						List<BinaryContentVersionObject> versionObjectList=documentContent.getAllLinearVersions();								
						for(BinaryContentVersionObject currentVersionObject:versionObjectList){					
							if(currentVersionObject.getCurrentVersionNumber().equals(historyVersion)){
								targetVersionBinaryContent=currentVersionObject.getBinaryContent();
								break;					
							}					
						}							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(targetVersionBinaryContent.getContentInputStream());						
						generatePreviewFile(autoCloseDocumentInputStream,previewTempFileGenerateVO.getTempFileName(),previewTempFileGenerateVO.isNeedDocumentConvert(),previewTempFileGenerateVO.getConvertOperation());	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(previewTempFileGenerateVO.getTempFileName());							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}				
			}else{
				previewFileGenerateResultVO.setGenerateResult(false);				
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return previewFileGenerateResultVO;		
	}
	
	@POST
	@Path("/generateThumbnailFile/")
	@Produces("application/json")	
	public PreviewFileGenerateResultVO generateThumbnailFile(PreviewTempFileGenerateVO previewTempFileGenerateVO){		
		PreviewFileGenerateResultVO previewFileGenerateResultVO=new PreviewFileGenerateResultVO();		
		String activitySpace=previewTempFileGenerateVO.getActivitySpaceName();		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_participant)){
				ParticipantFileVO participantFileVO=previewTempFileGenerateVO.getParticipantFileInfo();				
				String parentFolderPath=participantFileVO.getParentFolderPath();
				String fileName=participantFileVO.getFileName();					
				String participantName=participantFileVO.getParticipantName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());						
						String thumbnailFileName="Thum_"+activitySpace+"_"+participantName+"_Par";						
						String finalThumbFileName=generateThumbnailFile(autoCloseDocumentInputStream,thumbnailFileName);	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(finalThumbFileName);							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}				
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_activity)){
				ActivityTypeFileVO activityTypeFileVO=previewTempFileGenerateVO.getActivityTypeFileInfo();
				String parentFolderPath=activityTypeFileVO.getParentFolderPath();
				String fileName=activityTypeFileVO.getFileName();
				String activityType=activityTypeFileVO.getActivityName();
				String activityId=activityTypeFileVO.getActivityId();		
				String activityTypeFolderRootAbsPath="/"+activityType+"/";
				String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
				String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());
						String thumbnailFileName="Thum_"+activitySpace+"_"+activityId+"_Acti";	
						String finalThumbFileName=generateThumbnailFile(autoCloseDocumentInputStream,thumbnailFileName);	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(finalThumbFileName);							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}					
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_applicationSpace)){
				ApplicationSpaceFileVO applicationSpaceFileVO=previewTempFileGenerateVO.getApplicationSpaceFileInfo();
				String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
				String fileName=applicationSpaceFileVO.getFileName();				
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());
						String thumbnailFileName="Thum_"+activitySpace+"_"+applicationSpaceFileVO.getParticipantName()+"_Spac";
						String finalThumbFileName=generateThumbnailFile(autoCloseDocumentInputStream,thumbnailFileName);	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(finalThumbFileName);							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}								
			}else if(previewTempFileGenerateVO.getDocumentsOwnerType().equals(DocumentsOwnerType_role)){
				RoleFileVO roleFileVO=previewTempFileGenerateVO.getRoleFileInfo();				
				String parentFolderPath=roleFileVO.getParentFolderPath();
				String fileName=roleFileVO.getFileName();						
				String roleName=roleFileVO.getRoleName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						previewFileGenerateResultVO.setGenerateResult(false);
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);							
						AutoCloseInputStream autoCloseDocumentInputStream=new AutoCloseInputStream(documentContent.getContentInputStream());
						String thumbnailFileName="Thum_"+activitySpace+"_"+roleFileVO.getParticipantName()+"_Rol";
						String finalThumbFileName=generateThumbnailFile(autoCloseDocumentInputStream,thumbnailFileName);	
						previewFileGenerateResultVO.setPreviewFileLocation(getApplicationContext()+previewFileDownloadURL);		
						previewFileGenerateResultVO.setPreviewFileName(finalThumbFileName);							
						previewFileGenerateResultVO.setGenerateResult(true);
					}						
				}else{
					previewFileGenerateResultVO.setGenerateResult(false);
				}				
			}else{
				previewFileGenerateResultVO.setGenerateResult(false);				
			}		
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return previewFileGenerateResultVO;		
	}	
	
	@POST
	@Path("/addFileTag/")
	@Produces("application/json")	
	public String[] addFileTag(DocumentTagOperationVO documentTagOperationVO){	
		String activitySpace=documentTagOperationVO.getActivitySpaceName();		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_participant)){
				ParticipantFileVO participantFileVO=documentTagOperationVO.getParticipantFileInfo();				
				String parentFolderPath=participantFileVO.getParentFolderPath();
				String fileName=participantFileVO.getFileName();					
				String participantName=participantFileVO.getParticipantName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.addContentTags(tagValueArray);
					}						
				}else{
					return null;
				}				
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_activity)){
				ActivityTypeFileVO activityTypeFileVO=documentTagOperationVO.getActivityTypeFileInfo();
				String parentFolderPath=activityTypeFileVO.getParentFolderPath();
				String fileName=activityTypeFileVO.getFileName();
				String activityType=activityTypeFileVO.getActivityName();
				String activityId=activityTypeFileVO.getActivityId();		
				String activityTypeFolderRootAbsPath="/"+activityType+"/";
				String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
				String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.addContentTags(tagValueArray);
					}						
				}else{
					return null;
				}					
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_applicationSpace)){
				ApplicationSpaceFileVO applicationSpaceFileVO=documentTagOperationVO.getApplicationSpaceFileInfo();
				String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
				String fileName=applicationSpaceFileVO.getFileName();				
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.addContentTags(tagValueArray);
					}						
				}else{
					return null;
				}								
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_role)){
				RoleFileVO roleFileVO=documentTagOperationVO.getRoleFileInfo();				
				String parentFolderPath=roleFileVO.getParentFolderPath();
				String fileName=roleFileVO.getFileName();						
				String roleName=roleFileVO.getRoleName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.addContentTags(tagValueArray);
					}						
				}else{
					return null;
				}				
			}else{
				return null;				
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
	
	@DELETE
	@Path("/removeFileTag/")
	@Produces("application/json")	
	public String[] removeFileTag(DocumentTagOperationVO documentTagOperationVO){	
		String activitySpace=documentTagOperationVO.getActivitySpaceName();		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_participant)){
				ParticipantFileVO participantFileVO=documentTagOperationVO.getParticipantFileInfo();				
				String parentFolderPath=participantFileVO.getParentFolderPath();
				String fileName=participantFileVO.getFileName();					
				String participantName=participantFileVO.getParticipantName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.removeContentTags(tagValueArray);
					}						
				}else{
					return null;
				}				
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_activity)){
				ActivityTypeFileVO activityTypeFileVO=documentTagOperationVO.getActivityTypeFileInfo();
				String parentFolderPath=activityTypeFileVO.getParentFolderPath();
				String fileName=activityTypeFileVO.getFileName();
				String activityType=activityTypeFileVO.getActivityName();
				String activityId=activityTypeFileVO.getActivityId();		
				String activityTypeFolderRootAbsPath="/"+activityType+"/";
				String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
				String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.removeContentTags(tagValueArray);
					}						
				}else{
					return null;
				}					
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_applicationSpace)){
				ApplicationSpaceFileVO applicationSpaceFileVO=documentTagOperationVO.getApplicationSpaceFileInfo();
				String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
				String fileName=applicationSpaceFileVO.getFileName();				
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.removeContentTags(tagValueArray);
					}						
				}else{
					return null;
				}								
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_role)){
				RoleFileVO roleFileVO=documentTagOperationVO.getRoleFileInfo();				
				String parentFolderPath=roleFileVO.getParentFolderPath();
				String fileName=roleFileVO.getFileName();						
				String roleName=roleFileVO.getRoleName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);
						String tagValue=documentTagOperationVO.getTagValue();
						String[] tagValueArray=new String[]{tagValue};
						return documentContent.removeContentTags(tagValueArray);
					}						
				}else{
					return null;
				}				
			}else{
				return null;				
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
	@Path("/resetFileTags/")
	@Produces("application/json")	
	public String[] resetFileTags(DocumentTagOperationVO documentTagOperationVO){	
		String activitySpace=documentTagOperationVO.getActivitySpaceName();		
		ContentSpace activityContentSpace = null;		
		ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_participant)){
				ParticipantFileVO participantFileVO=documentTagOperationVO.getParticipantFileInfo();				
				String parentFolderPath=participantFileVO.getParentFolderPath();
				String fileName=participantFileVO.getFileName();					
				String participantName=participantFileVO.getParticipantName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						documentContent.removeContentTags(documentContent.getContentTags());
						String[] newTagValues=documentTagOperationVO.getTagValues();
						return documentContent.addContentTags(newTagValues);
					}						
				}else{
					return null;
				}				
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_activity)){
				ActivityTypeFileVO activityTypeFileVO=documentTagOperationVO.getActivityTypeFileInfo();
				String parentFolderPath=activityTypeFileVO.getParentFolderPath();
				String fileName=activityTypeFileVO.getFileName();
				String activityType=activityTypeFileVO.getActivityName();
				String activityId=activityTypeFileVO.getActivityId();		
				String activityTypeFolderRootAbsPath="/"+activityType+"/";
				String activityInstanceFolderRootPath=activityTypeFolderRootAbsPath+activityId+"/"+ActivityInstance_attachment;				
				String currentFolderFullPath=activityInstanceFolderRootPath+parentFolderPath;			
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						documentContent.removeContentTags(documentContent.getContentTags());
						String[] newTagValues=documentTagOperationVO.getTagValues();
						return documentContent.addContentTags(newTagValues);
					}						
				}else{
					return null;
				}					
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_applicationSpace)){
				ApplicationSpaceFileVO applicationSpaceFileVO=documentTagOperationVO.getApplicationSpaceFileInfo();
				String parentFolderPath=applicationSpaceFileVO.getParentFolderPath();
				String fileName=applicationSpaceFileVO.getFileName();				
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore+parentFolderPath;
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						documentContent.removeContentTags(documentContent.getContentTags());
						String[] newTagValues=documentTagOperationVO.getTagValues();
						return documentContent.addContentTags(newTagValues);
					}						
				}else{
					return null;
				}								
			}else if(documentTagOperationVO.getDocumentsOwnerType().equals(DocumentsOwnerType_role)){
				RoleFileVO roleFileVO=documentTagOperationVO.getRoleFileInfo();				
				String parentFolderPath=roleFileVO.getParentFolderPath();
				String fileName=roleFileVO.getFileName();						
				String roleName=roleFileVO.getRoleName();						
				String currentFolderFullPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName+parentFolderPath;				
				BaseContentObject currentFolderContentObject=activityContentSpace.getContentObjectByAbsPath(currentFolderFullPath);				
				if(currentFolderContentObject!=null){					
					BaseContentObject fileContentObject=currentFolderContentObject.getSubContentObject(fileName);
					if(fileContentObject==null){
						return null;
					}else{
						BinaryContent documentContent=coh.getBinaryContent(currentFolderContentObject, fileName);	
						documentContent.removeContentTags(documentContent.getContentTags());
						String[] newTagValues=documentTagOperationVO.getTagValues();
						return documentContent.addContentTags(newTagValues);
					}						
				}else{
					return null;
				}				
			}else{
				return null;				
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
	
	@DELETE
	@Path("/deletePerviewFile/{fileName}")
	@Produces("application/json")	
	public void deletePerviewFile(@PathParam("fileName")String fileName){
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PREVIEWFILE_TEMP/";
		String previewFilePath=tempFileRootPath+fileName;		
		File tempFileToDelete=new File(previewFilePath);		
		if(tempFileToDelete.exists()){
			tempFileToDelete.delete();
		}		
	}
	
	@POST
    @Path("/queryDocuments/")
	@Produces("application/json")
	public List<DocumentContentVO> queryDocuments(FileListQueryVO fileListQueryVO){
		List<DocumentContentVO> queryResultDocumentList=new ArrayList<DocumentContentVO>();	
		String activitySpace=fileListQueryVO.getActivitySpaceName();
		String queryBaseFolderPath="";		
		String queryContent=fileListQueryVO.getQueryContent();
		String documentsOwnerType=fileListQueryVO.getDocumentsOwnerType();
		if(documentsOwnerType.equals(DocumentsOwnerType_applicationSpace)){
			queryBaseFolderPath="/"+ActivitySpace_ContentStore+"/"+Space_ContentStore;			
		}
		if(documentsOwnerType.equals(DocumentsOwnerType_participant)){
			String participantName=fileListQueryVO.getParticipantName();			
			queryBaseFolderPath="/"+ActivitySpace_ContentStore+"/"+Participant_ContentStore+"/"+participantName;			
		}
		if(documentsOwnerType.equals(DocumentsOwnerType_role)){
			String roleName=fileListQueryVO.getRoleName();				
			queryBaseFolderPath="/"+ActivitySpace_ContentStore+"/"+Role_ContentStore+"/"+roleName;			
		}		
		ContentSpace activityContentSpace = null;	
		ContentQueryHelper cqh=ContentComponentFactory.getContentQueryHelper();
		try {
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpace);			
			BaseContentObject queryBaseContentObject=activityContentSpace.getContentObjectByAbsPath(queryBaseFolderPath);	
			if(fileListQueryVO.getQueryDocumentName()){
				List<BinaryContent> queryByName=cqh.selectBinaryContentsByTitle(queryBaseContentObject, queryContent);
				setDocumentContentList(activitySpace,queryResultDocumentList,queryByName);				
			}
			if(fileListQueryVO.getQueryDocumentContent()){
				List<BinaryContent> queryByContent=cqh.selectBinaryContentsByFullTextSearch(queryBaseContentObject, queryContent);	
				setDocumentContentList(activitySpace,queryResultDocumentList,queryByContent);
			}			
			if(fileListQueryVO.getQueryDocumentTag()){
				PropertyQueryHelper propertyQueryHelper=ContentComponentFactory.getPropertyQueryHelper(); 				
				String selectStr="contentObjects.[vfcr:contentTags] LIKE '%"+queryContent+"%'";	
				List<BaseContentObject> baseContentObjectQueryByTag=propertyQueryHelper.selectContentObjectsBySQL2(queryBaseContentObject, "contentObjects",selectStr, "vfcr:binary",PropertyQueryHelper.SCOPE_Descendant);				
				ContentOperationHelper coh = ContentComponentFactory.getContentOperationHelper();				
				List<BinaryContent> queryByTag=new ArrayList<BinaryContent>();
				for(BaseContentObject documentContentObject:baseContentObjectQueryByTag){
					BaseContentObject binaryContentObject=documentContentObject.getParentContentObject();				
					BinaryContent currentBinaryContent=coh.getBinaryContent(binaryContentObject, documentContentObject.getContentObjectName());	
					queryByTag.add(currentBinaryContent);
				}		
				setDocumentContentList(activitySpace,queryResultDocumentList,queryByTag);
			}			
		} catch (ContentReposityException e) {				
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return queryResultDocumentList;
	}
	
	private static String generateDownloadTempFile(InputStream fileInputStream,String tempFileName){		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/DOWNLOADFILE_TEMP/";
		String downloadTempFilePath=tempFileRootPath+tempFileName;	
		File currentFile=new File(downloadTempFilePath);
		currentFile.delete();
		OutputStream os;		
		try {				
			os = new FileOutputStream(downloadTempFilePath);
			byte[] buffer = new byte[4096];  
			int bytesRead;  
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {  
			  os.write(buffer, 0, bytesRead);  
			}  
			fileInputStream.close();  
			os.close(); 					
			return downloadTempFilePath;
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return null;
	}
	
	private static String generatePreviewFile(InputStream fileInputStream,String tempFileName,boolean needConvert,String convertOperation){		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PREVIEWFILE_TEMP/";
		String previewFilePath=tempFileRootPath+tempFileName;		  
		OutputStream os;		
		try {				
			if(needConvert){
				if(convertOperation!=null&&convertOperation.equals(document_convertOperation_MSOFFICE_To_PDF)){						
					 os = new FileOutputStream(previewFilePath+"_MIDDLE");
				}else if(convertOperation!=null&&convertOperation.equals(document_convertOperation_AddPOSTFIX_ODT)){
					os = new FileOutputStream(previewFilePath+".odt");
				}else if(convertOperation!=null&&convertOperation.equals(document_convertOperation_AddPOSTFIX_ODS)){
					os = new FileOutputStream(previewFilePath+".ods");
				}else if(convertOperation!=null&&convertOperation.equals(document_convertOperation_AddPOSTFIX_ODP)){
					os = new FileOutputStream(previewFilePath+".odp");
				}				
				else{
					os = new FileOutputStream(previewFilePath);
				}
			}else{
				os = new FileOutputStream(previewFilePath);
			}										
			byte[] buffer = new byte[4096];  
			int bytesRead;  
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {  
			  os.write(buffer, 0, bytesRead);  
			}  
			fileInputStream.close();  
			os.close(); 					
			if(needConvert){
				if(convertOperation!=null&&convertOperation.equals(document_convertOperation_MSOFFICE_To_PDF)){
					//convert MS office document to PDF format					
					OfficeDocumentConverter converter = new OfficeDocumentConverter(ServiceResourceHolder.getOfficeManager());					
					File middleFile=new File(previewFilePath+"_MIDDLE");
					File pdfFormatFile=new File(previewFilePath+".pdf");
					File previewFile =new File(previewFilePath);					
					converter.convert(middleFile, pdfFormatFile);					
					middleFile.delete();
					pdfFormatFile.renameTo(previewFile);
				}
			}				
			return previewFilePath;
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return null;
	}	
	
	private static String generateThumbnailFile(InputStream fileInputStream,String tempFileName){		
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PREVIEWFILE_TEMP/";
		String previewFilePath=tempFileRootPath+tempFileName;	
		File exisingThumbnailFile= new File(previewFilePath+".JPEG");
		if(exisingThumbnailFile.exists()){
			exisingThumbnailFile.delete();
		}				
		try {
			Thumbnails.of(fileInputStream).size(128, 128).keepAspectRatio(false).outputFormat("jpeg").toFile(exisingThumbnailFile);				
			return tempFileName+".JPEG";
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return null;
	}
	
	private static List<DocumentContentVO> buildDocumentContentList(BaseContentObject targetFolderContentObject ,String currentFolderAbsPath,String activitySpaceName)throws ContentReposityException{			
		List<DocumentContentVO> childContentList=new ArrayList<DocumentContentVO>();			
		List<BaseContentObject> subContentObjList=targetFolderContentObject.getSubContentObjects(null);			
		ContentOperationHelper coh=ContentComponentFactory.getContentOperationHelper();			
		SecurityOperationHelper soh=ContentComponentFactory.getSecurityOperationHelper();
		String cType;			
		for(BaseContentObject childContentObject:subContentObjList){				
			DocumentContentVO currentDocumentContentVO;
			cType = coh.getContentObjectType(childContentObject);	
			if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
				//folder object		
				currentDocumentContentVO=new DocumentContentVO();
				currentDocumentContentVO.setFolder(true);
				currentDocumentContentVO.setChildDocumentNumber(childContentObject.getSubContentObjectsCount());					
				currentDocumentContentVO.setDocumentFolderPath(currentFolderAbsPath);
				currentDocumentContentVO.setDocumentName(childContentObject.getContentObjectName());
				currentDocumentContentVO.setVersion(childContentObject.getCurrentVersion().getCurrentVersionNumber());	
				//currentDocumentContentVO.setLinked(childContentObject.isLinkContentObject());
				currentDocumentContentVO.setLinked(false);
				currentDocumentContentVO.setLocked(childContentObject.isLocked());	
				currentDocumentContentVO.setLockedBy(childContentObject.getLocker());						
				if(childContentObject.getProperty("vfcr:creator")!=null){
					List<String> userList=new ArrayList<String>();
					userList.add(childContentObject.getProperty("vfcr:creator").getPropertyValue().toString());
					ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
					participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
					participantDetailInfosQueryVO.setParticipantScope(activitySpaceName);						
					ParticipantDetailInfoVOsList participantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
					List<ParticipantDetailInfoVO> commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();							
					currentDocumentContentVO.setDocumentCreator(commentParticipantsList.get(0));						
				}										
				List<PermissionObject> folderPermissions=soh.getContentPermissions(childContentObject);						
				List<ContentPermissionVO> contentPermissionList=new ArrayList<ContentPermissionVO>();					
				for(PermissionObject permissionObject:folderPermissions){						
					ContentPermissionVO currentContentPermissionVO=new ContentPermissionVO();	
					currentContentPermissionVO.setDisplayContentPermission(permissionObject.getDisplayContentPermission());
					currentContentPermissionVO.setAddContentPermission(permissionObject.getAddContentPermission());
					currentContentPermissionVO.setAddSubFolderPermission(permissionObject.getAddSubFolderPermission());
					currentContentPermissionVO.setDeleteContentPermission(permissionObject.getDeleteContentPermission());
					currentContentPermissionVO.setDeleteSubFolderPermission(permissionObject.getDeleteSubFolderPermission());					
					currentContentPermissionVO.setEditContentPermission(permissionObject.getEditContentPermission());
					currentContentPermissionVO.setConfigPermissionPermission(permissionObject.getConfigPermissionPermission());
					currentContentPermissionVO.setPermissionScope(permissionObject.getPermissionScope());
					currentContentPermissionVO.setPermissionParticipant(permissionObject.getPermissionParticipant());						
					contentPermissionList.add(currentContentPermissionVO);		
				}
				currentDocumentContentVO.setContentPermissions(contentPermissionList);
			}else{
				//file object					
				BinaryContent bco;
				bco = coh.getBinaryContent(targetFolderContentObject,childContentObject.getContentObjectName());				
				currentDocumentContentVO= buildDocumentContentVO(activitySpaceName,currentFolderAbsPath,bco);				
			}			
			childContentList.add(currentDocumentContentVO);
		}			
		return childContentList;			
	}		
	
	private static DocumentContentVO buildDocumentContentVO(String activitySpaceName,String parentFolderAbsPath,BinaryContent bco) throws ContentReposityException{	
		DocumentContentVO currentDocumentContentVO=new DocumentContentVO();		
		currentDocumentContentVO.setFolder(false);	
		if(parentFolderAbsPath!=null){
			currentDocumentContentVO.setDocumentFolderPath(parentFolderAbsPath);
		}else{
			String contentPath=bco.getContentSpaceAbsPath();			
			String documentFolderPath="";
			if(contentPath.startsWith("/ActivitySpace_ContentStore/Space_ContentStore/")){				
				documentFolderPath=contentPath.replaceFirst("/ActivitySpace_ContentStore/Space_ContentStore", "");				
			}
			if(contentPath.startsWith("/ActivitySpace_ContentStore/Participant_ContentStore/")){				
				documentFolderPath=contentPath.replaceFirst("/ActivitySpace_ContentStore/Participant_ContentStore/", "");
				int firstFolderInx=documentFolderPath.indexOf("/");
				documentFolderPath=documentFolderPath.substring(firstFolderInx,documentFolderPath.length());				
			}
			if(contentPath.startsWith("/ActivitySpace_ContentStore/Role_ContentStore/")){				
				documentFolderPath=contentPath.replaceFirst("/ActivitySpace_ContentStore/Role_ContentStore/", "");	
				int firstFolderInx=documentFolderPath.indexOf("/");
				documentFolderPath=documentFolderPath.substring(firstFolderInx,documentFolderPath.length());
			}			
			int lastfolderDivInx=documentFolderPath.lastIndexOf("/");			
			documentFolderPath=documentFolderPath.substring(0,lastfolderDivInx);
			if(documentFolderPath.equals("")){
				currentDocumentContentVO.setDocumentFolderPath("/");	
			}else{
				currentDocumentContentVO.setDocumentFolderPath(documentFolderPath);	
			}					
		}		
		currentDocumentContentVO.setDocumentLastUpdateDate(bco.getLastModified().getTimeInMillis());					
		currentDocumentContentVO.setDocumentName(bco.getContentName());
		currentDocumentContentVO.setDocumentSize(bco.getContentSize());
		currentDocumentContentVO.setDocumentType(bco.getMimeType());
		currentDocumentContentVO.setVersion(bco.getCurrentVersion());
		//currentDocumentContentVO.setLinked(bco.isLinkObject());
		currentDocumentContentVO.setLinked(false);
		currentDocumentContentVO.setLocked(bco.isLocked());	
		currentDocumentContentVO.setLockedBy(bco.getLocker());
		currentDocumentContentVO.setDocumentTags(bco.getContentTags());					
		if(bco.getCreated()!=null){
			currentDocumentContentVO.setDocumentCreateDate(bco.getCreated().getTimeInMillis());
		}	
		List<String> userList=new ArrayList<String>();
		if(bco.getCreatedBy()!=null){												
			userList.add(bco.getCreatedBy());
		}
		if(bco.getLastModifiedBy()!=null){						
			userList.add(bco.getLastModifiedBy());
		}
		
		if(bco.getLocker()!=null){						
			userList.add(bco.getLocker());	
		}						
		if(userList.size()>0){
			ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
			participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
			participantDetailInfosQueryVO.setParticipantScope(activitySpaceName);						
			ParticipantDetailInfoVOsList participantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
			List<ParticipantDetailInfoVO> commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();						
			if(commentParticipantsList!=null&&commentParticipantsList.size()>0){
				if(commentParticipantsList.size()>=1){
					currentDocumentContentVO.setDocumentCreator(commentParticipantsList.get(0));
				}
				if(commentParticipantsList.size()>=2){
					currentDocumentContentVO.setDocumentLastUpdatePerson(commentParticipantsList.get(1));
				}							
				if(commentParticipantsList.size()>=3){
					currentDocumentContentVO.setDocumentLocker(commentParticipantsList.get(2));
				}							
			}
		}			
		return currentDocumentContentVO;
	}	
	
	private static void setDocumentContentList(String activitySpaceName,List<DocumentContentVO> documentContentList,List<BinaryContent> binaryContentList) throws ContentReposityException{
		for(BinaryContent binaryContent:binaryContentList){			
			DocumentContentVO documentContentVO=buildDocumentContentVO(activitySpaceName,null,binaryContent);
			String newDocumentFullpath=documentContentVO.getDocumentFolderPath()+documentContentVO.getDocumentName();
			boolean isExistDocument=false;
			for(DocumentContentVO currentDocumentContentVO:documentContentList){				
				String currentDocumentDocumentFullpath=currentDocumentContentVO.getDocumentFolderPath()+currentDocumentContentVO.getDocumentName();
				if(currentDocumentDocumentFullpath.equals(newDocumentFullpath)){
					isExistDocument=true;
					break;
				}
			}
			if(!isExistDocument){
				documentContentList.add(documentContentVO);
			}			
		}	
	}
	
	private String getApplicationContext(){
		if(applicationContext==null){
			Message message = PhaseInterceptorChain.getCurrentMessage();			
			HttpServletRequest httprequest = (HttpServletRequest)message.get(AbstractHTTPDestination.HTTP_REQUEST);
			applicationContext=httprequest.getContextPath();			
		}
		return applicationContext;
	}
	
	/*
	private static ContentFolderVO buildContentFolder(String activitySpaceName,String parentFolderAbsPath,String currentFolderName){
		ContentSpace activityContentSpace = null;			
		try {			
			ContentFolderVO contentFolderVO=new ContentFolderVO();			
			String currentFolderAbsPath=null;
			if(parentFolderAbsPath.endsWith("/")){
				currentFolderAbsPath=parentFolderAbsPath+currentFolderName;				
			}else{
				currentFolderAbsPath=parentFolderAbsPath+"/"+currentFolderName;
			}			
			contentFolderVO.setFolderName(currentFolderName);
			contentFolderVO.setFolderPath(parentFolderAbsPath);
			contentFolderVO.setParentFolderPath(currentFolderAbsPath);			
			List<DocumentContentVO> childContentList=new ArrayList<DocumentContentVO>();			
			contentFolderVO.setChildContentList(childContentList);	
			
			activityContentSpace=ContentComponentFactory.connectContentSpace(activitySpaceName);				
			BaseContentObject targetFolderContentObject=activityContentSpace.getContentObjectByAbsPath(parentFolderAbsPath);	
			List<BaseContentObject> subContentObjList=targetFolderContentObject.getSubContentObjects(null);
			
			ContentOperationHelper coh=ContentComponentFactory.getContentOperationHelper();			
			String cType;			
			for(BaseContentObject childContentObject:subContentObjList){
				DocumentContentVO currentDocumentContentVO=new DocumentContentVO();
				cType = coh.getContentObjectType(childContentObject);	
				if (cType.equals(ContentOperationHelper.CONTENTTYPE_FOLDEROBJECT)||cType.equals(ContentOperationHelper.CONTENTTYPE_STANDALONEOBJECT)){
					//folder object						
					currentDocumentContentVO.setFolder(true);
					currentDocumentContentVO.setChildDocumentNumber(childContentObject.getSubContentObjectsCount());
					
					currentDocumentContentVO.setDocumentFolderPath(currentFolderAbsPath);
					currentDocumentContentVO.setDocumentName(childContentObject.getContentObjectName());
					currentDocumentContentVO.setVersion(childContentObject.getCurrentVersion().getCurrentVersionNumber());					
				}else{
					//file object							
					currentDocumentContentVO.setFolder(false);
					BinaryContent bco;
					bco = coh.getBinaryContent(targetFolderContentObject,childContentObject.getContentObjectName());
					
					currentDocumentContentVO.setDocumentFolderPath(currentFolderAbsPath);
					currentDocumentContentVO.setDocumentLastUpdateDate(bco.getLastModified().getTimeInMillis());					
					currentDocumentContentVO.setDocumentName(bco.getContentName());
					currentDocumentContentVO.setDocumentSize(bco.getContentSize());
					currentDocumentContentVO.setDocumentType(bco.getMimeType());
					currentDocumentContentVO.setVersion(bco.getCurrentVersion());
				}			
				childContentList.add(currentDocumentContentVO);
			}			
			return contentFolderVO;		
		} catch (ContentReposityException e) {			
			e.printStackTrace();
		}finally{
			if(activityContentSpace!=null){
				activityContentSpace.closeContentSpace();
			}				
		}			
		return null;
	}	
	*/
}
