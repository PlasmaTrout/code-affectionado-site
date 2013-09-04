---
layout: page
title: "Apache Jackrabbit - Querying Nodes"
date: 2013-09-04 14:24
comments: true
sharing: true
footer: true
indexer: true
---
In a [previous tutorial](/labs-and-tutorials/apache-jackrabbit/importing-xml/) we learned how to import bulk XML into the JCR. However, once a significant number of nodes are in the JCR, navigating each node one by one in order to find a specific set of nodes can be a bit tedious. For this the JCR uses queries. This tutorial shows how to execute queries against the imported data.

## Audience
The core audience is seated in a classroom environment. Readers performing this tutorial have just finished previous tutorials and should be familiar with how the labs will work. Most will be at work or seated in a place where they can read from the tutorial page and code in their own editor.

##Overview
Once a large amount of nodes exist in a repository, getting to just a specific set is inefficient. For instance, if we wanted to just get at all of our books that are in the genre "computers", we would have to touch every single node and check. This would end up becoming a very inefficient process of:

1. Iterate each child node of bookshelf
1. Determine if that node had a property of genre matching computers
1. If not next
1. If so then push it into a collection
1. Return the collection

Luckily we don't have to do this. The standards specify a query API that can be used to help use find specific thing like this. This tutorial will demonstrate how this is done.

If you came directly into this tutorial and didn't do the first one, don't fret. The starter project is on GitHub and the most recent tutorial can be found working the branch lab3-xmlimport. The following clone should get you started from where we left off:

``` bash Getting The Previous Lab https://github.com/PlasmaTrout/jackrabbit-training-lab1 GitHub
git clone git@github.com:PlasmaTrout/jackrabbit-training-lab1.git
git checkout lab3-xmlimport
```
We will be modifying this code to support our queries.

## Remove Code We Won't Need
For this tutorial we aren't going to need the else statement from our previous example so lets remove it so our if statement appears as the following:

```java Our Starter If Statement
if(!root.hasNode("bookstore")){
	Node node = root.addNode("bookstore","nt:unstructured");
	FileInputStream stream = new FileInputStream("test.xml");
	session.importXML(node.getPath(),stream,ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
	stream.close();

	session.save();
}
```

This will also ensure that if we wipe our repository that we will rebuild the nodes back for these examples. Just to be sure lets run a:

```bash Maven Compile And Run
mvn compile exec:java
```

Just to be sure we have the latest build working. In the output we should still see our books and there only is around 12 of them:

```bash Our Current Book Dump
....
/bookstore/catalog/book[11]
/bookstore/catalog/book[11]/description=The Microsoft MSXML3 parser is covered in             detail, with attention to XML DOM interfaces, XSLT processing,             SAX and more.
/bookstore/catalog/book[11]/title=MSXML3: A Comprehensive Guide
/bookstore/catalog/book[11]/id=bk111
/bookstore/catalog/book[11]/publish_date=2000-12-01
/bookstore/catalog/book[11]/price=36.95
/bookstore/catalog/book[11]/genre=Computer
/bookstore/catalog/book[11]/jcr:primaryType=nt:unstructured
/bookstore/catalog/book[11]/author=O'Brien, Tim
/bookstore/catalog/book[12]
/bookstore/catalog/book[12]/description=Microsoft Visual Studio 7 is explored in depth,             looking at how Visual Basic, Visual C++, C#, and ASP+ are             integrated into a comprehensive development             environment.
/bookstore/catalog/book[12]/title=Visual Studio 7: A Comprehensive Guide
/bookstore/catalog/book[12]/id=bk112
/bookstore/catalog/book[12]/publish_date=2001-04-16
/bookstore/catalog/book[12]/price=49.95
/bookstore/catalog/book[12]/genre=Computer
/bookstore/catalog/book[12]/jcr:primaryType=nt:unstructured
/bookstore/catalog/book[12]/author=Galos, Mike
...
```

Notice how each book in the output has a genre? Let's suppose we wanted to get all of the books in the bookstore node that have a genre of "Computer", how would we do that? The answer is to use the query API.

## Query The Records
In order to use the API to query the records from the JCR, a specific pattern of steps will always be used. They essentially follow the same steps:

1. Get a reference to a query manager
1. Create a query
1. Execute the query to get a query result set
1. Get the nodes in the results set
1. Do something with them

So lets take a look at how thats done using the API by placing the following items after our if statement in the code.

### Get A Reference To A Query Manager
The query manager is a workspace level object. This means the proper way to get it is to first get the workspace you are in, then ask it for the query manager. You can chain these calls together if you wish like this:

```java Getting A Query Manager
QueryManager manager = session.getWorkspace().getQueryManager();
```

There aren't any arguments needed to get this one.

### Create A Query
Basically we create a query by calling createQuery on the manager. This will give us back a query object that we can use like so:

```java Creating A Query
Query query = manager.createQuery("select * from [nt:unstructured] where [genre]='Computer'", Query.JCR_SQL2);
```

The first argument to the create query methods is the query itself. This will differ based on the type of query you use, however we are using JCR_SQL2 to keep things simple.

