/*
 *   Copyright 2009-2010 Rustici Software. Licensed under the
 *   Educational Community License, Version 2.0 (the "License"); you may
 *   not use this file except in compliance with the License. You may
 *   obtain a copy of the License at
 *   
 *   http://www.osedu.org/licenses/ECL-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an "AS IS"
 *   BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *   or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */


package in.xebia.poc;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUploadUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUploadUtils.class);
    
	public static boolean parseFileUploadRequest(HttpServletRequest request, File outputFile, Map<String, String> params) throws Exception
	{
		log.debug("Request class? " + request.getClass().toString());
		log.debug("Request is multipart? " + ServletFileUpload.isMultipartContent(request));
		log.debug("Request method: " + request.getMethod());
		log.debug("Request params: ");
		for (Object key : request.getParameterMap().keySet()){
		    log.debug((String)key);
		}
		log.debug("Request attribute names: ");
		
		boolean filedataInAttributes = false;
		Enumeration attrNames = request.getAttributeNames();
		while(attrNames.hasMoreElements()){
		    String attrName = (String)attrNames.nextElement();
		    log.debug(attrName);
		    if("filedata".equals(attrName)){
		        filedataInAttributes = true;
		    }
		}
		
		if(filedataInAttributes){
		    log.debug("Found filedata in request attributes, getting it out...");
		    log.debug("filedata class? " + request.getAttribute("filedata").getClass().toString());
		    FileItem item = (FileItem)request.getAttribute("filedata");
		    item.write(outputFile);
		    for (Object key : request.getParameterMap().keySet()){
		        params.put((String)key, request.getParameter((String)key));
		    }
		    return true;
		}
		
		
		/*ServletFileUpload upload = new ServletFileUpload();
		//upload.setSizeMax(Globals.MAX_UPLOAD_SIZE);
		FileItemIterator iter = upload.getItemIterator(request);
		while(iter.hasNext()){
			FileItemStream item = iter.next();
			InputStream stream = item.openStream();
			
			//If this item is a file
			if(!item.isFormField()){
			    
			    log.debug("Found non form field in upload request with field name = " + item.getFieldName());
			    
			    String name = item.getName();
			    if(name == null){
			        throw new Exception("File upload did not have filename specified");
			    }
			    
                // Some browsers, including IE, return the full path so trim off everything but the file name
                name = getFileNameFromPath(name);
                 
				//Enforce required file extension, if present
				if(!name.toLowerCase().endsWith( ".zip" )){
					throw new Exception("File uploaded did not have required extension .zip");
				}
				
		        bufferedCopyStream(stream, new FileOutputStream(outputFile));
			}
			else {
				params.put(item.getFieldName(), Streams.asString(stream));
			}
		}
		return true;*/
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		List /* FileItem */ items = upload.parseRequest(request);
		
		// Process the uploaded items
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = (FileItem) iter.next();

		    if (!item.isFormField()) {
		        log.debug("Found non form field in upload request with field name = " + item.getFieldName());
                
                String name = item.getName();
                if(name == null){
                    throw new Exception("File upload did not have filename specified");
                }
                
                // Some browsers, including IE, return the full path so trim off everything but the file name
                name = getFileNameFromPath(name);
                
                item.write(outputFile);
		    } else {
		        params.put(item.getFieldName(), item.getString());
		    }
		}
		return true;
	}
	
	public static String getFileNameFromPath (String name) {
		
		if (name.indexOf("\\") > -1)
			name = name.substring(name.lastIndexOf("\\")+1);
		if (name.indexOf("/") > -1)
			name = name.substring(name.lastIndexOf("/")+1);
		
		return name;
	}
	
	//Copy inStream to outStream using Java's built in buffering
	public static boolean bufferedCopyStream(InputStream inStream, OutputStream outStream) throws Exception {
		BufferedInputStream bis = new BufferedInputStream( inStream );
		BufferedOutputStream bos = new BufferedOutputStream ( outStream );
		while(true){
			int data = bis.read();
			if (data == -1){
				break;
			}
			bos.write(data);
		}
		bos.flush();
		bos.close();
		return true;
	}
}
