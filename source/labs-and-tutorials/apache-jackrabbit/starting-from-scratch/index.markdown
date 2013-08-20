---
layout: page
title: "Apache Jackrabbit - Starting From Scratch"
date: 2013-08-19 14:31
comments: true
sharing: true
footer: true
indexer: true
---
## Purpose
This lab demonstrates JSR-170's an JSR-283's core APIs by using Apache Jackrabbit and Java. Adobe CQ and similar content management systems use these JSR specifications to store content. It's common to see design issues and code complexity develop in systems that use it mainly due to the scarcity of good educational resources. Having just basic knowledge of the JCR specification can make decisions much more solid on platforms that use it.

The larger the system that uses the JCR, the more layers of framework are put on top of the product. This means developer decisions on how to access a specific feature may be confusing or provide too many options to them. The purpose with this tutorial series is to show off the core features in a JSR compliant way hoping that knowing the standard way you will be less like to use a way that attach you to a specific product.

## Audience
The core audience is seated in a classroom environment. Readers performing this tutorial have just finished an overview of Apache Jackrabbit and the JCR specifications and are looking for examples of how to use them. Most will be at work or seated in an place where they can read from the tutorial page and code in their own editor.

## Overview
The intent with this lab (and the ones that follow) is to get everyone comfortable exploring Apache Jackrabbit. Apache Jackrabbit seems different from common server platforms in that using typical platforms you would download some packages and run them which would start up a server. In a different project you’d probably fire up some  drivers and/or write code that connects to that server to store data. Jackrabbit is really just an API that abstracts away how things are stored and only concentrates on hierarchical content storage.

Apache Jackrabbit is designed to be included with your project and not an appliance that stands on its own. It leaves up to the developer how to best use its API. Knowing that, let's start with a generic quick start project and include Apache Jackrabbit into a simple console application.

## Generate A Quick Start Project With Maven
Quick start projects are great starting points. Let's create one and modify it to support what we need it to do instead of using a pre-built archetype. This will help get us comfortable with Maven and building out your own project structures. To get started with a basic quick start project lets use the following command line:

``` bash New Maven Quick Start
mvn archetype:generate -DgroupId=org.codeaffectionado.training -DartifactId=jackrabbit-tutorial -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

Now lets open up our IDE and import this project (use whatever you like). In order to execute the main method of our console application in Maven, we will need to add a plugin that will allow us to run our project using Maven. This will just speed some things up for us later. In our POM lets add the following section to the root of the project:

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

Running java classes or jar projects can be quite a chore when we are using Maven as our build tool. This keeps us from having to go to our target directory and run our jars by hand, however if you are more comfortable running them that way, it should be fine.

## Add Some Dependencies To The POM
All we really need to perform some JCR programming is include three new dependencies into our POM like so:

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

_The javax.jcr library is needed to write code against the JCR. The jackrabbit-core library is the actual Jackrabbit software itself and slf4j is a requirements of Jackrabbit and is used for logging._

## Start The Repository For The First Time
So starting the repository may not be that obvious at present time, but its really easy to do. Let's go into our main method and add the following lines to the main method:

``` java Starting Up Our Repository

    Repository repository = new TransientRepository();

    Session session = repository.login(
            new SimpleCredentials("admin","admin".toCharArray()));

    System.out.println("Hello World!");

    session.logout();
```
So whats all this? Well its really pretty easy to explain. We talk about in class that the standard flow of calling the JCR revolves around:

1. Getting a reference to the Repository
2. Using the Repository to login and get a Session
3. Use The Session To Get A Node or The Root Node
4. Do Some Work
5. Save The Session
6. Logout

In our code above we just did 1,2 and 6. Just enough to get us a repository built. But lets look at what we accomplished in this code.

``` java
Repository repository = new TransientRepository();
```

Apache Jackrabbit has an implementation of [Repository](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Repository.html?is-external=true) (which is an interface BTW) called [TransientRepository](http://jackrabbit.apache.org/api/2.2/org/apache/jackrabbit/core/TransientRepository.html). It's considered Transient because it starts up when its needed and shuts down where there are no more sessions connected to it. This is the primary repository we will be using with our samples and represents the most common repository used with other applications.

``` java

Session session = repository.login(new SimpleCredentials("admin","admin".toCharArray()));
```

Here we are using the repository to login. Our login is using a set of Credentials (another interface) called SimpleCredentials. Currently there are only guest and simple credentials included with the version we are using. That's ok though, most of the time the JCR is a part of another application or framework. It isn't connected to from the outside.

``` java
session.logout();
```

Self explanatory, but this is how we log out of a session. It's somewhat redundant here, but good practice.

## First Run
Now run the application with the modified Maven command:

```
mvn compile exec:java
```

Notice the results that stream by? When it finishes refresh the root of your project in your IDE and take a look now. It appears some directories were created for you and some new config files where placed in the root of the project automatically. Apache Jackrabbit created a repository in your project root, set it up for you and ran it.


## Ask The Repository About Some Of Its Features
So let's ask the repository for some of its information. To get questions about the repository and what it supports back we use a method called getDescriptor. The [Repository interface](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Repository.html?is-external=true) contains some constants that can be used to query information using this method. Lets modify our example and just ask some questions to the repository itself. Primarily lets see what its vendor, name and version are:

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

There are some other things that the repository can tell you. I would highly recommend checking out the [Repository's interface documentation](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Repository.html?is-external=true) and trying some of the other constants. Experiment and get comfortable with this flow. We will be using it more and more as time goes on.

### GitHub Location
If you get lost during the tutorial, you can get the code for this example on [GitHub](https://github.com/PlasmaTrout/jackrabbit-training-lab1).




