---
layout: page
title: "Apache Felix - From Declarative Services to SCR Annotations"
date: 2013-08-22 10:01
comments: true
sharing: true
footer: true
indexer: true
---
OSGi has been a mature framework for building modular applications for quite some time. It has however, not not been embraced fully by the development community. The primary reason it stays unpopular in the development arena lies in its lack of beginning level tutorials or classes. This tutorial begins a series of labs to demystify the OSGi framework and begin familiarity with Apache Felix.

This lab improves upon our declarative services example by converting it to SCR annotations.

## Audience 
The core audience is seated in a comfortable classroom or conference room environment. Readers performing this tutorial have received an overview of OSGi and completed a previous lab in which we setup a framework for use. A reader performing this tutorial would have a printed version of this media or the web site up on one screen while they work on code in another. The audience can be made up of developers from different disciplines but knowledge and understanding of the Java language is assumed.

## Getting Started
In our previous [example tutorial](/labs-and-tutorials/osgi/apache-felix-pax-exam/) we used pax exam to unit test a bundle done using declarative services. The component xml files are rather efficient however there is another way we could do this that not have to markup an external file to accomplish the same goal. Using a special set of annotations, we could just mark up our classes and do the same thing as the component.xml files we used earlier.

If you haven't done the first bundle tutorial mentioned above you can grab the source at:

```bash Git Hub Quick Start https://github.com/PlasmaTrout/greeter-bundle-lab5 GitHub
git clone git@github.com:PlasmaTrout/greeter-bundle-lab5.git
```

## Remove The Component XML Files
In our project lets completely remove the file

> src/main/resources/OSGI-INF/greetercomponent.xml

from our project. This will ensure we don't accidentally use it or confuse people looking at our project. Remember to pull the Service-Component line from the maven plugin. It should end up looking like this:

```java Maven Plugin
<configuration>
	<instructions>
		<Bundle-Name>${project.name}</Bundle-Name>
		<Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
		<Export-Package>org.bhn.training.api</Export-Package>
	</instructions>
</configuration>
```

So lets do a **mvn clean** and then a **mvn test** and see what kind of error we get. As expected the framework can't find our service anymore. Instead we get an error that says:

> ServiceLookup gave up waiting for service org.bhn.training.api.Greeter

Since we not longer have a declarative services file, the service is no longer getting registered. This is exactly what we want.

## Add A New Plugin
What we are really using with this tutorial is a plugin that will write the xml file and line we just deleted for us so we don't manually have to. We will decorate our classes with SCR annotations and use a plugin for maven that will do the heavy lifting. In order to use it we first need to add it to the POM.

```xml New Plugin For SCR Annotations
<plugin>
	<groupId>org.apache.felix</groupId>
	<artifactId>maven-scr-plugin</artifactId>
	<version>1.9.0</version>
	<executions>
	  <execution>
		<id>generate-scr-scrdescriptor</id>
		<goals>
		  <goal>scr</goal>
		</goals>
	  </execution>
	</executions>
</plugin>
```

## Add A New Maven Dependency
In order to use the annotations in our classes we need to add another dependency to the POM. Lets add it to the POM.xml file now:

```xml New Maven Dependency
<dependency>
	<groupId>org.apache.felix</groupId>
	<artifactId>org.apache.felix.scr.annotations</artifactId>
	<version>1.7.0</version>
</dependency>
```

## Decorate Our Implementation Class
Now that we have the annotations available in our project, all that's left is to decorate our implementation class with some on them. Add the following ones to the top of the implementation of SimpleStringGreeterImpl:

```java SCR Annotations On Our Greeter Implementation
@Component(name = "Simple String Greeter Component", immediate = true)
@Service(value = org.bhn.training.api.Greeter.class)
public class SimpleStringGreeterImpl implements Greeter {
```

Technical we are ready to go, however since we using Pax Exam to test our classes, we need to do some radical changing of our unit testing setup.

## Convert Our Unit Test To An Integration Test
We really need our tests to run after the JAR has been built. The reason being is that the SCR plugin does most of its magic very late in the build cycle. This means we need to add a new plugin to the mix. This one will give us a new goal of verify that we can use to package and then test our bundle.

```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>failsafe-maven-plugin</artifactId>
	<version>2.4.3-alpha-1</version>
	<executions>
		<execution>
			<goals>
				<goal>integration-test</goal>
				<goal>verify</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

## Change Our Test Name And Config
In order for our integration testing plugin to pickup the test we need to changes it names from GreeterPaxExamTest to GreeterPaxExamIT (integration test). When thats done we need to change the runners config to load up the fresh JAR each time it's ran. The config method in this test ends up like so:

```java New Config Method Changes
@Configuration
public Option[] config(){
	return options(
			mavenBundle("org.apache.felix","org.apache.felix.scr","1.6.2"),
			bundle("reference:file:target/greeter-bundle-1.0-SNAPSHOT.jar"),
			junitBundles()
	);
}
```

If everything went right we should be able to issue a **mvn verify** and see the magic unfold. The project should build, package the jar and then begin the tests, which should pass as long as the assert in your test is still:

> assertEquals(testValue,"Hello World!");

Just for fun change it to

> assertEquals(testValue,"Hello World Fail!");

and notice that the fail message is different from the runner we were used to. Near the failure you will notice a line that tells us that we really need to look for why the test failed in our cat target/failsafe-reports directory. If we look into one of the file by using:

> cat target/failsafe-reports/org.bhn.training.GreeterPaxExamIT.txt

We can see that the reason it failed was:

> ComparisonFailure: expected:<Hello World[]!> but was:<Hello World[ Fail]!>

## So What Did The SCR Plugin Do
Well if you tear your jar apart, your will notice that it added a line to the MANIFEST.MF of:

> Service-Component: OSGI-INF/org.bhn.training.impl.SimpleStringGreeterImpl.xml

Interesting, lets look at that file in the JAR to see what the plugin did for us.

```xml The components file
<?xml version="1.0" encoding="UTF-8"?>
<components xmlns:scr="http://www.osgi.org/xmlns/scr/v1.0.0">
    <scr:component immediate="true" name="Simple String Greeter Component">
        <implementation class="org.bhn.training.impl.SimpleStringGreeterImpl"/>
        <service servicefactory="false">
            <provide interface="org.bhn.training.api.Greeter"/>
        </service>
        <property name="service.pid" value="Simple String Greeter Component"/>
    </scr:component>
</components>
```

So now we don't have to mess around with the xml file directly, we can just decorate our classes and let the SCR plugin manage the translation. This usually ends up becoming the preferred way developers like to create bundles. 

## Final Files
Since it's possible here to get some wacky setup problems, here are the 3 files needed to complete the project.

{% include_code Full SimpleStringGreeterImpl osgi-lab6/SimpleStringGreeterImpl.java %}

{% include_code Full GreeterPaxExamIT osgi-lab6/GreeterPaxExamIT.java %}

{% include_code Full Lab 6 POM osgi-lab6/pom.xml %}



