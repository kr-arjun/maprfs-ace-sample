# maprfs-ace-sample
A Simple Java program to illustrate basic MapR FS ACE operations.

## Getting Started

Clone the repository and build it using maven.

```
git clone git@github.com:kr-arjun/maprfs-ace-sample.git
cd maprfs-ace-sample/
mvn clean install
```
## Usage


#### 1) MapR ACE Demo:

Sample program to illustrate setAce ,getAce and delAce operation on test directory.

```
java -cp $(hadoop classpath):target/maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceDemo
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
#### 2) MapR Ace Client:

Simple Ace client program to do Ace operations on path passed as argument.

##### Usage:


Set Ace operation:

```
java -cp $(hadoop classpath):maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceClient \
-maprfsuri maprfs:/// -aceOp setace -path <path> -aceExpr <ace expression> \
-preserveModeBits <true/false> -setInherit <true/false> -recursive <true/false>
```
Get Ace operation

```
java -cp $(hadoop classpath):maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceClient \
-maprfsuri maprfs:/// -aceOp getace -path <path>
```

Delete Ace operation:

```
java -cp $(hadoop classpath):maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceClient \
-maprfsuri maprfs:/// -aceOp delace -path <path>
```
##### Sample:

```
$ java -cp $(hadoop classpath):maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceClient \
-maprfsuri maprfs:/// -aceOp setace -path /tmp/ace_test_dir -aceExpr "rf:u:mapr|u:root,wf:u:mapr|u:arjun,ac:u:mapr|u:arjun,dc:u:mapr|u:arjun,ld:u:mapr|u:arjun,rd:u:mapr|u:arjun" 
-preserveModeBits true -setInherit true -recursive true


Ace for path:/tmp/ace_test_dir has been set successfully! status :0

Current Ace values for path - /tmp/ace_test_dir
readfile : u:mapr | u:root
writefile : u:mapr | u:arjun
executefile :
readdir : u:mapr | u:arjun
addchild : u:mapr | u:arjun
deletechild : u:mapr | u:arjun
lookupdir : u:mapr | u:arjun
$


$java -cp $(hadoop classpath):maprfs-ace-sample-0.0.1-SNAPSHOT.jar com.mapr.MapRAceClient \
-maprfsuri maprfs:/// -aceOp getace -path /tmp/ace_test_dir
17/09/06 22:25:40 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable

Ace settings for path - /tmp/ace_test_dir
fileread: u:mapr | u:root
filewrite: u:mapr | u:arjun
fileexecute:
readdir: u:mapr | u:arjun
addchild: u:mapr | u:arjun
deletechild: u:mapr | u:arjun
lookupdir: u:mapr | u:arjun
inherit: true
mode: -w-------
$
```


