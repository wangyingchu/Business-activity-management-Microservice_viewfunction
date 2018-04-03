package com.viewfunction.contentRepository.util.helperImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.value.ValueFactoryImpl;

import com.viewfunction.contentRepository.contentBureau.BaseContentObject;
import com.viewfunction.contentRepository.contentBureauImpl.JCRContentObjectImpl;
import com.viewfunction.contentRepository.util.exception.ContentReposityDataException;
import com.viewfunction.contentRepository.util.exception.ContentReposityException;
import com.viewfunction.contentRepository.util.factory.ContentComponentFactory;
import com.viewfunction.contentRepository.util.helper.BinaryContent;
import com.viewfunction.contentRepository.util.helper.ContentQueryHelper;
import com.viewfunction.contentRepository.util.helper.TextContent;

public class JCRContentQueryHelperImpl implements ContentQueryHelper{

	@Override
	public List<BinaryContent> selectBinaryContentsByFullTextSearch(BaseContentObject contentObject, String contentValue) throws ContentReposityException {		
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();			
			//String sql = "SELECT * FROM [vfcr:resource] AS file WHERE ISDESCENDANTNODE(['"+np+"']) and contains(file.*,'"+contentValue.trim()+"')";
			
			//String sql = "SELECT * FROM [vfcr:resource] AS folder WHERE ISDESCENDANTNODE(folder,["+np+"]) and contains(file.*,'"+contentValue.trim()+"')";
			
			//String sql = "SELECT * FROM [vfcr:resource] AS folder WHERE ISDESCENDANTNODE(folder,["+np+"]) and contains([jcr:mimeType], 'pdf')";
			
			//String sql = "SELECT * FROM [vfcr:resource] AS folder WHERE ISDESCENDANTNODE(folder,["+np+"]) and contains(*, 'pdf')";
			String sql = "SELECT * FROM [vfcr:resource] AS folder WHERE ISDESCENDANTNODE(folder,["+np+"]) and contains(*, '"+contentValue.trim()+"')";
			
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	}

