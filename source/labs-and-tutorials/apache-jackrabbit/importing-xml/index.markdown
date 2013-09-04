---
layout: page
title: "Apache Jackrabbit - Importing XML"
date: 2013-09-03 10:55
comments: true
sharing: true
footer: true
indexer: true
---
The purpose of this lab is to enhance our [previous tutorial](/labs-and-tutorials/apache-jackrabbit/manipulating-nodes/). In that tutorial we learned how to create nodes programmatically. While easy to do, creating a large set of nodes can be somewhat tedious using code. If we have a pre-existing XML file or export of our structure, we can use the importer to do our dirty work for us.

## Audience
The core audience is seated in a classroom environment. Readers performing this tutorial have just finished previous tutorials and should be familiar with how the labs will work. Most will be at work or seated in a place where they can read from the tutorial page and code in their own editor.

##Overview
It's very common that a developer will have a significant amount of data that needs to be put into the JCR prior to actual development beginning. Writing Java import routines is definitely one way to solve the problem, but doing this node by node in code can be a bit extreme.

If we think about some of the JCR's core features like having a single root node and the ability to have children, it's easy to see that the JCRs node structure is very closely related to XML. This being the case, it's very easy to import data from XML documents into the JCR. In this tutorial we will be doing just that.

Lastly, if you came directly into this tutorial and didn't do the first one, don't fret. The starter project is on GitHub and the most recent tutorial can be found working the branch lab2-nodemanip. The following clone should get you started from where we left off:

``` bash Getting The Previous Lab https://github.com/PlasmaTrout/jackrabbit-training-lab1 GitHub
git clone git@github.com:PlasmaTrout/jackrabbit-training-lab1.git
git checkout lab2-nodemanip
```

##Getting Some Test Data
Before we get started, download some test data (or make some of your own). Make sure to place this file in the root of your project and name it test.xml.

{% include_code Test Data lang:xml jackrabbit/test.xml %}

Notice that the XML we have chosen to use contains attribute based notation. This was by choice. When XML is imported into Jackrabbit it will convert attributes into properties. Since Elements are treated as nodes, placing just text in an element will give you a node with a jcr:text property on the inside, this isn't what we want in this import.

##Clear Our Our Previous Main Method
Let's clean up our previous main method, however leaving the dump method alone for now. We should end up with a main method that looks like:

```java Cleaned Up Main Method
Repository repository = new TransientRepository();
Session session = repository.login(
		new SimpleCredentials("admin","admin".toCharArray()));

try{

}finally{
	session.logout();
}
```

##Import Some Data
The following steps will augment our try block with what we need to import some data.

###Get The Root Node
Lets start off by getting our root node. In the try section, lets add the line:

```java Getting The Root Node
Node root = session.getRootNode();
```

###Add A New Child Node For Our Import
Now that we have our root node we can add a new node to it, but just to protect ourselves in case we run this multiple times. Lets add some protection to our add:

```java Adding A Bookstore Node
if(!root.hasNode("bookstore")){
	Node node = root.addNode("bookstore","nt:unstructured");
}
```
This is so we won't add the node more than once if we run it over and over again.

###Get A Stream And Import Our Data
So all we really need to do know is open up a file stream, pipe our xml into the node and save. We can do that by:

```java Getting An Input Stream
FileInputStream stream = new FileInputStream("test.xml");
```

To open a file stream. Then:

```java Importing XML Into The Session
 session.importXML(node.getPath(),stream,ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
```

The first argument is the path to import the data to. We created a root node ahead of time so we can use the path to the node. It's safer this way just to prevent typos.

The second argument is our stream to the xml file.

