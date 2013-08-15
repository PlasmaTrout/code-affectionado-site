---
layout: post
title: "Apache Felix - First Bundle The Programmatic Way"
date: 2013-08-15 14:26
comments: true
categories: [OSGi, Apache Felix]
---
This tutorial is designed to guide the reader through creating a very simple bundle example. For simplicity we will use the commit Greeter/Greet examples that are seen very often in OSGi tutorials. Since this is a classroom taught course, usually done in corporate environments, it's rather targeted on a specific IDE. However, if you wish to use an alternate environment the instructions should be diverse enough to support building on alternate platforms.

Requirements
-----
-	Apache Felix (See [Starting From Scratch](/osgi/2013/08/09/felix-from-the-ground-up))
-	Apache Maven
-	IDE Of Choice (Preferably IntelliJ IDEA)
-   Internet Connection (For Maven Repositories)

Audience
-----
Typically, this is Lab #2 in a classroom environment, however anyone that wishes can use this tutorial to create a tutorial bundle. The typical audience already understands what OSGi is and that Apache Felix is just one implementation of an OSGi framework.

What We Are Going To Do
-----
1. Generate A Maven Quickstart Java Project
1. Change The POM
1. Add Some Code
1. Build And Deploy Our Bundle To Apache-Felix
1. Create A Gogo Command To Test The Code

Generate A Maven Quickstart Java Project
-----
We will use the command line to generate a new maven project(Using the IDE would take too many pictures and overhead from the presentation). First, create a directory that you wish to place the project in, we will just use our $HOME/projects directory in the examples. Then run the following command:

{% highlight bash %}
mvn archetype:generate -DgroupId=org.bhn.training -DartifactId=greeter-bundle -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
{% endhighlight %}

If you then open up IDEA and import an existing Maven project, you can select the new directory this command creates. At this point you have a very simple hello world application. You can compile and run this if you want, we are going to change it significantly though in the next few sections.

Change The Pom
-----
If we want to build bundles instead of plain ole JARS we have to modify the plugins of the POM to support building a new type of packaging. While we are modifying the POM we really should add some dependencies to, just because we will need to write some code later that will require a few specific APIs.

Your starter project has a very simple minimal POM (which is why we chose it). So lets first add the plugins and configure then for making OSGi bundles. The main plugin we will need to add and configure is the maven-bundle-plugin. Its job is to create the necessary manifest additions needed. So just before the dependencies line, add in the following plugin:

{% codeblock POM Modification lang:xml %}
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
{% endcodeblock %}

This plugins job is just to take the JAR and add the configuration instructions in MANIFEST.MF in the appropriate way. The only real difference between a JAR and an OSGi Bundle JAR is just some new attributes added to the manifest itself. In this case we are doing the bare minimum (for now). We also need to add some dependencies so lets add the following ones (if junit is in your dependencies already just delete it):

{% codeblock POM Dependencies lang:xml %}
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
{% endcodeblock %}

First we add org.osgi.core and then we just add a current version of junit for testing. That should be all we need for now. There is one last thing we need to do though. See that link towards the top of your POM that specifies that packaging should be JAR? Lets change that to bundle.

{% codeblock lang:xml %}
<groupId>com.bhn.training</groupId>
<artifactId>greeter-bundle</artifactId>
<packaging>bundle</packaging>
{% endcodeblock %}

Now our plugin will kick in and give us the correct output. If we run a **mvn package** we should get a build success and just o make sure our plugins are working lets go into the target directory of your project. There you should see a new jar file names greeter-bundle-1.0-SNAPSHOT.jar or something similar. If you execute the command:

{% codeblock %}
jar -xvf greeter-bundle-1.0-SNAPSHOT.jar META-INF/MANIFEST.MF
cat META-INF/MANIFEST.MF
{% endcodeblock %}

You should see now that the manifest included with this JAR is a bit different from a normal JAR. For instance a normal JAR might look something like this:

{% highlight bash %}
Manifest-Version: 1.0
Archiver-Version: Plexus Archiver
Created-By: Apache Maven
Built-By: PlasmaTrout
Build-Jdk: 1.6.0_51
{% endhighlight %}

However our new one, ends up very similar to:

{% highlight bash %}
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
{% endhighlight %}

Notice the extra entries? These will be become crucial to understand later, but just know that since our plugin really only specified two (name and symbolic name), quite a few more were actually written to the manifest by default thanks to the maven plugin.

Add Some Code
-----
Now that we can build like a bundle, we should be writing some code to act like one. First lets create two new packages, lets create a org.bhn.training.api and an org.bhn.training.impl. This will allow us to minimally keep our APIs and Implementations seperate for organizational purposes only. In a perfect world these really will be seperate bundles, but thats out of scope for this tutorial.

In our **api** package lets create an interface called Greeter. Greeter should look like the following:

{% codeblock Greeter API Interface lang:java %}
package org.bhn.training.api;

public interface Greeter {
    String greet();
}
{% endcodeblock %}

As you can see it, really won't do much, but it gets the point across. Now lets go to our **impl** package and add an implementation class SimpleStringGreeterImpl.java with the code:

{% highlight java %}
package org.bhn.training.impl;

import org.bhn.training.api.Greeter;

public class SimpleStringGreeterImpl implements Greeter {
    @Override
    public String greet() {
        return "Hello World!";
    }
}
{% endhighlight %}

