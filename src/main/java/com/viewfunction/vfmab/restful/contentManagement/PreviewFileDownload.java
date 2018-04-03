package com.viewfunction.vfmab.restful.contentManagement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viewfunction.contentRepository.util.RuntimeEnvironmentHandler;

public class PreviewFileDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {			
		String tempFileRootPath=RuntimeEnvironmentHandler.getApplicationRootPath()+"TEMP/PREVIEWFILE_TEMP/";	
		String fileName=request.getParameter("fileName");
		String filePath=tempFileRootPath+fileName;		         
		File downloadFile=new File(filePath);
		if (downloadFile.exists()) {	
			response.setContentType("application/octet-stream");
		    Long length=downloadFile.length();
		    response.setContentLength(length.intValue());
		    fileName = URLEncoder.encode(downloadFile.getName(), "UTF-8");
		    response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
		             
		    ServletOutputStream servletOutputStream=response.getOutputStream();
		    FileInputStream fileInputStream=new FileInputStream(downloadFile);
		    BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
		    int size=0;
		    byte[] b=new byte[4096];
		    while ((size=bufferedInputStream.read(b))!=-1) {		                
		    	servletOutputStream.write(b, 0, size);
		    }
		    servletOutputStream.flush();
		    servletOutputStream.close();
		    bufferedInputStream.close();
		}
	}	
}