---
layout: page
title: "Apache Felix - Starting From Scratch"
date: 2013-08-19 13:52
comments: true
sharing: true
footer: true
indexer: true
---
OSGi has been a mature framework for building modular applications for quite some time. It has however, not not been embraced by the development community. The primary reason it remains a mystery to the development world lies in its lack of beginning level tutorials or classes. This tutorial attempts to begin a series of labs to demystify the OSGi framework and begin familiarity with Apache Felix.

This tutorial is designed to provide guidance on downloading, installing and configuring Apache Felix for use in classroom labs. It's designed to end with a bare bones installation and provide a starting point needed in the remainder of the tutorials that will be given on Apache Felix. 

Audience
-----
The core audience is seated in a comfortable classroom or conference room environment. Readers performing this tutorial have received an overview of OSGi and had some discussions on the merits of modular development. A reader performing this tutorial would have a printed version of this media or the web site up on one screen while they work on code in another.

Install Apache Felix
-----
Apache Felix is pretty easy to get started with regardless of what discipline of development you come from. The first step is to get your hands on the binary, which at time of write is found at the following URL:

<http://felix.apache.org/downloads.cgi>

*For these examples use the zip version since it will translate regardless of what operating system your are using.*

These labs were designed to mimic the framework you will see most often in Adobe CQ and Apache Sling, which is why we are using Apache Felix. There are, however,  a plethora of OSGi frameworks to choose from and can have fun downloading and trying a few others out. Keep in mind, while some of the other frameworks will make things considerably easier, they may in fact, deviate from the OSGi Alliance's original specifications. If we work in Felix we are almost certain to be R4 compliant.

## Installation
Make a directory for yourself somewhere where you can work as a scratchpad. I typically move my files from the Download directory so they don’t get lost in the mass of downloads that end up in there. I usually do something like:

```bash Sample Path
$HOME/projects/apache-felix
```

Once you have your structure set up, mv the file (or copy) from the Downloads directory to your new location and unzip it. Mileage may vary on Windows devices but on MacOS set up can be accomplished by issuing the following commands:

``` bash Installation Sequence
cd $HOME/projects
mkdir apache-felix
cd apache-felix/
cp /Users/<<me>>/Downloads/org.apache.felix.main.distribution-4.2.1.zip .
unzip org.apache.felix.main.distribution-4.2.1.zip
```

Start It Up
-----
After its unzipped you’ll notice a felix-framework-x-x-x folder (depending on what distribution you have). If you change directory into that folder and explore the structure  you will notice a few directories exist. We won’t be deep diving into all of these just yet, just notice they are there. If you decide to explore, be sure not to edit them. The framework boots off of these directories and uses them to index and locate classes. Changing them around is not a good idea. Lets start up Felix, if you type:

```bash Running Felix
java -jar bin/felix.jar
```

You should be welcomed with a running framework console that looks like:

```bash The Gogo Shell
____________________________
Welcome to Apache Felix Gogo

g!
```

By default all that comes with Felix, as far as administration is concerned, is a console. Don’t get the wrong impression, this is one powerful console, it just doesn’t seem like it right now (especially since the prompt indicates its some sort of strange dance style).

