---
layout: page
title: "Apache Sling - Starting From Scratch"
date: 2015-05-28 21:31
comments: true
sharing: true
footer: true
indexer: true
---
In this tutorial we will download and setup a basic development environment using Apache Sling. While there are numerous ways to work with Apache Sling, the WebDav method is the easiest for this tutorial. Future tutorials will cover the more advanced features like deployment, replication and source control.

## What Is Apache Sling?
Well, to borrow from the official [Apache Sling web-site](http://sling.apache.org/), Apache Sling can be summed up in five bullet points:

* REST Based Web Framework
* Content-Driven (using a JCR)
* Powered By OSGi
* Multiple Languages Supported (JSP, Server Side JavaScript/ESP, Groovy, etc.)
* Open Source

## Getting Apache Sling
Apache Sling's download page is located at [http://sling.apache.org/downloads.cgi](http://sling.apache.org/downloads.cgi). Notice that under the Sling Application header there are 3 main types of downloads. Which one you get really depends on your needs but let's take a look at whats offered:

Download Type | Description
-------------------- | ----------- 
Standalone Application | This is what we will use in our development environment. It is a standalone runnable jar file and makes installing the platform a breeze. All of sling is encapsulated in a running Apache Felix OSGi instance. 
Sling Web Application | This is basically a war file made to snap into your web server of choice. It's important to know though that this WAR file embeds OSGi and the system console into the web application. So don't try to deploy it to a Jetty/WAR white-board in an existing OSGi framework. That would be a bit silly.
Sling Source Release | If for some reason you need to build Apache Sling under a different JDK or for a different processor type, the source is available to you.

Let's grab the [standalone jar file](http://mirror.sdunix.com/apache//sling/org.apache.sling.launchpad-7-standalone.jar) and move it to a new directory created on our file system. This step is important since running it will cause a few new directories and config files to be created at its root, and you don't want that cluttering your documents or downloads folder. I saved mine in ```D:/Sling/``` since I'm on a windows machine for this tutorial.

## Running The Jar File
Change directory to where you placed your file, and execute a java -jar on the jarfile. Mine looked something like this:

```
cd D:
cd Sling
java -jar org.apache.sling.launchpad-7-standalone.jar
```

That's really all there was to installing it. In the console you probably received quite a few lines of output:

{% codeblock %}
28.05.2015 21:45:05.497 *INFO * [main] Setting sling.home=sling (default)
28.05.2015 21:45:05.498 *INFO * [main] Starting Apache Sling in d:\Sling\sling
28.05.2015 21:45:05.500 *INFO * [main] Sling  Extension Lib Home : d:\Sling\sling\ext
28.05.2015 21:45:05.501 *INFO * [main] Checking launcher JAR in folder d:\Sling\sling
28.05.2015 21:45:05.509 *INFO * [main] Installing new launcher: jar:file:/D:/Sling/org.apache.sling.launchpad-7-sta
ndalone.jar!/resources/org.apache.sling.launchpad.base.jar, 4.4.1.2_5_2 (org.apache.sling.launchpad.base.jar.143286
3905508)
28.05.2015 21:45:05.510 *INFO * [main] Loading launcher class org.apache.sling.launchpad.base.app.MainDelegate from
 org.apache.sling.launchpad.base.jar.1432863905508
28.05.2015 21:45:05.510 *INFO * [main] External Libs Home (ext) is null or does not exists.
28.05.2015 21:45:05.527 *INFO * [main] Setting sling.launchpad=d:\Sling\sling
28.05.2015 21:45:05.527 *INFO * [main] Starting launcher ...
28.05.2015 21:45:05.528 *INFO * [main] HTTP server port: 8080
28.05.2015 21:45:06.979 *INFO* [FelixStartLevel] org.apache.sling.commons.log.logback.internal.Activator LogbackMan
ager initialized at bundle startup
28.05.2015 21:45:06.984 *INFO* [FelixStartLevel] org.apache.sling.commons.logservice Service [org.apache.sling.comm
ons.logservice.internal.LogServiceFactory,17] ServiceEvent REGISTERED
28.05.2015 21:45:06.985 *INFO* [FelixStartLevel] org.apache.sling.commons.logservice Service [org.apache.sling.comm
ons.logservice.internal.LogReaderServiceFactory,18] ServiceEvent REGISTERED
28.05.2015 21:45:06.985 *INFO* [FelixStartLevel] org.apache.sling.commons.logservice BundleEvent STARTED
28.05.2015 21:45:06.986 *INFO* [FelixStartLevel] org.apache.sling.installer.core BundleEvent RESOLVED
28.05.2015 21:45:06.987 *INFO* [FelixStartLevel] org.apache.sling.installer.core BundleEvent STARTING
28.05.2015 21:45:07.183 *INFO * [main] Startup completed
{% endcodeblock %}

The most important of these is the line ```HTTP server port: 8080``` which tells us where to go next.

## Hey Isn't This Felix?
If you navigate to <a href="http://localhost:8080/system/console" target="_blank">http://localhost:8080/system/console</a> and login as ```admin:admin``` you should be greeted with your old friend the Apache Felix web console. Of course this one is branded with Apache Slings logo, but it operates just the same. Feel free to browse around. Notice there are around 105 bundles in this installation. Most belong to Felix and Apache Jackrabbit (see why we started there first), but a large portion are Sling specific bundles.

Now navigate to [http://localhost:8080/index.html](http://localhost:8080/index.html) and you should be presented with a welcome screen, just like you would on tomcat or similar web server.

_Note: You may or may not be logged in as admin. If not click on the login link and try to stay logged in as admin/admin through these tutorials._

## The Tricky Part: WebDav
In order to do some of these new tutorials in Apache Sling, at least until we learn how to make content loaders in Maven, we will need access to the WebDav port that sling exposes so we can drop files on it. This will temporarily serve as our deployment method, however this method differs by OS.

Platform | Technique
-------- | ---------------
Mac      | In finder choose Go -> Connect To Server and put in  http://admin:admin@localhost:8080/ and click connect.
Ubuntu   | Places -> Connect To Server -> http://admin:admin@localhost:8080/
Windows  | Windows can be a real pain especially since windows 8. To connect to it see the next section.

### Connecting To Sling WebDav On Windows
For Windows users connecting to Sling, WebDav can be more difficult. For starters the actual URI you need to connect to is [http://localhost:8080/dav/default](http://localhost:8080/dav/default) (which isn't mentioned anywhere) and most of you will get a network error out of the box. This is due to a new default setting thats been around since Windows 7 that turns off basic authentication for non-SSL pages. So if you are seeing errors like:

{% img /images/sling/erro1.PNG 'Unknown Error Dialog' 'Unknown Error Dialog' %}

or the infamous ```0x80070043 Network Name Cannot Be Found``` 

{% img /images/sling/error2.PNG 'Network Name Cannot Be Found' 'Network Name Cannot Be Found' %}

Your issue is basic auth being disabled. To turn it back on follow the Microsoft KB Article [#84125](https://support.microsoft.com/en-us/kb/841215).

_Note: As long as you are not running Windows XP the TL;DR of this is adding a new DWORD to ```HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\WebClient\Parameters``` thats sets ```BasicAuthLevel``` to the value 2 and rebooting._

Afterwards, in windows explorer, you should be able to now map a network drive to [the webdav url](http://localhost:8080/dav/default). All said and done your explorer should end up like this:

{% img /images/sling/fix1.PNG 'WebDav Mounted' 'WebDav Mounted' %}

Feel free to explore these new directories but don't modify any of the files just yet. 

## Playing Around
Even though we have this mounted, you are in fact not on a file system. Instead you are inside a Apache Jackrabbit repository that has a RESTfull interface exposed. Notice that on your WebDav mount there is a folder called apps. Trying navigating to the following address in your browser and see what happens:

```
http://localhost:8080/apps.json
```

Notice the response:

```javascript
{
"jcr:createdBy": "admin",
"jcr:created": "Thu May 28 2015 21:45:14 GMT-0400",
"jcr:primaryType": "sling:Folder"
}
```

So we just received a JSON resource that explained that we hit a node of type sling:Folder. Interesting. Now try:

```
http://localhost:8080/apps.4.json
```

Whoa right? Now you can recursively see down 4 levels of nested folders (well they look like folders don't they?)

Even better try this:

```
http://localhost:8080/apps.xml
```

How did this happen? Well, we are essentially playing with the main way that Apache Sling operates. The path (/apps) after the host and port is really a way to tell Apache Jackrabbit what node you want to grab. In this case you said /apps. Once the node was located Sling used the extension you entered to determine how to render the node. In this case we used both the json and xml renderers which are always available, but what happens if you use .html instead? Try it.

{% img /images/sling/sling1.PNG 'Sling Render Error' 'Sling Render Error' %}

Notice how an another available type called the HTML renderer tried to process that node, but realized it was a folder and didn't know what to do with it (to be fair it was missing a special property that would have helped the rendered find it). 

So what happened here? Well, when Sling encounters a .html extension it looks first to see if the URI you gave actually points to a file that ends in .html (like http://localhost:8080/index.html) if so it just renders it. If it doesn't it looks to see if there are any script files (JSP,ESP,Etc) in JackRabbit that could render the node, otherwise it fails to a screen similar to this except telling you it searched and just could not find a renderer. Where it looks for the scripts can be hinted in the actual node themselves but this is out next tutorial, so let's leave it at that.

## Data First Development
So Apache Sling development basically functions backwards from the normal way we approach web apps. Essentially the work flow works like this:

1. Decide the URI you want to place the data
2. Place the data in JackRabbit under that URI
3. Access the data using the URI in browser
4. Build renderers for that data by making JSP, ESP or XSLT pages (there are more options)
5. Any other derivatives of the page can utilize selectors (eg: index.login.html).

This is known as data first development and it's the big reason why Sling is different from all the other web frameworks out there. In future tutorials we will alway start with some nodes of data before we do anything with them.

## Summary
In this tutorial we installed Apache Sling, verified it's operation by opening up the OSGi console and navigating to URLs and learned some basic mechanics behind how it renders things. In the next tutorial we need to give Sling some data and then build a simple JSP renderer for it.




