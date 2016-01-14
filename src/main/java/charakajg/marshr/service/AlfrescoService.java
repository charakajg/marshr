package charakajg.marshr.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import charakajg.marshr.model.ContainerEntry;
import charakajg.marshr.model.ContainerList;
import charakajg.marshr.model.NetworkEntry;
import charakajg.marshr.model.NetworkList;
import charakajg.marshr.util.Config;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Service for alfresco and CMIS API related methods
 * References: https://github.com/jpotts/alfresco-api-java-examples.git
 *  
 * @author charaka
 *
 */
@Service
public class AlfrescoService {
	//refer config.properties
	private static final String PROP_HOST = "host";
	private static final String PROP_USERNAME = "username";
	private static final String PROP_PASSWORD = "password";
	
	public static final String CMIS_URL = "/public/cmis/versions/1.1/atom";
	public static final String SITES_URL = "/public/alfresco/versions/1/sites/";

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private HttpRequestFactory requestFactory;
	private Session cmisSession;
	private String homeNetwork;

	/**
	 * @param cmisSession
	 * @param parentFolderId
	 * @param folderName
	 * @return Folder
	 * 
	 * @author jpotts
	 * 
	 */
	public Folder createFolder(String parentFolderId, String folderName) {
		Session cmisSession = getCmisSession();        
		Folder rootFolder = (Folder) cmisSession.getObject(parentFolderId);
       
		Folder subFolder = null;
		try {
			// Making an assumption here that you probably wouldn't normally do
			subFolder = (Folder) cmisSession.getObjectByPath(rootFolder.getPath() + "/" + folderName);
			System.out.println("Folder already existed!");
		} catch (CmisObjectNotFoundException onfe) {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("cmis:objectTypeId",  "cmis:folder");
			props.put("cmis:name", folderName);
			subFolder = rootFolder.createFolder(props);
			String subFolderId = subFolder.getId();
			System.out.println("Created new folder: " + subFolderId);        	
		}        
       
		return subFolder;
	}
	
	/**
	 * Use the CMIS API to create a document in a folder
	 * 
	 * @param cmisSession
	 * @param parentFolder
	 * @param file
	 * @param fileType
	 * @param props
	 * @return
	 * @author jpotts
	 * @throws IOException 
	 * 
	 */
	public Document createDocument(Folder parentFolder,
								   MultipartFile file,
								   String fileType,
								   Map<String, Object> props)
			throws IOException {

		Session cmisSession = getCmisSession();

		// create a map of properties if one wasn't passed in
		if (props == null) {
			props = new HashMap<String, Object>();
		}

		// Add the object type ID if it wasn't already
		if (props.get("cmis:objectTypeId") == null) {
			props.put("cmis:objectTypeId",  "cmis:document");
		}
		
		String fileName = file.getOriginalFilename();
		
		// Add the name if it wasn't already
		if (props.get("cmis:name") == null) {
			props.put("cmis:name", fileName);
		}

		ContentStream contentStream = cmisSession.getObjectFactory().
				  createContentStream(
					fileName,
					file.getSize(),
					fileType,
					file.getInputStream()
				  );

		Document document = null;
		try {
			document = parentFolder.createDocument(props, contentStream, null);		
			System.out.println("Created new document: " + document.getId());
		} catch (CmisContentAlreadyExistsException ccaee) {
			document = (Document) cmisSession.getObjectByPath(parentFolder.getPath() + "/" + fileName);
			System.out.println("Document already exists: " + fileName);
		}
		
		return document;		
	}

	/**
	 * Gets a CMIS Session by connecting to the local Alfresco server.
	 * 
	 * @return Session
	 */
	public Session getCmisSession() {
		if (cmisSession == null) {
			// default factory implementation
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();
	
			// connection settings
			parameter.put(SessionParameter.ATOMPUB_URL, getAtomPubURL(getRequestFactory()));
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
			parameter.put(SessionParameter.USER, getUsername());
			parameter.put(SessionParameter.PASSWORD, getPassword());
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
	
			List<Repository> repositories = factory.getRepositories(parameter);
	
			cmisSession = repositories.get(0).createSession();
		}
		return this.cmisSession;
	}
	
	/**
	 * Use the REST API to find the documentLibrary folder for
     * the target site
	 * @return String
	 * 
	 * @author jpotts
	 * 
	 */
	public String getRootFolderId(String site) throws IOException {
		
        GenericUrl containersUrl = new GenericUrl(getAlfrescoAPIUrl() +
                                             getHomeNetwork() +
        		                             SITES_URL +
                                             site +
                                             "/containers");
        System.out.println(containersUrl);
        HttpRequest request = getRequestFactory().buildGetRequest(containersUrl);
        ContainerList containerList = request.execute().parseAs(ContainerList.class);
        String rootFolderId = null;
        for (ContainerEntry containerEntry : containerList.list.entries) {
        		if (containerEntry.entry.folderId.equals("documentLibrary")) {
        			rootFolderId = containerEntry.entry.id;
        			break;
        		}
        }
        return rootFolderId;
	}
	public String getHomeNetwork() throws IOException {
		if (this.homeNetwork == null) {
			GenericUrl url = new GenericUrl(getAlfrescoAPIUrl());
	        
			HttpRequest request = getRequestFactory().buildGetRequest(url);
			NetworkList networkList = request.execute().parseAs(NetworkList.class);
	        System.out.println("Found " + networkList.list.pagination.totalItems + " networks.");
	        for (NetworkEntry networkEntry : networkList.list.entries) {
	        	if (networkEntry.entry.homeNetwork) {
	        		this.homeNetwork = networkEntry.entry.id;
	        	}
	        }
	
	        if (this.homeNetwork == null) {
	        	this.homeNetwork = "-default-";
	        }
	
	        System.out.println("Your home network appears to be: " + homeNetwork);
		}
	    return this.homeNetwork;
	}
	

	
	/**
	 * Uses basic authentication to create an HTTP request factory.
	 * 
	 * @return HttpRequestFactory
	 */
	public HttpRequestFactory getRequestFactory() {
		if (this.requestFactory == null) {
    		this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
    			@Override
    			public void initialize(HttpRequest request) throws IOException {
    				request.setParser(new JsonObjectParser(new JacksonFactory()));
    				request.getHeaders().setBasicAuthentication(getUsername(), getPassword());
    			}
    		});
		}
		return this.requestFactory;	
	}
	
	public String getAlfrescoAPIUrl() {
		String host = Config.getConfig().getProperty(PROP_HOST); 
		return host + "/api/"; 		
	}
	
	public String getAtomPubURL(HttpRequestFactory requestFactory) {
		String alfrescoAPIUrl = getAlfrescoAPIUrl();
		String atomPubURL = null;
	
		try {
			atomPubURL = alfrescoAPIUrl + getHomeNetwork() + CMIS_URL;
		} catch (IOException ioe) {
			System.out.println("Warning: Couldn't determine home network, defaulting to -default-");
			atomPubURL = alfrescoAPIUrl + "-default-" + CMIS_URL;
		}
		
		return atomPubURL;
	}
	
	//returns alfresco username
	public String getUsername() {
		return Config.getConfig().getProperty(PROP_USERNAME); 
	}

	//returns alfresco password
	public String getPassword() {
		return Config.getConfig().getProperty(PROP_PASSWORD);		 		
	}
}
