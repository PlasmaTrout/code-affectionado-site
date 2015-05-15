---
layout: page
title: "Setting Up JSP Support In BndTools Using Pax-Web"
date: 2015-05-14 22:02
comments: true
sharing: true
footer: true
indexer: true
---
In this tutorial we will setup a new BndTools project to compile and host JSP web pages as a Web Application Bundle. If you prefer video tutorials, there is a [video tutorial](https://youtu.be/_q7_8yGJ1nM) on how to do this, however it differs in that this example will use shortcuts for the sections that are mostly visual. This allows us to spend more time setting up our JSP bundle and less time worrying about the Pax-Web recipe.

## Prerequisites
There are a few preerquisites in order to follow along with this tutorial. It's important to have BndTools installed into Eclipse at this point. If you don't, take some time and watch the [video](https://youtu.be/AEUxeUBb6i0) on how to accomplish that step. 

### Download The Pax-Web Dependency Bundle
A zip file is needed before this tutorial will work properly (dont worry, it's all jar files and 0 viruses). It's a 15M zip of bundles ready to be put into the local repository in BndTools. Once it's downloaded and unzipped everything is ready to go.

Download: [CodeAffectionado_PaxJSP_Achive.zip](https://app.box.com/s/xwvcb9ai5v6ht43tqnnsn9ih706c6b2q)

## Getting Started

### Install the Zip File Bundles
Starting with a new workspace and a new bndtools osgi project (component development) project in Eclipse (the name doesn't matter), copy all of the jar files inside of the downloaded zip file to the Local repository. This is done by dragging them onto the Local folder of the Repositories window. Once all the jar files are visible in the repository, restart Eclipse.

### Create a New Run Descriptor
Right click the project folder and create a new _Run Descriptor_. Leave it default to use Apache Felix 4 with the Gogo Shell. It really doesn't matter what it's named, but I named mine paxweb. The final file for me that was created was named ```paxweb.bndrun```.

### Add the Run Bundles to you Run Descriptor
Double click on the new Run Descriptor that bndtools created to open the editor in Eclipse. Collapse the _Run Requirements_ section and expand the _Run Bundles_ section. This will help with the visibility of what bundles are being installed into the Felix runtime.

Now find and click on the _Source_ tab on the bottom of the ```paxweb.bndrun``` editor window. Inside this source editor examine the current configuration.

####Before
Currently there is a runbundles section of the source that looks like:

{% highlight plain%}
-runbundles:\
	org.apache.felix.gogo.runtime,\
	org.apache.felix.gogo.shell,\
	org.apache.felix.gogo.command
{% endhighlight %}
<br/><br/>
#### After
In order for the bundles downloaded earlier to be included in the framework they need to be added to the runbundles section. Replace that entire section with this:

{% highlight plain %}
-runbundles:  \
	org.apache.felix.gogo.command,\
	org.apache.felix.gogo.runtime,\
	org.apache.felix.gogo.shell,\
	org.apache.felix.configadmin,\
	org.apache.felix.eventadmin,\
	javax.servlet,\
	org.apache.felix.http.api,\
	org.objectweb.asm.all,\
	slf4j.api,\
	slf4j.simple,\
	org.apache.xbean.bundleutils,\
	org.apache.xbean.finder,\
	org.ops4j.pax.web.pax-web-jetty-bundle,\
	org.apache.felix.webconsole,\
	org.ops4j.pax.logging.pax-logging-api,\
	org.ops4j.pax.web.pax-web-jsp,\
	org.ops4j.pax.web.pax-web-extender-whiteboard,\
	org.ops4j.pax.web.pax-web-extender-war,\
	org.apache.felix.scr,\
	biz.aQute.bnd.annotation,\
	org.apache.felix.metatype,\
	org.eclipse.jdt.core.compiler.batch,\
	javax.servlet.jsp.jstl-api,\
	org.ops4j.pax.web.pax-web-api
{% endhighlight %}
<br/><br/>
Now hit **save** and go back to the _Run_ tab and verify that the bundles are now in the run bundles section of the editor.

### Startup The Laucher
Go ahead and hit the _Run OSGi_ button now. The framework should startup and wait for a plethora of messages that look like the following to scroll by:

{% highlight plain %}
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester] :   Fire body() for org.apache.tomcat.util.descriptor.tld.TldRuleSet$1@5156d4a7
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester] :   Popping body text ''
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester] :   Fire end() for org.apache.tomcat.util.descriptor.tld.TldRuleSet$1@5156d4a7
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester.sax] : endPrefixMapping()
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester.sax] : endPrefixMapping(xsi)
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester.sax] : endDocument()
org.ops4j.pax.web.pax-web-jsp[org.apache.tomcat.util.digester.Digester] : addRuleSet() with no namespace URI
{% endhighlight %}
<br/><br/>
This is the logger, in association with the jdt compiler, logging some of the JSP internals as they happen. Navigate to [http://localhost:8080/system/console](http://localhost:8080/system/console) and login as admin/admin and note that the webconsole, with full JSP/WAR/WAB/Whiteboard support, is running.

## Create A JSP WAB Project
To create a JSP Web Application Bundle (WAB) start with a fresh Bundle Descriptor.Right click on the project folder in the package explorer and choose New -> Bundle Descriptor. It's name doesn't really matter, but I named mine jsptest. Aftwards, double click on the .bnd file (mine was ```jsptest.bnd```) to open up the editor.

### Remove Default Private Package
Notice by default that BndTools included a private package in the private packages window. Go ahead and remove it by clicking on it and choosing the X icon at the top of the window. Hit **save**.

_Note: This step isn't critical, however I have ran into issues on my mac where a crazy osgi.ee missing requirement error message will show when this is left in. On windows I haven't seen the issue._

### Create Web Application Folder And Files
At the root of the project folder, create a new folder called webapp. Inside of the webapp folder create a WEB-INF folder. in the webapp folder create a file called ```test.jsp``` and give it the following contents:

```html
<html>
	<head>
		<title>OSGi JSP File</title>
	</head>
	<body>
		<h1>This Proves That My JSP Works!</h1>
	</body>
</html>
```
When complete the package explorer should look like this:

{% img /images/bndtools/Capture10.PNG 'Final Project Structure 'Final Project Structure' %}

_Note: I didn't include a web.xml, which is normally considered bad practice for a WAR file, but for a WAB it's purely optional._

### Edit Bundle Descriptor
Now let's find the source tab of the ```jsptest.bnd``` bundle descriptor and click on it. The rather minimal source file should resemble:

```
Bundle-Version: 0.0.0.${tstamp}
Service-Component:  \
	*
```

Some lines will need to be appended ot the source to instruct it how to create a web application bundle. First add this line to the bottom of the file:

```
Web-ContextPath: jsptest/
```

This determines the context path when the OSGi servlet container hosts the web application bundle. This is found in 128.3.1 of the [OSGi Enterprise 4.2 Specification](https://osgi.org/download/r4v42/r4.enterprise.pdf)\

Also add:

```
WebApp-Context: jsptest/
```
This is Pax specific and really only applies to older versions of Pax-Web and similar frameworks.

Finally add the final line:
```
-wab: webapp
```
This tells the bundler that everything found in the webapp directory should be included in the bundle and that any class based components should be moved to the appropriate locations for a web application. 

_Note: If we did add some components to this WAB you would see them go into WEB-INF/classes instead of their usual location._

The final file should look like this:
```
Bundle-Version: 0.0.0.${tstamp}
Service-Component:  \
	*
Web-ContextPath: jsptest/
WebApp-Context: jsptest/
-wab: webapp
```

### Add Bundle To Run Descriptor And Test
Go back to the ```paxweb.bndrun``` editor and go to the Run tab and search for the new Bundle Desciptor in the search box. When we find it, we can drag it into the run bundles window and hit save. 

Now navigate to [http://localhost:8080/jsptest/test.jsp](http://localhost:8080/jsptest/test.jsp) to see the plain vanilla JSP in all it's glory.

## Testing Scriplets
Modify the ```test.jsp``` file to include some scriptlet code by optining it in the editor and making the following changes:

```html
<html>
	<head>
		<title>OSGi JSP File</title>
	</head>
	<body>
		<h1>This Proves That My JSP Works!</h1>
		<h2>This Proves That <%= "Scriptlets".toString() %> Work!</h2>
	</body>
</html>
```
Now hit save and wait for the recompilation and check [http://localhost:8080/jsptest/test.jsp](http://localhost:8080/jsptest/test.jsp) again to make sure it worked.

## Testing TagLibs
In order to use taglibs in the JSP, they must first be setup one of two different ways:

* If its a standard taglib (such as JSTL) the namespaces must be imported by the bundle.
* If its a custom taglib it must be deployed by the web application bundle

In this case the standard tag library will be used for testing. Double click the  ```jsptest.bnd``` Bundle Descriptor again and on the Content tab expand the _Customise Imports_ section.

Click on the + button and then add ```org.apache.taglibs.standard.lang.jstl``` to the pattern box and hit save. Browse to the source section and verify it's similar to:

```
Bundle-Version: 1.0
Service-Component:  \
	*
Web-ContextPath: jsptest/
WebApp-Context: jsptest/
-wab: /webapp
Bundle-Name: Test Bundle
Import-Package:  \
	org.apache.taglibs.standard.lang.jstl,\
	*
```

Lastly, change the test.jsp file to look like this:

```html
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
	<head>
		<title>OSGi JSP File</title>
	</head>
	<body>
		<c:set var="testvar" value="Tag Libraries"/>
		<h1>This Proves That My JSP Works!</h1>
		<h2>This Proves That <%= "Scriptlets".toString() %> Work!</h2>
		<h3>This Proves That ${ testvar } and Expressions Work!</h3>
	</body>
</html>
```
After hitting save, browse back to [http://localhost:8080/jsptest/test.jsp](http://localhost:8080/jsptest/test.jsp) and see the JSP file at work.

### Summary
In this tutorial we setup our BndTools environment to run JSP Web Application Bundles and created a simple JSP page to test it. Getting core JSP applications to run is a big step toward creating a full fledged application in OSGi. In the next tutorials we will use JSPs and Servlets to call services and access more intense features of the OSGi framework.

## Some Things That May Go Wrong
During this tutorial, both on video and written, I had a number of things go wrong. Here are the errors and fixes that occurred while making the tutorials.

### JSP bundle shows in Run Bundles but is not actually in the list of running bundles
This happens when you start a plain project with a pre-existing bnd.bnd file but don't use it and create a completely new bundle descriptor instead. Basically the workspace bundles list needs to be refreshed to see the correct bundle. Try refreshing or closing the editor and re-opening it. This usually solves the problem. Once you can see the correct workspace bundle you can drag it over instead.

The reason for this is: Let's say your project is named A and you created a bundle descriptor named B. You ended up with a jar named A.B.jar. The available bundles section might have not refreshed and it still has an A.jar in it. If you drag that A.jar over to the running bundles section you have essentially loaded a bundle that doesn't exist anymore. The errors message will say something to the effect to, if you stop and restart the framework.

### org.apache.jasper.JasperException: The absolute uri: http://java.sun.com/jsp/jstl/core cannot be resolved in either web.xml or the jar files deployed with this application
Ah ha! Caught you skipping steps....just kidding. This happens because the bundle didn't either import the correct namespace or didn't import one at all. Make sure you're JSP bundle imports **org.apache.taglibs.standard.lang.jstl**. You can see in the Testing Taglibs Section of this tutorial were we do that.

### I added a bunch of bundles to the local repository but they aren't showing up in the available bundle section.
Yeah this one bothers me all the time. Just restart Eclipse. It's the easiest way to get it to work. I've found that single drags into the local repository don't usually cause a problem but as soon as I multi-drag I have to restart to see them. I just got used to doing it.