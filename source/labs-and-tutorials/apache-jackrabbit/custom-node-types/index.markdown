---
layout: page
title: "Apache Jackrabbit - Custom Node Types"
date: 2013-09-13 17:09
comments: true
sharing: true
footer: true
indexer: true
---
The purpose of this lab is to enhance our [previous tutorial](/labs-and-tutorials/apache-jackrabbit/querying-nodes). In that tutorial we learned how to query elements that were stored in the repository used SQL2 and JDOM. Up to now, we have been mostly dealing with unstructured nodes. This means the node themselves have no schema except for what the nt:unstructured types provided for us.

This tutorial will show how to create a custom node type and load it into the repository programmatically.

## Audience
The core audience is seated in a classroom environment. Readers performing this tutorial have just finished previous tutorials and should be familiar with how the labs will work. Most will be at work or seated in a place where they can read from the tutorial page and code in their own editor.

##Overview
In most cases, storing content will just require a node and property structure that is flexible. Node properties can differ from other node properties etc. There may come a time, however, that you want to create a schema and enforce some contraints against it, so that nodes inserted into the JCR will mandatorily have specific properties with specific types. The JSR refers to these as node type definitions.

Node types are defined using a special notation called Compact Node Type Definition Notation (or CND Notation for short). We don't have time in a single tutorial to go over the whole specification but if your interested the JSR-173 or 280 is a good place to start. There are lots of details on the grammar and usage in the specification.

Lastly, if you came directly into this tutorial and didn't do the last one, don't fret. The starter project is on GitHub and the most recent tutorial can be found working the branch lab5-nodetypes. The following clone should get you started from where we left off:

``` bash Getting The Previous Lab https://github.com/PlasmaTrout/jackrabbit-training-lab1 GitHub
git clone git@github.com:PlasmaTrout/jackrabbit-training-lab1.git
git checkout lab5-nodetypes
```

## A Quick Note Before We Get Started
I am going to have you delete the repository directory multiple times during this tutorial. In truth it's laziness, but registering node types is somewhat like building a table schema. It would be easier to have you delete it each time than issue alter statements and/or deal with dependency issues. You can however argue some options into the CndImporter command (that you will see later) and have it replace the schema each time, but this only scales so far and eventually you will cause a change that will break the semantics of the node type and it will throw an exception.

## Create A Compact Node Type Definition
In our example we are just going to create a small example to demonstrate how the definition language works. Let's say we have an node that's going to represent an article. We don't care how many properties is has, but it must have a headline and body filled out. 

Let's create a file in the root of the project called article.cnd. In its contents lets write the following:

```
<ca = 'http://www.codeaffectionado.com/training'>
[ca:article]
- ca:headline (string)
mandatory
- ca:body (string)
mandatory
```

This file is going to create a namespace for our node, then declaring a new type named ca:article. This type has two mandatory properties: headline and body.

## Registering A Node Type Into The Repository
In order to use our node type in Jackrabbit, we must first register it. To do so lets clear out our try block from the previous example and add some code:

```java Registering A Node Type From A CND File
CndImporter.registerNodeTypes(new FileReader("article.cnd"),session);
```

Now lets write some test code to see what happens if we create an article but leave out the mandatory fields.

```java Adding, Viewing and Removing Our Node
Node root = session.getRootNode();
Node articles = root.addNode("articles");
Node newNode = articles.addNode("article","ca:article");

// What do you think happens?

session.save();

// Look at our node
Node savedNode = root.getNode("articles/article");
dumpToConsole(savedNode);

// Cleanup
savedNode.remove();
session.save();
```

If we *mvn compile exec:java* we should get an exception now like:

```bash Exception
javax.jcr.nodetype.ConstraintViolationException: /articles/article: mandatory property {http://www.codeaffectionado.com/training}body does not exist
```

Let's modify the last few lines and add a body and see what happens:

```java Setting A Mandatory Property Our New Node Type
Node newNode = articles.addNode("article","ca:article");
newNode.setProperty("ca:body","Hello World!");

// Now what do you think happens?

session.save();
```

Now we see another exception:

```bash Exception
javax.jcr.nodetype.ConstraintViolationException: /articles/article: mandatory property {http://www.codeaffectionado.com/training}headline does not exist
```

If we add a headline we should be ok so lets add the line:

```java Adding The Final Mandatory Property
newNode.setProperty("ca:headline","New Hello World Article");
```

And run it again, you should see the output:

```bash Sample Output
/articles/article
/articles/article/ca:body=Hello World!
/articles/article/ca:headline=New Hello World Article
/articles/article/jcr:primaryType=ca:article
```

After your done playing, *delete your repository folder* and try the next example.

