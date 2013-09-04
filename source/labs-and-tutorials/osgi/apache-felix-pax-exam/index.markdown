---
layout: page
title: "Apache Felix - Pax Exam And Unit Testing Felix Bundles"
date: 2013-08-19 14:23
comments: true
sharing: true
footer: true
indexer: true
---
OSGi has been a mature framework for building modular applications for quite some time. It has however, not not been embraced fully by the development community. The primary reason it stays unpopular in the development arena lies in its lack of beginning level tutorials or classes. This tutorial begins a series of labs to demystify the OSGi framework and begin familiarity with Apache Felix.

Coding and deploying to a framework just to test a components functionality is somewhat cumbersome. This tutorial shows how to use Pax Exam to snap in unit tests to our bundle. The intent is to encourage more test driven design and reward service isolation with minimal dependencies.

Audience
-----
The core audience is seated in a comfortable classroom or conference room environment. Readers performing this tutorial have received an overview of OSGi and completed a previous lab in which we setup a framework for use. A reader performing this tutorial would have a printed version of this media or the web site up on one screen while they work on code in another. The audience can be made up of developers from different disciplines but knowledge and understanding of the Java language is assumed.

## Overview
In our previous [example tutorial](/osgi/2013/08/14/apache-felix---modifying-our-first-bundle-for-declaritive-services/) we created an OSGi bundle using declarative services. However, to test our bundle we needed to create a GOGO Shell command and run it manually to validate that the bundle was operating correctly. In this tutorial we will remove that command binding and use a unit testing framework to test our bundle.

If you haven't done the first few bundle tutorials mentioned above you can grab the source at:

```bash Lab Quick Start Code https://github.com/PlasmaTrout/greeter-bundle-lab4 GitHub
git clone git@github.com:PlasmaTrout/greeter-bundle-lab4.git
```

Remove Our Command Class and Its XML File
-----
In this step we are going to find and remove our command package from the project tree. Once we get to Apache Sling (which is where we are going by the way) we can just make some web pages to display our results. So delete the command package and the files in it and remove the greetcommandcomponent.xml file as well. We are done with it for now.

So now our JAR file is a bit smaller. If we take a look we can see the command is no more:

```bash Bundle Jar Contents
450 Wed Aug 14 16:03:34 EDT 2013 META-INF/MANIFEST.MF
0 Wed Aug 14 16:03:34 EDT 2013 META-INF/
0 Wed Aug 14 16:03:34 EDT 2013 OSGI-INF/
333 Wed Aug 14 15:58:16 EDT 2013 OSGI-INF/greetercomponent.xml
0 Wed Aug 14 16:03:34 EDT 2013 org/
0 Wed Aug 14 16:03:34 EDT 2013 org/bhn/
0 Wed Aug 14 16:03:34 EDT 2013 org/bhn/training/
0 Wed Aug 14 16:03:34 EDT 2013 org/bhn/training/api/
155 Wed Aug 14 16:03:32 EDT 2013 org/bhn/training/api/Greeter.class
0 Wed Aug 14 16:03:34 EDT 2013 org/bhn/training/impl/
482 Wed Aug 14 16:03:32 EDT 2013 org/bhn/training/impl/SimpleStringGreeterImpl.class
```

After you finish, take a look at your POM.xml and remove the Service-Component entry for the file you just removed. We also need to add some magical lines for our Pax Exam tests, so it should end up like:

```xml Modifying The Maven Plugin
<plugin>
    <groupId>org.apache.felix</groupId>
    <artifactId>maven-bundle-plugin</artifactId>
    <version>2.3.7</version>
    <extensions>true</extensions>
    <configuration>
        <instructions>
            <Bundle-Name>${project.name}</Bundle-Name>
            <Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
            <Export-Package>org.bhn.training.api</Export-Package>
            <Service-Component>OSGI-INF/greetercomponent.xml</Service-Component>
        </instructions>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>manifest</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

The new goal is to ensure that the manifest is actually written at a different part of the lifecycle. It's really just to improve the process of testing the bundle. Without this line you would end up with errors indicating that the manifest didn't exist during the testing phase. Putting this line in ensures that it really will exist prior to the test being run.

Add A Few More Maven Dependencies
-----
So now we need to add a few Maven dependencies. I won't explain them, just know that they are requirements for Pax Exam and are available in their documentation. We are also going to scope most of them to test only anyways. We won't be deploying them. In your POM.xml add the following dependency chain:

```xml Pax Exam POM Dependencies
<!--Used for Pax Exam unit testing only -->
<dependency>
    <groupId>org.ops4j.pax.exam</groupId>
    <artifactId>pax-exam-container-native</artifactId>
    <version>2.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.ops4j.pax.exam</groupId>
    <artifactId>pax-exam-junit4</artifactId>
    <version>2.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.ops4j.pax.exam</groupId>
    <artifactId>pax-exam-link-mvn</artifactId>
    <version>2.5.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.ops4j.pax.url</groupId>
    <artifactId>pax-url-aether</artifactId>
    <version>1.4.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.felix</groupId>
    <artifactId>org.apache.felix.framework</artifactId>
    <version>3.2.2</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-core</artifactId>
    <version>0.9.20</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>0.9.20</version>
    <scope>test</scope>
