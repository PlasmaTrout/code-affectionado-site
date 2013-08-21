---
layout: page
title: "Apache Felix - First Bundle The Programmatic Way"
date: 2013-08-19 13:59
comments: true
sharing: true
footer: true
indexer: true
---
OSGi has been a mature framework for building modular applications for quite some time. It has however, not not been embraced fully by the development community. The primary reason it stays unpopular in the development arena lies in its lack of beginning level tutorials or classes. This tutorial begins a series of labs to demystify the OSGi framework and begin familiarity with Apache Felix.

The purpose of this lab is to show the "code only" way to create a modular component for use in the OSGi framework. Understanding the manual way to do this assists in explaining the more abbreviated ways later.

## Audience 
The core audience is seated in a comfortable classroom or conference room environment. Readers performing this tutorial have received an overview of OSGi and completed a previous lab in which we setup a framework for use. A reader performing this tutorial would have a printed version of this media or the web site up on one screen while they work on code in another. The audience can be made up of developers from different disciplines but knowledge and understanding of the Java language is assumed.

## Generate A Maven Quick Start Java Project
First, create a directory to place the project in. Using the $HOME/projects directory would more closely match what will be shown in the examples, but any directory will do. After changing into the directory, run the following command:

```bash Starting A Quick Start Maven Project
mvn archetype:generate -DgroupId=org.bhn.training -DartifactId=greeter-bundle -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

At this point Maven has created a very simple hello world application. It shouldn't do much if it's compiled and run, but it will be modified heavily the next steps anyways.

## Change The POM
Building bundles instead of JARS requires modification of the plugins declared in the POM to support building a new type of packaging. 

The starter project has a very simple POM (which is why it was chosen). It really needs to be modified to support our new project type. So lets first add the appropriate maven plugin and configure it for making OSGi bundles. The main plugin needed to  create OSGi bundles is the maven-bundle-plugin. In order to set it up, add the following plugin:

```xml POM Modification
 <build>
    <plugins>
        <plugin>
            <groupId>org.apache.felix</groupId>
            <artifactId>maven-bundle-plugin</artifactId>
            <version>2.3.7</version>
            <extensions>true</extensions>
            <configuration>
                <instructions>
                    <Bundle-Name>${project.name}</Bundle-Name>
                    <Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
                </instructions>
            </configuration>
        </plugin>
    </plugins>
</build>
```

This plugins job is to expand the configuration instructions in JAR files manifest to include new bundle specific ones. The only real difference between a JAR and an OSGi Bundle is just some new attributes added to the manifest itself.

## Starter Bundle Dependencies
The bundle won't do very much without a few dependencies being added. Add the following starter dependencies into the dependencies section of the POM:

```xml POM Dependencies lang:xml
<dependency>
    <groupId>org.apache.felix</groupId>
    <artifactId>org.osgi.core</artifactId>
    <version>1.4.0</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.8.2</version>
</dependency>
```

The org.osgi.core library is needed so references to the API can be used. JUnit is only there for future labs. It will come into play during the Pax Exam examples later.

```xml Change Packaging Type lang:xml
<groupId>com.bhn.training</groupId>
<artifactId>greeter-bundle</artifactId>
<packaging>bundle</packaging>
```

The bundle packaging definition will trigger our bundle plugin. If we run a **mvn package** now, we should get a build success. If so, this verified our plugins are working.

## Examining The JARS
Lets go into the target directory of your project. There you should see a new jar file names greeter-bundle-1.0-SNAPSHOT.jar or something similar. If you execute the command:

```bash Unpack And Examine Manifest
jar -xvf greeter-bundle-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF
cat META-INF/MANIFEST.MF
```

You should see now that the manifest included with this JAR has some Bundle related properties. This differs quite a bit from a normal JAR manifest. For instance a normal JAR might look something like this:

```bash Normal JAR Manifest
Manifest-Version: 1.0
Archiver-Version: Plexus Archiver
Created-By: Apache Maven
Built-By: PlasmaTrout
Build-Jdk: 1.6.0_51
```

However our new one, ends up very similar to:

```bash OSGi JAR Manifest
Manifest-Version: 1.0
Bnd-LastModified: 1376419554357
Build-Jdk: 1.6.0_51
Built-By: PlasmaTrout
Bundle-ManifestVersion: 2
Bundle-Name: greeter-bundle
Bundle-SymbolicName: greeter-bundle
Bundle-Version: 1.0.0.SNAPSHOT
Created-By: Apache Maven Bundle Plugin
Export-Package: org.bhn.training;version="1.0.0.SNAPSHOT"
Tool: Bnd-1.50.0
```

Notice the extra entries? These will be become easier to understand later, but just know that since our plugin really only specified two (name and symbolic name), the plugin used some default values to fill our the rest for us.

Writing Our Interface
-----
In a normal development workflow when building bundle, determination of what will be exposed as our API and what we will hide is first major design decision we should make. After this is done, we then should create an API bundle and an implementation bundle that implements our API. We almost always want to deploy them separately. The API becomes our public facing aspects and the implementation never gets exposed.

For the sake of making the tutorial simple however, we aren't going to separate our API from our interface just yet. Instead lets just create two different packages to put our code in. 

Under the root namespace org.bhn.training add two new packages one named api and the other named impl. We should now have:

1. org.bhn.training.api
1. org.bhn.training.impl

In our **api** package lets create an interface called Greeter. Greeter should look like the following:

```java Greeter API Interface
package org.bhn.training.api;