## Creating Default Values
Maybe we wish to allow users to save articles that have no headline or body, but put in a default value instead. In order to do this, we need to revisit our node definition and change it to look like:

```bash New Node Type With Default Fields
<ca = 'http://www.codeaffectionado.com/training'>
[ca:article]
- ca:headline (string)
= 'Headline Goes Here'
mandatory autocreated
- ca:body (string)
= 'Body Goes Here'
mandatory autocreated
```

Now if we run this on a fresh repository (by deleting the old repository folder before running) we will see:

```bash Sample Output
/articles/article
/articles/article/ca:body=Body Goes Here
/articles/article/ca:headline=Headline Goes Here
/articles/article/jcr:primaryType=ca:article
```

## Adding Non Specified Properties To Our New Type
You may notice, if you play around, that if you try to add properties that aren't in the CND file into your node they will thrown an exception. This is because we never told the node definition that other properties are allowed. At the bottom of the file add:

```bash Allow Wildcard Sibling Properties
- * (undefined)
```

Then you will be able to. Lets delete our repository add this line and then add some java code like:

```java Set A Sibling Property
newNode.setProperty("otherProp","Yah! It Worked!");
```

Then when we run we will see:

```bash Sample Output
/articles/article
/articles/article/ca:body=Body Goes Here
/articles/article/otherProp=Yah! It Worked!
/articles/article/ca:headline=Headline Goes Here
/articles/article/jcr:primaryType=ca:article
```

## Repository Storage Of Node Types
If you want to see the fruits of your labor so far, find the custom node types xml file in the directory repository -> repository -> nodetypes -> custom_nodetypes.xml. If you look closely you can see your namespace declaration and a special system specific format for declaring what you put in the CND file.

```xml custom_nodetypes.xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<nodeTypes xmlns:ca="http://www.codeaffectionado.com/training" xmlns:fn="http://www.w3.org/2005/xpath-functions"
           xmlns:fn_old="http://www.w3.org/2004/10/xpath-functions" xmlns:jcr="http://www.jcp.org/jcr/1.0"
           xmlns:mix="http://www.jcp.org/jcr/mix/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0" xmlns:rep="internal"
           xmlns:sv="http://www.jcp.org/jcr/sv/1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <nodeType hasOrderableChildNodes="false" isAbstract="false" isMixin="false" isQueryable="true" name="ca:article"
              primaryItemName="">
        <supertypes>
            <supertype>nt:base</supertype>
        </supertypes>
        <propertyDefinition autoCreated="true" isFullTextSearchable="true" isQueryOrderable="true" mandatory="true"
                            multiple="false" name="ca:headline" onParentVersion="COPY" protected="false"
                            requiredType="String">
            <defaultValues>
                <defaultValue>Headline Goes Here</defaultValue>
            </defaultValues>
        </propertyDefinition>
        <propertyDefinition autoCreated="true" isFullTextSearchable="true" isQueryOrderable="true" mandatory="true"
                            multiple="false" name="ca:body" onParentVersion="COPY" protected="false"
                            requiredType="String">
            <defaultValues>
                <defaultValue>Body Goes Here</defaultValue>
            </defaultValues>
        </propertyDefinition>
        <propertyDefinition autoCreated="false" isFullTextSearchable="true" isQueryOrderable="true" mandatory="false"
                            multiple="false" name="*" onParentVersion="COPY" protected="false"
                            requiredType="undefined"/>
    </nodeType>
</nodeTypes>
```

Now you may ask, can't I use edit this file? I wouldn't suggest it, this file is processed on startup of the repository and could seriously damage your workspace if you made errors in it. It's best to leave the registration to the API.

## Resources

Heres our final CND file:

```bash Final CND File
<ca = 'http://www.codeaffectionado.com/training'>
[ca:article]
- ca:headline (string)
= 'Headline Goes Here'
mandatory autocreated
- ca:body (string)
= 'Body Goes Here'
mandatory autocreated
- * (undefined)
```

And our final main method:

```java Final Main Method
public static void main( String[] args ) throws Exception {
	Repository repository = new TransientRepository();
	Session session = repository.login(
			new SimpleCredentials("admin","admin".toCharArray()));

	try{
		CndImporter.registerNodeTypes(new FileReader("article.cnd"),session,true);

		Node root = session.getRootNode();
		Node articles = root.addNode("articles");
		Node newNode = articles.addNode("article","ca:article");
		newNode.setProperty("otherProp","Yah! It Worked!");
		//newNode.setProperty("ca:body","Hello World!");
		//newNode.setProperty("ca:headline","New Hello World Article");

		session.save();

		Node savedNode = root.getNode("articles/article");
		dumpToConsole(savedNode);

		savedNode.remove();
		session.save();


	}finally{
		session.logout();
	}
}
```