</dependency>
```

Thats really all we had to do in this step. Its probably best that we run a **mvn package** or **nv test** just to make sure we have no Maven errors.

Modify Our Existing JUnit Test To Use PaxRunner
-----
We already have a unit test that was created with the quick start archetype. Up to now we have been mostly ignoring it. Lets make it do some work. Locate your AppTest.java file and lets rename it to GreetingPaxExamTest.java so it describes itself a bit.

Now lets decorate the top of it with some annotations that will help Pax Exam pick up on its existence (I'm omitting the imports that are needed to support these, I'm hoping your IDE does some work here):

```java Decorating Our Test Class With Some Annotations
@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class GreeterPaxExamTest
    extends TestCase
{
```

The first is a common annotation to change what runner (test system) that the unit test will run on. Without this annotation, Maven will default to its own runner and we don't want that in this case. We want it to use JUnit 4.8.2's test runner.

The second tells Pax Exam what kind of strategy to use when starting and stopping the container. With our selection a brand new test container is started and stopped for each test that is run. The only other option is EagerSingleStagedReactorFactory which is explained in Pax Exam's documentation. Our statement is the default so its really not needed but let's leave it in anyways.

### Create Config Method
Pax Exams uses a special configuration method to help it load the framework when the test is being run. It's important that we add the bundles we need to it, just like we would do to a stand-alone Felix framework. Place this method in your test class:

```java Test Class Configuration Method
@Configuration
public Option[] config(){
    return options(
            mavenBundle("org.apache.felix","org.apache.felix.scr","1.6.2"),
            bundle("reference:file:target/classes"),
            junitBundles()
    );
}
```

This method is called by the runner to know how to load the OSGi framework up with bundles prior to the test launching. The syntax is a little strange, but just know that Pax Exam exposed a few static helpers that attempts to make things easier.

### Create A Unit Test
Now we need is an actual test, lets modify the create one to look like:

```java Test Class Test Method
@Test
public void simpleGreetingImplCheck(){
 	// test will go here  
}
```

So far we really aren't testing anything specific, but just to test your setup run **mvn test** and watch what happens now.

All of that logged information that flew by was telling your about a framework startup and shutdown. Notice its really fast for starting and stopping a bundle.

## Getting The Greater Service From A Unit Test
So we want to test our greeter service. We know that typically we ask the context for an instance of the service that implements an interface. So our basic question usually is "Hey framework, I need whatever implements this interface." the framework usually responds "ok" and gives it to you. To do that in our test, we need to inject a variable. This is very similar to what you will do in you code in later tutorials when you use them using SCR annotations, but it uses a different keyword. Lets create the following field in our test class:

```java Test Class Injection Example
@Inject
private Greeter greeter;
```

_Note: The import for inject is javax.inject.Inject. Some IDE's will put googles first, just know you want the javax annotation._

Now lets use that interface in our test. Notice that I **WILL NOT** instantiate anything the framework will handle that. Change your test to look like:

```java Test Class Unit Test
@Test
public void simpleGreetingImplCheck()
{
    String testValue = greeter.greet();
    assertEquals(testValue,"Hello World Fail!");
}
```

Just in case your having trouble, the final test class should look like the following:

{% include_code GreeterPaxExamTest.java %}

Build Our Application and Run Unit Tests
-----
Now all we have to do is run a **mvn test** and see if we are working properly. This test was designed to fail just to prove that it is actually working. If you scroll up a little in the text you will see the line:

```java Example Failure Message
GreeterPax ExamTest.simpleGreetingImplCheck:39 expected:<Hello World[]!> but was:<Hello World[ Fail]!>
```

This is due to our test case having the world fail in it. If we change it to just Hello World! it will pass.

With this setup we now can actually test our bundles prior to deploying them. In the next tutorial we will make declarative services one more step easier by removing the xml files and using our classes to define the component information.
