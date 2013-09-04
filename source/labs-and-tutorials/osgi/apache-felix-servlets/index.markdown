---
layout: page
title: "apache-felix-servlets"
date: 2013-08-22 14:16
comments: true
sharing: true
footer: true
---
OSGi has been a mature framework for building modular applications for quite some time. It has however, not not been embraced fully by the development community. The primary reason it stays unpopular in the development arena lies in its lack of beginning level tutorials or classes. This tutorial begins a series of labs to demystify the OSGi framework and begin familiarity with Apache Felix.

This lab improves upon our SCR annotated example and adds a new servlet component so the reader can become comfortable with the Reference annotation.

## Audience 
The core audience is seated in a comfortable classroom or conference room environment. Readers performing this tutorial have received an overview of OSGi and completed a previous lab in which we setup a framework for use. A reader performing this tutorial would have a printed version of this media or the web site up on one screen while they work on code in another. The audience can be made up of developers from different disciplines but knowledge and understanding of the Java language is assumed.

## Getting Started
In our previous [example tutorial](/labs-and-tutorials/osgi/apache-felix-scr-annotations/) we used SCR annotations and pax exam to unit test a bundle done using declarative services. In this tutorial we are going to register a servlet and call our service using it.

If you haven't done the first bundle tutorial mentioned above you can grab the source at:

```bash Git Hub Quick Start https://github.com/PlasmaTrout/greeter-bundle-lab6 GitHub
git clone git@github.com:PlasmaTrout/greeter-bundle-lab6.git
```

## Installing Felix Bundles
Two bundles will be needed to be installed in Felix for this to work. That is, on top of the ones we did in the very first tutorial. They are:

> felix:install http://www.poolsaboveground.com/apache//felix/org.apache.felix.http.bundle-2.2.0.jar

and 

> felix:install http://www.poolsaboveground.com/apache//felix/org.apache.felix.http.whiteboard-2.2.0.jar

For reference my current felix bundle list looks like:

```bash Installed Bundles
g! lb
START LEVEL 1
   ID|State      |Level|Name
    0|Active     |    0|System Bundle (4.2.1)
    1|Active     |    1|Apache Felix Bundle Repository (1.6.6)
    2|Active     |    1|Apache Felix Gogo Command (0.12.0)
    3|Active     |    1|Apache Felix Gogo Runtime (0.10.0)
    4|Active     |    1|Apache Felix Gogo Shell (0.10.0)
    6|Active     |    1|Apache Felix Web Management Console (All In One) (4.2.0.all)
    7|Active     |    1|Apache Felix Http Jetty (2.2.0)
   14|Active     |    1|Apache Felix Shell Service (1.4.3)
   16|Active     |    1|Apache Felix Declarative Services (1.6.2)
   17|Active     |    1|Apache Felix Web Console Service Component Runtime/Declarative Services Plugin (1.0.0)
   18|Active     |    1|Apache Felix Configuration Admin Service (1.6.0)
   19|Active     |    1|Apache Felix Metatype Service (1.0.6)
   22|Active     |    1|greeter-bundle (1.0.0.SNAPSHOT)
   23|Active     |    1|Apache Felix Http Bundle (2.2.0)
   24|Active     |    1|Apache Felix Http Whiteboard (2.2.0)
g!
```

If you don't have all of these, get them now from the [Apache Download Page](http://felix.apache.org/downloads.cgi).

## Add A Dependency To Maven
To get a servlet up and running we need access to the servlet api. This can be done simply in Maven by adding the following dependency:

```xml javax.servlet.api
<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>servlet-api</artifactId>
	<version>2.5</version>
</dependency>
```

## Create A New Servlet
Lets add a new package under org.bhn.training called servlets and create a new file named GreeterServlet.java. We will make it look like the following annotated class:

```java Our Test Servlet
@Component(name = "Greeter Servlet", immediate = true)
@Service(value = javax.servlet.Servlet.class)
@Properties({
        @Property(name = "servlet-name", value="Greeter Servlet"),
        @Property(name = "alias", value="/greeter")
})
public class GreeterServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("Servlet Is Bound!");
    }
}
```

You should now be able to build and deploy this bundle to felix and hit <http://localhost:8080/greeter> and see our test message. If so we are ready to modify our servlet to call our greeter service.

## Get Service And Call It


