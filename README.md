# Mars HR

Mars HR - A project to learn alfresco basics

## Case

A reputed consultancy firm which recently extended its services to Mars is facing some challenges out of its home planet.

One of its major challenge is how to deal with all the CVs they receive from Earth.

After they receive a CV, a HR person first checks its content and screens out unsuitable candidates.

The remaining CVs are checked by a technical person like an archtect.

If the technical person approved the CV, a HR person will call the job candidate for an phone interview.

After the phone interview he or she may decide whether to call the candidate for a 2nd interview or not.

## Solution

The problem of getting the CV can be handled by allowing job candidates to upload their CV through a website.

Here, I've created a spring boot application which uploads the received CV to an alfresco repository.

It will create a document in site/CVs folder.

HR workflow can be created as a custom workflow in alfresco.

The technical people, who may check the CV, need to be added under a group, so the relevant task can be assigned only to them.

I've created a workflow with Activiti Eclipse BPMN 2.0 Designer for Eclipse. It has to be deployed into alfresco.

After the script, which executes the workflow, need to be uploaded to alfresco and a folder rule need to be set for the CVs folder, so that it will start the script.

Note that, before configuring and running the application, I'll describe how to deploy work flow and configure alfresco.

### Workflow

![Mars HR Workflow](https://github.com/username/repository/charakajg/marshr/master/workflow.png)

### Configure Alfresco

1. Copy all the files from **copy-to-alfresco/extensions** folder into **{ALFRESCO PATH}/tomcat/shared/classes/alfresco/extension** (The extension folder may be different, if you use a different server instead of **tomcat**.
2. Start/Restart alfresco. This will deploy workflow into alfresco as bootstrap data. (Please refer references for more information)
3. Create a site in alfresco (If the site name is different from **mars-hr**, change **cv-site-name** property as described in next section).
4. Create a group in alfresco called **MarsHRTechies**. The technical people, who will be carry out technical check, should be added under this group. (Make sure you add at least one user under this)
5. Upload **start-mars-hr-workflow.js** file from **copy-to-alfresco/scripts** folder into **Repository>Data Dictionary>Scripts** (This is the script which executes the hr workflow)
6. Create a folder named **CVs** under the site you created.
7. Add a folder rule to the **site/CVs** folder so that it'll excute the **start-mars-hr-workflow.js** when a new document is added.

### Configure CV Upload Application

Please configure config.properties file accordingly:

**cv-site-name** - Site name

**host** - Alfresco host path

**username** - Username for the alfresco user through whose account the document will be created

**password** - Password

The application will be running on port 8888. If it's already occupied, please change **server.port** property in application.properties.

The uploaded document will be uploaded to **CVs** folder under the site.

### How to Run

1. Make sure alfresco is running.
2. Make sure it's properly configured. (site, workflow, group, script and folder rule)
3. Make sure **config.properties** and **application.propoerties** are properly configured.
4. Execute following commands (assuming maven has been installed):
	* mvn package 
	* java -jar target/marshr-0.1.jar

If this need to be run as a WAR file, refer this:

http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#build-tool-plugins-maven-packaging

### Uploading a CV

1. Open http://localhost:8888/ (or relevant port)
2. Select your PDF.
3. Click **Upload CV**
4. If succeed, a PDF must be created in the alfresco repository (under site_name/CVs folder) 

### Issues

Q.**Why can't I run the application on port X?**

A.Make sure port is not already occupied. If it is, change **server.port** property in application.properties as stated above.

Q.**Why doesn't the workflow doesn't start automatically?**

A.Make sure that the folder rule is added and that it executes the **start-mars-hr-workflow.js** script.

Q.**Why the script doesn't appear, when I try to add the folder rule?**

A.Make sure the script is in **Repository>Data Dictionary>Scripts** folder. Otherwise it won't appear.

Q.**Why nobody gets the **Technical Check** task?**

A.The **Technical Check** task need to be claimed by a user who belongs to the **MarsHRTechies** group. If no user is in that group, the task won't appear on any active tasks list.

## References

This was tested with **alfresco-5.0.b (community edition)** running on **ubuntu 14 LTS**. The application server and datbase were default postgresql and tomcat versions that comes with alfresco. If something need changes for any other reason, please refer articles and repositories below which have been used for the development of this solution.

http://ecmarchitect.com/alfresco-developer-series-tutorials/workflow/tutorial/tutorial.html

https://wiki.alfresco.com/wiki/WorkflowSample_Lifecycle

https://github.com/Alfresco/community-edition/tree/master/projects/repository/config/alfresco/extension

https://github.com/spring-guides/gs-uploading-files.git

https://github.com/jpotts/alfresco-api-java-examples.git
