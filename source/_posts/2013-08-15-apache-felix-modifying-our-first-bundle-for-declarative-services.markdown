---
layout: post
title: "Apache Felix - Modifying Our First Bundle For Declarative Services"
date: 2013-08-15 15:53
comments: true
categories: [OSGi, Apache Felix, Declarative Services]
---
In our previous [example tutorial](/blog/2013/08/15/apache-felix-first-bundle/) we created an OSGi bundle in a programmatic fashion. In that bundle we registered two services into the service registry. While rather simple in implementation, the solution did depend heavily on use of the Activator class. And since only one Activator can be defined in a bundle, registering even more services would present a significant challenge on readability. We are going to neaten this up a bit and use some features of the R4 compendium known as Declarative Services. We will expound on it a little as we go along, but I encourage you to read the specifications on the [OSGi Alliances Site](http://www.osgi.org/Download/HomePage).

If you haven't done the first bundle tutorial mentioned above you can grab the source at:

{% codeblock Git Hub Quick Start lang:bash https://github.com/PlasmaTrout/greeter-bundle-lab3 GitHub %}
git clone git@github.com:PlasmaTrout/greeter-bundle-lab3.git
{% endcodeblock %}

Requirements
-----
-	Apache Felix (See [Starting From Scratch](/blog/2013/08/14/apache-felix-starting-from-scratch/))
-	Apache Maven
-	IDE Of Choice (Preferably IntelliJ IDEA)
-   Internet Connection (For Maven Repositories) 
-   GIT (Needed To Pull Previous Project)

Audience
-----
Typically, this is Lab #3 in a classroom environment, however anyone that wishes can use this tutorial to create a tutorial bundle. The typical audience already understands what OSGi is and that Apache Felix is just one implementation of an OSGi framework.

What We Are Going To Do
-----
1. Remove The Activator From The Project
1. Add A XML File For The Greeter Service To The Resources Directory
1. Add Another XML File For The Greet Command To The Resources Directory
1. Build, Deploy And Test

Remove The Activator From The Project
-----
So let's delete that Activator now. Just completely wipe it out of the project. When your finished with that edit your POM.xml and completely wipe out this line (we don't need it anymore):

{% codeblock Remove This Activator Line lang:xml %}
<Bundle-Activator>org.bhn.training.SimpleActivator</Bundle-Activator>
{% endcodeblock %}

If you have any doubts as to whether its gone after a new build, just peek into the JAR file and make sure its gone. Mine looks something like this now (note no activator is inside the bundle):

{% codeblock Sample JAR Contents Now %}
0 Wed Aug 14 11:18:10 EDT 2013 org/
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/api/
155 Wed Aug 14 11:16:32 EDT 2013 org/bhn/training/api/Greeter.class
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/commands/
1132 Wed Aug 14 11:16:32 EDT 2013 org/bhn/training/commands/GreeterCommands.class
0 Wed Aug 14 11:18:10 EDT 2013 org/bhn/training/impl/
482 Wed Aug 14 11:16:32 EDT 2013 org/bhn/training/impl/SimpleStringGreeterImpl.class
{% endcodeblock %}

Add A XML File For The Greeter Service To The Resources Directory
-----
Because we are using maven, we need to make some new directories and maven knows about so we can include resources into our project. Make a resources dir under main and then put a new dir under the resources dir called OSGI-INF. Our new directories path should look like:

{% codeblock Add A New Resource Path %}
<<Project>>/src/main/resources/OSGI-INF
{% endcodeblock %}

Now in the OSGI-INF directory, lets create a new file called greetercomponent.xml. The name is somewhat irrelevant, however I would name it something that indicates what component it is for. The reason is, each service will be in its own xml file. After a while component1, component2 and component3 won't help developers quickly find its configuration.

Now we are going to do the same job that we did in the Activator in a declarative manner. Modify your greetercomponent.xml file to look like the following:

{% include_code Add A Declarative Services Component File greetercomponent.xml %}

So interpreting this file is rather straight-forward. We are first declaring a component. What's a component. Well a component is really just a Java class that is declared in an XML document. Very similar to the concepts of beans if you are used to other Java terminologies. It has a name just for management in the framework. You can install other web consoles that help manage them by their component name.

Then we tell the component what its implementation class is, so it can create it when its needed.

Then we create a service and tell it what interface to provide, that is, register for us. That's all there really is to a simple service. However, before we can use this xml file, we need to tell the maven plugin to add a manifest entry for where to find it. That entry is known as the Service-Component entry. In our POM in the instructions for our bundle plugin lets add the following line:

{% codeblock Tell The Bundle The File Exists lang:xml %}
<Service-Component>OSGI-INF/greetercomponent.xml</Service-Component>
{% endcodeblock %}

With this line in the file you are ready to deploy to your framework. But before you do, you need to make sure another specific bundle is installed in the container. It's called the SCR Declarative Services bundle and it should be running. If not not sweat, pull up your OSGi console and enter:

{% codeblock Install Service Component Registry Bundle %}
felix:install http://mirror.sdunix.com/apache//felix/org.apache.felix.scr-1.6.2.jar
felix:start X (where x is the number of the bundle)
{% endcodeblock %}

After you deploy your bundle. Pull up the [web console](http://localhost:8080/system/console) and expand it. You should see your new manifest entry is present and you should have a new service id registered. This is a pretty good indication that everything went as planned, but lets add our command otherwise testing it won't be too easy.

Add Another XML File For The Greet Command To The Resources Directory
-----
So lets add one more component file to the OSGI-INF directory. Lets add a greetcommandcomponent.xml file. And do something similar as the above example to register it. However, in this component, we need properties to set upon activation so we have some new elements to add to the definition:

{% include_code Add A Command Declarative Services Component File greetcommandcomponent.xml %}

Then we just add this to our manifest by changing our Service-Component entry to:

{% codeblock Add A Second Component File To The Maven Plugin lang:xml %}
<Service-Component>OSGI-INF/greetercomponent.xml,
            OSGI-INF/greetcommandcomponent.xml</Service-Component>
{% endcodeblock %}

Build, Deploy and Test
-----
So package this guy up with a **mvn package** and then deploy using your method of choice (for this one I used the web console personally). After its deployed and started you should see a new command in our console for **tutorialds:greet** and be able to call it. I did have some exceptions during the making of this tutorial and you may experience the same. The few things that happened to me were:

### IllegalStateExceptions
_ERROR: greeter-bundle (15): [GreeterComponent] Cannot register Component
java.lang.IllegalStateException: Invalid BundleContext_

This occured mainly because I had a typo in one of the component files which confused Felix into writing some properties it shouldn't have. I think the name attribute was missing from one of the properties. However, even after fixing it, the exception stayed there until I restarted the whole framework.

### XmlParseExceptions
Typically they come in a variety of flavors and they are usually due to badly formed xml documents. Mine came from a example in the R4 compendium that had a declarative services entry as the following:

{% codeblock Malformed Header lang:xml %}
<xml version="1.0" encoding="UTF-8">
{% endcodeblock %}

Which I pasted and didn't check, but its really supposed to be:

{% codeblock Standard Header lang:xml %}
<?xml version="1.0" encoding="UTF-8"?>
{% endcodeblock %}

In the next tutorial I think we will kill the command on this bundle and then add PaxExam so we can unit test our product. Generating commands for each bundle to test them is not a feasible option for long term sustainability. PaxExam will allow us to run Felix as a part of our Maven test lifecycle and test our bundle.
