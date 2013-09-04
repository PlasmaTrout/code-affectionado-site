---
layout: page
title: "Apache Felix - Modifying Our First Bundle for Declarative Services"
date: 2013-08-19 14:16
categories: [OSGi]
comments: true
sharing: true
footer: true
indexer: true
---
OSGi has been a mature framework for building modular applications for quite some time. It has however, not not been embraced fully by the development community. The primary reason it stays unpopular in the development arena lies in its lack of beginning level tutorials or classes. This tutorial begins a series of labs to demystify the OSGi framework and begin familiarity with Apache Felix.

This tutorial shows how to use declarative services in lieu of manual coding. It purpose it to demonstrate another way bundles can be done and improve the readers confidence level with OSGi.

Audience
-----
The core audience is seated in a comfortable classroom or conference room environment. Readers performing this tutorial have received an overview of OSGi and completed a previous lab in which we setup a framework for use. A reader performing this tutorial would have a printed version of this media or the web site up on one screen while they work on code in another. The audience can be made up of developers from different disciplines but knowledge and understanding of the Java language is assumed.

## Getting Started
In our previous [example tutorial](/labs-and-tutorials/osgi/apache-felix-programmatic-bundle/) we created an OSGi bundle in a programmatic fashion. In that bundle, we registered two services into the service registry. While rather simple in implementation, the solution did depend on use of the Activator class. And since only one Activator can be defined in a bundle, registering even more services would present a significant challenge on readability. We are going to neaten this up a bit and use some features of the R4 compendium known as Declarative Services. We will expound on it a little as we go along, but I encourage you to read the specifications on the [OSGi Alliances Site](http://www.osgi.org/Download/HomePage).

If you haven't done the first bundle tutorial mentioned above you can grab the source at:

```bash Git Hub Quick Start https://github.com/PlasmaTrout/greeter-bundle-lab3 GitHub
git clone git@github.com:PlasmaTrout/greeter-bundle-lab3.git
```

Remove The Previous Activator From The Project
-----
Go ahead and delete the Activator from the previous project. Just completely wipe it out. When your finished with that edit your POM.xml and completely wipe out this line (we don't need it anymore):

```xml Remove This Activator Line
<Bundle-Activator>org.bhn.training.SimpleActivator</Bundle-Activator>
```

If you have any doubts as to whether its gone after a new build, just peek into the JAR file. Mine looked something like this after I finished (note no activator is inside the bundle):

```bash Sample JAR Contents Now
0 Wed Aug 14 11:18:10 EDT 2013 org/
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/api/
155 Wed Aug 14 11:16:32 EDT 2013 org/bhn/training/api/Greeter.class
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/commands/
1132 Wed Aug 14 11:16:32 EDT 2013 org/bhn/training/commands/GreeterCommands.class
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/impl/
482 Wed Aug 14 11:16:32 EDT 2013 org/bhn/training/impl/SimpleStringGreeterImpl.class
```

Add A Component XML File
-----
Because we are using maven, we need to make some new directories and maven knows about so we can include resources into our project. Make a resources dir under main and then put a new dir under the resources dir called OSGI-INF. Our new directories path should look like:

```bash Add A New Resource Path
<<Project>>/src/main/resources/OSGI-INF
```

Now in the OSGI-INF directory, lets create a new file called greetercomponent.xml.

_Note: The name is somewhat irrelevant, however I would name it something that indicates what component it is for. The reason is, each service will be in its own xml file. After a while name these like component1, component2 and component3 won't help developers quickly find a configuration._

Now we are going to do the same job that that the Activator did, just in a declarative manner. Modify your greetercomponent.xml file to look like the following:

{% include_code Add A Declarative Services Component File greetercomponent.xml %}

In this file, we are first declaring a component. A component is really just a Java class that is declared in an XML document by definition. Very similar to the concepts of beans if you are used to other Java terminologies. It has a name just for management in the framework. You can install other web consoles that help manage them by their component name.

Then we tell the component what its implementation class is, so it can create it when its needed.

Then we create a service and tell it what interface to provide, that is, register for us. That's all there really is to a simple service. However, before we can use this xml file, we need to tell the maven plugin to add a manifest entry for where to find it. That entry is known as the Service-Component entry. In our POM in the instructions for our bundle plugin lets add the following line:

```xml
<Service-Component>OSGI-INF/greetercomponent.xml</Service-Component>
```

## Install SCR Declarative Services Into Framework
Before you deploy this example, make sure the SCR Declarative Services bundle into your running framework and start it up. If not not sweat, pull up your OSGi console and enter:

```bash Install Service Component Registry Bundle
felix:install http://mirror.sdunix.com/apache//felix/org.apache.felix.scr-1.6.2.jar
felix:start X (where x is the number of the bundle)
```

After you deploy your bundle. Pull up the [web console](http://localhost:8080/system/console) and expand it. You should see your new manifest entry is present and you should have a new service id registered. This is a pretty good indication that everything went as planned, but lets add our command otherwise testing it won't be too easy.

Add Another Component XML File
-----
So lets add another component file to the OSGI-INF directory and call it greetcommandcomponent.xml. We will do something similar to register it, however, in this component, we need to use properties to set it up. It looks like this:

{% include_code Add A Command Declarative Services Component File greetcommandcomponent.xml %}

Then we just add this one to our manifest by changing our Service-Component entry to:

```xml Add A Second Component File To The Maven Plugin lang:xml
<Service-Component>OSGI-INF/greetercomponent.xml,
            OSGI-INF/greetcommandcomponent.xml</Service-Component>
```

Build, Deploy and Test
-----
So package this guy up with a **mvn package** and then deploy using your method of choice (for this one I used the web console personally). After its deployed and started you should see a new command in our console for **tutorialds:greet** and be able to call it. 

#What Did We Just Do
We removed the need for an activator class and instead replaced all of its logic with XML files that accomplished a similar thing. Now some be thinking that we could use this XML files for injection or other clever uses, but we actually can't. If we want to use injection or a similar feature we need to use Blueprint to do it. We will demonstrate this much later.

## A Quick Note On Exceptions

I did have some exceptions during the making of this tutorial and you may experience the same. The few things that happened to me were:

### IllegalStateExceptions
_ERROR: greeter-bundle (15): [GreeterComponent] Cannot register Component
java.lang.IllegalStateException: Invalid BundleContext_

This occurred mainly because I had a typo in one of the component files which confused Felix into writing some properties it shouldn't have. I think the name attribute was missing from one of the properties. However, even after fixing it, the exception stayed there until I restarted the whole framework.

### XmlParseExceptions
Typically they come in a variety of flavors and they are usually due to badly formed xml documents. Mine came from a example in the R4 compendium that had a declarative services entry as the following:

{% codeblock Malformed Header lang:xml %}
<xml version="1.0" encoding="UTF-8">
{% endcodeblock %}

Which I pasted and didn't check, but its really supposed to be:

{% codeblock Standard Header lang:xml %}
<?xml version="1.0" encoding="UTF-8"?>
{% endcodeblock %}

In the next tutorial we will remove the command on this bundle and then add PaxExam to unit test our product. PaxExam will allow us to run Felix as a part of our Maven test lifecycle and test our bundle.
