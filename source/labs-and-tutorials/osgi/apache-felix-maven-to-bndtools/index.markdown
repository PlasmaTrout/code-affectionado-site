---
layout: page
title: "Apache Felix - From Maven/SCR To BndTools"
date: 2014-09-1 10:01
comments: true
sharing: true
footer: true
indexer: true
---
In a [previous tutorial](/labs-and-tutorials/osgi/apache-felix-scr-annotations) we built a project and tested it utilizing maven. While maven is easy 
to learn when making simple bundles, it can be much more difficult to manage when projects
have many bundles or when projects separate their API and Implementations frequently.
A product called Bndtools streamlines the building of OSGi components and provides a significant number of
 tools that help in this arena. In this tutorial the original simple greeter service is going to
  be rebuilt as a Bndtools project.

## Getting Bndtools
Bndtools is an Eclipse tool set. Using the Eclipse marketplace is the preferred way of installing it.
 The instructions are found at http://bndtools.org/installation.html.
 
## Create a New Project
To demonstrate Bndtools, this tutorial will start all over again with the uber simple greeter service
 used in previous posts. Start fresh with a new work-space and new project (I called my
  work-space `osgibndtutorial` and called the project `greeter-bundle`). Choose an "Empty Project" as
   the template and then finish.

_Note: If your prompted to create a configuration project, go ahead and create one._

## Create the API
Bndtools make separating bundles really easy. From one project you can have as many bundles as you would like built from it.  From the beginning of this tutorial we are going to start all over again and this time separate our bundles cleanly.

### Create an API Bundle
Bndtools, handles bundle creation a bit differently than previous tutorials. For each 
bundle created,  a bundle descriptor will be needed. This is used to build, test and configure 
each bundle independently of one another. Right click the project and choose New -> Bundle 
Descriptor and name it `greeter-bundle-api`:

{% img /images/bndtools/Capture.PNG 'The Bndtools Dialog' 'The Bndtools Dialog' %}

Once finished, in the directory of your project labeled `generated`, locate a created jar file called `greeter-bundle-api.jar`. Each time a bundle descriptor is created a new bundle jar file will be created and synchronized in this directory for you.

_Note: Double clicking the bnd descriptor file opens a configuration panel that's used to add packages the the bundle. By default, it's empty._

_Note: Double click any jar file to see whats inside of it._

### Create an Interface
In the projects `src` directory, recreate the `Greeter.java` interface again:

```java A New Interface For Our API
package org.bhn.training.api;

public interface Greeter {
	String greet();
}
```

In the previous tutorials we had to use the jar command to open up our bundles jar files to see what was inside.
Fortunately those days are over. A handy utility was installed into eclipse  that shows jar file contents without having to use the command line. Now if we double click the `greeter-bundle-api.jar`, we can see what's inside.

{% img /images/bndtools/Capture2.PNG 'The Jar File Viewer In Action' %}

Notice that the `MANIFEST.MF` file is the sole resident of the jar file. In order to get the newly
created interface in there the `greeter-bundle-api.bnd` configuration needs to be modified.

### Configuring the API Bundle ###

If we double click on the `greeter-bundle-api.bnd` file in the base directory of the project, we are presented with a new dialog. In the export packages section, click on the plus sign in the top right corner. The only option right now is the package that was just created. Double click the api package name in the dialog:

{% img /images/bndtools/Capture3.PNG 'Exposing Our API' %}

Now hit save all and double click the `greeter-bundle-api.jar` file again and examine the contents:

{% img /images/bndtools/Capture4.PNG 'API Jar Revisited' %}

Notice how the created interface package now appears in the jar file and the manifest is set to export it. All this with a few clicks of a button.

## Creating an Implementation
Right click on the project again and create a new bundle descriptor. This time call it `greeter-bundle-impl.jar`. Notice when done, that there are two jar files now in the generated directory. One for the API bundle and another for the new implementation bundle.


### Create a Greeter Implementation

Create a new class called `HelloWorldGreeter` and make it implement the `Greeter` interface:

