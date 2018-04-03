package com.viewfunction.vfmab.restful.activityManagement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.joda.time.DateTime;

import com.viewfunction.activityEngine.activityBureau.ActivitySpace;
import com.viewfunction.activityEngine.activityBureau.BusinessActivity;
import com.viewfunction.activityEngine.activityBureau.BusinessActivityDefinition;
import com.viewfunction.activityEngine.activityView.RoleQueue;
import com.viewfunction.activityEngine.activityView.common.ActivityComment;
import com.viewfunction.activityEngine.activityView.common.ActivityData;
import com.viewfunction.activityEngine.activityView.common.ActivityStep;
import com.viewfunction.activityEngine.activityView.common.CustomStructure;
import com.viewfunction.activityEngine.activityView.common.DataFieldDefinition;
import com.viewfunction.activityEngine.activityView.common.ParticipantTask;
import com.viewfunction.activityEngine.exception.ActivityEngineActivityException;
import com.viewfunction.activityEngine.exception.ActivityEngineDataException;
import com.viewfunction.activityEngine.exception.ActivityEngineProcessException;
import com.viewfunction.activityEngine.exception.ActivityEngineRuntimeException;
import com.viewfunction.activityEngine.helper.BatchOperationHelper;
import com.viewfunction.activityEngine.security.Participant;
import com.viewfunction.activityEngine.security.Role;
import com.viewfunction.activityEngine.util.factory.ActivityComponentFactory;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVOsList;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfosQueryVO;
import com.viewfunction.participantManagement.operation.restfulClient.ParticipantOperationServiceRESTClient;
import com.viewfunction.processRepository.exception.ProcessRepositoryRuntimeException;
import com.viewfunction.processRepository.processBureau.HistoricProcessStep;
import com.viewfunction.processRepository.processBureau.ProcessSpace;
import com.viewfunction.vfmab.restful.userManagement.CustomStructureVO;
import com.viewfunction.vfmab.restful.util.CommonOperationUtil;
import org.springframework.stereotype.Service;

@Service
@Path("/activityManagementService")  
@Produces("application/json")
public class ActivityManagementService {	
	
	private static final String DATAFIELD_TYPE_BOOLEAN="BOOLEAN";
	private static final String DATAFIELD_TYPE_DOUBLE="DOUBLE";
	private static final String DATAFIELD_TYPE_LONG="LONG";
	private static final String DATAFIELD_TYPE_DECIMAL="DECIMAL";
	private static final String DATAFIELD_TYPE_BINARY="BINARY";
	private static final String DATAFIELD_TYPE_DATE="DATE";
	private static final String DATAFIELD_TYPE_STRING="STRING";	
	
	private static final String TASK_DUESTATUS_NODEU="NODEU";
	private static final String TASK_DUESTATUS_OVERDUE="OVERDUE";
	private static final String TASK_DUESTATUS_DUETODAY="DUETODAY";
	private static final String TASK_DUESTATUS_DUETHISWEEK="DUETHISWEEK";
	
	private static final String ACTIVITYCOMMENT_TYPE_ACTIVITY="COMMENT_TYPE_ACTIVITY";
	private static final String ACTIVITYCOMMENT_TYPE_TASK="COMMENT_TYPE_TASK";
	
	private static final String INVOLVEACTION_LAUNCHACTIVITY ="LAUNCH_ACTIVITY";
	
