package charakajg.marshr.controller;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import charakajg.marshr.service.AlfrescoService;
import charakajg.marshr.util.Config;

/**
 * Controller for handling CV upload
 * References: https://github.com/spring-guides/gs-uploading-files.git
 * 
 * @author charaka
 *
 */
@Controller
public class UploadCVController {
	//refer config.properties
	private static final String PROP_CV_SITE_NAME = "cv-site-name";
	private static final String PROP_CV_FOLDER_NAME = "cv-folder-name";
	private static final String PROP_CV_FILE_TYPE = "cv-file-type";
	
	@Autowired
	private AlfrescoService alfrescoService;
	
	//handles CV upload
	@RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody String handleCVUpload(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
    			String rootFolderId = alfrescoService.getRootFolderId(getCVSiteName());
    			Folder cvFolder = alfrescoService.createFolder(rootFolderId, getCVFolderName());
    			
    			alfrescoService.createDocument(cvFolder, file, getCVFileType(),null);
                return "Successfully uploaded!";
            } catch (Exception e) {
            	e.printStackTrace();
                return "Failed to upload => " + e.getMessage();
            }
        } else {
            return "Failed to upload => The file was empty.";
        }
    }

	//returns CV Site Name
	public String getCVSiteName() {
		return Config.getConfig().getProperty(PROP_CV_SITE_NAME); 
	}

	//returns CV Folder Name
	public String getCVFolderName() {
		return Config.getConfig().getProperty(PROP_CV_FOLDER_NAME);		 		
	}
	
	//returns CV File Type
	public String getCVFileType() {
		return Config.getConfig().getProperty(PROP_CV_FILE_TYPE); 
	}
}
