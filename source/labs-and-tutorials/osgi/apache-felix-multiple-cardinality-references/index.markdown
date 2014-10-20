---
layout: page
title: "Apache Felix - Multiple Cardinality Reference"
date: 2014-10-20 10:01
comments: true
sharing: true
footer: true
indexer: true
---
In the [last example](/labs-and-tutorials/osgi/apache-felix-maven-to-bndtools), we made a simple greeter interface and a hello world class that used it in Bndtools. A Gogo shell command was then used to call the service and get back a string greeting. The reference was bound using the @Reference annotation in the command class and we stored this information into a single field. But, what would happen if many greeters were registered as services? What if all of them were needed?

In this tutorial, we will change our project to support multiple cardinality references and make the command to run all the greeters that it finds when it's typed.

## A Quick Review ##

The original command used the @Reference annotation. It used this annotation to set a field called greeter to the first service it found. Our class looked like this:

```java The Original Greeter Gogo Command
package org.bhn.training.commands;

import org.bhn.training.api.Greeter;

import aQute.bnd.annotation.component.Reference;

public class GreetCommand {
    private Greeter greeter;

    @Reference
    public void setGreeter(Greeter greeter){
        this.greeter = greeter;
    }

    public void greet(){
        System.out.println(greeter.greet());
    }
}
```
This works great if we think that only one greeter will ever be registered as a service. But, 
what if we wanted the command to save all of the greeters it found and use them all?

## Modify The Command To Take Multiple References ##
We need to change our field to store more than one service at a time. So lets change it to a list of greeters. That means:
```java Original Field Declaration
private Greeter greeter;
```

becomes

```java New Field Declaration
private List<Greeter> greeters;
```

Now we need to change the signature of the ```setGreeter``` method. We need to have Felix give us all the references one at a time that it finds. To do that change the signature:

```java Original Reference Property
@Reference
public void setGreeter(Greeter greeter){
     this.greeter = greeter;
}
```

to

```java New Reference Property
@Reference(multiple=true,dynamic=true)
public void setGreeter(Greeter greeter){
	if(this.greeters == null){
		this.greeters = new ArrayList<Greeter>();
	}
	System.out.println(&quot;Adding &quot;+greeter.getClass().getName());
	this.greeters.add(greeter);
}
```

_Note: You can see the effects of this annotation change if you later break apart the component.xml file it generates. Inside you will notice that multiple=true sets the cardinality to "1..n" and the dynamic=true sets a property called policy to "dynamic". The final component.xml file is shown at the end of this tutorial for reference._

## Create Unbind Method
When we just had a single field, it was ok for the variable to hold a reference or a null. However, now that we are dealing with a collection of references, we need this field to have zero to many references, but never null. To do this, we need to create a method that will remove references from the collection when they are removed from OSGi. 

Lets add a ```unsetGreeter``` method to unbind the references. The name alone is enough for the framework to find it. The framework is smart enough to know that if the method was named ```setSomething``` then ```unsetSomething``` must be the opposite. 

```java Creating Method To Unbind References
public void unsetGreeter(Greeter greeter){
     System.out.println(&quot;Removing &quot;+greeter.getClass().getName());
     this.greeters.remove(greeter);
}
```
Since these methods represent a property, set and unset are the best ways to name them. However, if you wanted to have unset be named something out there, you can. Adding the unbind property to the annotation will let you use whatever you would like. An example is:

```java Using The Unbind Property
@Reference(multiple=true,dynamic=true,unbind=wackaWackaMethod)
``` 
Now that we are done with the shell command, the class looks like this:

```java The Final Gogo Command
package org.bhn.training.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.service.command.CommandProcessor;
import org.bhn.training.api.Greeter;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(properties={
		CommandProcessor.COMMAND_SCOPE+&quot;:String=tutorial&quot;,
		CommandProcessor.COMMAND_FUNCTION+&quot;:String=greet&quot;
},provide=Object.class)
public class GreetCommand {
	private List<Greeter> greeters;
	
	@Reference(multiple=true,dynamic=true)
	public void setGreeter(Greeter greeter){
		if(this.greeters == null){
			this.greeters = new ArrayList<Greeter>();
		}
		System.out.println(&quot;Adding &quot;+greeter.getClass().getName());
		this.greeters.add(greeter);
	}
	
	public void unsetGreeter(Greeter greeter){
		System.out.println(&quot;Removing &quot;+greeter.getClass().getName());
		this.greeters.remove(greeter);
	}
	
	public void greet(){
		for(Greeter greeter : greeters){
			System.out.println(greeter.greet());
		}
	}
}
```

