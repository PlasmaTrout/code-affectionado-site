---
layout: page
title: "Apache Jackrabbit - Manipulating Nodes In The Repository"
date: 2013-08-19 15:05
comments: true
sharing: true
footer: true
indexer: true
---
The purpose of this lab is to enhance our [previous tutorial](/labs-and-tutorials/apache-jackrabbit/starting-from-scratch/). In that lab we learned how to setup our maven dependencies and write code against a TransientRepository. The purpose of this lab is to to continue exploration of the JCR APIs and see how node manipulation is done in Apache Jackrabbit.

## Audience
The core audience is seated in a classroom environment. Readers performing this tutorial have just finished previous tutorials and should be familiar with how the lab will work. Most will be at work or seated in a place where they can read from the tutorial page and code in their own editor.

## Overview
Working with Apache Jackrabbit only in code form can make it a bit tough to visualize the changes you are making to the node repository. There are visual tools in products such as Apache Sling and CRX which take care of this from their perspective. But, we will need to create a console method to "dump the node tree" to help us visualize what is happening.

Because it's difficult to see your changes, you may end up creating a bunch of garbage and want to start all over again. That's really easy to do. Just go into your project directory and remove the repository directory and all of its contents and re-run your project again. If the repository does not exist, It will be re-built each time you run the application.

_You can't hurt your application by removing the repository directory, the repository.Xml files or the derby.log file. If you want a fresh start, kill em. The next time you **mvn compile exec:java** you app they will be reproduced._

Lastly, if you came directly into this tutorial and didn't do the first one, don't fret. The starter project is on GitHub. The following clone should get you started from where we left off:

``` bash Getting The Previous Lab https://github.com/PlasmaTrout/jackrabbit-training-lab1 GitHub
git clone git@github.com:PlasmaTrout/jackrabbit-training-lab1.git
```

## Wipe Our Previous Code
We previously just made some called to getDescriptor a few times. Let's get that out of the way so we end up with a fresh start that looks something like:

``` java Main Starting Point
public static void main( String[] args ) throws RepositoryException {
    Repository repository = new TransientRepository();
    Session session = repository.login(
        new SimpleCredentials("admin","admin".toCharArray()));

		// Put Our New Stuff Here

    session.logout();
}
```

## Adding A Try Catch To Our Work
The JCR is all about the manipulation of nodes and properties. Due to safety in the API, the methods that do this have the potential to cause some exceptions that may cause your application to close. It's hard to determine what will happen to the session when the application dies. If there are other sessions open, the repository won't necessarily expire the sessions. The last session may stay open after your code threw an exception and exited. On the other hand, the application may have only session open. When the application dies, the repository will just simply expire the session shut down.

This won't be that big of a deal in our examples, since they are just command line tinkering. However, in a much larger application having sessions logged in as "admin" just hovering about may not be a good idea. In order to combat this, lets add a simple pattern to guarantee that the session gets logged out when this happens:

``` java Session Logout Exception Handling Style
public static void main( String[] args ) throws RepositoryException {
    Repository repository = new TransientRepository();
    Session session = repository.login(
            new SimpleCredentials("admin","admin".toCharArray()));

    try{
        // code goes here
    }finally{
        session.logout();
    }
}
```

Of course having a password of admin:admin is not the most secure method either. We won't dive into changing this just yet.

## Getting The Root Node
After a login to a repository, a coder may have a specific place in the hierarchy they would like to start from. They need to get a reference to a node to get started. There are a few ways to accommodate that. One of which is to get the root node, that is the first and most top level node in the tree. When we first login, it's the only node that guaranteed to be there anyway. Remember to put this in your try section:

``` java Getting The Root Node
Node root = session.getRootNode();
```

This convenient method always allows us to get the root rather quickly. Now let's add some nodes to the root node.

## Adding Some Nodes
Adding nodes, once you have a reference to a node your would like to add them to, is pretty simple. Let's add two nodes to our root node.

``` java Adding Two New Nodes
Node hello = root.addNode("nodeA");
Node world = hello.addNode("nodeB");
world.setProperty("message","Hello, World");
session.save();
```

Notice how each addNode returns back the node that was added. This means we can chain this whole statement into one statement such as:

``` java Adding Two Nodes Chain style
root.addNode("nodeA").addNode("nodeB")
	.setProperty("message","Hello World");

session.save();
```

Don't run this just yet. If you do you may end up with a new node each time your run it since Jackrabbit has duplicate nodes turned on by default. Let's finish all of these sections before we run it.

## Getting A Node From A Node
There are a few ways to get the node back that you just created. The first is to use the root node to get the information back. Then we only have to worry about the path relative from root such as:

``` java Getting A Node From The Root Node
Node ourNode = root.getNode("nodeA/NodeB");

System.out.println(ourNode.getPath());
System.out.println(ourNode.getProperty("message").getString());
```

## Getting A Node From The Session
We could use the session to get Nodes as well. One approach could be use to the session to just get the node like our top example:

``` java Getting A Node Without The Root Node Again
Node oneMoreTime = session.getNode("/nodeA/nodeB");
System.out.println(oneMoreTime.getPath());
System.out.println(oneMoreTime.getProperty("message").getString());
```

Another method is to use the session to find the correct item and cast it back. Lets add another set of lines:

``` java Getting A Node Without The Root Node
Node ourNodeAgain = (Node) session.getItem("/nodeA/nodeB");

System.out.println(ourNodeAgain.getPath());
System.out.println(ourNodeAgain.getProperty("message").getString());
```

