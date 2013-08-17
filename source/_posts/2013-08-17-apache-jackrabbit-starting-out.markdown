---
layout: post
title: "Apache Jackrabbit - Starting From Scratch"
date: 2013-08-17 12:39
comments: true
categories: [JCR, Apache Jackrabbit, Starting From Scratch]
---
This tutorial was designed to get the reader using JSR-170's an JSR-283's core APIs by using Apache Jackrabbit. These series of tutorials build on one another, so this one will form the foundation of the tutorials to come.

Audience
-----
Typically, this is Lab #1 in a classroom environment, however anyone that wishes can use this tutorial. Most people performing this tutorial have already had some presented instruction and an overview of Apache Jackrabbit and the JCR specifications.

Requirements
-----
-	Apache Maven
-	IDE Of Choice (Preferably IntelliJ IDEA)
-   Internet Connection (For Maven Repositories)

What We Are Going To Do
-----
Our intent with this lab section is just to get you kickstarted on your exploration of Apache Jackrabbit. Before we begin, it’s important to note that Jackrabbit is not your typical server platform. In a typical database platform you would download some packages and run them which would start up a server. Then in a different project you’d probably fire up some JDBC driver and write code that connects to that server and does stuff. Jackrabbit wasn’t exactly built this way, its really an API that abstracts away how things are stored and only concentrates on hierarchal content management.

In order to get a base project running that will form the foundation of the rest of our exploration we need to do the following:

1. Generate A Quick Start Project With Maven
2. Add Some Dependencies To The POM
3. Start The Repository For The First Time
4. Ask The Repository About Some Of Its Features

## Generate A Quick Start Project With Maven
Just like our usual lab series, we are going to start with a quick start project and modify it to support what we need it to do. The goal is to get you comfortable with Maven and building out your own project stuctures. To get started with a basic quickstart project lets use the following command line:

``` bash New Maven Quick Start
mvn archetype:generate -DgroupId=org.codeaffectionado.training -DartifactId=jackrabbit-tutorial -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

Now lets open up our IDE of choice and import this project. Before we get to deep into this tutorial lets add a plugin that will allow us to run our project using Maven. This will just speed some things up for us later. In our POM lets add the following section to the root of the project:

``` xml Maven Execute Plugin
<build>
      <plugins>
          <plugin>
              <groupId>org.codehaus.mojo</groupId>
              <artifactId>exec-maven-plugin</artifactId>
              <version>1.2.1</version>
              <executions>
                  <execution>
                      <goals>
                          <goal>java</goal>
                      </goals>
                  </execution>
              </executions>
              <configuration>
                  <mainClass>org.codeaffectionado.training.App</mainClass>
              </configuration>
          </plugin>
      </plugins>
  </build>
```

Now if go to the command line and do a **mvn compile exec:java** we can run our application and watch it happily report:

``` bash
[INFO] --- exec-maven-plugin:1.2.1:java (default-cli) @ jackrabbit-tutorial ---
Hello World!
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

This is important because running java classes or jar projects can be quite a chore when we are using Maven as our build lifecycle. This keeps us from having to go to our target directory and run our jars by hand, even though you are more than welcome to so if you wish. It will require you to further bundle your dependencies in the jar so you don't have to worry about classpath problems.

## Add Some Dependencies To The POM
All we really need to perform some JCR programming is include two new dependencies into our POM like so:

``` xml Dependencies Needed For Apache Jackrabbit
<dependency>
    <groupId>javax.jcr</groupId>
    <artifactId>jcr</artifactId>
    <version>2.0</version>
</dependency>
<dependency>
	<groupId>org.apache.jackrabbit</groupId>
	<artifactId>jackrabbit-core</artifactId>
	<version>2.5.0</version>
</dependency>
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-log4j12</artifactId>
	<version>1.6.1</version>
</dependency>
```

## Start The Repository For The First Time
So starting the repository may not be that obvious at present time, but its really easy to do. Let's go into our main method and add the following lines to the main method:

``` java Starting Up Our Repository
public static void main( String[] args ) throws RepositoryException {
    Repository repository = new TransientRepository();
    Session session = repository.login(
            new SimpleCredentials("admin","admin".toCharArray()));

    System.out.println("Hello World!");

    session.logout();
}
```
So whats all this? Well its really pretty easy to explain. We talk about in class that the standard flow of calling the JCR revolves around:

1. Getting a reference to the Repository
2. Using the Repository to login and get a Session
3. Use The Session To Get A Node or The Root Node
4. Do Some Work
5. Save The Session
6. Logout

In our code above we just did 1,2 and 6. Just enough to get us a repository built. But lets look at what we accomplished in this code.

{% blockquote %}
Repository repository = new TransientRepository();
{% endblockquote %}

Apache Jackrabbit has an implementation of [Repository](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Repository.html?is-external=true) (which is an interface BTW) called [TransientRepository](http://jackrabbit.apache.org/api/2.2/org/apache/jackrabbit/core/TransientRepository.html). It's considered Transient because it starts up when its needed and shuts down where there are no more sessions connected to it. This is the primary repository we will be using with our samples and represents the most common repository used with other applications.

{% blockquote %}
Session session = repository.login(
            new SimpleCredentials("admin","admin".toCharArray()));
{% endblockquote %}

Here we are using the repository to login. Our login is just using a set of Credentials (another interface). Currently there are only guest and simple credentials included with the version we are using. Thats ok though, most of the time the JCR is a part of another application or framework. It isn't connected to from the outside often.

{% blockquote %}
session.logout();
{% endblockquote %}

Self explanitory, but this is us logging out of our session. It's somewhat redundant here, but good practice. Because, when the primary thread dies it will expires anyway and the repository will shutdown.

Now run it with our new Maven sequence **mvn compile exec:java** and notice the results that stream by. When it finishes refresh the root of your project in your IDE and take a look now. It appears some directories were created for you and some new config files where put down automatically. Basically Jackrabbit created a respository in your project root and set it up for you. This includes storing everything in an Apache Derby database instance it created for you. 

## Ask The Repository About Some Of Its Features
So lets ask the repository about some of its features. To get information about the repository and what it supports back. The Repository interface contains some constants that can be used to query information from the repository. Lets modify our example and just ask some questions to the repository itself. Primarily lets see what its vendor, name and version are:

``` java Asking The Repository For Information
String vendor = repository.getDescriptor(Repository.REP_VENDOR_DESC);
String product = repository.getDescriptor(Repository.REP_NAME_DESC);
String version = repository.getDescriptor(Repository.REP_VERSION_DESC);

System.out.println("We Are Using "+vendor+" "+product+" version "+version);
```

Now run your **mvn compile exec:java** again and watch the repositories response:

``` bash
We Are Using Apache Software Foundation Jackrabbit version 2.5.0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

There are some other things that the repository can tell you. I would highly recomment checking out the [Repository's interface documentation](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Repository.html?is-external=true) and trying some of the other constants. Experiment and get comfortable with this flow. We will be using it more and more as time goes on.