The Gogo shell (the dance style in question), is a standard Apache shell that you will see in both Apache Felix and Apache Karaf.  Karaf however, does have a ANSI color one which makes it a little more exciting (and by exciting I mean 1990's IRC exciting).

The g! prompt is the surefire mechanism to recognize this shell over the others.

## Send Some Commands To Gogo
We came all this way, we might as well issue some commands. Type in the following to the shell:

```bash List Bundles Command
felix:lb
```

The command **felix:lb** is a command for listing all of the bundles installed in the framework. The felix prefix is basically a namespace qualifier (called a scope). It exists in case two different command bundles use the same command name (eg: felix:lb and other:lb). However, the felix scoped commands are special in that they can be used without the scope at all. So typing **lb** will work on the console as well.

As far as instructions go, let's always prefix the command with felix. My reasoning is that in other frameworks, there are no guarantees that command will be unique and its beter to be safe than sorry.

The output of our **felix:lb** command should have resembled the following:

```bash List Bundles Output
g! felix:lb
START LEVEL 1
   ID|State      |Level|Name
    0|Active     |    0|System Bundle (4.2.1)
    1|Active     |    1|Apache Felix Bundle Repository (1.6.6)
    2|Active     |    1|Apache Felix Gogo Command (0.12.0)
    3|Active     |    1|Apache Felix Gogo Runtime (0.10.0)
    4|Active     |    1|Apache Felix Gogo Shell (0.10.0)
g!
```

For those of you that have browsed another Felix installation, possibly Sling or Adobe CQ, you probably are saying “Yeah right, where are the other 200+ bundles?”. Well this is it for the default Felix framework. It manages to operate on these 5 bundles and doesn’t need much else when starting out. You can start developing right now on just these bundles. 


## Getting Help
So, what else can you do on the console? Well type help in the console and you should get back a list of all of the available commands out of the box. The console should happily respond:

```bash Help Command Output
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
```

Before you try these out, it’s important to note that the help system can also give you some pretty detailed information on each command. For instance type **help felix:ls** and look at the output:

```bash Help List Output
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
```

Then try it:

```bash List Command Output
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
```

Thats a brief overview of the Gogo shell. Mastering all of these commands would be a good idea, but it’s not really needed. For the desktop environments there is a web based console (strategically named WebConsole :) that we can employ to make working with Felix a little easier.

To do that take a look at the help on the **felix:install** command.

Install A Web Console
-----
After executing a **help felix:install**, you will notice that the command takes a multiple arguments. This means if we really wanted to, we could install multiple things at once.

Secondly, note it takes a URL to a file and not just a file path. This means its capable of installing over the internet. Let’s take advantage of the that to install the web console and a http server into the framework. In the console issue the command:

```bash Installing The Web Console
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.webconsole-4.2.0-all.jar
```

the web console requires a web server so lets install jetty

```bash Installing Jetty
felix:install http://mirror.switch.ch/mirror/apache/dist/felix/org.apache.felix.http.jetty-2.2.0.jar
```

Once the download and install is completed, the console should return back a Bundle ID: X response (where X is a number). This represents the bundle id that you just installed. We use this in other commands to tell the bundle to do stuff. Now do an **felix:lb** and look at your list. You should have two new bundles in the installed state.

In order to start up our console we will first need to start the Jetty bundle then our Web Management Console bundle. To do that type the command **felix:start x** (where x is the number of the Jetty bundle). 

```bash Starting Jetty and Web Console
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
```

Do the same for the Web Management Console.

## Testing The Web Console
Go to <http://localhost:8080> and look for the standard Jetty 404 ERROR. Believe it or not, this 404 is actually a good sign. It tells us Jetty is running and we have no pages on the root. We do, however, have servlets in the path /system/console now. So try going to this URL using a username of admin and a password of admin:

<http://localhost:8080/system/console/bundles>

After logging in, you should land on the bundles page. This page is probably the most important of the pages on the web console and you will use it more than the others in you quest to become a master bundle developer. The reasoning is simple, this is where you go to upload, start, stop and examine bundles. There are some important OSGi foundational things to take note of on this page so lets review a few of them.

### Every Bundle Has A Unique Id
The number you see in the left on the console is the ID column. Each bundle has a unique number that represents it.

### Bundle 0 belongs to the framework and cannot be stopped or started
We should have reviewed earlier that bundle 0 belongs to the framework. It is the running framework bundle which explains why it doesn’t have a stop button available for it. It’s version number tells you the version of the framework you are running.

### Bundle 0 only exports the bare essentials and only the standard Java libraries
If you click the arrow expander next to the name you can see some more detailed information (probably more that you’d like) about the system bundle. This bundle exports all of the core Java libraries and a few OSGi specific ones. Notice that a significant amount of XML libraries are exported to.

### Bundle 0 must start first
Notice the start level field. It’s blank. That’s because bundle 0 must start first.

### Bundle 0 must have no dependencies
Now take a look at the imported packages section. Notice that the system bundle imports nothing. It has no dependencies.

### Bundles have states
The status column shows what state the bundle is in.

### Bundles can be started and stopped independently of each other
Notice the stop button next to each bundle. This is where you can stop, start, or delete them from the framework.

If you continue to explore, take not of the services you run across as well. These are things that you will be able to use later when you start building your own applications.

Practice Adding More Bundles
-----
There are other prebuilt bundles that can be used to enhance Apache Felix. You can find them at:

<http://felix.apache.org/downloads.cgi> towards the bottom.

Lets end our lab by practicing some installs of the below bundles. If dependency problems occur, try fixing them as you go along. All you should need to resolve them is available from the download page.

```bash Adding More Features Practice
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
```
