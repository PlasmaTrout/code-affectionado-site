---
layout: page
title: "Apache Jackrabbit - Manipulating Nodes In The Repository"
date: 2013-08-19 15:05
comments: true
sharing: true
footer: true
indexer: true
---
In our [first tutorial](/labs-and-tutorials/apache-jackrabbit/starting-from-scratch/) we learned how to setup our maven dependencies and write code against a TransientRepository. We need to continue that tradition and learn how to use the JCR APIs to get, add and remove nodes.

## Table Of Contents
{{ page.indexer_aside }}

## Getting The GitHub Project
If you came directly into this tutorial and didn't do the first one, don't fret. The starter project is on GitHub. The following clone should get you started from where we left off:

``` bash Getting The Previous Lab https://github.com/PlasmaTrout/jackrabbit-training-lab1 GitHub
git clone git@github.com:PlasmaTrout/jackrabbit-training-lab1.git
```

## Quick Word Before We Start Adding Stuff
Working with Jackrabbit in code can make it a bit tough to visualize what you have saved, deleted or updated. In other frameworks, such as Sling, there will be visual viewers that help you see the node trees a bit better. We will create a console method to "dump the tree" as a reference to make sure thing did work, but there is a possibility that you may end up creating a bunch of garbage and want to start all over again. Fortunately, that's really easy to do. Just go into your project directory and blow out repository directory and all of its contents and re-run your project again.

You can't hurt your application by removing the repository directory, the repository.xml files or the derby.log file. If you want a fresh start, kill em. The next time you **mvn compile exec:java** you app they will be reproduced. 

## Wipe Our Our Previous Code
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
We are going to start doing some additions and deletions of nodes in this section. This has the potential to cause some exceptions thus killing our application. When an application dies, what happens to the session, can be hard to determine. If there are other sessions open, the repository itself won't necessarily expire the sessions and its possible that your last one may stay open after your code threw and exited.

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

There, now our super secure, un-guessable admin/admin login is protected.

## Add A Node
Generally the first thing one does after a login to a repository is get a node. There are a few ways to accommodate that. But for starters lets get into the habit of getting the root node. When we first login, it's the only node that guaranteed to be there anyways. Remember to put this in your try section:

``` java Getting The Root Node
Node root = session.getRootNode();
```

There. Simple enough. Now lets add two nodes and a property and save it:

``` java Adding Two New Nodes
Node hello = root.addNode("nodeA");
Node world = hello.addNode("nodeB");
world.setProperty("message","Hello, World");
session.save();
```

Notice how each addNode returns back the node that was added. Seems to reason that we could probably chain this whole statement. We could refactor this to:

``` java Adding Two Nodes Chain style
root.addNode("nodeA").addNode("nodeB")
	.setProperty("message","Hello World");

session.save();
```

Don't run this just yet. If you do you may end up with a new node each time your run it since Jackrabbit has duplicate nodes turned on by default. Let's finish all of these sections before we run it.

## Getting A Node From A Path
So there are a few ways to get the node back that you just created. The first is to use the root node to get the information back. Let's get the the node you just created and saved.

``` java Getting A Node From The Root Node
Node ourNode = root.getNode("nodeA/NodeB");

System.out.println(ourNode.getPath());
System.out.println(ourNode.getProperty("message").getString());
```

To do this not using the root node is pretty simple as well, you can just use the session to find the correct item and cast it back like so:

``` java Getting A Node Without The Root Node
Node ourNodeAgain = (Node) session.getItem("/nodeA/nodeB");

System.out.println(ourNodeAgain.getPath());
System.out.println(ourNodeAgain.getProperty("message").getString());
```

The first question after seeing this might be, "Why'd you have to cast that?". Well getItem returns an Item. [Item](http://www.day.com/maven/jsr170/javadocs/jcr-1.0/javax/jcr/Item.html) is an interface that both Node and Property inherit. This allows you to use it for either Nodes or Properties, thus having to cast. Wait, should session have its own mechanism then to get the Node back. Sure it looks like the following:

``` java Getting A Node Without The Root Node Again
Node oneMoreTime = session.getNode("/nodeA/nodeB");
System.out.println(oneMoreTime.getPath());
System.out.println(oneMoreTime.getProperty("message").getString());
```

So with either an absolute path or a relative from the node reference you have, you can find any node in the system.

## Getting Properties
We didn't mention it at the time, but all of our print lines above are getting the property message back from the node. A call to getProperty returns back a [Property](http://www.day.com/maven/javax.jcr/javadocs/jcr-2.0/javax/jcr/Property.html) object.  Once you get this back, you ask it for what data type you want and it tries to coerce that type back for you. For instance if you store an long and you call getString on the property. It will try to coerce that value back for you.

## Removing Nodes
Removing nodes is really easy in the API. All it takes is the following code added to the bottom of your try block:

``` java Deleting A Node
root.getNode("nodeA").remove();
session.save();
```

Remember to call save on the session every time your modify something. Your changes only go into a transient storage location without it and get lost when your session expires.

## Running Our App
So we should have an application now that looks something like the following:

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

What we see now is really interesting. Along with our nodes at the bottom we see a ton of jcr:system nodes (akin to system level settings) and some policy nodes (similar to ACL permissions). They are found off of the root and typically stay out of sight and out of mind. However, at the end of all of the spamming we see:

``` bash Our Node Dumper
/nodeA
/nodeA/jcr:primaryType=nt:unstructured
/nodeA/nodeB
/nodeA/nodeB/message=Hello World!
/nodeA/nodeB/jcr:primaryType=nt:unstructured
```

That's what we were looking for so lets add a statement just under the printing of the node path of our dump method to ignore properties on jcr:system nodes:

``` java Ignoring System Properties
if(node.getName().equals("jcr:system") || node.getName().equals("rep:policy")){
    return;
}
```

This will at least let us see the nodes, but not kill us the plethora of properties that they contain. 

We will use this dump method a few times throughout these tutorials so keep it on hand. I will try to keep the [GIST](https://gist.github.com/PlasmaTrout/6274163) updated on GitHub.
