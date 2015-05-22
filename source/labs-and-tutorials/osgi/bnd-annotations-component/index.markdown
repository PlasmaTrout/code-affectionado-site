---
layout: page
title: "Understanding The BND Annotations - @Component"
date: 2015-05-15 02:02
comments: true
sharing: true
footer: true
---
This reference is designed to help developers understand the BND Annotation and how they relate to OSGi bundles. This documentation will change as new points are brought up in the comments, email, etc.

## What's a Component Anyway
A component is just an all encompassing term that means any Java class inside a bundle that has given up its normal rights to be controlled exclusively by the component runtime of an OSGi framework. This means when it become available or unavailable is up to the runtime. 

A component is declared in OSGi by the presence of a specific component.xml file that gets deployed with the bundle declaring it. The SCR and BND annotations allow this file to be built for you using the decorations on your classes.

There are essentially three types of components:

1. Immediate Components, which are components that must be made active and ready as soon as it's possible for the runtime to do so.
2. Delayed Components, which are components that don't become active until someone needs them and calls them.
3. And Factory Components that allows other bundles, code or admin dialogs to active, stop and destroy them.

Starting there we have a lot to talk about.

## The Component Annotation
As you can image, putting the ```@Component``` annotation on a class causes a component.xml file to be generated and declares that class as a component. But what type of component? Well that depends.

If the component doesn't provide a service interface (which is what the provide parameter is for) and has no dependencies then it will be considered an immediate component. This is because with no dependencies and no service interface, it's impossible for the framework to figure out when to active it. It needs a solid trigger if the framework is going to delay it.

However, if you provide a service interface then the framework can delay the activation of your component until someone actually calls it. If you provide dependencies it can wait to active it until all of the required ones are met. If you have neither the framework has no choice but to activate it immediately, otherwise it would never be able to.

So lets take a simple class and show how this works.

```java
@Component(name="Example Component")
public class ExampleComponent {

	@Activate
	private void activate(BundleContext ctx){
		System.out.println("Component was activated!");
	}

}
```

In this case the component is activated immediately since it has no dependencies and provides no service interface to anyone. As soon as it's placed into the runtime it will print the message ```Component was activated!```.

If we implement an interface things change, the component annotation will automatically provide a service by that interfaces name and the component will become delayed.

```java
@Component(name="Example Component")
public class ExampleComponent implements ExampleInterface {

	@Activate
	private void activate(BundleContext ctx){
		System.out.println("Component was activated as a service!");
	}

}
```
You won't get that message in the println, because nothing has called it yet. This made it a Delayed Component. Now can we force it to become active if it has no dependencies? We sure can thats what the ```immediate``` parameter is for.

```java
@Component(name="Example Component",immediate=true)
public class ExampleComponent implements ExampleInterface {

	@Activate
	private void activate(BundleContext ctx){
		System.out.println("Component was activated as a service!");
	}

	@Override
	public void doSomething() {
		
	}

}
```
Now we will get the ```Component was activated as a service!``` message immeadiately after deploying it. 

All we needed to do was implement and interface and the component annotation automatically knew to expose the interfaces that it implemented as a service. But what if you want to just provide a specific interface? That's what the ```provide``` parameter is for. You will see it used in servlets, especially in cases where the component will implement HttpServlet but you only want to provide a service for javax.Servlet. Then your declaration looks like this:

```java
@Component(name="View Utility Servlet",provide=javax.servlet.Servlet.class,properties={
	"alias:String=/views"
})
public class ViewUtilityServlet extends ControllerServlet
```

This is critical because the Http Whiteboard will only respond to services that expose that specific interface.

This example also shows a new parameter we haven't talked about yet: The ```properties```  parameter. This parameter allows you to set values on the component that the OGSi framework can use when activating it. In this case the whiteboard service looks for the alias property to determine what name to host it under.

## The Parameters

Parameter | Description
-------------------- | -----------
name      | Sets the name of the component to something human readable. Using the namespace is passe.
configurationPolicy | Tells the framework how to handle component configurations. For some components you may not want to active them unless a required configuration exists. There is ConfigurationPolicy.ignore, ConfigurationPolicy.optional (default) and ConfigurationPolicy.required.
enabled    | This true or false parameter defaults to true. However you can set it false if for some reason you want to programmatically enable it later.
immediate  | This true or false field suggests your intent to the framework of whether to delay activation or do so immediately when all dependencies are met. If you have a component with no services and no dependencies like we described in the beginning of this article, setting this to false has no effect.
properties | Provides a way for you to pass properties to the bundles context
provide    | Allows an explicit way for you to control what interface you are exposing as an OSGi service. By default the component will use all implemented interfaces and expose services on all of them.
serviceFactory | When this is set to true, any bundle that requests the service gets a completely new and uniquely configured instance. Because of this you can't have immediate set to true and this set to true at the same time.
designate | Allows you to specify a class decorated with the metatype extensions that will serve as the configuration class for your service. It will re-activate and respond to changes in its configuration
designateFactory | This allows you to specify the class that will serve as the basis for every instance that the factory will create of a given service.
factory | Sets the pid so that you can use factories programmatically to create instances. Odds are, you will never use this.

Be wary of the factory and designateFactory parameters. They probably aren't used like you think. As a matter of fact some of the writers of the OSGi specification have even warned about using them outside of the scope of the very limited use cases they were designed for.