public interface Greeter {
    String greet();
}
```

As you can see it, really won't do much. It will simply have a method called greet that will return a string if you call it with no arguments.

## Writing The Implementation

Now lets go to our **impl** package and add an implementation class named SimpleStringGreeterImpl.java to the project:

```java Greeter Implementation lang:java
package org.bhn.training.impl;

import org.bhn.training.api.Greeter;

public class SimpleStringGreeterImpl implements Greeter {
    @Override
    public String greet() {
        return "Hello World!";
    }
}
```

## A Quick Note On Services
The term service is somewhat abstract so lets attempt to explain this using a very simple example. Suppose we implemented this as a normal set of java libraries. Even if we separated the api from the implementation in different packages, we still have the same core problem. We want people to use our API by using the interface, and we don't want the "new" keyword to be used against the implementation. With no modification we are stuck with a situation in which a caller would have to do:

```java
Greeter greeter = new SimpleStringGreeterImpl();
```

Inside of their classes. This presents an immediate problem by coupling the callers class directly to the implementation class. If we needed to change the implementation, say to a RandomStringGreeterImpl we wouldn't be able to do so to everyone using greeter.

In order to better enhance this, most teams would create a factory interface and implementation so that they themselves could be in control of the implementation details. This ends up like:

```java
GreeterFactory factory = new GreeterFactory();
Greeter greeter = factory.getInstance();
```

Better right? The downside is the GreeterFactory is now coupled. Many different injection possibilities exist to further help here, but all they really end up doing is moving the same issue to a different domain (like a config file). In the end, after all of that work though, someone will still use this in a class:

```java
SimpleStringGreeterImpl impl = new SimpleStringGreeterImpl();
```

Regardless of your pattern, the implementation still had to be public, therefore it still was visible to the caller.

OSGi takes a unique approach to this. The bundles can declare what they will expose and what they will not at a namespace or package level. This means we can expose just the interface and nothing else. But wait a minute. If thats the case how do we get the implementation? We ask the service registry to give us an implementation based on the interface that we have and it does. It does so without us every knowing what the implementation was (though we can find out).

This is how we define services.


## Creating An Activator
If we wanted to use our interface as a very simple service. What we need to do is register that service in a service registry. Then callers who have our interface can ask the service registry to give them an implementation that uses the interface. 

The programmatic way to do this is to create an Activator. An activator in a bundle is represented as a class that implements the interface [BundleActivator](http://www.osgi.org/javadoc/r4v43/core/org/osgi/framework/BundleActivator.html). This interface exposes two methods that allow us to register and un-register services programmatically. 

Let's rename App.java to SimpleActivator.java (It should be SimpleStringGreeterActivator buts that's too verbose for the tutorial right now). Then change it to look like the following:

```java Blank Activator Class
package org.bhn.training;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SimpleActivator implements BundleActivator
{
    @Override
    public void start(BundleContext bundleContext) throws Exception {
      // What to do when it starts
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
      // What to do when it stops
    }
}
```
See the two methods it exposed? We will put some code in here in a second, but before we forget lets add the appropriate manifest entry so that the framework knows where this is. In the plugin that we set up earlier add this element to the instructions element:

```xml Activator Instruction
<Bundle-Activator>org.bhn.training.SimpleActivator</Bundle-Activator>
```

This manifest entry tells the framework what class the activator is in so it can dynamically call the interface methods start and stop for it. If we build and checkout the manifest now we will see:

{% codeblock Manifest Revisited %}
Manifest-Version: 1.0
Bnd-LastModified: 1376421388797
Build-Jdk: 1.6.0_51
Built-By: JSDowning
Bundle-Activator: org.bhn.training.SimpleActivator
... bunch of other stuff ...
{% endcodeblock %}

## Registering Services
Now that we have an Activator, we must put some code in the start and stop methods to handle our service registration. The steps involved in registering a service are pretty simple.

1. We get a reference to the BundleContext
1. We use registerService to create a ServiceRegistration
1. When done we use the ServiceRegistration to un-register the service.

```java Fully Functioning Activator
package org.bhn.training;

