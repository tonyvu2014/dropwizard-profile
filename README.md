### Status
[![Build Status](https://travis-ci.org/tonyvu2014/dropwizard-profile.svg?branch=master)](https://travis-ci.org/tonyvu2014/dropwizard-profile)



This is a a sample project which uses Dropwizard to create several REST APIs. There are 4 APIs: Creating a profile, viewing list of profile, viewing another profile and viewing another profile's recent visit history. The recent visit history only shows up to 10 last visits and only shows visits which are not more than 10 days old. It demonstrates basic concepts in Dropwizard from setting up, creating database with Hibernate, basic authentication, serializing and deserializing, healthcheck and metrics, unit testing and job scheduling with Sundial. The profile_database.docx explains the rationale behind the database design.

Instructions to run the profile application:

* Note: The application is configured to create log file and archive log file in /tmp folder, 
so please change to folder of your choice in profile.yml configuration file and make sure that 
you have the write access to that folder. The test is run automatically after a push with Travis CI.

1) From the project root folder, run 
   
     mvn package

2) Setup and create schema with
    
     java -jar target/profile-0.0.1-SNAPSHOT.jar db migrate profile.yml

3) Run the application on jetty server

     java -jar target/profile-0.0.1-SNAPSHOT.jar server profile.yml 

4) To create a new user profile with curl (This API is protected with basic authentication)

     curl -u admin:m0r1ng@ -H "Content-Type: application/json" -X POST -d '{"name":"Tony Vu","username":"tonyvu"}' http://localhost:8080/profile/create

   To view list of current users, from browser go to:

     http://localhost:8080/profile/list

   To simulate user with profile id 1 visiting user with profile id 2, go to:

     http://localhost:8080/profile/1/visit/2
   
   To simulate user with profile id 1 accessing recent view history of user with profile id 2, go to:

     http://localhost:8080/profile/1/viewRecentVisits/2
     
   To access admin portal where you can view metrics and healthcheck, go to:
    
     http://localhost:8081/
     
 * Note: The last 2 APIs have the time measure metrics added.
    
 * Note: The app also works with https on port 8443 and the admin portal works with https on port 8444. 
 For this project, a self-signed certificate is used.
 
The command mvn package also runs the unit test cases for the 2 APIs: The API to store the views 
of user profiles and the API to view the users who viewed this user’s profile in the past. There are
5 unit test cases in total. If any of the test cases does not pass, the build will fail.
   
   To package the application without unit tests, run:
   
     mvn package -Dmaven.test.skip=true
     
   To run the unit tests only, use:
    
     mvn test   
     
There is also a daily batch job (RecentVisitCleanUpJob.java) scheduled to run at 23:59:59 to remove expired
records from RecentVisit table. The job is scheduled automatically when the application starts. 
   
   To run the job manually, execute:
    
     curl -X POST "http://localhost:8081/tasks/startjob?JOB_NAME=RecentVisitCleanUpJob"
      
   To stop the job manually, execute:
   
     curl -X POST "http://localhost:8081/tasks/stopjob?JOB_NAME=RecentVisitCleanUpJob"   
   
   And to remove the job:
   
     curl -X POST "http://localhost:8081/tasks/removejob?JOB_NAME=RecentVisitCleanUpJob"
    
        
  
 

