# Mars HR

Mars HR - A project to learn alfresco basics

## Case

A reputed consultancy firm which recently extended its services to Mars is facing a lot of challenges out of home planets.

One of its major challenge is how to deal with all the CVs they receive from Earth.

After they receive a CV, a HR person first checks its content and screens out unsuitable candidates.

The remaining CVs are checked by a technical person like an archtect.

If the techy okay'd the CV, a HR person will calls the job candidate for an phone interview.

After the phone interview he or she may decide whether to call the candidate for a 2nd interview or not.

## Solution

The problem of getting the CV can be handled by allowing job candidates to upload their CV through a website.

Here, I've created a spring boot application which uploads the received CV to alfresco repository.

It will create a document in site/CVs folder.


### References:

https://github.com/spring-guides/gs-uploading-files.git

https://github.com/jpotts/alfresco-api-java-examples.git

Please configure config.properties file accordingly:

cv-site-name - Site name

host - Alfresco host path

username - Username for the alfresco user through whose account the document will be created

password - Password

The application will be running on port 8888. If it's already occupied, please change server.port property in application.properties.

### How to Run

1. Make sure alfresco is running.
2. Make sure config.properties and application.propoerties are properly configured
3. Execute following commands (assuming maven has been installed):
	* mvn package 
	* java -jar target/marshr-0.1.jar

If this need to be run as a WAR file, refer this:

http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#build-tool-plugins-maven-packaging

## Then?

A rule can be added to the site/CVs folder to start a simple workflow to Approve and Reject the file.

Based on outcome, the file can be moved to the revelant folder.

A better solution would be to create a custom workflow and deploy the process definition file to alfresco.

After that, a rule can be added to the site/CVs folder to execute a script that runs the workflow whenever a document is added.

I'll update the project, once I learn how to create and deploy a custom workflow to alfresco.