The second argument tells the manager what types of query to expect. In our case we used SQL2 but the available options can be found [here](http://www.day.com/maven/jsr170/javadocs/jcr-2.0/javax/jcr/query/Query.html). Notice how the others, with the exception of JCR_JQOM, have been deprecated.

### Execute The Query
Now that we have a query all worked out, we can call execute against it. This will give us back a query result object:

```java Executing A Query
QueryResult result = query.execute();
```

### Get The Nodes In The Result Set
Now all we have to do is ask our result set for the nodes and print them by adding:

```java Getting And Printing The Query Results
NodeIterator nodes = result.getNodes();

while(nodes.hasNext()){
	Node node = nodes.nextNode();
	Property property = node.getProperty("title");
	System.out.println("Matched: "+property.getString());
}

//dumpToConsole(root);
```

It's a good idea to comment out the dump to console call for right now, and our final main method should look something like this:

```java Our Main Method
public static void main( String[] args ) throws Exception {
	Repository repository = new TransientRepository();
	Session session = repository.login(
			new SimpleCredentials("admin","admin".toCharArray()));

	try{
		Node root = session.getRootNode();

		if(!root.hasNode("bookstore")){
			Node node = root.addNode("bookstore","nt:unstructured");
			FileInputStream stream = new FileInputStream("test.xml");
			session.importXML(node.getPath(), stream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			stream.close();

			session.save();

		}

		QueryManager manager = session.getWorkspace().getQueryManager();
		Query query = manager.createQuery("select * from [nt:unstructured] where [genre]='Computer'", Query.JCR_SQL2);
		QueryResult result = query.execute();
		NodeIterator nodes = result.getNodes();

		while(nodes.hasNext()){
			Node node = nodes.nextNode();
			Property property = node.getProperty("title");
			System.out.println("Matched: "+property.getString());
		}

		//dumpToConsole(root);



	}finally{
		session.logout();
	}
}
```

## Running Our Query
If we do a **mvn compile exec:java** on our code now, we should see the results of our query placed in the output just before the success notification.

```bash The Query Results
Matched: MSXML3: A Comprehensive Guide
Matched: Microsoft .NET: The Programming Bible
Matched: Visual Studio 7: A Comprehensive Guide
Matched: XML Developer's Guide
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Testing Another Query
So lets try a different query just to test things out. Let's try to query all of the Fantasy book by Corets:

```java Modifying Our Query
Query query = manager.createQuery(
	"select * from [nt:unstructured] where [genre]='Fantasy' and [author] LIKE 'Corets%'",
	Query.JCR_SQL2);
```

We should get back:

```bash Our New Query Results
Matched: Oberon's Legacy
Matched: The Sundered Grail
Matched: Maeve Ascendant
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Summary
What else can you query? Well its really up to your imagination, but I would suggest googling a few examples. They don't have to be Jackrabbit specific to work. Take a look at [The ExoPlatforms Help Page](http://docs.exoplatform.com/PLF30/refguide/html/ch-jcr-query-usecases.html#JCR.FindAllNodes) and look at some of their examples. Reading the [H2 Databases Grammar Page](http://www.h2database.com/jcr/grammar.html) may help as well. 

## Common Questions
In this section, I'll add some common questions I get during the classes.

> Are the queries case sensitive?

You mean if you tried *select 
\* from [nt:unstructured] where [genre]='fantasy'* would you get the same results as *[genre]='Fantasy'*? The answer is no, the queries must match the property name exactly including case. This can trip you up if you came from the SQL world.

> What the heck are JQOM queries?

They are really low level API queries used to dynamically build queries one section at a time. For instance we would do something like:

```java The JQOM Low Level API

// Obtain the query manager for the session ...
javax.jcr.query.QueryManager queryManager = session.getWorkspace().getQueryManager();
 
// Create a query object model factory ...
QueryObjectModelFactory factory = queryManager.getQOMFactory();
 
// Create the FROM clause: a selector for the [nt:unstructured] nodes ...
Selector source = factory.selector("nt:unstructured","unstructNodes");
 
// Create the SELECT clause (we want all columns defined on the node type) ...
Column[] columns = null;
 
// Create the WHERE clauses and then "and" them together
Constraint constraint1 = factory.comparison(
		factory.propertyValue("unstructNodes","genre"),
		Operator.EQ.toString(),
		factory.literal(new StringValue("Fantasy")));

Constraint constraint2 = factory.comparison(
		factory.propertyValue("unstructNodes","author"),
		Operator.LIKE.toString(),
		factory.literal(new StringValue("Corets%")));

Constraint and = factory.and(constraint1,constraint2);

// Define the orderings (we have none for this query)...
Ordering[] orderings = null;
 
// Create the query ...
QueryObjectModel query = factory.createQuery(source,and,orderings,columns);
 
// Execute the query and get the results ...
// (This is the same as before.)
javax.jcr.QueryResult result = query.execute();
```
The best case to use them is when you need to write code to dynamically generate queries on the fly. It's API provides a sort of safety against you executing a malformed string.