	@Override
	public List<TextContent> selectTextContentsByEncoding(BaseContentObject contentObject, String encodingValue)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();			
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [jcr:encoding] LIKE '%"+encodingValue.trim()+"%'";
			return selectTextContentsBySQL2(contentObject,sql);					
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}		
	}
	
	@Override
	public List<BinaryContent> selectBinaryContentsByMimeType(BaseContentObject contentObject, String mimeTypeValue)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [jcr:mimeType] LIKE '%"+mimeTypeValue.trim()+"%'";
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}
	
	@Override
	public List<TextContent> selectTextContentsByMimeType(BaseContentObject contentObject, String mimeTypeValue)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();			
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [jcr:mimeType] LIKE '%"+mimeTypeValue.trim()+"%' and [jcr:encoding] <>''";
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	}
	
	@Override
	public List<BinaryContent> selectBinaryContentsByTitle(BaseContentObject contentObject, String titleValue)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:contentName] LIKE '%"+titleValue.trim()+"%'";			
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	} 
	
	@Override
	public List<TextContent> selectTextContentsByTitle(BaseContentObject contentObject, String titleValue)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:contentName] LIKE '%"+titleValue.trim()+"%' and [jcr:encoding] <>''";
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	} 
	
	private List<BinaryContent> selectBinaryContentsBySQL2(BaseContentObject contentObject, String sql2Str)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {
			QueryManager qm = jmpl.getJcrSession().getWorkspace().getQueryManager();
			Query q = qm.createQuery(sql2Str, Query.JCR_SQL2);
			QueryResult result = q.execute();			
			NodeIterator nodeIterator=result.getNodes();
			List<BinaryContent> bcl=new ArrayList<BinaryContent>();			
			while(nodeIterator.hasNext()){				
				Node n=nodeIterator.nextNode();							
				JCRBinaryContentImpl jcrBco= (JCRBinaryContentImpl)ContentComponentFactory.createBinaryContentObject();
				jcrBco.setBinaryContainerNode(n.getParent());
				jcrBco.setContentBinary(n.getProperty("jcr:data").getBinary());
				jcrBco.setMimeType(n.getProperty("jcr:mimeType").getString()); 
				jcrBco.setContentName(n.getProperty("vfcr:contentName").getString());           		
           		jcrBco.setContentDescription(n.getProperty("vfcr:contentDescription").getString());  
           		if(n.hasProperty("vfcr:createDate")){
           			jcrBco.setCreated(n.getProperty("vfcr:createDate").getDate());               			
           		}
           		if(n.hasProperty("vfcr:creator")){
           			jcrBco.setCreatedBy(n.getProperty("vfcr:creator").getString());               			
           		}               		
           		if(n.hasProperty("vfcr:lastUpdatePerson")){
           			jcrBco.setLastModifiedBy(n.getProperty("vfcr:lastUpdatePerson").getString()); 
           		}   
           		if(n.hasProperty("vfcr:lastUpdateDate")){
           			jcrBco.setLastModified(n.getProperty("vfcr:lastUpdateDate").getDate()); 
           		}
           		bcl.add(jcrBco);							
			}
			return bcl;			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	}	

	private List<TextContent> selectTextContentsBySQL2(BaseContentObject contentObject, String sql2Str)throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {								
			QueryManager qm = jmpl.getJcrSession().getWorkspace().getQueryManager();
			Query q = qm.createQuery(sql2Str, Query.JCR_SQL2);
			QueryResult result = q.execute();			
			NodeIterator nodeIterator=result.getNodes();
			List<TextContent> bcl=new ArrayList<TextContent>();			
			while(nodeIterator.hasNext()){				
				Node n=nodeIterator.nextNode();	       	
        		JCRTextContentImpl jcrBco= (JCRTextContentImpl)ContentComponentFactory.createTextContentObject(); 
        		jcrBco.setBinaryContainerNode(n.getParent());
        		jcrBco.setEncoding(n.getProperty("jcr:encoding").getString());
        		jcrBco.setContentBinary(n.getProperty("jcr:data").getBinary());
				jcrBco.setMimeType(n.getProperty("jcr:mimeType").getString()); 
				jcrBco.setContentName(n.getProperty("vfcr:contentName").getString());           		
           		jcrBco.setContentDescription(n.getProperty("vfcr:contentDescription").getString()); 
           		if(n.hasProperty("vfcr:createDate")){
           			jcrBco.setCreated(n.getProperty("vfcr:createDate").getDate());               			
           		}
           		if(n.hasProperty("vfcr:creator")){
           			jcrBco.setCreatedBy(n.getProperty("vfcr:creator").getString());               			
           		}               		
           		if(n.hasProperty("vfcr:lastUpdatePerson")){
           			jcrBco.setLastModifiedBy(n.getProperty("vfcr:lastUpdatePerson").getString()); 
           		}   
           		if(n.hasProperty("vfcr:lastUpdateDate")){
           			jcrBco.setLastModified(n.getProperty("vfcr:lastUpdateDate").getDate()); 
           		} 
            	bcl.add(jcrBco);	
			}
			return bcl;					
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}		
	}

	@Override
	public List<BinaryContent> selectBinaryContentsByCreator(BaseContentObject contentObject, String creatorValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:creator] LIKE '%"+creatorValue.trim()+"%'";
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}

	@Override
	public List<TextContent> selectTextContentsByCreator(BaseContentObject contentObject, String creatorValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();			
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:creator] LIKE '%"+creatorValue.trim()+"%' and [jcr:encoding] <>''";
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	}

	@Override
	public List<BinaryContent> selectBinaryContentsByLastUpdater(BaseContentObject contentObject,String lastUpdaterValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:lastUpdatePerson] LIKE '%"+lastUpdaterValue.trim()+"%'";
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}

	@Override
	public List<TextContent> selectTextContentsByLastUpdater(BaseContentObject contentObject, String lastUpdaterValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();			
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:lastUpdatePerson] LIKE '%"+lastUpdaterValue.trim()+"%' and [jcr:encoding] <>''";
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	}

	@Override
	public List<BinaryContent> selectBinaryContentsByDescription(BaseContentObject contentObject,String descriptionValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:contentDescription] LIKE '%"+descriptionValue.trim()+"%'";
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}

	@Override
	public List<TextContent> selectTextContentsByByDescription(BaseContentObject contentObject, String descriptionValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();			
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"]) and [vfcr:contentDescription] LIKE '%"+descriptionValue.trim()+"%' and [jcr:encoding] <>''";
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			throw cpe;
		}
	}

	@Override
	public List<BinaryContent> selectBinaryContentsByCreateDate(BaseContentObject contentObject, Date fromDateValue,Date toDateValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"])";
			
			if(fromDateValue!=null){				
				Calendar cal= Calendar.getInstance();
				cal.setTime(fromDateValue);
				String fromDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:createDate] >= CAST('"+fromDateStringValue+"' AS DATE)";
			}
			if(toDateValue!=null){
				Calendar cal= Calendar.getInstance();
				cal.setTime(toDateValue);
				String toDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:createDate] <= CAST('"+toDateStringValue+"' AS DATE)";				
			}
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}

	@Override
	public List<TextContent> selectTextContentsByCreateDate(BaseContentObject contentObject, Date fromDateValue,Date toDateValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"])";
			
			if(fromDateValue!=null){				
				Calendar cal= Calendar.getInstance();
				cal.setTime(fromDateValue);
				String fromDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:createDate] >= CAST('"+fromDateStringValue+"' AS DATE)";
			}
			if(toDateValue!=null){
				Calendar cal= Calendar.getInstance();
				cal.setTime(toDateValue);
				String toDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:createDate] <= CAST('"+toDateStringValue+"' AS DATE)";				
			}
			
			sql=sql+" and [jcr:encoding] <>''";			
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}

	@Override
	public List<BinaryContent> selectBinaryContentsByLastUpdateDate(BaseContentObject contentObject, Date fromDateValue,Date toDateValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"])";
			
			if(fromDateValue!=null){				
				Calendar cal= Calendar.getInstance();
				cal.setTime(fromDateValue);
				String fromDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:lastUpdateDate] >= CAST('"+fromDateStringValue+"' AS DATE)";
			}
			if(toDateValue!=null){
				Calendar cal= Calendar.getInstance();
				cal.setTime(toDateValue);
				String toDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:lastUpdateDate] <= CAST('"+toDateStringValue+"' AS DATE)";				
			}
			return selectBinaryContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}

	@Override
	public List<TextContent> selectTextContentsByLastUpdateDate(BaseContentObject contentObject, Date fromDateValue,Date toDateValue) throws ContentReposityException {
		JCRContentObjectImpl jmpl=(JCRContentObjectImpl)contentObject;
		try {			
			String np=jmpl.getJcrNode().getPath();				
			String sql = "SELECT * FROM [vfcr:resource] AS folder where ISDESCENDANTNODE(folder,["+np+"])";
			
			if(fromDateValue!=null){				
				Calendar cal= Calendar.getInstance();
				cal.setTime(fromDateValue);
				String fromDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:lastUpdateDate] >= CAST('"+fromDateStringValue+"' AS DATE)";
			}
			if(toDateValue!=null){
				Calendar cal= Calendar.getInstance();
				cal.setTime(toDateValue);
				String toDateStringValue=ValueFactoryImpl.getInstance().createValue(cal).getString();				
				sql=sql+" and [vfcr:lastUpdateDate] <= CAST('"+toDateStringValue+"' AS DATE)";				
			}
			sql=sql+" and [jcr:encoding] <>''";			
			return selectTextContentsBySQL2(contentObject,sql);			
		} catch (RepositoryException e) {
			ContentReposityDataException cpe=new ContentReposityDataException();
			cpe.initCause(e);
			e.printStackTrace();
			throw cpe;
		}
	}	
}