	@GET
    @Path("/participantTasksInfo/{applicationSpaceName}/{participantName}")
	@Produces("application/json")
	public ParticipantTaskVOList getUsersActivitiesInfo(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName){
		ParticipantTaskVOList participantTaskVOList=new ParticipantTaskVOList();		
		List<ParticipantTaskVO> participantTaskVOsList=new ArrayList<ParticipantTaskVO>();
		participantTaskVOList.setParticipantTasksVOList(participantTaskVOsList);		
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			Participant userParticipant=activitySpace.getParticipant(participantName);				
			List<ParticipantTask> participantTasksList=userParticipant.fetchParticipantTasks();			
			for(ParticipantTask participantTask:participantTasksList){				
				ParticipantTaskVO currentParticipantTaskVO=new ParticipantTaskVO();
				participantTaskVOsList.add(currentParticipantTaskVO);				
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();
				ActivityStep currentActivityStep=participantTask.getActivityStep();				
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());
				currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
				currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
				currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());				
				if(currentActivityStep.getFinishTime()!=null){
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}				
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());					
					currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));
				}else{
					currentActivityStepVO.setDueStatus(TASK_DUESTATUS_NODEU);
				};				
				
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());				
				String[] stepResponse=currentActivityStep.getBusinessActivity().getActivityDefinition().getStepDecisionPointChoiseList(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setStepResponse(stepResponse);
				
				Role relatedRole=currentActivityStep.getRelatedRole();
				if(relatedRole!=null){
					RoleVO currentRoleVO=new RoleVO();					
					currentActivityStepVO.setRelatedRole(currentRoleVO);
					currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					currentRoleVO.setDescription(relatedRole.getDescription());
					currentRoleVO.setDisplayName(relatedRole.getDisplayName());
					currentRoleVO.setRoleName(relatedRole.getRoleName());						
				}							
				
				currentParticipantTaskVO.setActivityStep(currentActivityStepVO);
				currentParticipantTaskVO.setActivityStepName(participantTask.getActivityStepName());
				currentParticipantTaskVO.setActivityType(participantTask.getActivityType());
				currentParticipantTaskVO.setCreateTime(participantTask.getCreateTime().getTime());
				if(participantTask.getDueDate()!=null){
					currentParticipantTaskVO.setDueDate(participantTask.getDueDate().getTime());
					currentParticipantTaskVO.setDueStatus(getTaskDueStatus(participantTask.getDueDate().getTime()));
				}else{
					currentParticipantTaskVO.setDueStatus(TASK_DUESTATUS_NODEU);
				}				
				
				currentParticipantTaskVO.setRoleName(participantTask.getRoleName());
				currentParticipantTaskVO.setStepAssignee(participantTask.getStepAssignee());
				currentParticipantTaskVO.setStepDescription(participantTask.getStepDescription());
				currentParticipantTaskVO.setStepOwner(participantTask.getStepOwner());				
			}			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		}	catch ( ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		return participantTaskVOList;
	}	
	
	@GET
    @Path("/participantTasksDetailInfo/{applicationSpaceName}/{participantName}")
	@Produces("application/json")
	public ParticipantTaskVOList getUsersActivitiesDetailInfo(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName){
		ParticipantTaskVOList participantTaskVOList=new ParticipantTaskVOList();		
		List<ParticipantTaskVO> participantTaskVOsList=new ArrayList<ParticipantTaskVO>();
		participantTaskVOList.setParticipantTasksVOList(participantTaskVOsList);		
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			Participant userParticipant=activitySpace.getParticipant(participantName);	
			List<ParticipantTask> participantTasksList=userParticipant.fetchParticipantTasks();			
			HashMap<String, Role> activityStepRelatedRoleMap=new HashMap<String,Role>();
			HashMap<String,Map<String,String>> activitySpaceStepProcessEditorsMappingMap=new HashMap<String,Map<String,String>>();
			for(ParticipantTask participantTask:participantTasksList){				
				ParticipantTaskVO currentParticipantTaskVO=new ParticipantTaskVO();
				participantTaskVOsList.add(currentParticipantTaskVO);				
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();
				ActivityStep currentActivityStep=participantTask.getActivityStep();	
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());
				currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
				currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
				currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());				
				if(currentActivityStep.hasParentActivityStep()){
					ActivityStep parentActivityStep=currentActivityStep.getParentActivityStep();
					if(parentActivityStep!=null){
						currentActivityStepVO.setParentActivityStepName(parentActivityStep.getActivityStepName());						
					}									
				}				
				if(currentActivityStep.getFinishTime()!=null){					
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}	
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
					currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));
				}else{
					currentActivityStepVO.setDueStatus(TASK_DUESTATUS_NODEU);
				};
				
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());	
				
				BusinessActivityDefinition targetActivityDefinition=currentActivityStep.getBusinessActivity().getActivityDefinition();					
				String[] stepResponse=targetActivityDefinition.getStepDecisionPointChoiseList(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setStepResponse(stepResponse);				
				
				//use this method for better performance
				DataFieldDefinition[] currentStepDataFieldDefinitionArray=targetActivityDefinition.getActivityStepsExposedDataField().get(currentActivityStep.getActivityStepDefinitionKey());
				ActivityData[] stepDataArray=currentActivityStep.getBusinessActivity().getActivityData(currentStepDataFieldDefinitionArray);
				ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
				List<ActivityDataFieldValueVO> activityDataFieldValueList=new ArrayList<ActivityDataFieldValueVO>();
				activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueList);
				currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);				
				if(stepDataArray!=null){					
					for(ActivityData currentActivityData:stepDataArray){					
						ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(currentActivityData);						
						activityDataFieldValueList.add(activityDataFieldValueVO);					
					}	
				}
				
				Role relatedRole=activityStepRelatedRoleMap.get(currentActivityStep.getActivityStepDefinitionKey());
				if(relatedRole==null){
					 relatedRole=currentActivityStep.getRelatedRole();
					 if(relatedRole!=null){
						 activityStepRelatedRoleMap.put(currentActivityStep.getActivityStepDefinitionKey(), relatedRole);
					 }
				}
				if(relatedRole!=null){
					RoleVO currentRoleVO=new RoleVO();					
					currentActivityStepVO.setRelatedRole(currentRoleVO);
					currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					currentRoleVO.setDescription(relatedRole.getDescription());
					currentRoleVO.setDisplayName(relatedRole.getDisplayName());
					currentRoleVO.setRoleName(relatedRole.getRoleName());						
				}
				
				Map<String,String> stepProcessEditorsMap=activitySpaceStepProcessEditorsMappingMap.get(currentActivityStep.getActivityType());
				if(stepProcessEditorsMap==null){
					stepProcessEditorsMap=activitySpace.getBusinessActivityDefinitionStepProcessEditorsInfo(currentActivityStep.getActivityType());
					if(stepProcessEditorsMap!=null){
						activitySpaceStepProcessEditorsMappingMap.put(currentActivityStep.getActivityType(), stepProcessEditorsMap);
					}				
				}					
				if(stepProcessEditorsMap!=null){
					String currentStepProcessEditor=stepProcessEditorsMap.get(currentActivityStep.getActivityStepDefinitionKey());
					if(currentStepProcessEditor!=null){							
						currentActivityStepVO.setStepProcessEditor(currentStepProcessEditor);							
					}
				}		
				
				currentParticipantTaskVO.setActivityStep(currentActivityStepVO);
				currentParticipantTaskVO.setActivityStepName(participantTask.getActivityStepName());
				currentParticipantTaskVO.setActivityType(participantTask.getActivityType());
				currentParticipantTaskVO.setCreateTime(participantTask.getCreateTime().getTime());
				if(participantTask.getDueDate()!=null){
					currentParticipantTaskVO.setDueDate(participantTask.getDueDate().getTime());
					currentParticipantTaskVO.setDueStatus(getTaskDueStatus(participantTask.getDueDate().getTime()));
				}else{
					currentParticipantTaskVO.setDueStatus(TASK_DUESTATUS_NODEU);
				}
				
				//currentParticipantTaskVO.setRoleName(participantTask.getRoleName());
				//for better performance
				if(relatedRole!=null){
					currentParticipantTaskVO.setRoleName(relatedRole.getRoleName());
				}
				currentParticipantTaskVO.setStepAssignee(participantTask.getStepAssignee());
				currentParticipantTaskVO.setStepDescription(participantTask.getStepDescription());
				currentParticipantTaskVO.setStepOwner(participantTask.getStepOwner());	
			}			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		}	catch ( ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}
		return participantTaskVOList;
	}	
	
	@GET
    @Path("/participantTaskDetailInfo/{applicationSpaceName}/{participantName}/{activityId}/{activityStepName}")
	@Produces("application/json")
	public ParticipantTaskVO getUsersActivityDetailInfoByActivityInfo(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName,
			@PathParam("activityId")String activityId,@PathParam("activityStepName")String activityStepName){
		ParticipantTaskVO currentParticipantTaskVO=new ParticipantTaskVO();
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			Participant userParticipant=activitySpace.getParticipant(participantName);				
			List<ParticipantTask> participantTasksList=userParticipant.fetchParticipantTasks();			
			for(ParticipantTask participantTask:participantTasksList){				
				String currentActivityId=participantTask.getActivityStep().getActivityId();
				String currentActivityStepName=participantTask.getActivityStepName();
				if(currentActivityId.equals(activityId)&&currentActivityStepName.equals(activityStepName)){
					ActivityStepVO currentActivityStepVO=new ActivityStepVO();
					ActivityStep currentActivityStep=participantTask.getActivityStep();	
					currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
					currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
					currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
					currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
					currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());
					currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
					currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
					currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
					currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
					currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());
					if(currentActivityStep.hasParentActivityStep()){
						ActivityStep parentActivityStep=currentActivityStep.getParentActivityStep();
						if(parentActivityStep!=null){
							currentActivityStepVO.setParentActivityStepName(parentActivityStep.getActivityStepName());						
						}									
					}				
					if(currentActivityStep.getFinishTime()!=null){					
						currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
					}else{
						currentActivityStepVO.setFinishTime(0);
					}	
					if(currentActivityStep.getDueDate()!=null){
						currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
						currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));
					}else{
						currentActivityStepVO.setDueStatus(TASK_DUESTATUS_NODEU);
					};
					
					currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());
					currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());
					currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());					
					String[] stepResponse=currentActivityStep.getBusinessActivity().getActivityDefinition().getStepDecisionPointChoiseList(currentActivityStep.getActivityStepDefinitionKey());
					currentActivityStepVO.setStepResponse(stepResponse);
					
					ActivityData[] stepDataArray=currentActivityStep.getActivityStepData();	
					ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
					List<ActivityDataFieldValueVO> activityDataFieldValueList=new ArrayList<ActivityDataFieldValueVO>();
					activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueList);
					currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);				
					if(stepDataArray!=null){					
						for(ActivityData currentActivityData:stepDataArray){					
							ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(currentActivityData);						
							activityDataFieldValueList.add(activityDataFieldValueVO);					
						}	
					}
					
					Role relatedRole=currentActivityStep.getRelatedRole();
					if(relatedRole!=null){
						RoleVO currentRoleVO=new RoleVO();					
						currentActivityStepVO.setRelatedRole(currentRoleVO);
						currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
						currentRoleVO.setDescription(relatedRole.getDescription());
						currentRoleVO.setDisplayName(relatedRole.getDisplayName());
						currentRoleVO.setRoleName(relatedRole.getRoleName());						
					}
					
					Map<String,String> stepProcessEditorsMap=activitySpace.getBusinessActivityDefinitionStepProcessEditorsInfo(currentActivityStep.getActivityType());					
					if(stepProcessEditorsMap!=null){
						String currentStepProcessEditor=stepProcessEditorsMap.get(currentActivityStep.getActivityStepDefinitionKey());
						if(currentStepProcessEditor!=null){							
							currentActivityStepVO.setStepProcessEditor(currentStepProcessEditor);							
						}
					}
					
					currentParticipantTaskVO.setActivityStep(currentActivityStepVO);
					currentParticipantTaskVO.setActivityStepName(participantTask.getActivityStepName());
					currentParticipantTaskVO.setActivityType(participantTask.getActivityType());
					currentParticipantTaskVO.setCreateTime(participantTask.getCreateTime().getTime());
					if(participantTask.getDueDate()!=null){
						currentParticipantTaskVO.setDueDate(participantTask.getDueDate().getTime());
						currentParticipantTaskVO.setDueStatus(getTaskDueStatus(participantTask.getDueDate().getTime()));
					}else{
						currentParticipantTaskVO.setDueStatus(TASK_DUESTATUS_NODEU);
					}
					currentParticipantTaskVO.setRoleName(participantTask.getRoleName());
					currentParticipantTaskVO.setStepAssignee(participantTask.getStepAssignee());
					currentParticipantTaskVO.setStepDescription(participantTask.getStepDescription());
					currentParticipantTaskVO.setStepOwner(participantTask.getStepOwner());	
					break;
				}
			}			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		}	catch ( ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		return currentParticipantTaskVO;
	}
	
	@POST
    @Path("/returnParticipantTask/")
	@Produces("application/json")
	public ActivityStepOperationResultVO returnParticipantTask(ActivityStepOperationVO activityStepOperationVO){
		ActivityStepOperationResultVO activityStepOperationResultVO=new ActivityStepOperationResultVO();
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			List<ParticipantTask> currentParticipantTaskList=activitySpace.getParticipant(activityStepOperationVO.getCurrentStepOwner()).fetchParticipantTasks();			
			for(ParticipantTask participantTask:currentParticipantTaskList){				
				String activityId=participantTask.getActivityStep().getActivityId();
				String activityType=participantTask.getActivityType();
				String activityStepName=participantTask.getActivityStepName();
				if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
				activityStepName.equals(activityStepOperationVO.getActivityStepName())){					
					boolean returnTaskResult=participantTask.getActivityStep().returnActivityStep();
					activityStepOperationResultVO.setOperationResult(returnTaskResult);					
					return activityStepOperationResultVO;					
				}								
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {		
			e.printStackTrace();
		}
		activityStepOperationResultVO.setOperationResult(false);
		return activityStepOperationResultVO;		
	}	
	
	@POST
    @Path("/createChildTask/")
	@Produces("application/json")
	public ChildActivityStepsInfoVO createChildTask(CreateChildTaskVO createChildTaskVO){			
		ChildActivityStepsInfoVO childActivityStepsInfoVO=new ChildActivityStepsInfoVO();
		childActivityStepsInfoVO.setAllChildStepsFinished(true);		
		List<ActivityStepVO> childActivityStepsList=new ArrayList<ActivityStepVO>();
		childActivityStepsInfoVO.setChildActivitySteps(childActivityStepsList);
		
		String activitySpaceName=createChildTaskVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		List<ParticipantTask> currentParticipantTaskList;
		try {
			currentParticipantTaskList = activitySpace.getParticipant(createChildTaskVO.getCurrentStepOwner()).fetchParticipantTasks();			
			ActivityStep currentactivityStep=null;			
			for(ParticipantTask participantTask:currentParticipantTaskList){				
				String activityId=participantTask.getActivityStep().getActivityId();
				String activityType=participantTask.getActivityType();
				String activityStepName=participantTask.getActivityStepName();
				if(activityId.equals(createChildTaskVO.getActivityId())&&activityType.equals(createChildTaskVO.getActivityType())&&
				activityStepName.equals(createChildTaskVO.getActivityStepName())){							
					currentactivityStep=participantTask.getActivityStep();					
					break;				
				}								
			}
			Date childTaskDueDate=null;
			if(createChildTaskVO.getChildTaskDueDate()!=0){
				childTaskDueDate=new Date(createChildTaskVO.getChildTaskDueDate());
			}			
			currentactivityStep.createChildActivityStep(createChildTaskVO.getChildTaskStepAssignee(), createChildTaskVO.getChildTaskName(), createChildTaskVO.getChildTaskDescription(), childTaskDueDate);			
			boolean isAllChildActivityStepsFinished=currentactivityStep.isAllChildActivityStepsFinished();
			childActivityStepsInfoVO.setAllChildStepsFinished(isAllChildActivityStepsFinished);
			List<ActivityStep> childActivityStepList=currentactivityStep.getChildActivitySteps();				
			
			List<String> stepAssigneeUserList=new ArrayList<String>();				
			for(ActivityStep currentActivityStep:childActivityStepList){				
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();				
				childActivityStepsList.add(currentActivityStepVO);					
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());					
				currentActivityStepVO.setHasChildActivityStep(false);
				currentActivityStepVO.setHasParentActivityStep(true);
				//child step uses parent step's priority value
				currentActivityStepVO.setStepPriority(currentactivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentactivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentactivityStep.isSuspendedActivityStep());
				currentActivityStepVO.setParentActivityStepName(createChildTaskVO.getActivityStepName());
				if(currentActivityStep.getFinishTime()!=null){
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}				
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
					currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));						
				}else{
					currentActivityStepVO.setDueDate(0);
				}				
				if(currentActivityStep.getRelatedRole()!=null){
					RoleVO currentRoleVO=new RoleVO();					
					currentActivityStepVO.setRelatedRole(currentRoleVO);
					Role relatedRole=currentActivityStep.getRelatedRole();
					currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					currentRoleVO.setDescription(relatedRole.getDescription());
					currentRoleVO.setDisplayName(relatedRole.getDisplayName());
					currentRoleVO.setRoleName(relatedRole.getRoleName());	
					currentActivityStepVO.setRelatedRole(currentRoleVO);					
				}					
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());					
				if(currentActivityStep.getStepAssignee()!=null){					
					stepAssigneeUserList.add(currentActivityStep.getStepAssignee());		
				}					
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());				
				// child activitysteps have the same data as the parent activitystep
				/*
				if(currentActivityStep.getActivityStepData()!=null&&currentActivityStep.getActivityStepData().length>0){					
					ActivityData[] currentActivityData=currentActivityStep.getActivityStepData();				
					ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
					List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
					activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
					if(currentActivityData!=null){
						for(ActivityData activityData:currentActivityData){							
							ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
							activityDataFieldValueVOs.add(activityDataFieldValueVO);						
						}
					}			
					currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);
				}	
				*/			
			}			
			//get owner's participant info
			stepAssigneeUserList.add(createChildTaskVO.getCurrentStepOwner());
			
			ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
			stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
			stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
			ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
			List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();
				
			ParticipantDetailInfoVO ownerParticipantsInfo=stepAssigneeParticipantDetailInfoVOList.get(childActivityStepsList.size());	
				
			for(int i=0;i<childActivityStepsList.size();i++){
				ActivityStepVO targetActivityStepVO=childActivityStepsList.get(i);
				targetActivityStepVO.setStepAssigneeParticipant(stepAssigneeParticipantDetailInfoVOList.get(i));
				targetActivityStepVO.setStepOwnerParticipant(ownerParticipantsInfo);						
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}			
		return childActivityStepsInfoVO;		
	}		
	
	@POST
    @Path("/childTasksInfo/")
	@Produces("application/json")
	public ChildActivityStepsInfoVO getChildTasksInfo(ActivityStepOperationVO activityStepOperationVO){			
		ChildActivityStepsInfoVO childActivityStepsInfoVO=new ChildActivityStepsInfoVO();
		childActivityStepsInfoVO.setAllChildStepsFinished(true);		
		List<ActivityStepVO> childActivityStepsList=new ArrayList<ActivityStepVO>();
		childActivityStepsInfoVO.setChildActivitySteps(childActivityStepsList);
		
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		List<ParticipantTask> currentParticipantTaskList;
		try {
			currentParticipantTaskList = activitySpace.getParticipant(activityStepOperationVO.getCurrentStepOwner()).fetchParticipantTasks();			
			ActivityStep currentactivityStep=null;			
			for(ParticipantTask participantTask:currentParticipantTaskList){				
				String activityId=participantTask.getActivityStep().getActivityId();
				String activityType=participantTask.getActivityType();
				String activityStepName=participantTask.getActivityStepName();
				if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
				activityStepName.equals(activityStepOperationVO.getActivityStepName())){							
					currentactivityStep=participantTask.getActivityStep();					
					break;				
				}								
			}				
			boolean isAllChildActivityStepsFinished=currentactivityStep.isAllChildActivityStepsFinished();
			childActivityStepsInfoVO.setAllChildStepsFinished(isAllChildActivityStepsFinished);
			List<ActivityStep> childActivityStepList=currentactivityStep.getChildActivitySteps();				
			
			List<String> stepAssigneeUserList=new ArrayList<String>();				
			for(ActivityStep currentActivityStep:childActivityStepList){				
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();				
				childActivityStepsList.add(currentActivityStepVO);					
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());					
				currentActivityStepVO.setHasChildActivityStep(false);
				currentActivityStepVO.setHasParentActivityStep(true);				
				currentActivityStepVO.setParentActivityStepName(activityStepOperationVO.getActivityStepName());
				//child step uses parent step's priority value
				currentActivityStepVO.setStepPriority(currentactivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentactivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentactivityStep.isSuspendedActivityStep());
				if(currentActivityStep.getFinishTime()!=null){
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}				
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
					currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));						
				}else{
					currentActivityStepVO.setDueDate(0);
				}				
				if(currentActivityStep.getRelatedRole()!=null){
					RoleVO currentRoleVO=new RoleVO();					
					currentActivityStepVO.setRelatedRole(currentRoleVO);
					Role relatedRole=currentActivityStep.getRelatedRole();
					currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					currentRoleVO.setDescription(relatedRole.getDescription());
					currentRoleVO.setDisplayName(relatedRole.getDisplayName());
					currentRoleVO.setRoleName(relatedRole.getRoleName());	
					currentActivityStepVO.setRelatedRole(currentRoleVO);					
				}					
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());					
				if(currentActivityStep.getStepAssignee()!=null){					
					stepAssigneeUserList.add(currentActivityStep.getStepAssignee());		
				}					
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());				
				// child activitysteps have the same data as the parent activitystep
				/*
				if(currentActivityStep.getActivityStepData()!=null&&currentActivityStep.getActivityStepData().length>0){					
					ActivityData[] currentActivityData=currentActivityStep.getActivityStepData();				
					ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
					List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
					activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
					if(currentActivityData!=null){
						for(ActivityData activityData:currentActivityData){							
							ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
							activityDataFieldValueVOs.add(activityDataFieldValueVO);						
						}
					}			
					currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);
				}	
				*/			
			}			
			//get owner's participant info
			stepAssigneeUserList.add(activityStepOperationVO.getCurrentStepOwner());
			
			ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
			stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
			stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
			ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
			List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();
				
			ParticipantDetailInfoVO ownerParticipantsInfo=stepAssigneeParticipantDetailInfoVOList.get(childActivityStepsList.size());	
				
			for(int i=0;i<childActivityStepsList.size();i++){
				ActivityStepVO targetActivityStepVO=childActivityStepsList.get(i);
				targetActivityStepVO.setStepAssigneeParticipant(stepAssigneeParticipantDetailInfoVOList.get(i));
				targetActivityStepVO.setStepOwnerParticipant(ownerParticipantsInfo);						
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}			
		return childActivityStepsInfoVO;		
	}	
	
	@POST
    @Path("/acceptRoleQueueTask/")
	@Produces("application/json")
	public ActivityStepOperationResultVO acceptParticipantTask(ActivityStepOperationVO activityStepOperationVO){
		ActivityStepOperationResultVO activityStepOperationResultVO=new ActivityStepOperationResultVO();
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			String stepRealtedRoleQueue=activityStepOperationVO.getActivityStepRelatedRoleQueue();			
			RoleQueue currentRoleQueue=activitySpace.getRoleQueue(stepRealtedRoleQueue);			
			List<ActivityStep> activityStepList=currentRoleQueue.fetchActivitySteps();
			for(ActivityStep currentActivityStep:activityStepList){
				String activityId=currentActivityStep.getActivityId();
				String activityType=currentActivityStep.getActivityType();
				String activityStepName=currentActivityStep.getActivityStepName();
				if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
						activityStepName.equals(activityStepOperationVO.getActivityStepName())){
					boolean acceptTaskResult=currentActivityStep.handleActivityStep(activityStepOperationVO.getNewStepOwner());
					activityStepOperationResultVO.setOperationResult(acceptTaskResult);					
					return activityStepOperationResultVO;					
				}				
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {		
			e.printStackTrace();
		}
		activityStepOperationResultVO.setOperationResult(false);
		return activityStepOperationResultVO;		
	}	
	
	@POST
    @Path("/reassignParticipantTask/")
	@Produces("application/json")
	public ActivityStepOperationResultVO reassignParticipantTask(ActivityStepOperationVO activityStepOperationVO){
		ActivityStepOperationResultVO activityStepOperationResultVO=new ActivityStepOperationResultVO();
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			List<ParticipantTask> currentParticipantTaskList=activitySpace.getParticipant(activityStepOperationVO.getCurrentStepOwner()).fetchParticipantTasks();			
			for(ParticipantTask participantTask:currentParticipantTaskList){				
				String activityId=participantTask.getActivityStep().getActivityId();
				String activityType=participantTask.getActivityType();
				String activityStepName=participantTask.getActivityStepName();
				if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
				activityStepName.equals(activityStepOperationVO.getActivityStepName())){						
					boolean reassignTaskResult=participantTask.getActivityStep().reassignActivityStep(activityStepOperationVO.getNewStepOwner());			
					activityStepOperationResultVO.setOperationResult(reassignTaskResult);					
					return activityStepOperationResultVO;					
				}								
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {		
			e.printStackTrace();
		}
		activityStepOperationResultVO.setOperationResult(false);
		return activityStepOperationResultVO;		
	}	
	
	@POST
    @Path("/setTaskDueDate/")
	@Produces("application/json")
	public ActivityStepVO setParticipantTaskDueDate(ActivityStepOperationVO activityStepOperationVO){			
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			List<ParticipantTask> currentParticipantTaskList=activitySpace.getParticipant(activityStepOperationVO.getCurrentStepOwner()).fetchParticipantTasks();				
			for(ParticipantTask participantTask:currentParticipantTaskList){				
				String activityId=participantTask.getActivityStep().getActivityId();
				String activityType=participantTask.getActivityType();
				String activityStepName=participantTask.getActivityStepName();
				if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
				activityStepName.equals(activityStepOperationVO.getActivityStepName())){					
					ActivityStep currentActivityStep=participantTask.getActivityStep();
					if(activityStepOperationVO.getActivityStepDueDate()==0){
						currentActivityStep.getActivityProcessStep().updateCurrentStepDueDate(null);
					}else{
						currentActivityStep.getActivityProcessStep().updateCurrentStepDueDate(new Date(activityStepOperationVO.getActivityStepDueDate()));						
					}						
					ActivityStepVO currentActivityStepVO=new ActivityStepVO();
					currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
					currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
					currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
					currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());			
					currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());				
					currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());				
					currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());				
					currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());	
					currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
					currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
					currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
					currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
					currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());
					if(currentActivityStep.getFinishTime()!=null){
						currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
					}else{
						currentActivityStepVO.setFinishTime(0);
					}					
					if(currentActivityStep.getDueDate()!=null){
						currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
						currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));
					}else{
						currentActivityStepVO.setDueDate(0);
						currentActivityStepVO.setDueStatus(TASK_DUESTATUS_NODEU);
					}				
					String[] stepResponse=currentActivityStep.getBusinessActivity().getActivityDefinition().getStepDecisionPointChoiseList(currentActivityStep.getActivityStepDefinitionKey());
					currentActivityStepVO.setStepResponse(stepResponse);
					
					Role relatedRole=currentActivityStep.getRelatedRole();
					RoleVO relatedRoleVO=new RoleVO();
					relatedRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					relatedRoleVO.setDescription(relatedRole.getDescription());
					relatedRoleVO.setDisplayName(relatedRole.getDisplayName());
					relatedRoleVO.setRoleName(relatedRole.getRoleName());				
					currentActivityStepVO.setRelatedRole(relatedRoleVO);
					/*
					ActivityData[] currentActivityData=currentActivityStep.getActivityStepData();				
					ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
					List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
					activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
					if(currentActivityData!=null){
						for(ActivityData activityData:currentActivityData){							
							ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
							activityDataFieldValueVOs.add(activityDataFieldValueVO);						
						}
					}			
					currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);					
					*/										
					return currentActivityStepVO;					
				}								
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {		
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		} catch (ProcessRepositoryRuntimeException e) {			
			e.printStackTrace();
		}		
		return null;		
	}	
	
	@GET
	@Path("/roleRelatedRoleQueuesDetail/{applicationSpaceName}/{roleName}")
	@Produces("application/json")
	public List<RoleQueueVO> getRoleQueueTasksDetail(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("roleName")String roleName){		
		String activitySpaceName=applicationSpaceName;
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		List<RoleQueueVO> roleQueueVOs=new ArrayList<RoleQueueVO>();
		try {
			Role crrentRole=activitySpace.getRole(roleName);	
			if(crrentRole!=null){
				RoleQueue[] currentRoleQueues=crrentRole.getRelatedRoleQueues();			
				for(RoleQueue currentRoleQueue:currentRoleQueues){
					RoleQueueVO currentRoleQueueVO=buildRoleQueueVO(currentRoleQueue,false,activitySpace);
					roleQueueVOs.add(currentRoleQueueVO);				
				}	
			}					
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		return roleQueueVOs;
	}	
	
	@GET
	@Path("/participantRelatedRoleQueuesDetail/{applicationSpaceName}/{participantName}")
	@Produces("application/json")
	public List<RoleQueueVO> getParticipantRelatedQueueTasksDetail(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName){		
		String activitySpaceName=applicationSpaceName;
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		List<RoleQueueVO> roleQueueVOs=new ArrayList<RoleQueueVO>();
		List<String> alreadyGeneratedRoleQueues=new ArrayList<String>();
		try {
			Participant currentParticipant=activitySpace.getParticipant(participantName);			
			Role[] participantRelatedRoles=currentParticipant.getRoles();
			if(participantRelatedRoles!=null){
				for(Role currentRole:participantRelatedRoles){
					RoleQueue[] currentRoleQueues=currentRole.getRelatedRoleQueues();
					if(currentRoleQueues!=null){
						for(RoleQueue currentRoleQueue:currentRoleQueues){							
							if(!alreadyGeneratedRoleQueues.contains(currentRoleQueue.getQueueName())){
								RoleQueueVO currentRoleQueueVO=buildRoleQueueVO(currentRoleQueue,true,activitySpace);
								roleQueueVOs.add(currentRoleQueueVO);								
								alreadyGeneratedRoleQueues.add(currentRoleQueue.getQueueName());
							}
						}
					}
				}			
			}			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		alreadyGeneratedRoleQueues.clear();
		alreadyGeneratedRoleQueues=null;
		return roleQueueVOs;
	}
	
	@GET
	@Path("/participantRelatedRolesDetail/{applicationSpaceName}/{participantName}")
	@Produces("application/json")
	public List<RoleVO> getParticipantRelatedRolesDetail(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName){		
		String activitySpaceName=applicationSpaceName;
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		List<RoleVO> roleVOs=new ArrayList<RoleVO>();		
		try {
			Participant currentParticipant=activitySpace.getParticipant(participantName);			
			Role[] participantRelatedRoles=currentParticipant.getRoles();
			if(participantRelatedRoles!=null){
				for(Role currentRole:participantRelatedRoles){					
					RoleVO currentRoleVO=new RoleVO();					
					currentRoleVO.setActivitySpaceName(activitySpaceName);
					currentRoleVO.setDescription(currentRole.getDescription());
					currentRoleVO.setDisplayName(currentRole.getDisplayName());
					currentRoleVO.setRoleName(currentRole.getRoleName());					
					roleVOs.add(currentRoleVO);					
				}			
			}			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} 	
		return roleVOs;
	}	
	
	@GET
	@Path("/roleQueuesDetail/{applicationSpaceName}/{roleQueueName}")
	@Produces("application/json")
	public RoleQueueVO getQueueTasksDetail(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("roleQueueName")String roleQueueName){			
		String activitySpaceName=applicationSpaceName;
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);	
		try {
			RoleQueue currentRoleQueue=activitySpace.getRoleQueue(roleQueueName);
			RoleQueueVO roleQueueVO=buildRoleQueueVO(currentRoleQueue,true,activitySpace);			
			return roleQueueVO;
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		return null;
	}		
	
	@GET
	@Path("/fetchRoleQueueTasks/{applicationSpaceName}/{roleQueueName}")
	@Produces("application/json")
	public List<ActivityStepVO> getRoleQueueTasks(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("roleQueueName")String roleQueueName){
		String activitySpaceName=applicationSpaceName;
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		List<ActivityStepVO> containedActivyStepVOs=new ArrayList<ActivityStepVO>();
		try {
			RoleQueue currentRoleQueue=activitySpace.getRoleQueue(roleQueueName);			
			List<ActivityStep> activityStepsInQueue=currentRoleQueue.fetchActivitySteps();			
			for(ActivityStep currentActivityStep:activityStepsInQueue){
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());			
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());				
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());				
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());				
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());	
				currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
				currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
				currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());
				if(currentActivityStep.getFinishTime()!=null){
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}					
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
					currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));
				}else{
					currentActivityStepVO.setDueDate(0);
					currentActivityStepVO.setDueStatus(TASK_DUESTATUS_NODEU);
				}				
				String[] stepResponse=currentActivityStep.getBusinessActivity().getActivityDefinition().getStepDecisionPointChoiseList(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setStepResponse(stepResponse);
				
				Role relatedRole=currentActivityStep.getRelatedRole();
				RoleVO relatedRoleVO=new RoleVO();
				relatedRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
				relatedRoleVO.setDescription(relatedRole.getDescription());
				relatedRoleVO.setDisplayName(relatedRole.getDisplayName());
				relatedRoleVO.setRoleName(relatedRole.getRoleName());				
				currentActivityStepVO.setRelatedRole(relatedRoleVO);
				
				ActivityData[] currentActivityData=currentActivityStep.getActivityStepData();				
				ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
				List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
				activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
				if(currentActivityData!=null){
					for(ActivityData activityData:currentActivityData){							
						ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
						activityDataFieldValueVOs.add(activityDataFieldValueVO);						
					}
				}			
				currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);					
				containedActivyStepVOs.add(currentActivityStepVO);
			}			
		}catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}				
		return containedActivyStepVOs;
	}	
	
	@POST
    @Path("/saveTaskData/")
	@Produces("application/json")
	public ActivityStepOperationResultVO saveTaskData(ActivityStepDataUpdateVO activityStepDataUpdateVO){			
		ActivityStepOperationResultVO activityStepOperationResultVO=new ActivityStepOperationResultVO();		
		ActivityStepOperationVO activityStepOperationVO=activityStepDataUpdateVO.getActivityStepOperationVO();		
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();		
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		ActivityDataFieldValueVOList activityDataFieldValueList=activityStepDataUpdateVO.getActivityDataFieldValueList();
		try {
			if(activityStepDataUpdateVO.getActivityStepOperationVO().getCurrentStepOwner()!=null){
				String participantName=activityStepOperationVO.getCurrentStepOwner();
				List<ParticipantTask> currentParticipantTaskList=activitySpace.getParticipant(participantName).fetchParticipantTasks();				
				for(ParticipantTask participantTask:currentParticipantTaskList){				
					String activityId=participantTask.getActivityStep().getActivityId();
					String activityType=participantTask.getActivityType();
					String activityStepName=participantTask.getActivityStepName();
					if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
					activityStepName.equals(activityStepOperationVO.getActivityStepName())){						
						ActivityData[] activityDataArry=buildActivityDataArray(activityDataFieldValueList);
						boolean reassignTaskResult=participantTask.getActivityStep().getBusinessActivity().setActivityData(activityDataArry);			
						activityStepOperationResultVO.setOperationResult(reassignTaskResult);					
						return activityStepOperationResultVO;					
					}								
				}					
			}else{
				String roleQueueName=activityStepOperationVO.getActivityStepRelatedRoleQueue();
				RoleQueue currentRoleQueue=activitySpace.getRoleQueue(roleQueueName);			
				List<ActivityStep> activityStepsInQueue=currentRoleQueue.fetchActivitySteps();	
				for(ActivityStep activityStep:activityStepsInQueue){
					String activityId=activityStep.getActivityId();
					String activityType=activityStep.getActivityType();
					String activityStepName=activityStep.getActivityStepName();
					if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
					activityStepName.equals(activityStepOperationVO.getActivityStepName())){						
						ActivityData[] activityDataArry=buildActivityDataArray(activityDataFieldValueList);
						boolean reassignTaskResult=activityStep.getBusinessActivity().setActivityData(activityDataArry);			
						activityStepOperationResultVO.setOperationResult(reassignTaskResult);					
						return activityStepOperationResultVO;					
					}							
				}
			}		
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {		
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}
		activityStepOperationResultVO.setOperationResult(false);
		return activityStepOperationResultVO;		
	}

	@POST
    @Path("/completeTask/")
	@Produces("application/json")
	public ActivityStepOperationResultVO completeTask(ActivityStepDataUpdateVO activityStepDataUpdateVO){		
		ActivityStepOperationResultVO activityStepOperationResultVO=new ActivityStepOperationResultVO();
		ActivityStepOperationVO activityStepOperationVO=activityStepDataUpdateVO.getActivityStepOperationVO();
		String activitySpaceName=activityStepOperationVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			List<ParticipantTask> currentParticipantTaskList=activitySpace.getParticipant(activityStepOperationVO.getCurrentStepOwner()).fetchParticipantTasks();			
			for(ParticipantTask participantTask:currentParticipantTaskList){				
				String activityId=participantTask.getActivityStep().getActivityId();
				String activityType=participantTask.getActivityType();
				String activityStepName=participantTask.getActivityStepName();
				if(activityId.equals(activityStepOperationVO.getActivityId())&&activityType.equals(activityStepOperationVO.getActivityType())&&
				activityStepName.equals(activityStepOperationVO.getActivityStepName())){					
					ActivityStep currentActivityStep=participantTask.getActivityStep();		
					boolean completeTaskResult=true;					
					Map<String,ActivityData> activityDataMap=buildActivityDataMap(activityStepDataUpdateVO.getActivityDataFieldValueList());					
					String stepAssigne=activityStepOperationVO.getCurrentStepOwner();
					String stepDecisionPointAttribute=currentActivityStep.getBusinessActivity().getActivityDefinition()
							.getStepDecisionPointAttributeName(currentActivityStep.getActivityStepDefinitionKey());
					String[] stepProcessVariableList=currentActivityStep.getBusinessActivity().getActivityDefinition()
							.getStepProcessVariableList(currentActivityStep.getActivityStepDefinitionKey());			
					String stepUserIdentityAttributeName=currentActivityStep.getBusinessActivity().getActivityDefinition()
							.getStepUserIdentityAttributeName(currentActivityStep.getActivityStepDefinitionKey());
					if(activityStepOperationVO.getActivityStepResponse()!=null||stepProcessVariableList!=null||stepUserIdentityAttributeName!=null){				
						Map<String,Object> variables=new HashMap<String,Object>();
						if(activityStepOperationVO.getActivityStepResponse()!=null){					
							variables.put(stepDecisionPointAttribute, activityStepOperationVO.getActivityStepResponse());
						}
						if(stepUserIdentityAttributeName!=null){					
							variables.put(stepUserIdentityAttributeName, stepAssigne);
						}				
						if(stepProcessVariableList!=null){
							for(String variableName:stepProcessVariableList){						 
								if(activityDataMap.get(variableName)!=null){							
									variables.put(variableName,activityDataMap.get(variableName).getDatFieldValue());
								}						 
							 }
						}				
						completeTaskResult=currentActivityStep.completeActivityStep(stepAssigne,variables);				
					}else{
						completeTaskResult=currentActivityStep.completeActivityStep(stepAssigne);
					}		
					activityStepOperationResultVO.setOperationResult(completeTaskResult);					
					return activityStepOperationResultVO;					
				}								
			}			
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {		
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}
		activityStepOperationResultVO.setOperationResult(false);
		return activityStepOperationResultVO;		
	}	
	
	@POST
    @Path("/activityTypeDefinitions/")
	@Produces("application/json")
	public List<ActivityTypeDefinitionVO> getActivityTypeDefinitions(ActivityOperatorVO activityOperatorVO){
		List<ActivityTypeDefinitionVO> activityTypeDefinitionList=new ArrayList<ActivityTypeDefinitionVO>();
		String activitySpaceName=activityOperatorVO.getActivitySpaceName();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);			
		try {
			BusinessActivityDefinition[] activityTypeDefinitionArray=activitySpace.getBusinessActivityDefinitions();
			for(BusinessActivityDefinition currentBusinessActivityDefinition:activityTypeDefinitionArray){				
				if(currentBusinessActivityDefinition.isEnabled()){					
					ActivityTypeDefinitionVO currentActivityTypeDefinitionVO=new ActivityTypeDefinitionVO();				
					currentActivityTypeDefinitionVO.setActivitySpaceName(currentBusinessActivityDefinition.getActivitySpaceName());
					currentActivityTypeDefinitionVO.setActivityType(currentBusinessActivityDefinition.getActivityType());
					currentActivityTypeDefinitionVO.setActivityTypeDesc(currentBusinessActivityDefinition.getActivityDescription());
					currentActivityTypeDefinitionVO.setEnabled(currentBusinessActivityDefinition.isEnabled());
					currentBusinessActivityDefinition.setActivityCategories(currentBusinessActivityDefinition.getActivityCategories());				
					if(currentBusinessActivityDefinition.getLaunchDecisionPointAttributeName()!=null){
						currentActivityTypeDefinitionVO.setLaunchDecisionPointAttributeName(currentBusinessActivityDefinition.getLaunchDecisionPointAttributeName());
						if(currentBusinessActivityDefinition.getLaunchDecisionPointChoiseList()!=null){
							currentActivityTypeDefinitionVO.setLaunchDecisionPointChoiseList(currentBusinessActivityDefinition.getLaunchDecisionPointChoiseList());
						}
					}
					if(currentBusinessActivityDefinition.getLaunchUserIdentityAttributeName()!=null){
						currentActivityTypeDefinitionVO.setLaunchUserIdentityAttributeName(currentBusinessActivityDefinition.getLaunchUserIdentityAttributeName());
					}
					if(currentBusinessActivityDefinition.getLaunchProcessVariableList()!=null){
						currentActivityTypeDefinitionVO.setLaunchProcessVariableList(currentBusinessActivityDefinition.getLaunchProcessVariableList());
					}
					if(currentBusinessActivityDefinition.getActivityLaunchRoles()!=null){
						currentActivityTypeDefinitionVO.setActivityLaunchRoles(currentBusinessActivityDefinition.getActivityLaunchRoles());					
					}
					if(currentBusinessActivityDefinition.getActivityLaunchParticipants()!=null){
						currentActivityTypeDefinitionVO.setActivityLaunchParticipants(currentBusinessActivityDefinition.getActivityLaunchParticipants());					
					}				
					DataFieldDefinition[] launchActivityDataFieldsArray=null;				
					DataFieldDefinition[] launchPointDataFieldDefineArray=currentBusinessActivityDefinition.getLaunchPointExposedDataFields();
					if(launchPointDataFieldDefineArray!=null&&launchPointDataFieldDefineArray.length>0){
						launchActivityDataFieldsArray=launchPointDataFieldDefineArray;
					}else{
						launchActivityDataFieldsArray=currentBusinessActivityDefinition.getActivityDataFields();
					}				
					
					ActivityDataFieldValueVO[] activityDataFieldValueVOArray=new ActivityDataFieldValueVO[launchActivityDataFieldsArray.length];				
					for(int i=0;i<launchActivityDataFieldsArray.length;i++){
						DataFieldDefinition currentDataFieldDefinition=launchActivityDataFieldsArray[i];
						ActivityDataFieldValueVO activityDataFieldValueVO=new ActivityDataFieldValueVO();					
						ActivityDataDefinitionVO activityDataDefinitionVO=new ActivityDataDefinitionVO();					
						activityDataFieldValueVO.setActivityDataDefinition(activityDataDefinitionVO);					
						activityDataDefinitionVO.setArrayField(currentDataFieldDefinition.isArrayField());
						activityDataDefinitionVO.setDescription(currentDataFieldDefinition.getDescription());
						activityDataDefinitionVO.setDisplayName(currentDataFieldDefinition.getDisplayName());
						activityDataDefinitionVO.setFieldName(currentDataFieldDefinition.getFieldName());
						activityDataDefinitionVO.setMandatoryField(currentDataFieldDefinition.isMandatoryField());
						activityDataDefinitionVO.setSystemField(currentDataFieldDefinition.isSystemField());	
						activityDataDefinitionVO.setReadableField(currentDataFieldDefinition.isReadableField());
						activityDataDefinitionVO.setWriteableField(currentDataFieldDefinition.isWriteableField());	
						activityDataDefinitionVO.setFieldType(getDataDefinitionFieldType(currentDataFieldDefinition.getFieldType()));	
						activityDataFieldValueVOArray[i]=activityDataFieldValueVO;
					}						
					currentActivityTypeDefinitionVO.setActivityLaunchData(activityDataFieldValueVOArray);
					activityTypeDefinitionList.add(currentActivityTypeDefinitionVO);
				}				
			}			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}				
		return activityTypeDefinitionList;
	}
	
	@POST
    @Path("/launchActivity/")
	@Produces("application/json")
	public BusinessActivityVO launchActivity(LaunchActivityDataVO launchActivityDataVO){		
		ActivityStepOperationResultVO activityStepOperationResultVO=new ActivityStepOperationResultVO();
		activityStepOperationResultVO.setOperationResult(false);		
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(launchActivityDataVO.getActivityTypeDefinition().getActivitySpaceName());
		try {			
			ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
			activityDataFieldValueVOList.setActivityDataFieldValueList(launchActivityDataVO.getLaunchActivityData());
			ActivityData[] activityDataArry=buildActivityDataArray(activityDataFieldValueVOList);		
			String activityStarterId=launchActivityDataVO.getStartUserId();
			String activityType=launchActivityDataVO.getActivityTypeDefinition().getActivityType();	
			
			String launchUserIdAttr=launchActivityDataVO.getActivityTypeDefinition().getLaunchUserIdentityAttributeName();				
			String launchDecisionPointAttr=launchActivityDataVO.getActivityTypeDefinition().getLaunchDecisionPointAttributeName();			
			String[] launchProcessVariableList=launchActivityDataVO.getActivityTypeDefinition().getLaunchProcessVariableList();
			
			BusinessActivity resultActivity=null;
			if(launchUserIdAttr!=null||launchDecisionPointAttr!=null||(launchProcessVariableList!=null&&launchProcessVariableList.length>0)){
				Map<String,Object> processVariables=new HashMap<String,Object>();
				if(launchUserIdAttr!=null){
					if(launchActivityDataVO.getLaunchUserIdentity()!=null){
						processVariables.put(launchUserIdAttr, launchActivityDataVO.getLaunchUserIdentity());
					}else{
						processVariables.put(launchUserIdAttr, launchActivityDataVO.getStartUserId());
					}
				}
				if(launchDecisionPointAttr!=null){
					processVariables.put(launchDecisionPointAttr, launchActivityDataVO.getLaunchDecisionPointChoise());						
				}
				if(launchProcessVariableList!=null&&launchProcessVariableList.length>0){					
					Map<String,ActivityData> activityDataMap=buildActivityDataMap(activityDataFieldValueVOList);
					for(String processVariableName:launchProcessVariableList){
						if(activityDataMap.get(processVariableName)!=null){							
							processVariables.put(processVariableName,activityDataMap.get(processVariableName).getDatFieldValue());
						}							
					}							
				}
				resultActivity=activitySpace.launchBusinessActivity(activityType,activityDataArry,processVariables,activityStarterId);				
			}else{
				resultActivity=activitySpace.launchBusinessActivity(activityType, activityDataArry, activityStarterId);				
			}				
			BusinessActivityVO businessActivityVO=new BusinessActivityVO();	
			businessActivityVO.setActivityType(resultActivity.getActivityDefinition().getActivityType());
			businessActivityVO.setActivityId(resultActivity.getActivityId());
			businessActivityVO.setFinished(resultActivity.isFinished());
			businessActivityVO.setRosterName(resultActivity.getRosterName());			
			return businessActivityVO;			
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		}		
		return null;		
	}
	
	@POST
    @Path("/activityComments/")
	@Produces("application/json")
	public List<ActivityCommentVO> getActivityComments(ActivityCommentOperationVO activityCommentOperationVO){			
		String activitySpaceName=activityCommentOperationVO.getActivitySpaceName();			
		String activityType=activityCommentOperationVO.getActivityType();
		String activityId=activityCommentOperationVO.getActivityId();
		String commentType=activityCommentOperationVO.getCommentType();		
		String activityStepName=activityCommentOperationVO.getActivityStepName();
		List<ActivityCommentVO> activityCommentsList=new ArrayList<ActivityCommentVO>();
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		try {					
			BusinessActivity currentBusinessActivity=activitySpace.getBusinessActivityByActivityInfo(activityType, activityId);		
			List<ActivityComment> activityComments=null;
			if(commentType.equals(ACTIVITYCOMMENT_TYPE_ACTIVITY)){
				activityComments=currentBusinessActivity.getComments();	
			}
			if(commentType.equals(ACTIVITYCOMMENT_TYPE_TASK)){
				ActivityStep currentActivityStep=currentBusinessActivity.getCurrentActivityStepByStepName(activityStepName);
				activityComments=currentActivityStep.getComments();					
			}					
			if(activityComments!=null){				
				ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
				List<String> userList=new ArrayList<String>();			
				participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
				participantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
				for(ActivityComment currentComment:activityComments){					
					userList.add(currentComment.getParticipant().getParticipantName());					
				}
				List<ParticipantDetailInfoVO> commentParticipantsList=null;
				if(userList.size()>0){
					ParticipantDetailInfoVOsList participantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);	
					commentParticipantsList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();
				}							
				for(int i=0;i<activityComments.size();i++){
					ActivityComment currentComment=activityComments.get(i);				
					long createdDate=currentComment.getAddDate().getTime();
					String commentContent=currentComment.getCommentContent();
					Role creatorRole=currentComment.getRole();					
					ActivityCommentVO currentCommentVO=new ActivityCommentVO();
					currentCommentVO.setCreatedDate(createdDate);
					currentCommentVO.setCommentContent(commentContent);					
					if(creatorRole!=null){
						RoleVO currentRoleVO=new RoleVO();
						currentRoleVO.setActivitySpaceName(creatorRole.getActivitySpaceName());
						currentRoleVO.setDescription(creatorRole.getDescription());
						currentRoleVO.setDisplayName(creatorRole.getDisplayName());
						currentRoleVO.setRoleName(creatorRole.getRoleName());	
						currentCommentVO.setCreatorRole(currentRoleVO);						
					}				
					if(commentParticipantsList!=null&&commentParticipantsList.get(i)!=null){
						currentCommentVO.setCreatorParticipant(commentParticipantsList.get(i));
					}					
					activityCommentsList.add(currentCommentVO);
				}				
			}
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		return activityCommentsList;
	}
	
	@POST
    @Path("/addActivityComment/")
	@Produces("application/json")
	public ActivityCommentOperationResultVO addActivityComments(ActivityCommentOperationVO activityCommentOperationVO){		
		ActivityCommentOperationResultVO activityCommentOperationResultVO=new ActivityCommentOperationResultVO();
		activityCommentOperationResultVO.setOperationResult(false);
		String activitySpaceName=activityCommentOperationVO.getActivitySpaceName();			
		String activityType=activityCommentOperationVO.getActivityType();
		String activityId=activityCommentOperationVO.getActivityId();
		String commentType=activityCommentOperationVO.getCommentType();		
		String activityStepName=activityCommentOperationVO.getActivityStepName();	
		String commentContent=activityCommentOperationVO.getCommentContent();		
		String commentCreator=activityCommentOperationVO.getCommentWriter();
		String commentWriterRoleName=activityCommentOperationVO.getCommentWriterRoleName();		
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {	
			ActivityComment newComment=new ActivityComment();		
			newComment.setCommentContent(commentContent);			
			Participant commentCreatorParticipant=activitySpace.getParticipant(commentCreator);
			newComment.setParticipant(commentCreatorParticipant);			
			if(commentWriterRoleName!=null){
				Role commentCreatorRole=activitySpace.getRole(commentWriterRoleName);
				newComment.setRole(commentCreatorRole);
			}	
			BusinessActivity currentBusinessActivity=activitySpace.getBusinessActivityByActivityInfo(activityType, activityId);			
			if(commentType.equals(ACTIVITYCOMMENT_TYPE_ACTIVITY)){
				currentBusinessActivity.addComment(newComment);	
			}
			if(commentType.equals(ACTIVITYCOMMENT_TYPE_TASK)){
				ActivityStep currentActivityStep=currentBusinessActivity.getCurrentActivityStepByStepName(activityStepName);
				currentActivityStep.addComment(newComment);						
			}								
			activityCommentOperationResultVO.setOperationResult(true);
		} catch (ActivityEngineProcessException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} 
		return activityCommentOperationResultVO;
	}	
	
	@GET
    @Path("/participantStartedActivities/{applicationSpaceName}/{participantName}")
	@Produces("application/json")
	public List<ActivityInstanceVO> getUserStartedActivitiesInfo(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName){
		List<ActivityInstanceVO> activityInstanceList=new ArrayList<ActivityInstanceVO>();		
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			List<BusinessActivity> businessActivityList=activitySpace.getBusinessActivitiesByStartUserId(participantName, ProcessSpace.PROCESS_STATUS_ALL);			
			ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
			List<String> userList=new ArrayList<String>();			
			participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
			participantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
			
			Map<String,ActivityStepVO> activityStepVO_StepAssigneeIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> stepAssigneeUserList=new ArrayList<String>();	
			Map<String,ActivityStepVO> activityStepVO_StepOwnerIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> stepOwnerUserList=new ArrayList<String>();
			
			Map<String,ActivityStepVO> finishedActivityStepVO_StepAssigneeIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> finishedActivityStepAssigneeUserList=new ArrayList<String>();	
			Map<String,ActivityStepVO> finishedActivityActivityStepVO_StepOwnerIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> finishedActivityStepOwnerUserList=new ArrayList<String>();
			
			for(BusinessActivity currentBusinessActivity:businessActivityList){					
				ActivityInstanceVO currentActivityInstance=new ActivityInstanceVO();
				activityInstanceList.add(currentActivityInstance);
				currentActivityInstance.setActivityId(currentBusinessActivity.getActivityId());
				currentActivityInstance.setIsFinished(currentBusinessActivity.isFinished());
				currentActivityInstance.setActivityStartUserId(currentBusinessActivity.getActivityProcessObject().getProcessStartUserId());
				currentActivityInstance.setIsSuspended(currentBusinessActivity.isSuspendedActivity());				
				
				userList.add(currentBusinessActivity.getActivityProcessObject().getProcessStartUserId());					
				currentActivityInstance.setActivityStartTime(currentBusinessActivity.getActivityProcessObject().getProcessStartTime().getTime());
				if(currentBusinessActivity.isFinished()){
					currentActivityInstance.setActivityDuration(currentBusinessActivity.getActivityProcessObject().getProcessDurationInMillis());
					currentActivityInstance.setActivityEndTime(currentBusinessActivity.getActivityProcessObject().getProcessEndTime().getTime());						
				}
					
				ActivityTypeDefinitionVO currentActivityTypeDefinition=new ActivityTypeDefinitionVO();					
				currentActivityTypeDefinition.setActivityCategories(currentBusinessActivity.getActivityDefinition().getActivityCategories());					
				currentActivityTypeDefinition.setActivityLaunchParticipants(currentBusinessActivity.getActivityDefinition().getActivityLaunchParticipants());
				currentActivityTypeDefinition.setActivityLaunchRoles(currentBusinessActivity.getActivityDefinition().getActivityLaunchRoles());
				currentActivityTypeDefinition.setActivitySpaceName(activitySpaceName);
				currentActivityTypeDefinition.setActivityType(currentBusinessActivity.getActivityDefinition().getActivityType());
				currentActivityTypeDefinition.setActivityTypeDesc(currentBusinessActivity.getActivityDefinition().getActivityDescription());
				currentActivityTypeDefinition.setEnabled(currentBusinessActivity.getActivityDefinition().isEnabled());
				/*
				currentActivityTypeDefinition.setActivityLaunchData(activityLaunchData);
				currentActivityTypeDefinition.setLaunchDecisionPointAttributeName(launchDecisionPointAttributeName);
				currentActivityTypeDefinition.setLaunchDecisionPointChoiseList(launchDecisionPointChoiseList);
				currentActivityTypeDefinition.setLaunchProcessVariableList(launchProcessVariableList);
				currentActivityTypeDefinition.setLaunchUserIdentityAttributeName(launchUserIdentityAttributeName);
				*/					
				currentActivityInstance.setActivityTypeDefinition(currentActivityTypeDefinition);		
					
				List<ActivityStep> currentActivityStepsList=currentBusinessActivity.getCurrentActivitySteps();						
				List<ActivityStepVO> currentActivityStepVOList=new ArrayList<>();					
				
				for(int i=0;i<currentActivityStepsList.size();i++){
					ActivityStep currentActivityStep=currentActivityStepsList.get(i);
					ActivityStepVO currentActivityStepVO=new ActivityStepVO();		
					currentActivityStepVOList.add(currentActivityStepVO);
					currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
					currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
					currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
					currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
					currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());	
					currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
					currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
					currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
					currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
					currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());
					if(currentActivityStep.getFinishTime()!=null){
						currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
					}else{
						currentActivityStepVO.setFinishTime(0);
					}		
					if(currentActivityStep.getDueDate()!=null){
						currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());							
						String dueStatue=getTaskDueStatus(currentActivityStep.getDueDate().getTime());
						currentActivityStepVO.setDueStatus(dueStatue);
					}			
					currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());	
					
					currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());					
					if(currentActivityStep.getStepAssignee()!=null){
						activityStepVO_StepAssigneeIndexMap.put(""+i, currentActivityStepVO);
						stepAssigneeUserList.add(currentActivityStep.getStepAssignee());
					}	
					
					currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());
					if(currentActivityStep.getStepOwner()!=null){
						activityStepVO_StepOwnerIndexMap.put(""+i, currentActivityStepVO);
						stepOwnerUserList.add(currentActivityStep.getStepOwner());
					}					
					
					Role relatedRole=currentActivityStep.getRelatedRole();
					if(relatedRole!=null){
						RoleVO currentRoleVO=new RoleVO();					
						currentActivityStepVO.setRelatedRole(currentRoleVO);
						currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
						currentRoleVO.setDescription(relatedRole.getDescription());
						currentRoleVO.setDisplayName(relatedRole.getDisplayName());
						currentRoleVO.setRoleName(relatedRole.getRoleName());	
						currentActivityStepVO.setRelatedRole(currentRoleVO);
					}						
					//currentActivityStep.getActivityStepData();
					//currentActivityStepVO.setActivityDataFieldValueList(data);
					//currentActivityStepVO.setStepResponse(stepResponse)
				}					
				currentActivityInstance.setCurrentActivitySteps(currentActivityStepVOList);	
				
				if(stepAssigneeUserList.size()>0){
					ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
					stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
					stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
					ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
					List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
					for(int i=0;i<stepAssigneeParticipantDetailInfoVOList.size();i++){
						String key=""+i;					
						ActivityStepVO targetActivityStepVO=activityStepVO_StepAssigneeIndexMap.get(key);
						if(targetActivityStepVO!=null){
							targetActivityStepVO.setStepAssigneeParticipant(stepAssigneeParticipantDetailInfoVOList.get(i));
						}					
					}	
					stepAssigneeUserList.clear();
				}
				
				if(stepOwnerUserList.size()>0){
					ParticipantDetailInfosQueryVO stepOwnerParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
					stepOwnerParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepOwnerUserList);		
					stepOwnerParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
					ParticipantDetailInfoVOsList stepOwnerParticipantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepOwnerParticipantDetailInfosQueryVO);			
					List<ParticipantDetailInfoVO> stepOwnerParticipantDetailInfoVOList=stepOwnerParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
					for(int i=0;i<stepOwnerParticipantDetailInfoVOList.size();i++){
						String key=""+i;					
						ActivityStepVO targetActivityStepVO=activityStepVO_StepOwnerIndexMap.get(key);
						if(targetActivityStepVO!=null){
							targetActivityStepVO.setStepOwnerParticipant(stepOwnerParticipantDetailInfoVOList.get(i));
						}					
					}			
					stepOwnerUserList.clear();
				}				
					
				List<String> nextSteps=currentBusinessActivity.getActivityProcessObject().getNextProcessSteps();
				currentActivityInstance.setNextActivitySteps(nextSteps);				
				
				List<HistoricProcessStep> historicProcessStepList=currentBusinessActivity.getActivityProcessObject().getFinishedProcessSteps();
				List<ActivityStepVO> finishedActivityStepVOList=new ArrayList<>();				
				for(int i=0;i<historicProcessStepList.size();i++){
					HistoricProcessStep currentHistoricProcessStep=historicProcessStepList.get(i);
					ActivityStepVO currentFinishedActivityStepVO=new ActivityStepVO();		
					finishedActivityStepVOList.add(currentFinishedActivityStepVO);			
					currentFinishedActivityStepVO.setActivityId(currentBusinessActivity.getActivityId());
					currentFinishedActivityStepVO.setActivityStepDefinitionKey(currentHistoricProcessStep.getStepDefinitionKey());
					currentFinishedActivityStepVO.setActivityStepName(currentHistoricProcessStep.getStepName());
					currentFinishedActivityStepVO.setActivityType(currentBusinessActivity.getActivityDefinition().getActivityType());
					currentFinishedActivityStepVO.setCreateTime(currentHistoricProcessStep.getStartTime().getTime());					
					currentFinishedActivityStepVO.setHasChildActivityStep(currentHistoricProcessStep.hasChildStep());
					currentFinishedActivityStepVO.setHasParentActivityStep(currentHistoricProcessStep.hasParentStep());					
					if(currentHistoricProcessStep.getEndTime()!=null){
						currentFinishedActivityStepVO.setFinishTime(currentHistoricProcessStep.getEndTime().getTime());								
					}						
					if(currentHistoricProcessStep.getDueDate()!=null){
						currentFinishedActivityStepVO.setDueDate(currentHistoricProcessStep.getDueDate().getTime());
						String dueStatue=getTaskDueStatus(currentHistoricProcessStep.getEndTime().getTime());
						currentFinishedActivityStepVO.setDueStatus(dueStatue);						
					}					
					currentFinishedActivityStepVO.setStepDescription(currentHistoricProcessStep.getStepDescription());					
					
					currentFinishedActivityStepVO.setStepAssignee(currentHistoricProcessStep.getStepAssignee());					
					if(currentHistoricProcessStep.getStepAssignee()!=null){
						finishedActivityStepVO_StepAssigneeIndexMap.put(""+i, currentFinishedActivityStepVO);
						finishedActivityStepAssigneeUserList.add(currentHistoricProcessStep.getStepAssignee());						
					}						
					currentFinishedActivityStepVO.setStepOwner(currentHistoricProcessStep.getStepOwner());
					if(currentHistoricProcessStep.getStepOwner()!=null){
						finishedActivityActivityStepVO_StepOwnerIndexMap.put(""+i, currentFinishedActivityStepVO);
						finishedActivityStepOwnerUserList.add(currentHistoricProcessStep.getStepOwner());
					}						
				}
				currentActivityInstance.setFinishedActivitySteps(finishedActivityStepVOList);
				
				if(finishedActivityStepAssigneeUserList.size()>0){
					ParticipantDetailInfosQueryVO finishedStepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
					finishedStepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(finishedActivityStepAssigneeUserList);
					finishedStepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);	
					ParticipantDetailInfoVOsList finishedStepAssigneeParticipantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(finishedStepAssigneeParticipantDetailInfosQueryVO);			
					List<ParticipantDetailInfoVO> finishedStepAssigneeParticipantDetailInfoVOList=finishedStepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
					for(int i=0;i<finishedStepAssigneeParticipantDetailInfoVOList.size();i++){
						String key=""+i;					
						ActivityStepVO targetActivityStepVO=finishedActivityStepVO_StepAssigneeIndexMap.get(key);
						if(targetActivityStepVO!=null){
							targetActivityStepVO.setStepAssigneeParticipant(finishedStepAssigneeParticipantDetailInfoVOList.get(i));
						}					
					}	
					finishedActivityStepAssigneeUserList.clear();
				}
				
				if(finishedActivityStepOwnerUserList.size()>0){
					ParticipantDetailInfosQueryVO finishedStepOwnerParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
					finishedStepOwnerParticipantDetailInfosQueryVO.setParticipantsUserUidList(finishedActivityStepOwnerUserList);		
					finishedStepOwnerParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
					ParticipantDetailInfoVOsList finishedStepOwnerParticipantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(finishedStepOwnerParticipantDetailInfosQueryVO);			
					List<ParticipantDetailInfoVO> finishedStepOwnerParticipantDetailInfoVOList=finishedStepOwnerParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
					for(int i=0;i<finishedStepOwnerParticipantDetailInfoVOList.size();i++){
						String key=""+i;					
						ActivityStepVO targetActivityStepVO=finishedActivityActivityStepVO_StepOwnerIndexMap.get(key);
						if(targetActivityStepVO!=null){
							targetActivityStepVO.setStepOwnerParticipant(finishedStepOwnerParticipantDetailInfoVOList.get(i));
						}					
					}		
					finishedActivityStepOwnerUserList.clear();
				}				
			}			
			ParticipantDetailInfoVOsList participantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
			List<ParticipantDetailInfoVO> participantDetailInfoVOList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();			
			for(int i=0;i<activityInstanceList.size();i++){
				ParticipantDetailInfoVO currentStarterParticipantDetailInfoVO=participantDetailInfoVOList.get(i);
				activityInstanceList.get(i).setActivityStartUserParticipant(currentStarterParticipantDetailInfoVO);				
			}			
		} catch (ProcessRepositoryRuntimeException e) {
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {
			e.printStackTrace();
		}		
		return activityInstanceList;
	}			
	
	@GET
    @Path("/activityInstanceDetail/{applicationSpaceName}/{activityId}")
	@Produces("application/json")
	public ActivityInstanceVO getActivityInstanceDetailById(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("activityId")String activityId){			
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);	
		ActivityInstanceVO currentActivityInstance=new ActivityInstanceVO();
		try {			
			BusinessActivity targetBusinessActivity=activitySpace.getBusinessActivityByActivityId(activityId);	
			if(targetBusinessActivity==null){
				return currentActivityInstance;
			}			
			ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
			List<String> userList=new ArrayList<String>();			
			participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
			participantDetailInfosQueryVO.setParticipantScope(activitySpaceName);					
			
			currentActivityInstance.setActivityId(targetBusinessActivity.getActivityId());
			currentActivityInstance.setIsFinished(targetBusinessActivity.isFinished());
			currentActivityInstance.setActivityStartUserId(targetBusinessActivity.getActivityProcessObject().getProcessStartUserId());			
			currentActivityInstance.setIsSuspended(targetBusinessActivity.isSuspendedActivity());
			userList.add(targetBusinessActivity.getActivityProcessObject().getProcessStartUserId());			
			ParticipantDetailInfoVOsList participantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
			List<ParticipantDetailInfoVO> participantDetailInfoVOList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();
			if(participantDetailInfoVOList.size()>0){
				currentActivityInstance.setActivityStartUserParticipant(participantDetailInfoVOList.get(0));				
			}			
			
			Map<String,ActivityStepVO> activityStepVO_StepAssigneeIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> stepAssigneeUserList=new ArrayList<String>();	
			Map<String,ActivityStepVO> activityStepVO_StepOwnerIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> stepOwnerUserList=new ArrayList<String>();
			
			Map<String,ActivityStepVO> finishedActivityStepVO_StepAssigneeIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> finishedActivityStepAssigneeUserList=new ArrayList<String>();	
			Map<String,ActivityStepVO> finishedActivityActivityStepVO_StepOwnerIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> finishedActivityStepOwnerUserList=new ArrayList<String>();			
			
			currentActivityInstance.setActivityStartTime(targetBusinessActivity.getActivityProcessObject().getProcessStartTime().getTime());
			if(targetBusinessActivity.isFinished()){
				currentActivityInstance.setActivityDuration(targetBusinessActivity.getActivityProcessObject().getProcessDurationInMillis());
				currentActivityInstance.setActivityEndTime(targetBusinessActivity.getActivityProcessObject().getProcessEndTime().getTime());						
			}
				
			ActivityTypeDefinitionVO currentActivityTypeDefinition=new ActivityTypeDefinitionVO();					
			currentActivityTypeDefinition.setActivityCategories(targetBusinessActivity.getActivityDefinition().getActivityCategories());					
			currentActivityTypeDefinition.setActivityLaunchParticipants(targetBusinessActivity.getActivityDefinition().getActivityLaunchParticipants());
			currentActivityTypeDefinition.setActivityLaunchRoles(targetBusinessActivity.getActivityDefinition().getActivityLaunchRoles());
			currentActivityTypeDefinition.setActivitySpaceName(activitySpaceName);
			currentActivityTypeDefinition.setActivityType(targetBusinessActivity.getActivityDefinition().getActivityType());
			currentActivityTypeDefinition.setActivityTypeDesc(targetBusinessActivity.getActivityDefinition().getActivityDescription());
			currentActivityTypeDefinition.setEnabled(targetBusinessActivity.getActivityDefinition().isEnabled());
			/*
			currentActivityTypeDefinition.setActivityLaunchData(activityLaunchData);
			currentActivityTypeDefinition.setLaunchDecisionPointAttributeName(launchDecisionPointAttributeName);
			currentActivityTypeDefinition.setLaunchDecisionPointChoiseList(launchDecisionPointChoiseList);
			currentActivityTypeDefinition.setLaunchProcessVariableList(launchProcessVariableList);
			currentActivityTypeDefinition.setLaunchUserIdentityAttributeName(launchUserIdentityAttributeName);
			*/					
			currentActivityInstance.setActivityTypeDefinition(currentActivityTypeDefinition);				
			
			
			List<ActivityStep> currentActivityStepsList=targetBusinessActivity.getCurrentActivitySteps();						
			List<ActivityStepVO> currentActivityStepVOList=new ArrayList<>();					
			
			for(int i=0;i<currentActivityStepsList.size();i++){
				ActivityStep currentActivityStep=currentActivityStepsList.get(i);
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();		
				currentActivityStepVOList.add(currentActivityStepVO);
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());	
				currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
				currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
				currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());								
				if(currentActivityStep.getFinishTime()!=null){
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}		
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());							
					String dueStatue=getTaskDueStatus(currentActivityStep.getDueDate().getTime());
					currentActivityStepVO.setDueStatus(dueStatue);
				}			
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());	
				
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());					
				if(currentActivityStep.getStepAssignee()!=null){
					activityStepVO_StepAssigneeIndexMap.put(""+i, currentActivityStepVO);
					stepAssigneeUserList.add(currentActivityStep.getStepAssignee());
				}	
				
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());
				if(currentActivityStep.getStepOwner()!=null){
					activityStepVO_StepOwnerIndexMap.put(""+i, currentActivityStepVO);
					stepOwnerUserList.add(currentActivityStep.getStepOwner());
				}					
				
				Role relatedRole=currentActivityStep.getRelatedRole();
				if(relatedRole!=null){
					RoleVO currentRoleVO=new RoleVO();					
					currentActivityStepVO.setRelatedRole(currentRoleVO);
					currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					currentRoleVO.setDescription(relatedRole.getDescription());
					currentRoleVO.setDisplayName(relatedRole.getDisplayName());
					currentRoleVO.setRoleName(relatedRole.getRoleName());	
					currentActivityStepVO.setRelatedRole(currentRoleVO);
				}						
				//currentActivityStep.getActivityStepData();
				//currentActivityStepVO.setActivityDataFieldValueList(data);
				//currentActivityStepVO.setStepResponse(stepResponse)
			}					
			currentActivityInstance.setCurrentActivitySteps(currentActivityStepVOList);	
			
			if(stepAssigneeUserList.size()>0){
				ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
				stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
				ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
				for(int i=0;i<stepAssigneeParticipantDetailInfoVOList.size();i++){
					String key=""+i;					
					ActivityStepVO targetActivityStepVO=activityStepVO_StepAssigneeIndexMap.get(key);
					if(targetActivityStepVO!=null){
						targetActivityStepVO.setStepAssigneeParticipant(stepAssigneeParticipantDetailInfoVOList.get(i));
					}					
				}			
			}
			
			if(stepOwnerUserList.size()>0){
				ParticipantDetailInfosQueryVO stepOwnerParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				stepOwnerParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepOwnerUserList);		
				stepOwnerParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
				ParticipantDetailInfoVOsList stepOwnerParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepOwnerParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> stepOwnerParticipantDetailInfoVOList=stepOwnerParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
				for(int i=0;i<stepOwnerParticipantDetailInfoVOList.size();i++){
					String key=""+i;					
					ActivityStepVO targetActivityStepVO=activityStepVO_StepOwnerIndexMap.get(key);
					if(targetActivityStepVO!=null){
						targetActivityStepVO.setStepOwnerParticipant(stepOwnerParticipantDetailInfoVOList.get(i));
					}					
				}									
			}						
			
			List<String> nextSteps=targetBusinessActivity.getActivityProcessObject().getNextProcessSteps();
			currentActivityInstance.setNextActivitySteps(nextSteps);				
			
			List<HistoricProcessStep> historicProcessStepList=targetBusinessActivity.getActivityProcessObject().getFinishedProcessSteps();
			List<ActivityStepVO> finishedActivityStepVOList=new ArrayList<>();				
			for(int i=0;i<historicProcessStepList.size();i++){
				HistoricProcessStep currentHistoricProcessStep=historicProcessStepList.get(i);
				ActivityStepVO currentFinishedActivityStepVO=new ActivityStepVO();		
				finishedActivityStepVOList.add(currentFinishedActivityStepVO);			
				currentFinishedActivityStepVO.setActivityId(targetBusinessActivity.getActivityId());
				currentFinishedActivityStepVO.setActivityStepDefinitionKey(currentHistoricProcessStep.getStepDefinitionKey());
				currentFinishedActivityStepVO.setActivityStepName(currentHistoricProcessStep.getStepName());
				currentFinishedActivityStepVO.setActivityType(targetBusinessActivity.getActivityDefinition().getActivityType());
				currentFinishedActivityStepVO.setCreateTime(currentHistoricProcessStep.getStartTime().getTime());					
				currentFinishedActivityStepVO.setHasChildActivityStep(currentHistoricProcessStep.hasChildStep());
				currentFinishedActivityStepVO.setHasParentActivityStep(currentHistoricProcessStep.hasParentStep());				
				if(currentHistoricProcessStep.getEndTime()!=null){
					currentFinishedActivityStepVO.setFinishTime(currentHistoricProcessStep.getEndTime().getTime());								
				}						
				if(currentHistoricProcessStep.getDueDate()!=null){
					currentFinishedActivityStepVO.setDueDate(currentHistoricProcessStep.getDueDate().getTime());
					String dueStatue=getTaskDueStatus(currentHistoricProcessStep.getEndTime().getTime());
					currentFinishedActivityStepVO.setDueStatus(dueStatue);						
				}					
				currentFinishedActivityStepVO.setStepDescription(currentHistoricProcessStep.getStepDescription());					
				
				currentFinishedActivityStepVO.setStepAssignee(currentHistoricProcessStep.getStepAssignee());					
				if(currentHistoricProcessStep.getStepAssignee()!=null){
					finishedActivityStepVO_StepAssigneeIndexMap.put(""+i, currentFinishedActivityStepVO);
					finishedActivityStepAssigneeUserList.add(currentHistoricProcessStep.getStepAssignee());						
				}						
				currentFinishedActivityStepVO.setStepOwner(currentHistoricProcessStep.getStepOwner());
				if(currentHistoricProcessStep.getStepOwner()!=null){
					finishedActivityActivityStepVO_StepOwnerIndexMap.put(""+i, currentFinishedActivityStepVO);
					finishedActivityStepOwnerUserList.add(currentHistoricProcessStep.getStepOwner());
				}						
			}
			currentActivityInstance.setFinishedActivitySteps(finishedActivityStepVOList);
			
			if(finishedActivityStepAssigneeUserList.size()>0){
				ParticipantDetailInfosQueryVO finishedStepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				finishedStepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(finishedActivityStepAssigneeUserList);
				finishedStepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
				ParticipantDetailInfoVOsList finishedStepAssigneeParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(finishedStepAssigneeParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> finishedStepAssigneeParticipantDetailInfoVOList=finishedStepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
				for(int i=0;i<finishedStepAssigneeParticipantDetailInfoVOList.size();i++){
					String key=""+i;					
					ActivityStepVO targetActivityStepVO=finishedActivityStepVO_StepAssigneeIndexMap.get(key);
					if(targetActivityStepVO!=null){
						targetActivityStepVO.setStepAssigneeParticipant(finishedStepAssigneeParticipantDetailInfoVOList.get(i));
					}					
				}						
			}
			
			if(finishedActivityStepOwnerUserList.size()>0){
				ParticipantDetailInfosQueryVO finishedStepOwnerParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				finishedStepOwnerParticipantDetailInfosQueryVO.setParticipantsUserUidList(finishedActivityStepOwnerUserList);		
				finishedStepOwnerParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
				ParticipantDetailInfoVOsList finishedStepOwnerParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(finishedStepOwnerParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> finishedStepOwnerParticipantDetailInfoVOList=finishedStepOwnerParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
				for(int i=0;i<finishedStepOwnerParticipantDetailInfoVOList.size();i++){
					String key=""+i;					
					ActivityStepVO targetActivityStepVO=finishedActivityActivityStepVO_StepOwnerIndexMap.get(key);
					if(targetActivityStepVO!=null){
						targetActivityStepVO.setStepOwnerParticipant(finishedStepOwnerParticipantDetailInfoVOList.get(i));
					}					
				}						
			}				
		} catch (ActivityEngineRuntimeException e) {
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {
			e.printStackTrace();
		}		
		return currentActivityInstance;
	}		
		
	@GET
    @Path("/participantWorkedActivitySteps/{applicationSpaceName}/{participantName}")
	@Produces("application/json")
	public List<ActivityStepVO> getUserWorkedActivityStepsInfo(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("participantName")String participantName){
		List<ActivityStepVO> activityStepsList=new ArrayList<ActivityStepVO>();		
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);		
		try {
			List<ActivityStep> workedActivityStepsList=activitySpace.getActivityStepsByInvolvedUserId(participantName, ProcessSpace.PROCESS_STATUS_FINISHED);				
			Map<String,ActivityStepVO> activityStepVO_StepAssigneeIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> stepAssigneeUserList=new ArrayList<String>();	
			Map<String,ActivityStepVO> activityStepVO_StepOwnerIndexMap=new HashMap<String,ActivityStepVO>();
			List<String> stepOwnerUserList=new ArrayList<String>();			
			for(int i=0;i<workedActivityStepsList.size();i++){	
				ActivityStep currentActivityStep=workedActivityStepsList.get(i);				
				ActivityStepVO currentActivityStepVO=new ActivityStepVO();				
				activityStepsList.add(currentActivityStepVO);					
				currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
				currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
				currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
				currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());
				currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());
				currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());		
				currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
				currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());	
				currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
				currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
				currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());								
				if(currentActivityStep.getFinishTime()!=null){
					currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
				}else{
					currentActivityStepVO.setFinishTime(0);
				}				
				if(currentActivityStep.getDueDate()!=null){
					currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
					currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));						
				}else{
					currentActivityStepVO.setDueDate(0);
				}				
				if(currentActivityStep.getRelatedRole()!=null){
					RoleVO currentRoleVO=new RoleVO();					
					currentActivityStepVO.setRelatedRole(currentRoleVO);
					Role relatedRole=currentActivityStep.getRelatedRole();
					currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
					currentRoleVO.setDescription(relatedRole.getDescription());
					currentRoleVO.setDisplayName(relatedRole.getDisplayName());
					currentRoleVO.setRoleName(relatedRole.getRoleName());	
					currentActivityStepVO.setRelatedRole(currentRoleVO);					
				}					
				currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());					
				if(currentActivityStep.getStepAssignee()!=null){
					activityStepVO_StepAssigneeIndexMap.put(""+i, currentActivityStepVO);
					stepAssigneeUserList.add(currentActivityStep.getStepAssignee());
				}					
				currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());
				if(currentActivityStep.getStepOwner()!=null){
					activityStepVO_StepOwnerIndexMap.put(""+i, currentActivityStepVO);
					stepOwnerUserList.add(currentActivityStep.getStepOwner());
				}						
				if(currentActivityStep.getActivityStepData()!=null&&currentActivityStep.getActivityStepData().length>0){					
					ActivityData[] currentActivityData=currentActivityStep.getActivityStepData();				
					ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
					List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
					activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
					if(currentActivityData!=null){
						for(ActivityData activityData:currentActivityData){							
							ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
							activityDataFieldValueVOs.add(activityDataFieldValueVO);						
						}
					}			
					currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);
				}							
			}					
			if(stepAssigneeUserList.size()>0){
				ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
				stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
				ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
				for(int i=0;i<stepAssigneeParticipantDetailInfoVOList.size();i++){
					String key=""+i;					
					ActivityStepVO targetActivityStepVO=activityStepVO_StepAssigneeIndexMap.get(key);
					if(targetActivityStepVO!=null){
						targetActivityStepVO.setStepAssigneeParticipant(stepAssigneeParticipantDetailInfoVOList.get(i));
					}					
				}			
			}
			
			if(stepOwnerUserList.size()>0){
				ParticipantDetailInfosQueryVO stepOwnerParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				stepOwnerParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepOwnerUserList);		
				stepOwnerParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
				ParticipantDetailInfoVOsList stepOwnerParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepOwnerParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> stepOwnerParticipantDetailInfoVOList=stepOwnerParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();				
				for(int i=0;i<stepOwnerParticipantDetailInfoVOList.size();i++){
					String key=""+i;					
					ActivityStepVO targetActivityStepVO=activityStepVO_StepOwnerIndexMap.get(key);
					if(targetActivityStepVO!=null){
						targetActivityStepVO.setStepOwnerParticipant(stepOwnerParticipantDetailInfoVOList.get(i));
					}					
				}									
			}				
		} catch (ProcessRepositoryRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {
			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {
			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {
			
			e.printStackTrace();
		} catch (ActivityEngineProcessException e) {
			
			e.printStackTrace();
		}		
		return activityStepsList;		
	}
	
	@GET
    @Path("/activityInvolvedParticipants/{applicationSpaceName}/{activityId}")
	@Produces("application/json")
	public List<ActivityInvolveInfoVO> getActivityInstanceInvolvedParticipantsById(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("activityId")String activityId){			
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);	
		List<ActivityInvolveInfoVO> activityInvolveInfoList=new ArrayList<ActivityInvolveInfoVO>();
		try {			
			BusinessActivity targetBusinessActivity=activitySpace.getBusinessActivityByActivityId(activityId);	
			if(targetBusinessActivity==null){
				return activityInvolveInfoList;
			}			
			ParticipantDetailInfosQueryVO participantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();
			List<String> userList=new ArrayList<String>();			
			participantDetailInfosQueryVO.setParticipantsUserUidList(userList);		
			participantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
			
			//activity starter
			userList.add(targetBusinessActivity.getActivityProcessObject().getProcessStartUserId());
			targetBusinessActivity.getActivityProcessObject().getProcessStartTime();
			ActivityInvolveInfoVO starterActivityInvolveInfoVO=new ActivityInvolveInfoVO();
			starterActivityInvolveInfoVO.setInitTime(targetBusinessActivity.getActivityProcessObject().getProcessStartTime().getTime());
			starterActivityInvolveInfoVO.setInvolveAction(INVOLVEACTION_LAUNCHACTIVITY);
			starterActivityInvolveInfoVO.setIsChildActivityStep(false);
			activityInvolveInfoList.add(starterActivityInvolveInfoVO);
			ParticipantDetailInfoVOsList participantDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);			
			List<ParticipantDetailInfoVO> participantDetailInfoVOList=participantDetailInfoVOsList.getParticipantDetailInfoVOsList();
			if(participantDetailInfoVOList!=null&&participantDetailInfoVOList.size()>0){
				starterActivityInvolveInfoVO.setInitInvolver(participantDetailInfoVOList.get(0));
			}			
			userList.clear();			
			
			List<String> ownerList=new ArrayList<String>();
			List<ActivityInvolveInfoVO> assigneeStorageList=new ArrayList<ActivityInvolveInfoVO>();
			List<ActivityInvolveInfoVO> ownerStorageList=new ArrayList<ActivityInvolveInfoVO>();
			
			//finished step			
			List<HistoricProcessStep> historicProcessStepList=targetBusinessActivity.getActivityProcessObject().getFinishedProcessSteps();
			for(int i=0;i<historicProcessStepList.size();i++){
				HistoricProcessStep currentHistoricProcessStep=historicProcessStepList.get(i);				
				ActivityInvolveInfoVO finishedActivityInvolveInfoVO=new ActivityInvolveInfoVO();
				finishedActivityInvolveInfoVO.setInvolveAction(currentHistoricProcessStep.getStepName());
				finishedActivityInvolveInfoVO.setStartTime(currentHistoricProcessStep.getStartTime().getTime());
				finishedActivityInvolveInfoVO.setEndTime(currentHistoricProcessStep.getEndTime().getTime());
				finishedActivityInvolveInfoVO.setIsChildActivityStep(false);
				activityInvolveInfoList.add(finishedActivityInvolveInfoVO);
				
				String stepAssignee=currentHistoricProcessStep.getStepAssignee();
				assigneeStorageList.add(finishedActivityInvolveInfoVO);
				userList.add(stepAssignee);
				
				String stepOwner=currentHistoricProcessStep.getStepOwner();
				if(stepOwner!=null){
					ownerStorageList.add(finishedActivityInvolveInfoVO);
					ownerList.add(stepOwner);					
				}			
				
				List<HistoricProcessStep> childHistoricProcessStep=currentHistoricProcessStep.getChildProcessSteps();					
				for(HistoricProcessStep historicProcessStep:childHistoricProcessStep){								
					ActivityInvolveInfoVO childFinishedActivityInvolveInfoVO=new ActivityInvolveInfoVO();
					childFinishedActivityInvolveInfoVO.setInvolveAction(historicProcessStep.getStepName());
					childFinishedActivityInvolveInfoVO.setStartTime(historicProcessStep.getStartTime().getTime());
					childFinishedActivityInvolveInfoVO.setEndTime(historicProcessStep.getEndTime().getTime());
					childFinishedActivityInvolveInfoVO.setIsChildActivityStep(true);
					activityInvolveInfoList.add(childFinishedActivityInvolveInfoVO);					
					String childStepAssignee=historicProcessStep.getStepAssignee();
					assigneeStorageList.add(childFinishedActivityInvolveInfoVO);
					userList.add(childStepAssignee);					
					String childStepOwner=historicProcessStep.getStepOwner();
					if(stepOwner!=null){
						ownerStorageList.add(childFinishedActivityInvolveInfoVO);
						ownerList.add(childStepOwner);					
					}	
				}
			}
			
			//running step
			List<ActivityStep> currentActivityStepsList=targetBusinessActivity.getCurrentActivitySteps();	
			for(int i=0;i<currentActivityStepsList.size();i++){
				ActivityStep currentActivityStep=currentActivityStepsList.get(i);
				String stepOwner=currentActivityStep.getStepOwner();
				String stepAssignee=currentActivityStep.getStepAssignee();
				if(stepAssignee!=null||stepOwner!=null){
					ActivityInvolveInfoVO currentActivityInvolveInfoVO=new ActivityInvolveInfoVO();
					currentActivityInvolveInfoVO.setInvolveAction(currentActivityStep.getActivityStepName());
					currentActivityInvolveInfoVO.setStartTime(currentActivityStep.getCreateTime().getTime());
					currentActivityInvolveInfoVO.setIsChildActivityStep(false);
					activityInvolveInfoList.add(currentActivityInvolveInfoVO);
					if(stepAssignee!=null){
						assigneeStorageList.add(currentActivityInvolveInfoVO);
						userList.add(stepAssignee);					
					}
					if(stepOwner!=null){
						ownerStorageList.add(currentActivityInvolveInfoVO);
						ownerList.add(stepOwner);					
					}
				}	
				
				List<ActivityStep> childActivitySteps=currentActivityStep.getChildActivitySteps();				
				for(ActivityStep activityStep:childActivitySteps){					
					String childStepOwner=activityStep.getStepOwner();
					String childStepAssignee=activityStep.getStepAssignee();
					if(childStepAssignee!=null||childStepOwner!=null){
						ActivityInvolveInfoVO currentActivityInvolveInfoVO=new ActivityInvolveInfoVO();
						currentActivityInvolveInfoVO.setInvolveAction(activityStep.getActivityStepName());
						currentActivityInvolveInfoVO.setStartTime(activityStep.getCreateTime().getTime());		
						currentActivityInvolveInfoVO.setIsChildActivityStep(true);
						if(activityStep.getFinishTime()!=null){
							long finishedTime=activityStep.getFinishTime().getTime();
							if(finishedTime!=0){
								currentActivityInvolveInfoVO.setEndTime(finishedTime);
							}
						}						
						activityInvolveInfoList.add(currentActivityInvolveInfoVO);
						if(childStepAssignee!=null){
							assigneeStorageList.add(currentActivityInvolveInfoVO);
							userList.add(childStepAssignee);					
						}
						if(childStepOwner!=null){
							ownerStorageList.add(currentActivityInvolveInfoVO);
							ownerList.add(childStepOwner);					
						}
					}						
				}				
			}					
			
			//set assignee user info
			ParticipantDetailInfoVOsList assigneeDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);
			List<ParticipantDetailInfoVO> assigneeDetailInfoVOList=assigneeDetailInfoVOsList.getParticipantDetailInfoVOsList();
			if(assigneeDetailInfoVOList!=null){
				for(int i=0;i<assigneeDetailInfoVOList.size();i++){
					assigneeStorageList.get(i).setAssigneeInvolver(assigneeDetailInfoVOList.get(i));				
				}
			}			
			//set owner user info
			participantDetailInfosQueryVO.setParticipantsUserUidList(ownerList);
			ParticipantDetailInfoVOsList ownerDetailInfoVOsList=
					ParticipantOperationServiceRESTClient.getUsersDetailInfo(participantDetailInfosQueryVO);
			List<ParticipantDetailInfoVO> ownerDetailInfoVOList=ownerDetailInfoVOsList.getParticipantDetailInfoVOsList();
			if(ownerDetailInfoVOList!=null){
				for(int i=0;i<ownerDetailInfoVOList.size();i++){
					ownerStorageList.get(i).setOwnerInvolver(ownerDetailInfoVOList.get(i));				
				}
			}				
		} catch (ActivityEngineProcessException e) {
			e.printStackTrace();
		}		
		return activityInvolveInfoList;
	}	
	
	@GET
    @Path("/activityStepDetail/{applicationSpaceName}/{activityId}/{activityStepName}")
	@Produces("application/json")
	public ActivityStepVO getActivityStepDetailInfo(@PathParam("applicationSpaceName")String applicationSpaceName,@PathParam("activityId")String activityId,@PathParam("activityStepName")String activityStepName){
		String activitySpaceName=applicationSpaceName;	
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);			
		try {			
			BusinessActivity targetBusinessActivity=activitySpace.getBusinessActivityByActivityId(activityId);	
			if(targetBusinessActivity==null){
				return null;
			}			
			ActivityStep currentActivityStep=targetBusinessActivity.getCurrentActivityStepByStepName(activityStepName);
			if(currentActivityStep==null){
				return null;
			}				
			ActivityStepVO currentActivityStepVO=new ActivityStepVO();	
			currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
			currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
			currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
			currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());			
			currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());			
			currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());		
			currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
			currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());	
			currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
			currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
			currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());								
			if(currentActivityStep.getFinishTime()!=null){
				currentActivityStepVO.setFinishTime(currentActivityStep.getFinishTime().getTime());
			}else{
				currentActivityStepVO.setFinishTime(0);
			}				
			if(currentActivityStep.getDueDate()!=null){
				currentActivityStepVO.setDueDate(currentActivityStep.getDueDate().getTime());
				currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));						
			}else{
				currentActivityStepVO.setDueDate(0);
			}				
			if(currentActivityStep.getRelatedRole()!=null){
				RoleVO currentRoleVO=new RoleVO();					
				currentActivityStepVO.setRelatedRole(currentRoleVO);
				Role relatedRole=currentActivityStep.getRelatedRole();
				currentRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
				currentRoleVO.setDescription(relatedRole.getDescription());
				currentRoleVO.setDisplayName(relatedRole.getDisplayName());
				currentRoleVO.setRoleName(relatedRole.getRoleName());	
				currentActivityStepVO.setRelatedRole(currentRoleVO);					
			}					
			currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());					
			if(currentActivityStep.getStepAssignee()!=null){
				List<String> stepAssigneeUserList=new ArrayList<String>();
				stepAssigneeUserList.add(currentActivityStep.getStepAssignee());
				
				ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
				stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);				
				ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();
				if(stepAssigneeParticipantDetailInfoVOList!=null&&stepAssigneeParticipantDetailInfoVOList.size()>0){
					currentActivityStepVO.setStepAssigneeParticipant(stepAssigneeParticipantDetailInfoVOList.get(0));
				}
			}					
			currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());
			if(currentActivityStep.getStepOwner()!=null){
				List<String> stepOwnerUserList=new ArrayList<String>();
				stepOwnerUserList.add(currentActivityStep.getStepOwner());
				ParticipantDetailInfosQueryVO stepOwnerParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
				stepOwnerParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepOwnerUserList);		
				stepOwnerParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceName);
				ParticipantDetailInfoVOsList stepOwnerParticipantDetailInfoVOsList=
						ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepOwnerParticipantDetailInfosQueryVO);			
				List<ParticipantDetailInfoVO> stepOwnerParticipantDetailInfoVOList=stepOwnerParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();	
				if(stepOwnerParticipantDetailInfoVOList!=null&&stepOwnerParticipantDetailInfoVOList.size()>0){
					currentActivityStepVO.setStepOwnerParticipant(stepOwnerParticipantDetailInfoVOList.get(0));
				}
			}						
			if(currentActivityStep.getActivityStepData()!=null&&currentActivityStep.getActivityStepData().length>0){					
				ActivityData[] currentActivityData=currentActivityStep.getActivityStepData();				
				ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
				List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
				activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
				if(currentActivityData!=null){
					for(ActivityData activityData:currentActivityData){							
						ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
						activityDataFieldValueVOs.add(activityDataFieldValueVO);						
					}
				}			
				currentActivityStepVO.setActivityDataFieldValueList(activityDataFieldValueVOList);
			}							
			return currentActivityStepVO;		
		} catch (ActivityEngineProcessException e) {
			e.printStackTrace();
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}		
		return null;		
	}	
	
	@GET
    @Path("/activityTypeGlobalConfigurationItem/{applicationSpaceName}/{activityType}/{configurationItem}")
	@Produces("application/json")
	public CustomStructureVO getBusinessTypeGlobalConfigurationItem(@PathParam("applicationSpaceName")String activitySpaceName,
			@PathParam("activityType")String activityType,@PathParam("configurationItem")String configurationItem){		
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		CustomStructureVO resultCustomStructureVO=null;
		try {			
			CustomStructure globalCustomStructure=activitySpace.getBusinessActivityDefinitionGlobalCustomStructure(activityType);	
			if(globalCustomStructure!=null){
				CustomStructure targetCustomStructure=globalCustomStructure.getSubCustomStructure(configurationItem);				
				if(targetCustomStructure!=null){
					resultCustomStructureVO= CommonOperationUtil.loadCustomStructure(targetCustomStructure);	
				}
			}
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}				
		return resultCustomStructureVO;
	}
	
	@GET
    @Path("/activityTypeStepConfigurationItem/{applicationSpaceName}/{activityType}/{stepId}")
	@Produces("application/json")
	public CustomStructureVO getBusinessTypeStepCustomStructure(@PathParam("applicationSpaceName")String activitySpaceName,
			@PathParam("activityType")String activityType,@PathParam("stepId")String stepId){		
		ActivitySpace activitySpace=ActivityComponentFactory.getActivitySpace(activitySpaceName);
		CustomStructureVO resultCustomStructureVO=null;
		try {			
			CustomStructure stepCustomStructure=activitySpace.getBusinessActivityDefinitionStepCustomStructure(activityType,stepId);
			if(stepCustomStructure!=null){
				resultCustomStructureVO= CommonOperationUtil.loadCustomStructure(stepCustomStructure);									
			}				
		} catch (ActivityEngineRuntimeException e) {			
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {			
			e.printStackTrace();
		}				
		return resultCustomStructureVO;
	}
	
	private static Map<String,ActivityData> buildActivityDataMap(ActivityDataFieldValueVOList activityDataFieldValueList){
		ActivityData[] activityDataArray=buildActivityDataArray(activityDataFieldValueList);
		Map<String,ActivityData> activityDataMap=new HashMap<String,ActivityData>();
		for(ActivityData currentActivityData:activityDataArray){			
			activityDataMap.put(currentActivityData.getDataFieldDefinition().getFieldName(), currentActivityData);			
		}		
		return activityDataMap;
	}
	
	private static ActivityData[] buildActivityDataArray(ActivityDataFieldValueVOList activityDataFieldValueList){		
		List<ActivityDataFieldValueVO> activityDataFieldsList=activityDataFieldValueList.getActivityDataFieldValueList();
		int activityDataArraySize=activityDataFieldsList.size();	
		ActivityData[] activityDataArray=new ActivityData[activityDataArraySize];		
		for(int i=0;i<activityDataArraySize;i++){
			ActivityDataFieldValueVO currentActivityDataFieldValueVO=activityDataFieldsList.get(i);			
			String dataFieldName=currentActivityDataFieldValueVO.getActivityDataDefinition().getFieldName();			
			int dataFieldNameType=getDataDefinitionFieldTypeNumber(currentActivityDataFieldValueVO.getActivityDataDefinition().getFieldType());			
			boolean dataFieldMultiProp=currentActivityDataFieldValueVO.getActivityDataDefinition().isArrayField();			
			DataFieldDefinition currentDataFieldDefinition=ActivityComponentFactory.cteateDataFieldDefinition(dataFieldName,dataFieldNameType, dataFieldMultiProp);
			Object dataValue=null;
			if(dataFieldMultiProp){
				String[] arrayFieldValue=currentActivityDataFieldValueVO.getArrayDataFieldValue();
				String fieldTypeString=currentActivityDataFieldValueVO.getActivityDataDefinition().getFieldType();
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_BOOLEAN)){
					if(arrayFieldValue!=null){					
						boolean[] booleanArray=new boolean[arrayFieldValue.length];
						for(int idx=0;idx<arrayFieldValue.length;idx++){
							String stringValue=arrayFieldValue[idx];
							boolean booleanValue=Boolean.parseBoolean(stringValue);
							booleanArray[idx]=booleanValue;						
						}			
						dataValue=booleanArray;
					}else{
						dataValue=new boolean[0];
					}
				}
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_DOUBLE)){
					if(arrayFieldValue!=null){
						double[] doubleArray=new double[arrayFieldValue.length];
						for(int idx=0;idx<arrayFieldValue.length;idx++){
							String stringValue=arrayFieldValue[idx];
							double doubleValue=Double.parseDouble(stringValue);
							doubleArray[idx]=doubleValue;						
						}	
						dataValue=doubleArray;	
					}else{
						dataValue=new double[0];
					}
				}
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_LONG)){
					if(arrayFieldValue!=null){
						long[] longArray=new long[arrayFieldValue.length];
						for(int idx=0;idx<arrayFieldValue.length;idx++){
							String stringValue=arrayFieldValue[idx];
							long longValue=Long.parseLong(stringValue);
							longArray[idx]=longValue;						
						}	
						dataValue=longArray;
					}else{
						dataValue=new long[0];
					}
				}
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_DECIMAL)){
					if(arrayFieldValue!=null){
						BigDecimal[] bigDecimalArray=new BigDecimal[arrayFieldValue.length];
						for(int idx=0;idx<arrayFieldValue.length;idx++){
							String stringValue=arrayFieldValue[idx];
							BigDecimal bigDecimalValue=new BigDecimal(stringValue);						
							bigDecimalArray[idx]=bigDecimalValue;						
						}	
						dataValue=bigDecimalArray;	
					}else{
						dataValue=new BigDecimal[0];
					}
				}
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_BINARY)){}
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_DATE)){
					if(arrayFieldValue!=null){
						Calendar[] calendarArray=new Calendar[arrayFieldValue.length];
						for(int idx=0;idx<arrayFieldValue.length;idx++){
							String stringValue=arrayFieldValue[idx];
							long timeStamp=Long.parseLong(stringValue);
							Date date = new Date(timeStamp);
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(date);
							calendarArray[idx]=calendar;											
						}	
						dataValue=calendarArray;
					}else{
						dataValue=new Calendar[0];
					}
				}
				if(fieldTypeString.endsWith(DATAFIELD_TYPE_STRING)){
					if(arrayFieldValue!=null){
						dataValue=arrayFieldValue;
					}else{
						dataValue=new String[0];
					}
				}			
			}else{
				String singleFieldValue=currentActivityDataFieldValueVO.getSingleDataFieldValue();				
				String fieldTypeString=currentActivityDataFieldValueVO.getActivityDataDefinition().getFieldType();				
				if(singleFieldValue==null||singleFieldValue.equals("")){
					dataValue=null;
				}else{
					if(fieldTypeString.equals(DATAFIELD_TYPE_BOOLEAN)){
						if(singleFieldValue.equals("-")){
							dataValue=null;
						}else{
							dataValue=new Boolean(singleFieldValue);
						}						
					}
					if(fieldTypeString.equals(DATAFIELD_TYPE_DOUBLE)){
						dataValue=new Double(singleFieldValue);
					}
					if(fieldTypeString.equals(DATAFIELD_TYPE_LONG)){
						dataValue=new Long(singleFieldValue);
					}
					if(fieldTypeString.equals(DATAFIELD_TYPE_DECIMAL)){
						dataValue=new BigDecimal(singleFieldValue);										
					}
					if(fieldTypeString.equals(DATAFIELD_TYPE_BINARY)){}
					if(fieldTypeString.equals(DATAFIELD_TYPE_DATE)){
						long timeStamp=Long.parseLong(singleFieldValue);					
						Date date = new Date(timeStamp);
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date);
						dataValue=calendar;					
					}
					if(fieldTypeString.equals(DATAFIELD_TYPE_STRING)){
						dataValue=singleFieldValue;
					}										
				}	
			}			
			ActivityData currentActivityData=ActivityComponentFactory.createActivityData(currentDataFieldDefinition, dataValue);
			activityDataArray[i]=currentActivityData;		
		}		
		return activityDataArray;	
	}		
	
	private static RoleQueueVO buildRoleQueueVO(RoleQueue currentRoleQueue,boolean exposeRelatedRolesInfo,ActivitySpace activitySpace) throws ActivityEngineRuntimeException, ActivityEngineActivityException, ActivityEngineProcessException, ActivityEngineDataException{		
		if(currentRoleQueue==null){return null;}
		
		RoleQueueVO roleQueueVO=new RoleQueueVO();
		roleQueueVO.setActivitySpaceName(currentRoleQueue.getActivitySpaceName());			
		roleQueueVO.setDescription(currentRoleQueue.getDescription());
		roleQueueVO.setDisplayName(currentRoleQueue.getDisplayName());			
		roleQueueVO.setQueueName(currentRoleQueue.getQueueName());
		
		if(exposeRelatedRolesInfo){
			Role[] relatedRoles=currentRoleQueue.getRelatedRoles();
			List<RoleVO> roleVOs=new ArrayList<RoleVO>();
			for(Role currentRole:relatedRoles){
				RoleVO currentRoleVO=new RoleVO();				
				currentRoleVO.setActivitySpaceName(currentRole.getActivitySpaceName());
				currentRoleVO.setDescription(currentRole.getDescription());
				currentRoleVO.setDisplayName(currentRole.getDisplayName());
				currentRoleVO.setRoleName(currentRole.getRoleName());
				roleVOs.add(currentRoleVO);				
			}			
			roleQueueVO.setRelatedRoles(roleVOs);
		}		
		
		DataFieldDefinition[] exposedDataFields = currentRoleQueue.getExposedDataFields();
		List<ActivityDataDefinitionVO> exposedDataFieldVOs=new ArrayList<ActivityDataDefinitionVO>();
		if(exposedDataFields!=null){			
			for(DataFieldDefinition currentDataFieldDefinition:exposedDataFields){
				ActivityDataDefinitionVO currentActivityDataDefinitionVO=new ActivityDataDefinitionVO();				
				currentActivityDataDefinitionVO.setArrayField(currentDataFieldDefinition.isArrayField());
				currentActivityDataDefinitionVO.setDescription(currentDataFieldDefinition.getDescription());
				currentActivityDataDefinitionVO.setDisplayName(currentDataFieldDefinition.getDisplayName());
				currentActivityDataDefinitionVO.setFieldName(currentDataFieldDefinition.getFieldName());
				currentActivityDataDefinitionVO.setFieldType(getDataDefinitionFieldType(currentDataFieldDefinition.getFieldType()));
				currentActivityDataDefinitionVO.setMandatoryField(currentDataFieldDefinition.isMandatoryField());
				currentActivityDataDefinitionVO.setSystemField(currentDataFieldDefinition.isSystemField());				
				currentActivityDataDefinitionVO.setReadableField(currentDataFieldDefinition.isReadableField());
				currentActivityDataDefinitionVO.setWriteableField(currentDataFieldDefinition.isWriteableField());					
				exposedDataFieldVOs.add(currentActivityDataDefinitionVO);				
			}			
		}					
		roleQueueVO.setExposedDataFields(exposedDataFieldVOs);
		List<ActivityStep> containedActivySteps=currentRoleQueue.fetchActivitySteps();
		
		HashMap<String, BusinessActivityDefinition> businessActivityDefinitionMap=new HashMap<String,BusinessActivityDefinition>();	
		HashMap<String,Map<String,String>> activitySpaceStepProcessEditorsMappingMap=new HashMap<String,Map<String,String>>();
		List<ActivityStepVO> containedActivyStepVOs=new ArrayList<ActivityStepVO>();
		for(ActivityStep currentActivityStep:containedActivySteps){
			ActivityStepVO currentActivityStepVO=new ActivityStepVO();
			currentActivityStepVO.setActivityId(currentActivityStep.getActivityId());
			currentActivityStepVO.setActivityStepDefinitionKey(currentActivityStep.getActivityStepDefinitionKey());
			currentActivityStepVO.setActivityStepName(currentActivityStep.getActivityStepName());
			currentActivityStepVO.setActivityType(currentActivityStep.getActivityType());			
			currentActivityStepVO.setStepDescription(currentActivityStep.getStepDescription());				
			currentActivityStepVO.setCreateTime(currentActivityStep.getCreateTime().getTime());				
			currentActivityStepVO.setStepAssignee(currentActivityStep.getStepAssignee());				
			currentActivityStepVO.setStepOwner(currentActivityStep.getStepOwner());					
			currentActivityStepVO.setHasChildActivityStep(currentActivityStep.hasChildActivityStep());
			currentActivityStepVO.setHasParentActivityStep(currentActivityStep.hasParentActivityStep());
			currentActivityStepVO.setStepPriority(currentActivityStep.getActivityStepPriority());
			currentActivityStepVO.setIsDelegatedStep(currentActivityStep.isDelegatedActivityStep());
			currentActivityStepVO.setIsSuspendedStep(currentActivityStep.isSuspendedActivityStep());							
			if(currentActivityStep.getDueDate()!=null){
				currentActivityStepVO.setFinishTime(currentActivityStep.getDueDate().getTime());
				currentActivityStepVO.setDueStatus(getTaskDueStatus(currentActivityStep.getDueDate().getTime()));
			}else{
				currentActivityStepVO.setFinishTime(0);
				currentActivityStepVO.setDueStatus(TASK_DUESTATUS_NODEU);
			}
			Role relatedRole=currentActivityStep.getRelatedRole();
			RoleVO relatedRoleVO=new RoleVO();
			relatedRoleVO.setActivitySpaceName(relatedRole.getActivitySpaceName());
			relatedRoleVO.setDescription(relatedRole.getDescription());
			relatedRoleVO.setDisplayName(relatedRole.getDisplayName());
			relatedRoleVO.setRoleName(relatedRole.getRoleName());				
			currentActivityStepVO.setRelatedRole(relatedRoleVO);
			
			BusinessActivityDefinition targetActivityDefinition=businessActivityDefinitionMap.get(currentActivityStep.getActivityId());			
			if(targetActivityDefinition==null){
				targetActivityDefinition=currentActivityStep.getBusinessActivity().getActivityDefinition();			
				businessActivityDefinitionMap.put(currentActivityStep.getActivityId(), targetActivityDefinition);
			}
			String[] stepResponse=targetActivityDefinition.getStepDecisionPointChoiseList(currentActivityStep.getActivityStepDefinitionKey());
			currentActivityStepVO.setStepResponse(stepResponse);			
			
			Map<String,String> stepProcessEditorsMap=activitySpaceStepProcessEditorsMappingMap.get(currentActivityStep.getActivityType());
			if(stepProcessEditorsMap==null){
				stepProcessEditorsMap=activitySpace.getBusinessActivityDefinitionStepProcessEditorsInfo(currentActivityStep.getActivityType());
				if(stepProcessEditorsMap!=null){
					activitySpaceStepProcessEditorsMappingMap.put(currentActivityStep.getActivityType(), stepProcessEditorsMap);
				}				
			}							
			if(stepProcessEditorsMap!=null){
				String currentStepProcessEditor=stepProcessEditorsMap.get(currentActivityStep.getActivityStepDefinitionKey());
				if(currentStepProcessEditor!=null){							
					currentActivityStepVO.setStepProcessEditor(currentStepProcessEditor);							
				}
			}			
			containedActivyStepVOs.add(currentActivityStepVO);
		}
		
		//use this method for better performance
		BatchOperationHelper boh=ActivityComponentFactory.getBatchOperationHelper();
		List<ActivityData[]> adal=boh.batchQueryActivityStepsData(currentRoleQueue.getActivitySpaceName(),containedActivySteps, businessActivityDefinitionMap);
		for(int i=0;i<adal.size();i++){
			ActivityData[] currentActivityDataArray=adal.get(i);
			ActivityDataFieldValueVOList activityDataFieldValueVOList=new ActivityDataFieldValueVOList();
			List<ActivityDataFieldValueVO> activityDataFieldValueVOs=new ArrayList<ActivityDataFieldValueVO>();
			activityDataFieldValueVOList.setActivityDataFieldValueList(activityDataFieldValueVOs);
			if(currentActivityDataArray!=null){
				for(ActivityData activityData:currentActivityDataArray){							
					ActivityDataFieldValueVO activityDataFieldValueVO=buildActivityDataFieldValueVO(activityData);
					activityDataFieldValueVOs.add(activityDataFieldValueVO);						
				}
			}			
			containedActivyStepVOs.get(i).setActivityDataFieldValueList(activityDataFieldValueVOList);
		}		
		
		roleQueueVO.setActivitySteps(containedActivyStepVOs);	
		businessActivityDefinitionMap.clear();
		businessActivityDefinitionMap=null;
		return roleQueueVO;		
	}	
	
	public static String getTaskDueStatus(long dueStatus){		
		//dueStatus shoud be the long value of step due date, not step finish date. need logic to set this value
		if(dueStatus==0){
			return TASK_DUESTATUS_NODEU;
		}
		DateTime currentTime = new DateTime();				
		if(currentTime.isAfter(dueStatus)){
			return TASK_DUESTATUS_OVERDUE;
		}		
		if(currentTime.plusDays(1).isAfter(dueStatus)){
			return TASK_DUESTATUS_DUETODAY;
		}
		if(currentTime.plusDays(7).isAfter(dueStatus)){
			return TASK_DUESTATUS_DUETHISWEEK;
		}		
		return TASK_DUESTATUS_NODEU;
	}
	
	private static ActivityDataFieldValueVO buildActivityDataFieldValueVO(ActivityData currentActivityData){
		ActivityDataFieldValueVO activityDataFieldValueVO=new ActivityDataFieldValueVO();					
		ActivityDataDefinitionVO activityDataDefinitionVO=new ActivityDataDefinitionVO();					
		activityDataFieldValueVO.setActivityDataDefinition(activityDataDefinitionVO);
		
		DataFieldDefinition dataFieldDefinition=currentActivityData.getDataFieldDefinition();
		activityDataDefinitionVO.setArrayField(dataFieldDefinition.isArrayField());
		activityDataDefinitionVO.setDescription(dataFieldDefinition.getDescription());
		activityDataDefinitionVO.setDisplayName(dataFieldDefinition.getDisplayName());
		activityDataDefinitionVO.setFieldName(dataFieldDefinition.getFieldName());
		activityDataDefinitionVO.setMandatoryField(dataFieldDefinition.isMandatoryField());
		activityDataDefinitionVO.setSystemField(dataFieldDefinition.isSystemField());		
		activityDataDefinitionVO.setReadableField(dataFieldDefinition.isReadableField());
		activityDataDefinitionVO.setWriteableField(dataFieldDefinition.isWriteableField());		
		
		Object dataFieldValue=currentActivityData.getDatFieldValue();					
		if(dataFieldDefinition.getFieldType()==PropertyType.BOOLEAN){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_BOOLEAN);
			if(dataFieldDefinition.isArrayField()){
				boolean[] dataValue=null;				
				if(dataFieldValue instanceof boolean[]){
					dataValue=(boolean[])dataFieldValue;					
				}												
				if(dataValue==null){
					activityDataFieldValueVO.setArrayDataFieldValue(null);
				}else{
					String[] arrayDataFieldValue=new String[dataValue.length];
					for(int i=0;i<dataValue.length;i++){
						arrayDataFieldValue[i]=""+dataValue[i];
					}		
					activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);
				}	
			}else{
				Boolean dataValue=(Boolean)dataFieldValue;
				if(dataValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					activityDataFieldValueVO.setSingleDataFieldValue(""+dataValue.booleanValue());
				}						
			}						
		}
		if(dataFieldDefinition.getFieldType()==PropertyType.DOUBLE){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_DOUBLE);
			if(dataFieldDefinition.isArrayField()){
				double[] dataValue=null;
				if(dataFieldValue instanceof double[]){
					dataValue=(double[])dataFieldValue;
				}
				if(dataValue==null){
					activityDataFieldValueVO.setArrayDataFieldValue(null);
				}else{
					String[] arrayDataFieldValue=new String[dataValue.length];
					for(int i=0;i<dataValue.length;i++){
						arrayDataFieldValue[i]=""+dataValue[i];
					}			
					activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);
				}								
			}else{
				Double dataValue=(Double)dataFieldValue;
				if(dataValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					activityDataFieldValueVO.setSingleDataFieldValue(""+dataValue.doubleValue());
				}								
			}		
		}
		if(dataFieldDefinition.getFieldType()==PropertyType.LONG){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_LONG);
			if(dataFieldDefinition.isArrayField()){
				long[] dataValue=null;
				if(dataFieldValue instanceof long[]){
					dataValue=(long[])dataFieldValue;;
				}
				if(dataValue==null){
					activityDataFieldValueVO.setArrayDataFieldValue(null);
				}else{
					String[] arrayDataFieldValue=new String[dataValue.length];
					for(int i=0;i<dataValue.length;i++){
						arrayDataFieldValue[i]=""+dataValue[i];
					}	
					activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);
				}
			}else{
				Long dataValue=(Long)dataFieldValue;	
				if(dataValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					activityDataFieldValueVO.setSingleDataFieldValue(""+dataValue.longValue());
				}								
			}		
		}
		if(dataFieldDefinition.getFieldType()==PropertyType.DECIMAL){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_DECIMAL);
			if(dataFieldDefinition.isArrayField()){
				BigDecimal[] dataValue=null;
				if(dataFieldValue instanceof BigDecimal[]){
					dataValue=(BigDecimal[])dataFieldValue;
				}
				if(dataValue==null){
					activityDataFieldValueVO.setArrayDataFieldValue(null);
				}else{
					String[] arrayDataFieldValue=new String[dataValue.length];
					for(int i=0;i<dataValue.length;i++){
						arrayDataFieldValue[i]=dataValue[i].toString();
					}	
					activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);	
				}								
			}else{
				BigDecimal dataValue=(BigDecimal)dataFieldValue;	
				if(dataValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					activityDataFieldValueVO.setSingleDataFieldValue(dataValue.toString());
				}								
			}		
		}
		if(dataFieldDefinition.getFieldType()==PropertyType.BINARY){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_BINARY);
			if(dataFieldDefinition.isArrayField()){
				Binary[] dataValue=null;
				if(dataFieldValue instanceof Binary[]){
					dataValue=(Binary[])dataFieldValue;
				}				
				if(dataValue==null){
					activityDataFieldValueVO.setArrayDataFieldValue(null);
				}else{
					String[] arrayDataFieldValue=new String[dataValue.length];
					for(int i=0;i<dataValue.length;i++){
						arrayDataFieldValue[i]=dataValue[i].toString();
					}	
					activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);
				}	
			}else{
				Binary dataValue=(Binary)dataFieldValue;
				if(dataValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					activityDataFieldValueVO.setSingleDataFieldValue(dataValue.toString());
				}								
			}		
		}
		if(dataFieldDefinition.getFieldType()==PropertyType.DATE){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_DATE);
			if(dataFieldDefinition.isArrayField()){
				Calendar[] dataValue=null;
				if(dataFieldValue instanceof Calendar[]){
					dataValue=(Calendar[])dataFieldValue;
				}				
				if(dataValue==null){
					activityDataFieldValueVO.setArrayDataFieldValue(null);
				}else{
					String[] arrayDataFieldValue=new String[dataValue.length];
					for(int i=0;i<dataValue.length;i++){
						arrayDataFieldValue[i]=""+dataValue[i].getTimeInMillis();
					}	
					activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);
				}								
			}else{
				Calendar dataValue=(Calendar)dataFieldValue;
				if(dataValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					activityDataFieldValueVO.setSingleDataFieldValue(""+dataValue.getTimeInMillis()); //need check
				}								
			}		
		}
		if(dataFieldDefinition.getFieldType()==PropertyType.STRING){
			activityDataDefinitionVO.setFieldType(DATAFIELD_TYPE_STRING);
			if(dataFieldDefinition.isArrayField()){
				String[] arrayDataFieldValue=(String[])dataFieldValue;				
				activityDataFieldValueVO.setArrayDataFieldValue(arrayDataFieldValue);
			}else{
				if(dataFieldValue==null){
					activityDataFieldValueVO.setSingleDataFieldValue(null);
				}else{
					String dataValue=(String)dataFieldValue;
					activityDataFieldValueVO.setSingleDataFieldValue(dataValue);
				}				
			}		
		}		
		return activityDataFieldValueVO;
	}
	
	private static String getDataDefinitionFieldType(int fieldTypeNumber){
		if(fieldTypeNumber==PropertyType.BOOLEAN){return DATAFIELD_TYPE_BOOLEAN;}
		if(fieldTypeNumber==PropertyType.DOUBLE){return DATAFIELD_TYPE_DOUBLE;}
		if(fieldTypeNumber==PropertyType.LONG){return DATAFIELD_TYPE_LONG;}
		if(fieldTypeNumber==PropertyType.DECIMAL){return DATAFIELD_TYPE_DECIMAL;}
		if(fieldTypeNumber==PropertyType.BINARY){return DATAFIELD_TYPE_BINARY;}
		if(fieldTypeNumber==PropertyType.DATE){return DATAFIELD_TYPE_DATE;}
		if(fieldTypeNumber==PropertyType.STRING){return DATAFIELD_TYPE_STRING;}		
		return null;
	}
	
	private static int getDataDefinitionFieldTypeNumber(String fieldTypeString){
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_BOOLEAN)){return PropertyType.BOOLEAN;}
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_DOUBLE)){return PropertyType.DOUBLE;}
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_LONG)){return PropertyType.LONG;}
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_DECIMAL)){return PropertyType.DECIMAL;}
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_BINARY)){return PropertyType.BINARY;}
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_DATE)){return PropertyType.DATE;}
		if(fieldTypeString.endsWith(DATAFIELD_TYPE_STRING)){return PropertyType.STRING;}		
		return 0;
	}
	
	public static void main(String[] args){
		DateTime currentTime = new DateTime(2014,2,18,13,45,0,0);		
		System.out.println(getTaskDueStatus(currentTime.getMillis()));	
	}
}