The third argument tells the importer how to handle specific ids that may be present in the document already. A way this could happen, would be if the xml nodes had the predefined "jcr:uuid" attribute in them. The chances are slim that this exists unless you exported this already from another repository, however the constant document is [here](http://www.day.com/maven/jsr170/javadocs/jcr-2.0/javax/jcr/ImportUUIDBehavior.html).

Finally we can close everything up with:

```java Cleaning Up 
stream.close();
session.save();
```

To persist our changes.

###Add Our Dump Method To See The Results
Now after the if block lets add our call to:

```java
dumpToConsole(root);
```

So we can see what we get.

## Run Our Application 
If we do a:

```
mvn compile exec:java
```

We will get an output like this from our dumpToConsole method:

```bash Node Dump Output
/bookstore
/bookstore/jcr:primaryType=nt:unstructured
/bookstore/catalog
/bookstore/catalog/jcr:primaryType=nt:unstructured
/bookstore/catalog/book
/bookstore/catalog/book/description=An in-depth look at creating applications             with XML.
/bookstore/catalog/book/title=XML Developer's Guide
/bookstore/catalog/book/id=bk101
/bookstore/catalog/book/publish_date=2000-10-01
/bookstore/catalog/book/price=44.95
/bookstore/catalog/book/genre=Computer
/bookstore/catalog/book/jcr:primaryType=nt:unstructured
/bookstore/catalog/book/author=Gambardella, Matthew
/bookstore/catalog/book[2]
/bookstore/catalog/book[2]/description=A former architect battles corporate zombies,             an evil sorceress, and her own childhood to become queen             of the world.
/bookstore/catalog/book[2]/title=Midnight Rain
/bookstore/catalog/book[2]/id=bk102
/bookstore/catalog/book[2]/publish_date=2000-12-16
/bookstore/catalog/book[2]/price=5.95
/bookstore/catalog/book[2]/genre=Fantasy
/bookstore/catalog/book[2]/jcr:primaryType=nt:unstructured
/bookstore/catalog/book[2]/author=Ralls, Kim
......
```

Notice how each book node in now indexed as book, book[1], book[2] etc. For each book our import created a new node and created properties for each attribute in the node. To better see what we did, lets export this to XML and see what we get.

##Export Some XML
In the above example, we checked to see if the node exists, if it didn't we imported some xml into it. Lets add an export to it by adding an else clause:

```java Adding An Else Clause
if(!root.hasNode("bookstore")){
....our code...
}else{

}
```

Now inside our else clause lets create a stream and export our results:

```java Exporting The Document View
FileOutputStream ostream = new FileOutputStream("fulldump.xml");
session.exportDocumentView("/bookstore",ostream,true,false);
ostream.close();
```

*Wait, why isn't this called exportXml instead of exportDocumentView? Well the output xml has two forms a **system** view and a **document** view. At the end of this tutorial I will post what the system view output looks like, but for right now lets just do a document view.*

The first argument of exporting a document view is the absolute path to export. The export will start at this node as the root of the document.

The second argument is of course the stream to write to.

The third is whether or not we should skip binary objects. Interestingly enough you can put types like nt:file in a JCR. Exporting those types to XML would make a rather large encoded blob in the file. Changing this option to true prevents the binary data from coming back (not that we have any).

The fourth argument is weather to recurse into any nodes we find. This is handy if we just want the XML to the node we specify and not deeper.

All together our main method now should look like:

```java The Full Main Method
public static void main( String[] args ) throws Exception {
	Repository repository = new TransientRepository();
	Session session = repository.login(
			new SimpleCredentials("admin","admin".toCharArray()));

	try{
		Node root = session.getRootNode();

		if(!root.hasNode("bookstore")){
			Node node = root.addNode("bookstore","nt:unstructured");
			FileInputStream stream = new FileInputStream("test.xml");
			session.importXML(node.getPath(),stream,ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			stream.close();

			session.save();

		}else{
			FileOutputStream ostream = new FileOutputStream("fulldump.xml");
			session.exportDocumentView("/bookstore",ostream,true,false);
			ostream.close();
		}

		dumpToConsole(root);



	}finally{
		session.logout();
	}
}
```

## Run Again
Running this should product a new file in the top level of your project named fulldump.xml. If we look at that file we will see the structure of our JCR mapped back to XML:

```xml The Document View Of Our XML
<?xml version="1.0" encoding="UTF-8"?>
<bookstore jcr:primaryType="nt:unstructured" xmlns:fn="http://www.w3.org/2005/xpath-functions"
           xmlns:fn_old="http://www.w3.org/2004/10/xpath-functions" xmlns:xs="http://www.w3.org/2001/XMLSchema"
           xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:mix="http://www.jcp.org/jcr/mix/1.0"
           xmlns:sv="http://www.jcp.org/jcr/sv/1.0" xmlns:rep="internal" xmlns:nt="http://www.jcp.org/jcr/nt/1.0">
    <catalog jcr:primaryType="nt:unstructured">
        <book jcr:primaryType="nt:unstructured" author="Gambardella, Matthew"
              description="An in-depth look at creating applications             with XML." genre="Computer" id="bk101"
              price="44.95" publish_date="2000-10-01" title="XML Developer&apos;s Guide"/>
        <book jcr:primaryType="nt:unstructured" author="Ralls, Kim"
              description="A former architect battles corporate zombies,             an evil sorceress, and her own childhood to become queen             of the world."
              genre="Fantasy" id="bk102" price="5.95" publish_date="2000-12-16" title="Midnight Rain"/>
        <book jcr:primaryType="nt:unstructured" author="Corets, Eva"
              description="After the collapse of a nanotechnology             society in England, the young survivors lay the             foundation for a new society."
              genre="Fantasy" id="bk103" price="5.95" publish_date="2000-11-17" title="Maeve Ascendant"/>
        <book jcr:primaryType="nt:unstructured" author="Corets, Eva"
              description="In post-apocalypse England, the mysterious             agent known only as Oberon helps to create a new life             for the inhabitants of London. Sequel to Maeve             Ascendant."
              genre="Fantasy" id="bk104" price="5.95" publish_date="2001-03-10" title="Oberon&apos;s Legacy"/>
        <book jcr:primaryType="nt:unstructured" author="Corets, Eva"
              description="The two daughters of Maeve, half-sisters,             battle one another for control of England. Sequel to             Oberon&apos;s Legacy."
              genre="Fantasy" id="bk105" price="5.95" publish_date="2001-09-10" title="The Sundered Grail"/>
        <book jcr:primaryType="nt:unstructured" author="Randall, Cynthia"
              description="When Carla meets Paul at an ornithology             conference, tempers fly as feathers get ruffled."
              genre="Romance" id="bk106" price="4.95" publish_date="2000-09-02" title="Lover Birds"/>
        <book jcr:primaryType="nt:unstructured" author="Thurman, Paula"
              description="A deep sea diver finds true love twenty             thousand leagues beneath the sea."
              genre="Romance" id="bk107" price="4.95" publish_date="2000-11-02" title="Splish Splash"/>
        <book jcr:primaryType="nt:unstructured" author="Knorr, Stefan"
              description="An anthology of horror stories about roaches,             centipedes, scorpions  and other insects."
              genre="Horror" id="bk108" price="4.95" publish_date="2000-12-06" title="Creepy Crawlies"/>
        <book jcr:primaryType="nt:unstructured" author="Kress, Peter"
              description="After an inadvertant trip through a Heisenberg             Uncertainty Device, James Salway discovers the problems             of being quantum."
              genre="Science Fiction" id="bk109" price="6.95" publish_date="2000-11-02" title="Paradox Lost"/>
        <book jcr:primaryType="nt:unstructured" author="O&apos;Brien, Tim"
              description="Microsoft&apos;s .NET initiative is explored in             detail in this deep programmer&apos;s reference."
              genre="Computer" id="bk110" price="36.95" publish_date="2000-12-09"
              title="Microsoft .NET: The Programming Bible"/>
        <book jcr:primaryType="nt:unstructured" author="O&apos;Brien, Tim"
              description="The Microsoft MSXML3 parser is covered in             detail, with attention to XML DOM interfaces, XSLT processing,             SAX and more."
              genre="Computer" id="bk111" price="36.95" publish_date="2000-12-01"
              title="MSXML3: A Comprehensive Guide"/>
        <book jcr:primaryType="nt:unstructured" author="Galos, Mike"
              description="Microsoft Visual Studio 7 is explored in depth,             looking at how Visual Basic, Visual C++, C#, and ASP+ are             integrated into a comprehensive development             environment."
              genre="Computer" id="bk112" price="49.95" publish_date="2001-04-16"
              title="Visual Studio 7: A Comprehensive Guide"/>
    </catalog>
</bookstore>
```

## The System View?
I promised earlier I would show what the system view looks like and how it differs from the document view. All it takes is some tweaks to our else clause and we can see it. Let's change it to:

```java Modify Our Code To See The System View
}else{
	FileOutputStream ostream = new FileOutputStream("systemview.xml");
	//session.exportDocumentView("/bookstore",ostream,true,false);
	session.exportSystemView("/bookstore",ostream,true,false);
	ostream.close();
}
```

When we checkout the systemview.xml file we will see:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<sv:node sv:name="bookstore" xmlns:fn="http://www.w3.org/2005/xpath-functions"
         xmlns:fn_old="http://www.w3.org/2004/10/xpath-functions" xmlns:xs="http://www.w3.org/2001/XMLSchema"
         xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:mix="http://www.jcp.org/jcr/mix/1.0"
         xmlns:sv="http://www.jcp.org/jcr/sv/1.0" xmlns:rep="internal" xmlns:nt="http://www.jcp.org/jcr/nt/1.0">
    <sv:property sv:name="jcr:primaryType" sv:type="Name">
        <sv:value>nt:unstructured</sv:value>
    </sv:property>
    <sv:node sv:name="catalog">
        <sv:property sv:name="jcr:primaryType" sv:type="Name">
            <sv:value>nt:unstructured</sv:value>
        </sv:property>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Gambardella, Matthew</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>An in-depth look at creating applications with XML.</sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Computer</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk101</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>44.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-10-01</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>XML Developer's Guide</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Ralls, Kim</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>A former architect battles corporate zombies, an evil sorceress, and her own childhood to
                    become queen of the world.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Fantasy</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk102</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>5.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-12-16</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Midnight Rain</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Corets, Eva</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>After the collapse of a nanotechnology society in England, the young survivors lay the
                    foundation for a new society.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Fantasy</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk103</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>5.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-11-17</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Maeve Ascendant</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Corets, Eva</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>In post-apocalypse England, the mysterious agent known only as Oberon helps to create a new
                    life for the inhabitants of London. Sequel to Maeve Ascendant.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Fantasy</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk104</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>5.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2001-03-10</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Oberon's Legacy</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Corets, Eva</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>The two daughters of Maeve, half-sisters, battle one another for control of England. Sequel to
                    Oberon's Legacy.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Fantasy</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk105</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>5.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2001-09-10</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>The Sundered Grail</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Randall, Cynthia</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>When Carla meets Paul at an ornithology conference, tempers fly as feathers get ruffled.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Romance</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk106</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>4.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-09-02</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Lover Birds</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Thurman, Paula</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>A deep sea diver finds true love twenty thousand leagues beneath the sea.</sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Romance</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk107</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>4.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-11-02</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Splish Splash</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Knorr, Stefan</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>An anthology of horror stories about roaches, centipedes, scorpions and other insects.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Horror</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk108</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>4.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-12-06</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Creepy Crawlies</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Kress, Peter</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>After an inadvertant trip through a Heisenberg Uncertainty Device, James Salway discovers the
                    problems of being quantum.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Science Fiction</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk109</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>6.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-11-02</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Paradox Lost</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>O'Brien, Tim</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>Microsoft's .NET initiative is explored in detail in this deep programmer's reference.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Computer</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk110</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>36.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-12-09</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Microsoft .NET: The Programming Bible</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>O'Brien, Tim</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>The Microsoft MSXML3 parser is covered in detail, with attention to XML DOM interfaces, XSLT
                    processing, SAX and more.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Computer</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk111</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>36.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2000-12-01</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>MSXML3: A Comprehensive Guide</sv:value>
            </sv:property>
        </sv:node>
        <sv:node sv:name="book">
            <sv:property sv:name="jcr:primaryType" sv:type="Name">
                <sv:value>nt:unstructured</sv:value>
            </sv:property>
            <sv:property sv:name="author" sv:type="String">
                <sv:value>Galos, Mike</sv:value>
            </sv:property>
            <sv:property sv:name="description" sv:type="String">
                <sv:value>Microsoft Visual Studio 7 is explored in depth, looking at how Visual Basic, Visual C++, C#,
                    and ASP+ are integrated into a comprehensive development environment.
                </sv:value>
            </sv:property>
            <sv:property sv:name="genre" sv:type="String">
                <sv:value>Computer</sv:value>
            </sv:property>
            <sv:property sv:name="id" sv:type="String">
                <sv:value>bk112</sv:value>
            </sv:property>
            <sv:property sv:name="price" sv:type="String">
                <sv:value>49.95</sv:value>
            </sv:property>
            <sv:property sv:name="publish_date" sv:type="String">
                <sv:value>2001-04-16</sv:value>
            </sv:property>
            <sv:property sv:name="title" sv:type="String">
                <sv:value>Visual Studio 7: A Comprehensive Guide</sv:value>
            </sv:property>
        </sv:node>
    </sv:node>
</sv:node>
```

Notice that this more closely represents the storage structure that the JSR specifies.

## Summary
It's easy to see how XML importation and exportation is a good fit for the JCR. We will keep our xml file around in our project for later. Hopefully, at this point, you are comfortable with how importing and exporting data works.