The first question after seeing this might be, "Whats an Item and how'd that cast work?". [Item](http://www.day.com/maven/jsr170/javadocs/jcr-1.0/javax/jcr/Item.html) is an interface that both Node and Property as sub-interfaces of. This allows you to use it for either Nodes or Properties directly from the session, but makes you cast.

Which one should you use? Well, it's really up to your preferences, but getItem comes with a warning in the javadocs:


> This method should only be used if the application does not know whether the item at the indicated path is property or node. In cases where the application has this information, either getNode(java.lang.String) or getProperty(java.lang.String) should be used, as appropriate. In many repository implementations the node and property-specific methods are likely to be more efficient than getItem.




## Getting Properties
Getting a property from a node takes two forms. One is really just a shortcut for another. We won't code the following examples as they point to a node named myNode and we haven't created it, so just look at them for now:

``` java Getting A Property Using The Value Shortcut Method
Property property = myNode.getProperty("message");
String message = property.getString();
```

Notice how getProperty returns back a property. That property object is used to coerce the value back out. But everything is not as it seems with that example. There is a special [Value](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Value.html) object for the values contained in the property and really that code was just a shortcut for:

``` java Getting A Property The Long Way
Property property = myNode.getProperty("message");
Value propValue = property.getValue();
String message = propValue.getString();
```

We didn't mention it at the time, but all of our prints in the getting nodes section are retrieving the property back from the node. A call to getProperty returns back a [Property](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Property.html) object.  Once you get this back, you ask it for what data type you want and it tries to coerce that type back for you. For instance if you store a long and call getString on the property containing it, it, it will try to coerce a string back for you.

## Removing Nodes
Removing nodes is easy in the API. All it takes is the following code added to the bottom of your try block:

``` java Deleting A Node
root.getNode("nodeA").remove();
session.save();
```

Remember to call save on the session every time your modify something. When you are modifying nodes, you changes are going in a special transient storage location. Until saved it called, they haven't happened in the workspace. If your session dies, your changes may die with it.

## Running Our App
We should have a main method now that looks something like the following:

``` java Full Example
public static void main( String[] args ) throws RepositoryException {
    Repository repository = new TransientRepository();
    Session session = repository.login(
            new SimpleCredentials("admin","admin".toCharArray()));

    try{

        Node root = session.getRootNode();

        root.addNode("nodeA").addNode("nodeB")
                .setProperty("message", "Hello World!");
        session.save();

        Node ourNode = root.getNode("nodeA/nodeB");
        System.out.println(ourNode.getPath());
        System.out.println(ourNode.getProperty("message").getString());

        Node ourNodeAgain = (Node) session.getItem("/nodeA/nodeB");
        System.out.println(ourNodeAgain.getPath());
        System.out.println(ourNodeAgain.getProperty("message").getString());

        Node oneMoreTime = session.getNode("/nodeA/nodeB");
        System.out.println(oneMoreTime.getPath());
        System.out.println(oneMoreTime.getProperty("message").getString());

        root.getNode("nodeA").remove();
        session.save();

    }finally{
        session.logout();
    }
}
```

Let's execute this with a **mvn compile exec:java** and see what we get. We should get:

``` bash Our First Output
/nodeA/nodeB
Hello World!
/nodeA/nodeB
Hello World!
/nodeA/nodeB
Hello World!
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```


## A Better View Of Whats Going On
Let's create a static method really fast in our project that will dump all of the nodes and properties. This will show a few ways to iterate the nodes. Paste this under you main method:

``` java Dumping Out A Node Structure
public static void dumpToConsole(Node node) throws RepositoryException {

    System.out.println(node.getPath());

    if(node.hasProperties()){
        PropertyIterator props = node.getProperties();
        while(props.hasNext()){
            Property property = (Property) props.next();

            if(property.isMultiple()){
                Value[] values = property.getValues();
                System.out.print(property.getPath()+"="+"[");

                for(Value value : values){
                    System.out.print(value.getString()+",");
                }
                System.out.println("]");
            } else {
                System.out.println(property.getPath()+ "="+property.getString());
            }
        }
    }

    NodeIterator iterator = node.getNodes();

    while(iterator.hasNext()){
        Node next = (Node) iterator.next();

        dumpToConsole(next);
    }
}
```

Now we can make our main method look like:

``` java Cleaning Up Our Example
public static void main( String[] args ) throws RepositoryException {
    Repository repository = new TransientRepository();
    Session session = repository.login(
            new SimpleCredentials("admin","admin".toCharArray()));

    try{

        Node root = session.getRootNode();

        root.addNode("nodeA").addNode("nodeB")
                .setProperty("message", "Hello World!");
        session.save();

        dumpToConsole(root);

        root.getNode("nodeA").remove();
        session.save();

    }finally{
        session.logout();
    }
}
```

When we run this now, we can see that along with our nodes at the bottom we see some of the of jcr:system nodes (akin to system level settings) and some policy nodes (similar to ACL permissions). They are found off of the root and typically stay out of sight and out of mind. Our nodes are at the end of this print out:

``` bash Our Node Dumper
/nodeA
/nodeA/jcr:primaryType=nt:unstructured
/nodeA/nodeB
/nodeA/nodeB/message=Hello World!
/nodeA/nodeB/jcr:primaryType=nt:unstructured
```

We should add some code to ignore properties on jcr:system and rep:policy node types. This will reduce the clutter:

``` java Ignoring System Properties
if(node.getName().equals("jcr:system") || node.getName().equals("rep:policy")){
    return;
}
```

Now when we run again, we can see our changes without a plethora of properties that belong to the system.

We will use this dump method a few times throughout these tutorials so keep it on hand. I will try to keep the [GIST](https://gist.github.com/PlasmaTrout/6274163) updated on GitHub.
