---
layout: post
title: "Apache Felix - Starting From Scratch"
date: 2013-08-14 14:20
comments: true
categories: [OSGi, Apache Felix, Starting From Scratch]
---
This tutorial is designed to provide guidance on downloading, installing and configuring Apache Felix for use in other classroom labs. It's designed to give the bare bones and provide a starting point needed in the remainder of the tutorials that will be given on Apache Felix. Its can also easily be adapter for classroom demonstrations and hackathons.

Audience
-----
Typically, this is Lab #1 in a classroom environment, however anyone that wishes can use this tutorial to set up their own Apache Felix envinroment for learning OSGi. The typical audience already understands what OSGi is and that Apache Felix is just one implementation of an OSGi framework.

What We Are Going To Do
-----
 1. Install Apache Felix
 1. Start It Up
 1. Install A Web Console
 1. Add Some Bundles

Installing Apache Felix
-----
Apache Felix is pretty easy to get started with regardless of what discipline of development you come from. The first step is to get your hands on the binary, which at time of write is found at the following URL:

<http://felix.apache.org/downloads.cgi>

*For these examples we will use the zip version since it will translate regardless of what operating system your are using.*

Before we get moving let me explain that these labs were designed to mimic the framework you will see most often in Adobe CQ, which is why we are using Apache Felix here. But there are a plethora of OSGi frameworks to choose from and you may have fun downloading and trying a few others out. Keep in mind, while some of the other frameworks will make things considerably easier, they may in fact, deviate from the OSGi Alliance's original specifications. If we work in Felix we are almost certain to be R4 compliant. Let's get started.

Make a directory for yourself somewhere where we can work as a scratchpad. I typically move my files from the Download directory so they don’t get lost in the mass of downloads that end up in there. I usually do something like:

{% codeblock Sample Path %}
$HOME/projects/apache-felix
{% endcodeblock %}

Once you have your structure set up, mv the file (or copy) from the Downloads directory to your new location and unzip it. Mileage may vary on Windows devices but on MacOS set up can be accomplished by issuing the following commands:

{% codeblock Installation %}
cd $HOME/projects
mkdir apache-felix
cd apache-felix/
cp /Users/<<me>>/Downloads/org.apache.felix.main.distribution-4.2.1.zip .
unzip org.apache.felix.main.distribution-4.2.1.zip
{% endcodeblock %}

Start It Up
-----
After its unzipped you’ll notice an felix-framework-x-x-x folder (depending on what distribution you have). If you change directory into that folder you can explore the structure of this bare bones install. We won’t be deep diving into all of these directories just yet, just notice they are there. If you decide to explore, be sure not to edit them. The framework boots off of these directories and uses them to index and locate classes. Changing them around is not a great idea. So lets start up Felix, if you type:

{% codeblock Running Felix %}
java -jar bin/felix.jar
{% endcodeblock %}

You should be welcomed with a running framework console that looks like:

{% codeblock The Gogo Shell %}
____________________________
Welcome to Apache Felix Gogo

g!
{% endcodeblock %}

What this? Well by default, all that comes with Felix, as far as administration is concerned, is a console. Now, don’t get the wrong impression, this is one powerful console thats staring back at you. It just doesn’t seem like it right now (especially since the terminal apparently is some sort of strange dance style).