```java Our New Greeter Implementation
package org.bhn.training.impl;

import org.bhn.training.api.Greeter;

public class HelloWorldGreeter implements Greeter {

	@Override
	public String greet() {
		return "Hello World";
	}

}
```

### Annotate the Implementation
In one of the previous tutorials, SCR annotations were used to markup classes. In Bndtools we are going to use a different set of annotations. 

Double click on the `bnd.bnd` file that was created when the project was created. Once it appears, click on the plus sign next to build path text box and add the `biz.aQuote.bnd.annotation` package. After a quick save, make sure the screen shows something similar to the following:

{% img /images/bndtools/Capture5.PNG 'Build Settings' %}

_Note: Remember how to do this. It will become common to use this dialog to add jars to the path. You may wonder where this list was populated from. If you look at the cnf folder that was created when you created the project, you will see a few directories in there. Some actually have the jars needed to build your projects but some are xml configurations that specify external repositories. Bndtools uses these repositories to make other bundles and libraries available to you in the tool. You can add repositories to this list as well, but that's out of the scope of this specific tutorial._

In the `HelloWorldGreeter` implementation add the following annotation to the class declaration:

```java Our New Annotation
@Component
public class HelloWorldGreeter implements Greeter {
```

_Note: You may be thinking, wait...that's it? Yep. That's it. The annotations for bndtools were well thought out ahead of time. They don't need you to tell them what interface to expose since you obviously implemented it and being a component must mean you want declarative services to register it for you._

### Adding The Implementation To The Bundle
Double click the `greeter-bundle-impl.bnd` file again and add this bundle to it as a private package. In the declarative services pull down select the Bnd Annotations and verify the screen looks like:

{% img /images/bndtools/Capture6.PNG 'Implementation Bundle Settings' %}

After a save all, peek into the jar again. See that the Service-Component descriptor and the xml file were written automatically.

## Remaking The Gogo Command
Before we recreate the original Gogo command from a previous tutorial, create a new bundle descriptor named `greeter-bundle-commands`. Once setup, double click the `bnd.bnd` file again and this time add a build path entry of `org.apache.felix.gogo.runtime`.

When complete, create a new class called GreeterCommand.java and make it look like the following:

```java Our new Greeter
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

The reference annotation seen above automatically calls the service registry and gets the service injected into the method.

_Note: You may ask what if more than one is registered? Well it will still get one. Which one depends on the implementation of the framework. It is possible to filter what service that will be chosen. It's also possible to get all of them at once, which we will do in the next tutorial._

### Annotating The Command
The greeter command doesn't have an interface, per say. So we will use a trick here to ensure it always gets registered by telling the framework it provides the Object class as its interface. To do that, some properties and alternate configurations for the component must be specified in the annotation:

```java Annotation Our Command
@Component(properties={
		CommandProcessor.COMMAND_SCOPE+":String=tutorial",
		CommandProcessor.COMMAND_FUNCTION+":String=greet"
},provide=Object.class)
public class GreetCommand {
```

### Bundling The Command
In the `greeter-bundle-command.bnd` file. Add the command package as a private package and set the Declarative Services to Bnd Annotations. After a quick save, verify the screen looks like the following:

{% img /images/bndtools/Capture7.PNG 'Bundling The Command' %}

## Running The Bundles
Double click the `bnd.bnd` file and go to the run tab. Drag the following bundles from the available bundles section to the run bundles section:

* org.apache.felix.gogo.runtime
* org.apache.felix.gogo.shell
* org.apache.felix.gogo.commands
* org.apache.felix.scr
* greeter-bundle-commands

Now press the run button and the familiar Gogo shell should appear. Typing help should show that indeed the `tutorial:greet` command is available and ready. Typing `tutorial:greet` should get the hello world message just like it did in our very first tutorial. Here's an example of what it looks like:

{% img /images/bndtools/Capture9.PNG 'Running The Command' %}

## Summary
In this tutorial we took our original greeter service from a maven / scr project to using Bndtools. In the next tutorial multiple implementations of the Greeter service will be created and our command will execute all of them dynamically.