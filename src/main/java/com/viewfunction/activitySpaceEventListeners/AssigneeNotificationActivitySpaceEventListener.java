package com.viewfunction.activitySpaceEventListeners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.viewfunction.activityEngine.activityBureau.ActivitySpace;
import com.viewfunction.activityEngine.activityView.common.ActivityStep;
import com.viewfunction.activityEngine.activityView.common.ParticipantTask;
import com.viewfunction.activityEngine.exception.ActivityEngineActivityException;
import com.viewfunction.activityEngine.exception.ActivityEngineDataException;
import com.viewfunction.activityEngine.exception.ActivityEngineProcessException;
import com.viewfunction.activityEngine.exception.ActivityEngineRuntimeException;
import com.viewfunction.activityEngine.extension.ActivitySpaceEventContext;
import com.viewfunction.activityEngine.extension.ActivitySpaceEventListener;
import com.viewfunction.messageEngine.exchange.MessageServiceConstant;
import com.viewfunction.messageEngine.exchange.restful.ActivityTaskNotificationVO;
import com.viewfunction.messageEngine.exchange.restful.CommonNotificationVO;
import com.viewfunction.messageEngine.exchange.restful.MessageReceiverVO;
import com.viewfunction.messageEngine.exchange.restful.SendActivityTaskNotificationVO;
import com.viewfunction.messageEngine.exchange.restful.SendMessageResultVO;
import com.viewfunction.messageEngine.exchange.restfulClient.NotificationOperationServiceRESTClient;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVOsList;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfosQueryVO;
import com.viewfunction.participantManagement.operation.restfulClient.ParticipantOperationServiceRESTClient;
import com.viewfunction.vfmab.restful.activityManagement.ActivityManagementService;

public class AssigneeNotificationActivitySpaceEventListener extends ActivitySpaceEventListener{