The GOGO shell (the dance style in question), is a standard Apache shell that you will see on both Felix and Karaf.  Karaf however, does have a ANSI color one which makes it a little more exciting (and by exciting I mean 1990's IRC exciting). The g! prompt is the surefire mechism to recognize this shell over the others.

So we came all this way, we might as well issue some commands. Type in the following to the shell:

{% codeblock List Bundles Command %}
felix:lb
{% endcodeblock %}

The command “lb” is a felix command for listing all of the bundles installed in Felix. The felix prefix is sort of a namespace qualifier. It exists primarily in the case two different command bundles use the same command name. But since they come with Felix, there are also aliased directly on the console. So typing “lb” will work as well. As far as instruction go, however, I will almost always prefix the command. My rationale is that in other systems, like Apache Karaf, it will be mandatory since so many open source bundles exist in the framework and you will need to get used to it anyways.

The output of our **felix:lb** command should have resembled the following:

{% codeblock List Bundles Output %}
g! felix:lb
START LEVEL 1
   ID|State      |Level|Name
    0|Active     |    0|System Bundle (4.2.1)
    1|Active     |    1|Apache Felix Bundle Repository (1.6.6)
    2|Active     |    1|Apache Felix Gogo Command (0.12.0)
    3|Active     |    1|Apache Felix Gogo Runtime (0.10.0)
    4|Active     |    1|Apache Felix Gogo Shell (0.10.0)
g!
{% endcodeblock %}

For those of you that have browsed another Felix installation, possibly Sling or Adobe CQ, you probably are asking “Yeah right, where are the other 200+ bundles?”. Well this is it, the default Felix framework operates on these 5 bundles and doesn’t need much else. You can start developing right now on these building whatever you heart desires. 

So, what else can you do on the console? Well type help in the console and you should get back a list of all of the available commands out of the box. The console should happily respond:

{% codeblock Help Command Output %}
... some stuff ...
felix:install
felix:lb
felix:log
felix:ls
felix:refresh
felix:resolve
felix:start
felix:stop
... more stuff ...
{% endcodeblock %}

Before you try these out, it’s important to note that the help system can also give you some pretty detailed information on each command. For instance type **help felix:ls** and look at the output:

{% codeblock Help List Output %}
g! help felix:ls

ls - get current directory contents
   scope: felix
   parameters:
      CommandSession   automatically supplied shell session

ls - get specified path contents
   scope: felix
   parameters:
      CommandSession   automatically supplied shell session
      String   path with optionally wildcarded file name
g!
{% endcodeblock %}

Then try it:

{% codeblock List Command Output %}
g! felix:ls
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/bin
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/bundle
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/conf
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/DEPENDENCIES
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/doc
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/felix-cache
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/LICENSE
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/LICENSE.kxml2
/Users/Me/ExternalLibraries/apache-felix-bare/felix-framework-4.2.1/NOTICE

g!
{% endcodeblock %}

Thats all there is really to understanding the GOGO shell. Now mastering all of these commands would be a good idea, but it’s not really needed. That is unless you want to stay lightweight, say on a mobile device or micro-board type installation. For the desktop environments there is a web based console (strategically named WebConsole :) that we can employ to make this a little easier for us. 

Before you install it, take a look at the help on the **felix:install** command.

Installing A Web Console
-----
After executing a **help felix:install**, you will notice that the command is scoped to the felix prefix and takes a collection of parameters. This means if we really wanted to we could install multiple things at once. Secondly, note it takes a URL to a file and not just a file path. This means it’s more than capable of installing over the internet. Let’s take advantage of the that to install the web console and a http server into the framework. In the console issue the command:

{% codeblock Installing The Web Console %}
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.webconsole-4.2.0-all.jar
{% endcodeblock %}

the web console requires a web server so lets install jetty

{% codeblock Installing Jetty %}
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.http.jetty-2.2.0.jar
{% endcodeblock %}

Once the download and install is completed, the console should return back a Bundle ID: X (where X is a number). This represent the bundle id that you just installed and will use to find out more information about the bundle and start/stop it. Now do an **felix:lb** and look at your list. You should have two bundles in the installed state.
In order to start up our console we will first need to start the Jetty bundle then our Web Management Console bundle. To do that type the command **felix:start X** (where x is the number of the Jetty bundle). 

{% codeblock Starting Jetty and Web Console %}
g! lb
START LEVEL 1
   ID|State      |Level|Name
    0|Active     |    0|System Bundle (4.2.1)
    1|Active     |    1|Apache Felix Bundle Repository (1.6.6)
    2|Active     |    1|Apache Felix Gogo Command (0.12.0)
    3|Active     |    1|Apache Felix Gogo Runtime (0.10.0)
    4|Active     |    1|Apache Felix Gogo Shell (0.10.0)
    6|Installed  |    1|Apache Felix Web Management Console (All In One) (4.2.0.all)
    7|Installed  |    1|Apache Felix Http Jetty (2.2.0)
g! felix:start 7
g! [INFO] Started jetty 6.1.x at port(s) HTTP:8080

g! felix:start 6
g!
{% endcodeblock %}

Do the same for the Web Management Console. Then go to <http://localhost:8080> and see if you get a subtle Jetty 404 ERROR. Believe it or not, this 404 is actually a good thing. It tells us Jetty is running and we have no pages on the root. We do, however, have servlets in the path /system/console now. So try going to this URL using a username of admin and a password of admin:

<http://localhost:8080/system/console/bundles>

After logging in, you should land on the bundles page. This page is probably the most important of the pages on the web console and you will use it more than the others in you quest to become a master bundle developer. The reasoning is simple, this is where you go to upload, start, stop and examine bundles. There are some important OSGi foundational things to take note of on this page so lets review a few of them.

### Every Bundle Has A Unique Id
Yep. That’s it there in the Id column. They stay pretty simple as integers. Definitely prevents a lot of typing when using the console.

### Bundle 0 belongs to the framework and cannot be stopped or started
We should have reviewed earlier that bundle 0 belongs to the framework. Heck, it really is the running framework which explains why it doesn’t have a stop button available for it. It’s version number tells you the version of the framework you are running and if you ever see this bundle stopped someone is playing a trick on you because the web console wouldn’t be available if it was. 

### Bundle 0 only exports the bare essentials and only the standard Java libraries
If you click the right arrow expander next to the name you can see a ton of more information (probably more that you’d like) about the system bundle. As you’d expect this bundle exports all of the core Java libraries and a few OSGi specific ones. The best part is that a significant amount of XML libraries are exported to, so always check here before exporting your own.

### Bundle 0 must start first
Notice the start level field. It’s blank? Yep. That’s because bundle 0 must start first and therefore is really start level 0.

### Bundle 0 must have no dependencies
Now take a look at the Imported Packaged section. Notice that the system bundle imports nothing. It has no dependencies. At all. None.

### Bundles have states
So the status column shows what state the entire bundle is in. All of ours should show active at this point.

### Bundles can be started and stopped independently of each other
Notice the stop button next to each bundle. This is where you can stop, start, or delete them from the framework.

*NOTE: Bundle 0 has a lot to it. Look around. It also registers two services, they are two interfaces named PackageAdmin and StartLevel.*

Well there’s the 1 dollar tour of the bundles section of the app. Let’s take a look at the rest of the menu structure
Under OSGi we get Bundles (which is where we are), Configuration, Log Service and Services (Image 3.11). Ignore the Status and Web Console top level menus for now. They really just drive to informational content anyways. The stock OSGi sections of this menu apply to some UI pieces we haven’t installed yet. If you try to go to Configuration or Log Service you may notice they just tell you they aren’t installed. Let click on Services. 


Add Some More Bundles
-----
So that really the bare minimum install, however there are some Felix provided bundles you may want to browse yourself and install at will. You can find them at:

<http://felix.apache.org/downloads.cgi> towards the bottom.

Lets continue our class setup with the following ones (They are mostly to enhance our web console and will provide good practice):

{% codeblock Adding More Features Practice %}
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.configadmin-1.6.0.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.log-1.0.1.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.metatype-1.0.6.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.dependencymanager-3.1.0.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.eventadmin-1.3.2.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.deploymentadmin-0.9.4.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.webconsole.plugins.event-1.0.2.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.webconsole.plugins.packageadmin-1.0.0.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.scr-1.6.2.jar
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.webconsole.plugins.ds-1.0.0.jar
{% endcodeblock %}

