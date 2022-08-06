pubsub-spark
=============
A simple piece of code that connects to Google Cloud Pubsub and prints messages from a topic.  

It works when run as a standalone program. But when run using spark-submit, hangs without consuming any messages 
(although it can authenticate and manage to connect to the subscription).

# Prerequisites
1. A Google account that can work with gcloud
1. A Gcloud Project and Subscription to which you have access (code has placeholders for this)

# It runs when used standalone. Steps below
1. Log in Google Cloud auth to set up your username based credentials via a webflow. More details in 
[their docs](https://cloud.google.com/sdk/gcloud/reference/auth/application-default/login)  
```
$ gcloud auth application-default login
```
2. Set up correct project id and subscription id in the main() method of PubsubConsumer.java
3. mvn clean compile
4. mvn exec:java -Dexec.classpathScope="compile" -Dexec.mainClass=spark.pubsub.example.PubsubConsumer

On the console you should see some log messages followed by messages from whichever topic subscription you've
provided in earlier step.

# It fails when used with spark-submit
1. Assuming you've downloaded Spark, make sure your conf/spark-defaults.conf file within the Spark folder has 
the following settings. This makes Spark override its version of Google Guava and use the one needed for Cloud Pubsub:  
```
spark.driver.extraClassPath <insert_path_to_m2_repository>/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:<insert_path_to_m2_repository>/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar
spark.executor.extraClassPath <insert_path_to_m2_repository>/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:<insert_path_to_m2_repository>/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar
```
2. ```mvn clean package``` to generate the basic JAR for this project.
3. To make all the rest of the dependencies available, use the below command to generate a comma-separated list of Jars:
```
$ mvn  dependency:build-classpath | grep cloud-pubsub | tr ':' ','
``` 
4. Copy the JAR created by the earlier step to the Spark folder
5. Run spark-submit as below, by inserting the comma-separated list of jar file paths appropriately
```
$ bin/spark-submit --jars <comma-separated list of jar files> --class spark.pubsub.example.PubsubConsumer  pubsub-spark-1.0-SNAPSHOT.jar
```
You will see a message that it connected to the topic and is listening for messages, but no messages are actually received.

