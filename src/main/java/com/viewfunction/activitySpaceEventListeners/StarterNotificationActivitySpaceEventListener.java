package com.viewfunction.activitySpaceEventListeners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.viewfunction.activityEngine.activityBureau.BusinessActivity;
import com.viewfunction.activityEngine.exception.ActivityEngineActivityException;
import com.viewfunction.activityEngine.exception.ActivityEngineDataException;
import com.viewfunction.activityEngine.exception.ActivityEngineRuntimeException;
import com.viewfunction.activityEngine.extension.ActivitySpaceEventContext;
import com.viewfunction.activityEngine.extension.ActivitySpaceEventListener;
import com.viewfunction.messageEngine.exchange.MessageServiceConstant;
import com.viewfunction.messageEngine.exchange.restful.CommonNotificationVO;
import com.viewfunction.messageEngine.exchange.restful.MessageReceiverVO;
import com.viewfunction.messageEngine.exchange.restful.SendMessageResultVO;
import com.viewfunction.messageEngine.exchange.restfulClient.NotificationOperationServiceRESTClient;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVO;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfoVOsList;
import com.viewfunction.participantManagement.operation.restful.ParticipantDetailInfosQueryVO;
import com.viewfunction.participantManagement.operation.restfulClient.ParticipantOperationServiceRESTClient;
import com.viewfunction.processRepository.processBureau.ProcessObject;

public class StarterNotificationActivitySpaceEventListener extends ActivitySpaceEventListener{