	@Override
	public void executeActivitySpaceEventHandleLogic(ActivitySpaceEventContext activitySpaceEventContext) {
		/*
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("executeActivitySpaceEventHandleLogic");
		System.out.println(activitySpaceEventContext.getActivitySpaceName());
		System.out.println(activitySpaceEventContext.getActivitySpace());
		System.out.println(activitySpaceEventContext.getApplicationSpaceDocumentFolderPath());
		System.out.println(activitySpaceEventContext.getAttachedActivityEventType());
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
		try {
			System.out.println(activitySpaceEventContext.getAttachedBusinessActivity());
		} catch (ActivityEngineProcessException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(activitySpaceEventContext.getAttachedBusinessActivityDefinition());
		} catch (ActivityEngineRuntimeException e) {
			e.printStackTrace();
		} catch (ActivityEngineActivityException e) {
			e.printStackTrace();
		} catch (ActivityEngineDataException e) {
			e.printStackTrace();
		}
		System.out.println(activitySpaceEventContext.getAttachedBusinessActivityDocumentFolder());
		*/
		
		//Send notification to activity step assignee when new step assigned
		try {
			List<ActivityStep> stepList=activitySpaceEventContext.getAttachedActivitySteps();
			if(stepList!=null){
				for(ActivityStep activityStep:stepList){
					//System also send ACTIVITYSTEP_ASSIGNED event before complete a step. so need use isNewAssignedStep to check if current event is for a already handled step
					//this step is handled by step assignee and will complete after the executeActivitySpaceEventHandleLogic execution
					if(isNewAssignedStep(activitySpaceEventContext,activityStep)){
						List<String> stepAssigneeUserList=new ArrayList<String>();	
						stepAssigneeUserList.add(activityStep.getStepAssignee());
						stepAssigneeUserList.add(activitySpaceEventContext.getAttachedBusinessActivity().getActivityProcessObject().getProcessStartUserId());
						
						ParticipantDetailInfosQueryVO stepAssigneeParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
						stepAssigneeParticipantDetailInfosQueryVO.setParticipantsUserUidList(stepAssigneeUserList);		
						stepAssigneeParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceEventContext.getActivitySpaceName());				
						ParticipantDetailInfoVOsList stepAssigneeParticipantDetailInfoVOsList=
								ParticipantOperationServiceRESTClient.getUsersDetailInfo(stepAssigneeParticipantDetailInfosQueryVO);			
						List<ParticipantDetailInfoVO> stepAssigneeParticipantDetailInfoVOList=stepAssigneeParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();
						ParticipantDetailInfoVO stepAssigneeParticipantInfo=stepAssigneeParticipantDetailInfoVOList.get(0);
						
						CommonNotificationVO commonNotificationVO=new CommonNotificationVO();
						String notificationTitle=PropertiesConfigUtil.getPropertyValue("AssigneeNotification_NotificationTitle_Per")+" "+activityStep.getActivityStepName()+
								" "+PropertiesConfigUtil.getPropertyValue("AssigneeNotification_NotificationTitle_Post");
						commonNotificationVO.setNotificationTitle(notificationTitle);
						commonNotificationVO.setNotificationSenderId(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_NotificationSenderId"));
						commonNotificationVO.setNotificationSenderName(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_NotificationSenderDisplayName"));	
						commonNotificationVO.setNotificationStatus("INFO");
						
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:m a");
						StringBuffer notificationContentBuffer=new StringBuffer();
						notificationContentBuffer.append("<i class='icon-tag'></i> "+PropertiesConfigUtil.getPropertyValue("AssigneeNotification_ActivityStep"));
						notificationContentBuffer.append("<b>"+activityStep.getActivityStepName()+"</b>"+"");
						notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
						notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-time'></i>&nbsp;");
						notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_StepCreateDate"));
						String createDateStr = format.format(activityStep.getCreateTime());
						notificationContentBuffer.append(createDateStr);
						notificationContentBuffer.append("</div>");
						
						if(activityStep.getDueDate()!=null){
							notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
							notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-time'></i>&nbsp;");
							notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_StepDueDate"));
							String dueDateStr = format.format(activityStep.getDueDate());
							notificationContentBuffer.append(dueDateStr);
							notificationContentBuffer.append("</div>");
						}								
						try {
							if(activityStep.getRelatedRole()!=null){
								notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
								notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-group'></i>&nbsp;");
								notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_StepRole"));
								notificationContentBuffer.append(activityStep.getRelatedRole().getDisplayName());
								notificationContentBuffer.append("</div>");
							}
						} catch (ActivityEngineRuntimeException e) {
							e.printStackTrace();
						} catch (ActivityEngineActivityException e) {
							e.printStackTrace();
						} catch (ActivityEngineDataException e) {
							e.printStackTrace();
						}	
						
						if(activityStep.getStepDescription()!=null){
							notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
							notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-comment'></i>&nbsp;");
							notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_StepDesc"));
							notificationContentBuffer.append(activityStep.getStepDescription());
							notificationContentBuffer.append("</div>");
						}
					
						notificationContentBuffer.append("<br/><br/>");
					
						notificationContentBuffer.append("<i class='icon-retweet'></i> "+PropertiesConfigUtil.getPropertyValue("AssigneeNotification_ActivityType"));
						notificationContentBuffer.append("<b>"+activityStep.getActivityType()+"</b> ("+activityStep.getActivityId()+")");
						notificationContentBuffer.append("<br/>");
						notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
						notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-time'></i>&nbsp;");
						notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_ActivityStartDate"));
						String dateStr = format.format(activitySpaceEventContext.getAttachedBusinessActivity().getActivityProcessObject().getProcessStartTime());
						notificationContentBuffer.append(dateStr);
						notificationContentBuffer.append("</div>");
						
						notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
						notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-male'></i>&nbsp;&nbsp;");
						notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_ActivityStartPerson"));
						ParticipantDetailInfoVO activityStarterParticipantInfo=stepAssigneeParticipantDetailInfoVOList.get(1);
						notificationContentBuffer.append(activityStarterParticipantInfo.getDisplayName());
						notificationContentBuffer.append("</div>");
						try {
							if(activitySpaceEventContext.getAttachedBusinessActivityDefinition().getActivityDescription()!=null&&
									!activitySpaceEventContext.getAttachedBusinessActivityDefinition().getActivityDescription().equals("")){
								String activityDesc=activitySpaceEventContext.getAttachedBusinessActivityDefinition().getActivityDescription();
								notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
								notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-comment'></i>&nbsp;&nbsp;");
								notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_ActivityDesc"));
								notificationContentBuffer.append(activityDesc);
								notificationContentBuffer.append("</div>");
							}
						} catch (ActivityEngineRuntimeException
								| ActivityEngineActivityException
								| ActivityEngineDataException e1) {
							e1.printStackTrace();
						}
						
						commonNotificationVO.setNotificationContent(notificationContentBuffer.toString());
						
						commonNotificationVO.setNotificationHandleable(true);
						commonNotificationVO.setNotificationReceiverId(activityStep.getStepAssignee());		
						List<MessageReceiverVO> receiverList=new ArrayList<MessageReceiverVO>();		
						MessageReceiverVO messageReceiverVO1=new MessageReceiverVO();		
						messageReceiverVO1.setReceiverDisplayName(stepAssigneeParticipantInfo.getDisplayName());
						messageReceiverVO1.setReceiverId(activityStep.getStepAssignee());
						messageReceiverVO1.setReceiverType(MessageServiceConstant.MESSAGESERVICE_Property_MessageReceiverType_People);		
						receiverList.add(messageReceiverVO1);
						commonNotificationVO.setNotificationReceivers(receiverList);		
						commonNotificationVO.setNotificationScope(activitySpaceEventContext.getActivitySpaceName());
						
						ActivityTaskNotificationVO activityTaskNotificationVO=new ActivityTaskNotificationVO();
						activityTaskNotificationVO.setActivityId(activityStep.getActivityId());
						activityTaskNotificationVO.setActivityName(activityStep.getActivityType());	
						activityTaskNotificationVO.setTaskDescription(activityStep.getStepDescription());
						long taskDueDateLongValue=0;
						activityTaskNotificationVO.setTaskDueDate(0);
						if(activityStep.getDueDate()!=null){
							taskDueDateLongValue=activityStep.getDueDate().getTime();
							activityTaskNotificationVO.setTaskDueDate(taskDueDateLongValue);
						}
						String dueDateStatus=ActivityManagementService.getTaskDueStatus(taskDueDateLongValue);
						activityTaskNotificationVO.setTaskDueStatus(dueDateStatus);
						activityTaskNotificationVO.setTaskName(activityStep.getActivityStepName());
						
						try {
							if(activityStep.getRelatedRole()!=null){
								activityTaskNotificationVO.setTaskRole(activityStep.getRelatedRole().getDisplayName());
							}
						} catch (ActivityEngineRuntimeException e) {
							e.printStackTrace();
						} catch (ActivityEngineActivityException e) {
							e.printStackTrace();
						} catch (ActivityEngineDataException e) {
							e.printStackTrace();
						}	
						SendActivityTaskNotificationVO sendActivityTaskNotificationVO=new SendActivityTaskNotificationVO();
						sendActivityTaskNotificationVO.setActivityTaskNotificationVO(activityTaskNotificationVO);
						sendActivityTaskNotificationVO.setCommonNotificationVO(commonNotificationVO);
						
						SendMessageResultVO sendMessageResultVO=NotificationOperationServiceRESTClient.sendActivityTaskNotification(sendActivityTaskNotificationVO);
						//System.out.println(sendMessageResultVO.isSendSuccess());
					}
				}
			}
		} catch (ActivityEngineProcessException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isNewAssignedStep(ActivitySpaceEventContext activitySpaceEventContext,ActivityStep targetActivityStep){		
		if(targetActivityStep.getFinishTime()!=null){
			return false;
		}
		String stepAssignee=targetActivityStep.getStepAssignee();
		String activityId=targetActivityStep.getActivityId();
		String activityStepId=targetActivityStep.getActivityStepId();
		String activityStepDefinitionKey=targetActivityStep.getActivityStepDefinitionKey();
		String activityStepName=targetActivityStep.getActivityStepName();		
		if(stepAssignee==null){
			return false;
		}else{
			ActivitySpace attachedActivitySpace=activitySpaceEventContext.getActivitySpace();
			boolean isNewAssignedStep=true;
			try {
				List<ParticipantTask> participantTaskList=attachedActivitySpace.getParticipant(stepAssignee).fetchParticipantTasks();
				for(ParticipantTask participantTask:participantTaskList){
					String taskStepId=participantTask.getActivityStep().getActivityStepId();
					String taskStepDefinitionKey=participantTask.getActivityStep().getActivityStepDefinitionKey();
					String taskActivityId=participantTask.getActivityStep().getActivityId();
					String taskActivityStepName=participantTask.getActivityStep().getActivityStepName();					
					if(activityStepId!=null){
						if(taskStepId.equals(activityStepId)&&taskStepDefinitionKey.equals(activityStepDefinitionKey)&&taskActivityId.equals(activityId)){
							isNewAssignedStep=false;
							break;
						}
					}else{
						//if related activityStep is child step, the activityStepId will be none, in this case uses ActivityStepName
						if(taskActivityStepName.equals(activityStepName)&&taskStepDefinitionKey.equals(activityStepDefinitionKey)&&taskActivityId.equals(activityId)){
							isNewAssignedStep=false;
							break;
						}					
					}					
				}					
				return isNewAssignedStep;
			} catch (ActivityEngineRuntimeException e2) {
				e2.printStackTrace();
			} catch (ActivityEngineProcessException e) {
				e.printStackTrace();
			}
		}		
		return false;
	}

	@Override
	public void handelListenerLogicError(ActivitySpaceEventContext arg0, Exception arg1) {
		// TODO Auto-generated method stub
	}
}