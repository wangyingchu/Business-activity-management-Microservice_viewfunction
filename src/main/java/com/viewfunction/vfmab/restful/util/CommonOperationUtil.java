package com.viewfunction.vfmab.restful.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.PropertyType;

import com.viewfunction.activityEngine.activityView.common.CustomAttribute;
import com.viewfunction.activityEngine.activityView.common.CustomStructure;
import com.viewfunction.activityEngine.exception.ActivityEngineDataException;
import com.viewfunction.activityEngine.exception.ActivityEngineRuntimeException;
import com.viewfunction.vfmab.restful.userManagement.CustomAttributeVO;
import com.viewfunction.vfmab.restful.userManagement.CustomStructureVO;

public class CommonOperationUtil {
	
	private static final String CustomAttributeType_String="STRING";
	private static final String CustomAttributeType_Long="LONG";
	private static final String CustomAttributeType_Date="DATE";
	private static final String CustomAttributeType_Double="DOUBLE";
	private static final String CustomAttributeType_Boolean="BOOLEAN";
	private static final String CustomAttributeType_Decimal="DECIMAL";	
	
	public static CustomStructureVO loadCustomStructure(CustomStructure targetCustomStructure) throws ActivityEngineRuntimeException, ActivityEngineDataException{
		CustomStructureVO targetCustomStructureVO=new CustomStructureVO();				
		targetCustomStructureVO.setStructureName(targetCustomStructure.getStructureName());
		targetCustomStructureVO.setStructureId(targetCustomStructure.getStructureId());			
		List<CustomAttribute> subCustomAttributesList=targetCustomStructure.getCustomAttributes();		
		List<CustomAttributeVO> customAttributeVOList=new ArrayList<CustomAttributeVO>();	
		if(subCustomAttributesList!=null){
			for(CustomAttribute currentCustomAttribute:subCustomAttributesList){
				CustomAttributeVO targetCustomAttributeVO=new CustomAttributeVO();			
				targetCustomAttributeVO.setAttributeName(currentCustomAttribute.getAttributeName());
				targetCustomAttributeVO.setArrayAttribute(currentCustomAttribute.isArrayAttribute());			
				if(currentCustomAttribute.getAttributeType()==PropertyType.STRING){
					targetCustomAttributeVO.setAttributeType(CustomAttributeType_String);
				}
				if(currentCustomAttribute.getAttributeType()==PropertyType.LONG){
					targetCustomAttributeVO.setAttributeType(CustomAttributeType_Long);
				}
				if(currentCustomAttribute.getAttributeType()==PropertyType.DATE){
					targetCustomAttributeVO.setAttributeType(CustomAttributeType_Date);
				}
				if(currentCustomAttribute.getAttributeType()==PropertyType.DOUBLE){
					targetCustomAttributeVO.setAttributeType(CustomAttributeType_Double);
				}
				if(currentCustomAttribute.getAttributeType()==PropertyType.BOOLEAN){
					targetCustomAttributeVO.setAttributeType(CustomAttributeType_Boolean);
				}
				if(currentCustomAttribute.getAttributeType()==PropertyType.DECIMAL){
					targetCustomAttributeVO.setAttributeType(CustomAttributeType_Decimal);
				}			
				String[] attributeRowValue=generateCustomAttributeRowValue(targetCustomAttributeVO.getAttributeType(),targetCustomAttributeVO.getArrayAttribute(),currentCustomAttribute.getAttributeValue());				
				targetCustomAttributeVO.setAttributeRowValue(attributeRowValue);				
				customAttributeVOList.add(targetCustomAttributeVO);
			}
		}		
		targetCustomStructureVO.setSubCustomAttributes(customAttributeVOList);			
		
		List<CustomStructure> subCustomStructureList=targetCustomStructure.getSubCustomStructures();
		List<CustomStructureVO> customStructureVOList=new ArrayList<CustomStructureVO>();	
		if(subCustomStructureList!=null){
			for(CustomStructure currentCustomStructure:subCustomStructureList){
				CustomStructureVO currentCustomStructureVO=loadCustomStructure(currentCustomStructure);			
				customStructureVOList.add(currentCustomStructureVO);
			}
		}		
		targetCustomStructureVO.setSubCustomStructures(customStructureVOList);			
		return targetCustomStructureVO;
	}
	