This is all we needed to do to make our bundle operate like before, but I promised to show mulitple references. In order to do that we need more greeters.

Note: In the next sections I'm going to breeze through creating a new greeter bundle. If you get stuck and can't remember how to do it in Bndtools, the [previous tutorial](/labs-and-tutorials/osgi/apache-felix-maven-to-bndtools) had sections that can help.

## Create An Alternate Implementation
Using Eclipse and Bndtools, create a new Bundle Descriptor. Name this one ```greeter-bundle-impl2``` and inside it create the following implementation.:

```java Alternate Greeter Implementation
package org.bhn.training.impl2;

import org.bhn.training.api.Greeter;

import aQute.bnd.annotation.component.Component;

@Component
public class HelloUniverseGreeter implements Greeter {

	@Override
	public String greet() {
		return "Hello Universe!";
	}

}
```
Now add this new implementation to the private packages of its ```greeter-bundle-impl2.bnd``` file and set the Declarative services to _Bnd Annotations_.

## Setup And Run
Now in our ```bnd.bnd``` file again, click on the run tab and drag our new ```greeter-bundle-impl2``` file into the run bundles, if it isn't already there. When you click on the Run OSGi button now and run the tutorial:greet command, notice what you see:

```
___________________________
Welcome to Apache Felix Gogo

g! tutorial:greet
Adding org.bhn.training.impl.HelloWorldGreeter
Adding org.bhn.training.impl2.HelloUniverseGreeter
Hello World!
Hello Universe!
Removing org.bhn.training.impl.HelloWorldGreeter
Removing org.bhn.training.impl2.HelloUniverseGreeter
g! 
```
When the command was ran, the command found two services and set them in the collection. It then called both of them. Once complete the service references were unset. In most cases this is OK. But what if we wanted the greeters to be loaded and remain loaded as long as the command was registered? 

Lets change this behavior by telling the framework that the command should be activated immediately. To do this we need to add one more property to the command's annotation:

```java Binding Immeadiately
@Component(properties={
		CommandProcessor.COMMAND_SCOPE+&quot;:String=tutorial&quot;,
		CommandProcessor.COMMAND_FUNCTION+&quot;:String=greet&quot;
},provide=Object.class,immediate=true)
```

Notice after the Object.class we added a new property for ```immediate=true```. Now lets run this again.

```
____________________________
Welcome to Apache Felix Gogo

g! Adding org.bhn.training.impl.HelloWorldGreeter
Adding org.bhn.training.impl2.HelloUniverseGreeter

g! tutorial:greet
Hello World!
Hello Universe!
g! 
```

When the bundle started, the references were bound immediately. They will now stay that way unless the implementation bundles are stopped or uninstalled. If you stop impl or impl2 in the console, watch what happens. This dynamic setting and unsetting is what makes OSGi so powerful.

## A Peek Into The Component XML File ##
So the annotation changes we made did some interesting things to the reference section of the component file. You can see it by double clicking on the greeter-bundle-commands.jar. It now looks like this:

```xml The Component XML File Now
<component name="org.bhn.training.commands.GreetCommand" immediate="true">
  <implementation class="org.bhn.training.commands.GreetCommand"/>
  <service>
    <provide interface="java.lang.Object"/>
  </service>
  <reference name="greeter" cardinality="1..n" policy="dynamic" interface="org.bhn.training.api.Greeter" bind="setGreeter" unbind="unsetGreeter"/>
  <property name="osgi.command.function" type="String" value="greet"/>
  <property name="osgi.command.scope" type="String" value="tutorial"/>
</component>
```

We can now see the immediate attribute is filled out on the component. We can also see the cardinality, policy and unbind attributes have been filled out. When makes the component.xml file by hand, it's important to note that these attributes are not always named the same as the annotations. Nor are the values of these attribute the same. 

##Summary##
In this tutorial, we changed an existing project to support multiple reference binding. By doing so, we asked for every service that implemented a specific interface to be bound. In our next tutorial we will learn more about the concept of filtering references so that we can keep certain references from binding, yet allow others.