	@Override
	public void executeActivitySpaceEventHandleLogic(ActivitySpaceEventContext activitySpaceEventContext) {
		try {
			BusinessActivity businessActivity=activitySpaceEventContext.getAttachedBusinessActivity();
			if(businessActivity!=null){
				if(businessActivity.isFinished()){
					ProcessObject processObject=businessActivity.getActivityProcessObject();
					List<String> targetUserList=new ArrayList<String>();	
					targetUserList.add(processObject.getProcessStartUserId());
					ParticipantDetailInfosQueryVO targetParticipantDetailInfosQueryVO=new ParticipantDetailInfosQueryVO();						
					targetParticipantDetailInfosQueryVO.setParticipantsUserUidList(targetUserList);		
					targetParticipantDetailInfosQueryVO.setParticipantScope(activitySpaceEventContext.getActivitySpaceName());				
					ParticipantDetailInfoVOsList targetParticipantDetailInfoVOsList=
							ParticipantOperationServiceRESTClient.getUsersDetailInfo(targetParticipantDetailInfosQueryVO);			
					List<ParticipantDetailInfoVO> targetParticipantDetailInfoVOList=targetParticipantDetailInfoVOsList.getParticipantDetailInfoVOsList();
					ParticipantDetailInfoVO activityStarterParticipantInfo=targetParticipantDetailInfoVOList.get(0);
					String notificationTitle=PropertiesConfigUtil.getPropertyValue("StarterNotification_NotificationTitle_Per")+" "+businessActivity.getActivityDefinition().getActivityType()+
							" "+PropertiesConfigUtil.getPropertyValue("StarterNotification_NotificationTitle_Post");
					
					CommonNotificationVO commonNotificationVO=new CommonNotificationVO();
					commonNotificationVO.setNotificationTitle(notificationTitle);	
					commonNotificationVO.setNotificationScope(activitySpaceEventContext.getActivitySpaceName());
					commonNotificationVO.setNotificationStatus("INFO");
					commonNotificationVO.setNotificationHandleable(false);
					
					commonNotificationVO.setNotificationSenderId(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_NotificationSenderId"));
					commonNotificationVO.setNotificationSenderName(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_NotificationSenderDisplayName"));	
					commonNotificationVO.setNotificationReceiverId(processObject.getProcessStartUserId());	
					
					List<MessageReceiverVO> receiverList=new ArrayList<MessageReceiverVO>();		
					MessageReceiverVO messageReceiverVO1=new MessageReceiverVO();		
					messageReceiverVO1.setReceiverDisplayName(activityStarterParticipantInfo.getDisplayName());
					messageReceiverVO1.setReceiverId(activityStarterParticipantInfo.getUserId());
					messageReceiverVO1.setReceiverType(MessageServiceConstant.MESSAGESERVICE_Property_MessageReceiverType_People);		
					receiverList.add(messageReceiverVO1);
					commonNotificationVO.setNotificationReceivers(receiverList);
					
					StringBuffer notificationContentBuffer=new StringBuffer();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:m a");
					
					notificationContentBuffer.append("<i class='icon-retweet'></i> "+PropertiesConfigUtil.getPropertyValue("StarterNotification_ActivityType"));
					notificationContentBuffer.append("<b>"+businessActivity.getActivityDefinition().getActivityType()+"</b> ("+businessActivity.getActivityId()+")");
					notificationContentBuffer.append("<br/>");
					
					notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
					notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-time'></i>&nbsp;");
					notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("StarterNotification_ActivityStartDate"));
					String dateStartStr = format.format(businessActivity.getActivityProcessObject().getProcessStartTime());
					notificationContentBuffer.append(dateStartStr);
					notificationContentBuffer.append("</div>");
					
					notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
					notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-time'></i>&nbsp;");
					notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("StarterNotification_ActivityFinishDate"));
					
					Date processEndTime=businessActivity.getActivityProcessObject().getProcessEndTime();
					//at this moment businessActivity not finished completed, so endtime is null yet, use current time instead
					if(processEndTime==null){
						processEndTime=new Date();
					}
					String dateFinishStr = format.format(processEndTime);
					notificationContentBuffer.append(dateFinishStr);
					notificationContentBuffer.append("</div>");
					notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
					notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-resize-horizontal'></i>&nbsp;");
					notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("StarterNotification_ActivityDurationTime"));
					
					Long durationValue=businessActivity.getActivityProcessObject().getProcessDurationInMillis();
					long durationLongValue=0;
					//at this moment businessActivity not finished completed, so sDurationInMillis is null yet
					if(durationValue==null){
						durationLongValue=processEndTime.getTime()-businessActivity.getActivityProcessObject().getProcessStartTime().getTime();
					}else{
						durationLongValue=durationValue.longValue();
					}
					String durationString=getDurationStr(durationLongValue);
					notificationContentBuffer.append(durationString);
					notificationContentBuffer.append("</div>");
					
					notificationContentBuffer.append("<div style='font-size:0.9em;color:#666666;padding-top:5px;'>");
					notificationContentBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i class='icon-male'></i>&nbsp;&nbsp;");
					notificationContentBuffer.append(PropertiesConfigUtil.getPropertyValue("AssigneeNotification_ActivityStartPerson"));
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
					} catch (ActivityEngineRuntimeException| ActivityEngineActivityException| ActivityEngineDataException e1) {
						e1.printStackTrace();
					}
					commonNotificationVO.setNotificationContent(notificationContentBuffer.toString());
					SendMessageResultVO sendMessageResultVO=NotificationOperationServiceRESTClient.sendCommonNotification(commonNotificationVO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getDurationStr(long costInLong){
		int dayCost=(int) Math.floor( costInLong / (1000*3600*24));
        int remainLongCostForHour=(int) (costInLong-dayCost*(1000*3600*24));
        int hourCost=(int) Math.floor( remainLongCostForHour / (1000*3600));
        int remainLongCostForMinute=remainLongCostForHour-hourCost*(1000*3600);
        int minuteCost=(int) Math.floor( remainLongCostForMinute / (1000*60));
		
		String activityDuration="";
        if(dayCost>0){
            activityDuration=activityDuration+dayCost+"天 ";
        }if(hourCost>0){
            activityDuration=activityDuration+hourCost+"小时 ";
        }if(minuteCost>0){
            activityDuration=activityDuration+minuteCost+"分钟";
        }
        if(activityDuration.equals("")){
        	return "小于1分钟";
        }else{
        	return activityDuration;
        }
	}

	@Override
	public void handelListenerLogicError(ActivitySpaceEventContext arg0, Exception arg1) {
		// TODO Auto-generated method stub
	}
}