	public static String[] generateCustomAttributeRowValue(String attributeType,boolean arrayAttribute,Object attributeValue){
		if(attributeType.equals(CustomAttributeType_String)){				
			if(arrayAttribute){	
				String[] currentData=(String[])attributeValue;				
				return currentData;				
			}else{						
				String currentData=(String)attributeValue;				
				return new String[]{currentData};	
			}		
		}
		if(attributeType.equals(CustomAttributeType_Long)){
			if(arrayAttribute){	
				long[] currentData=(long[])attributeValue;				
				String[] arrayData=new String[currentData.length];
				for(int i=0;i<currentData.length;i++){
					long currentValue=currentData[i];					
					arrayData[i]=""+currentValue;					
				}
				return arrayData;			
			}else{
				Long currentData=(Long)attributeValue;				
				return new String[]{""+currentData.longValue()};
			}					
		}
		if(attributeType.equals(CustomAttributeType_Date)){				
			if(arrayAttribute){	
				Calendar[] currentData=(Calendar[])attributeValue;				
				String[] arrayData=new String[currentData.length];
				for(int i=0;i<currentData.length;i++){
					Calendar currentValue=currentData[i];					
					arrayData[i]=""+currentValue.getTimeInMillis();					
				}				
				return arrayData;	
			}else{	
				Calendar currentData=(Calendar)attributeValue;
				return new String[]{""+currentData.getTimeInMillis()};			
			}	
		}
		if(attributeType.equals(CustomAttributeType_Double)){				
			if(arrayAttribute){	
				double[] currentData=(double[])attributeValue;				
				String[] arrayData=new String[currentData.length];
				for(int i=0;i<currentData.length;i++){
					double currentValue=currentData[i];					
					arrayData[i]=""+currentValue;					
				}
				return arrayData;
			}else{		
				Double currentData=(Double)attributeValue;				
				return new String[]{""+currentData.doubleValue()};
			}	
		}
		if(attributeType.equals(CustomAttributeType_Boolean)){				
			if(arrayAttribute){	
				boolean[] currentData=(boolean[])attributeValue;				
				String[] arrayData=new String[currentData.length];
				for(int i=0;i<currentData.length;i++){
					boolean currentValue=currentData[i];					
					arrayData[i]=""+currentValue;					
				}
				return arrayData;
			}else{		
				Boolean currentData=(Boolean)attributeValue;				
				return new String[]{""+currentData.booleanValue()};
			}	
		}
		if(attributeType.equals(CustomAttributeType_Decimal)){				
			if(arrayAttribute){	
				BigDecimal[] currentData=(BigDecimal[])attributeValue;				
				String[] arrayData=new String[currentData.length];
				for(int i=0;i<currentData.length;i++){
					BigDecimal currentValue=currentData[i];					
					arrayData[i]=""+currentValue.toString();					
				}
				return arrayData;
			}else{
				BigDecimal currentData=(BigDecimal)attributeValue;				
				return new String[]{""+currentData.toString()};
			}	
		}
		return null;
	}	
	
	public static Object generateCustomAttributeValueFromRowData(String attributeType,boolean arrayAttribute,String[] attributeRowValue){
		if(attributeType.equals(CustomAttributeType_String)){				
			if(arrayAttribute){	
				return attributeRowValue;				
			}else{		
				String rowValueString=attributeRowValue[0];
				return rowValueString;
			}		
		}
		if(attributeType.equals(CustomAttributeType_Long)){
			if(arrayAttribute){	
				long[] arrayData=new long[attributeRowValue.length];
				for(int i=0;i<attributeRowValue.length;i++){
					String currentValue=attributeRowValue[i];
					long currentLongValue=Long.parseLong(currentValue);
					arrayData[i]=currentLongValue;					
				}
				return arrayData;			
			}else{
				String rowValueString=attributeRowValue[0];
				return new Long(rowValueString);
			}					
		}
		if(attributeType.equals(CustomAttributeType_Date)){				
			if(arrayAttribute){	
				Calendar[] arrayData=new Calendar[attributeRowValue.length];
				for(int i=0;i<attributeRowValue.length;i++){
					String currentValue=attributeRowValue[i];
					long timeStamp=Long.parseLong(currentValue);
					Date date = new Date(timeStamp);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);						
					arrayData[i]=calendar;					
				}
				return arrayData;	
			}else{		
				String rowValueString=attributeRowValue[0];				
				long timeStamp=Long.parseLong(rowValueString);
				Date date = new Date(timeStamp);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(date);
				return calendar;
			}	
		}
		if(attributeType.equals(CustomAttributeType_Double)){				
			if(arrayAttribute){	
				double[] arrayData=new double[attributeRowValue.length];
				for(int i=0;i<attributeRowValue.length;i++){
					String currentValue=attributeRowValue[i];
					double currentDoubleValue=Double.parseDouble(currentValue);
					arrayData[i]=currentDoubleValue;					
				}
				return arrayData;	
			}else{		
				String rowValueString=attributeRowValue[0];
				return new Double(rowValueString);
			}	
		}
		if(attributeType.equals(CustomAttributeType_Boolean)){				
			if(arrayAttribute){	
				boolean[] arrayData=new boolean[attributeRowValue.length];
				for(int i=0;i<attributeRowValue.length;i++){
					String currentValue=attributeRowValue[i];
					boolean currentBooleanValue=Boolean.parseBoolean(currentValue);
					arrayData[i]=currentBooleanValue;					
				}
				return arrayData;
			}else{		
				String rowValueString=attributeRowValue[0];
				return Boolean.valueOf(rowValueString);
			}	
		}
		if(attributeType.equals(CustomAttributeType_Decimal)){				
			if(arrayAttribute){	
				BigDecimal[] arrayData=new BigDecimal[attributeRowValue.length];
				for(int i=0;i<attributeRowValue.length;i++){
					String currentValue=attributeRowValue[i];
					BigDecimal currentDecimalValue=new BigDecimal(currentValue);
					arrayData[i]=currentDecimalValue;					
				}
				return arrayData;
			}else{		
				String rowValueString=attributeRowValue[0];
				return new BigDecimal(rowValueString);
			}	
		}			
		return null;
	}	

}