Now that we have an implementation of a simple service. What we need to do is write some code to register that service in whats called a service registry. Then anyone who needs to be greeted can simple use the interface to get an instance to the service. 

The programmatic way to do this is to create an Activator. An activator in a bundle is represented as a class that implements the interface [BundleActivator](http://www.osgi.org/javadoc/r4v43/core/org/osgi/framework/BundleActivator.html). When the bundle is activated it calls methods found in the class dynamically. This exposes two methods that allow us to register and unregister services programmtically. Lets change that App.java class that sits in the main level to be our Activator.

First rename App.java to SimpleActivator.java (It should be SimpleStringGreeterActivator buts thats too verbose for the tutorial right now). Then change it to looks like the following:

{% highlight java %}
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
{% endhighlight %}

See the two methods it exposed? We will put some code in here in a second, but before we forget lets add the appropriate manifest entry so that the framework knows where this is. In the plugin that we set up earlier add this element to the instructions element:

{% highlight xml %}
<Bundle-Activator>org.bhn.training.SimpleActivator</Bundle-Activator>
{% endhighlight %}

This manifest entry tells the framework what class the activator is in so it can dynamically call the interface methods start and stop for it. If we build and checkout the manifest now we will see:

{% highlight xml %}
Manifest-Version: 1.0
Bnd-LastModified: 1376421388797
Build-Jdk: 1.6.0_51
Built-By: JSDowning
Bundle-Activator: org.bhn.training.SimpleActivator
... bunch of other stuff ...
{% endhighlight %}

Good, we can see our Activator in there, lets put some code in it. The steps involved in registering a service are pretty simple.

1. We get a reference to the BundleContext
1. We use registerService to keep the ServiceRegistration
1. When done we use the ServiceRegistration to unregister the service.

{% highlight java %}
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

{% endhighlight %}

So the gist of this is that the service registers our Implementation class under the string name of the interface. This means anyone who wants a reference to this service can use the interface to get it. It's important to note that right now, it won't work because even though our interfaces are public, they won't be exposed in the framework. We must explicitly tell OSGi to expose our interfaces. Let's do that so we can call it later. The easiest way to do this is to export the interfaces namespace, so lets add this line to the plugin:

{% highlight xml %}
<Export-Package>org.bhn.training.api</Export-Package>
{% endhighlight %}

This tells the framework to allow other bundles to see this namespace. Without this there would be know way a caller could get a reference to our service, because there would be no interface found called org.bhn.training.api.Greeter to lookup. Notice that the impl class is never exposed. There is absolutely NO method here that someone could instantiate a new SimpleStringGreetingImpl(). Even though its public, it's not exposed by the framework. This gets to the heart of why OSGi is so modular. It's relatively impossible to couple your classes together without some seriously bad practices.

Deploy Our Bundle
-----
We are going to use the command line to deploy it, but you don't have to. If you still have the web interface from the previous tutorial you can update and install it from there as well. But from the command line it looks like:

{% highlight bash %}
g! felix:install file:/Users/PlasmaTrout/Projects/greeter-bundle/target/greeter-bundle-1.0-SNAPSHOT.jar
Bundle ID: 13

g! felix:start 13
{% endhighlight %}


Create A Gogo Shell Command
-----
It's started but we still have no way of seeing it. Lets create a way to actually call it. In other tutorials we talked about the Gogo shell and how extensible it is, but lets create a new command just to demonstrate how easy it is to expose our interface as a command as well. All we need to do to make this happen is add two new properties to our service. Lets change our startup to be:

{% highlight java %}
public void start(BundleContext bundleContext) throws Exception {
      Hashtable<String,Object> props = new Hashtable<String, Object>();
      props.put("osgi.command.scope","tutorial");
      props.put("osgi.command.function",new String[] { "greet" });
      registration = bundleContext
              .registerService(org.bhn.training.api.Greeter.class.getName(),
              new SimpleStringGreeterImpl(),props);
}
{% endhighlight %}

Using these two properties, you can expose any class method as a Gogo shell command. The first specifies the scope of the command, which is just a clever way to avoid conflicts with other command named the same. The second is the method to call and command name wrapped it one. So calling **tutorial:greet** should cause a hello world to appear. Lets **mvn package** again then run an update on our bundle. My bundle was 13 so typing **update 13** will reinstall from the installed location. Now if you look at your command list by typing **help** you should have a **tutorial:greet** command visible. Type it and see what happens:

{% highlight bash %}
g! tutorial:greet
Hello World!
g!
{% endhighlight %}

This still isn't a very good test of the service from a consumer standpoint. You really would want to test that the service is in the registry and that you can ask for it by interface name. If you want to do that, create a new package named org.bhn.training.commands and put a class called GreeterCommands in it. Should end up like the following:

{% highlight java %}
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
{% endhighlight %}
This code shows a programmatic method to call a service from code. We will expound on this somewhat later because there is a lot easier ways of doing this, but this way represents the classic steps to OSGi service calling:

1. Get the BundleContext
2. Get the ServiceReference
3. Use the ServiceReference to get the Service
4. Call The Service

Next we need ot change the startup to register two services instead of one like so:

{% highlight java %}
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
{% endhighlight %}

Now after an **felix:update** you can call your command again and witness that its actually calling the service using the OSGi API. 

_Note: After looking at the Activator imagine you have more than two services to register. Now picture 10 or more. It can get a little verbose to register all these programmatically. Let's fix that. In the next tutorial we will convert this to use an xml file to declare the service components. This will improve the readability considerably._