import org.bhn.training.impl.SimpleStringGreeterImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Hashtable;

public class SimpleActivator implements BundleActivator
{
    ServiceRegistration registration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
      Hashtable<String,Object> props = new Hashtable<String, Object>();
      registration = bundleContext
      	.registerService(org.bhn.training.api.Greeter.class.getName(),
        	new SimpleStringGreeterImpl(),props);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
      registration.unregister();
    }
}
```
## Exposing Our Interface Package
Then final thing we must do, in order to have our API visible, is explicitly tell OSGi to expose our interfaces. This can easily be done by adding this line to the maven plugin instructions:

```xml Exposing Our API In Maven
<Export-Package>org.bhn.training.api</Export-Package>
```

This tells the framework to allow other bundles to see the package org.bhn.training.api. Since our interfaces are in that package, they will be exposed when we deploy it.

Notice that the impl class is never exposed. There is absolutely NO method here that someone could instantiate a new SimpleStringGreetingImpl(). Even though its public, it's not exposed by the framework. This gets to the heart of why OSGi is so modular. It's relatively impossible to couple your classes together without bad practices.

Deploy Our Bundle
-----
We are going to use the command line to deploy it, but you don't have to. If you still have the web interface from the previous tutorial you can update and install it from there as well. But from the command line it looks like:

```bash Deploying A Bundle Using Felix Command Line
g! felix:install file:/Users/PlasmaTrout/Projects/greeter-bundle/target/greeter-bundle-1.0-SNAPSHOT.jar
Bundle ID: 13

g! felix:start 13
```


Create A Gogo Shell Command
-----
Our bundle was started but we still have no way of seeing it. Lets create a way to actually call it. We can use the Gogo shell and create a command by creating a class and method and then using our service registry again.

Create a new package named org.bhn.training.commands and put a class called GreeterCommands in it. Should end up like the following:

```java Making A Command Class
package org.bhn.training.commands;

import org.bhn.training.api.Greeter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import static org.osgi.framework.FrameworkUtil.*;

public class GreeterCommands {

    public String greet(){
        BundleContext ctx = getBundle(org.bhn.training.api.Greeter.class)
                .getBundleContext();
        ServiceReference ref = ctx.getServiceReference(
                org.bhn.training.api.Greeter.class.getName());
        Greeter greeter = (Greeter) ctx.getService(ref);
        return greeter.greet();
    }
}
```

This code shows the manual way to call a service from code. We will improve this later because there are easier ways of doing this, but this way at least demonstrates the classic steps of OSGi service calling:

1. Get the BundleContext
2. Get the ServiceReference
3. Use the ServiceReference to get the Service
4. Call The Service

Next we need to change the startup to register two services instead of one like so:

```java Registering Two Services
public void start(BundleContext bundleContext) throws Exception {
      Hashtable props = new Hashtable();
      props.put("osgi.command.scope","tutorial");
      props.put("osgi.command.function",new String[] { "greet" });
	
      Hashtable noprops = new Hashtable();

        registration = bundleContext
              .registerService(org.bhn.training.api.Greeter.class.getName(),
              new SimpleStringGreeterImpl(),noprops);
        command = bundleContext.registerService(
        	org.bhn.training.commands.GreeterCommands.class.getName(),
            new GreeterCommands(),props);

}
```

Now after an **felix:update** you can call your command again and witness that its actually calling the service using the OSGi API. 

_Note: After looking at the Activator imagine you have more than two services to register. Now picture 10 or more. It can get a little verbose to register all these in code. Let's fix that. In the next tutorial we will convert this to use an xml file to declare the service components. This will improve the readability considerably._