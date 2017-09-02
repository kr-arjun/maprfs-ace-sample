# maprfs-ace-sample

A Simple Java program to illustrate basic MapR FS ACE operations.

**Usage:**

Clone this project, then:

```mvn clean install
java -cp $(hadoop classpath):target/maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceTest
```
You should see output like:

```
Creating path /tmp/ace_test_file for Ace Testing.

Ace for path:/tmp/ace_test_file has been set successfully!

Current Ace values for path - /tmp/ace_test_file
AccessType:READFILE : p
AccessType:WRITEFILE : u:mapr | g:mapr | u:root
AccessType:EXECUTEFILE :

Ace for path:/tmp/ace_test_file has been deleted successfully!

Current Ace values for path - /tmp/ace_test_file
AccessType:READFILE :
AccessType:WRITEFILE :
AccessType:EXECUTEFILE :
```
