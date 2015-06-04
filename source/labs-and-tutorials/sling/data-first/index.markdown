---
layout: page
title: "Apache Sling - Data First Development"
date: 2015-06-03 22:45
comments: true
sharing: true
footer: true
indexer: true
---
In this tutorial we will learn how post data to our [CastR](/same-app-different-way) application by using the Sling POST Servlet built into Apache Sling. Future tutorials will use this data to show how to build pages that render the contents of this exercise. 

## Data First Development
Before we begin let's have a quick review on how Sling works. Apache Sling operates a bit differently from most frameworks in that it's considered a data first development platform, but what does that mean exactly? 

### The Normal Way
In a typical application we first must decide what framework we want to use, because once we choose we are usually confined to it's development and build process. After making a decision on the specific framework (Grails, Tomcat, .NET etc), we generally start a project structure and start working with a specific style of pages (GSP, JSP, ASPX, etc). Once the project is running and a few test pages have been built, we normally decide on a data-store and then start hooking our application up to the database. We may even decide later that building a RESTfull interface over the data is a better solution to help integrate with other systems.
Then we have to prime the database with some starting content and then we start development of our dynamic sections of code.

### The Sling Way
Sling works backwards from the normal way. In Sling a content storage system is already built into the framework and this content storage system already has a RESTfull API over top of it (BTW built by the same person that wrote the REST specification in the first place). It also exposes a programmatic API within it so server side pages and code can talk directly to the content repository without using DB related queries and code. 

Development is almost always started with a core set of data to store or view. Once the data has been stored in the content repository (called a JCR) it can then be accessed via a URL using the RESTfull layer on top of it. 

When data in Sling is accessed via a URL, a set of servlets try to make an intelligent decision about how to render it back to the browser by using the extension (.html, .json, .xml etc). Sling also understands that html can be rendered by other scripting languages and searches for plugged in languages that can be used. Because of this, Apache Sling doesn't care how you want to build pages, your more than welcome to use whatever method (JSP, ESP, XSLT, Groovy etc) or a mix of these methods if you wish. 

### The Nutshell
Normal development usually constrains you to a specific scripting language, gives you free reign of databases, and requires you to implement RESTfull facets.

Apache Sling constrains you to a content storage paradigm, provides a RESTfull interface and gives you free reign of what scripting/languages you would like to use (with some obvious exceptions).

## Starting Our Podcast Site
Armed with our new understanding of how Apache Sling works we need to take some steps to set everything up:

1. We need to decide on our canonical URL structure of our site
2. We need to store some data to make our URL structure live
3. We can then test the rendering in JSON and XML formats to make sure it looks good

_Note: Future tutorials will render these pages in HTML so don't sweat that part just yet._

### The CastR URL Structure
Before we go too far, let's first talk about the JCR and node based storage. Node storage is very similar to the file tree style controls in Windows Explorer and Mac Finder. Each one starts with a root note, signified by ```/``` and usually folders underneath help organize things into logical structures. We typically don't just throw a ton of stuff in the root folder because then it becomes hard to understand and organize. Imagine if hundreds of documents were just sitting in the root of your drive.

In Apache Sling we have another concern in that the root folder already has some other folders in it. These folders are in lower case and some have specific purposes and we should mess with them. For instance the ```apps/``` folder is very important to Apache Sling. Right now our directory looks like:

{% img /images/sling/fix1.PNG 'WebDav Mounted' 'WebDav Mounted' %}

This is solved in the Apache Sling and Adobe AEM world by a habit that developers have of creating a folder named ```content/``` to store the content in (don't do this just yet, we're getting to it). Sticking with this convention, and knowing we were creating a site to store podcasts and episodes, it makes since to have a URL structure like this:

```
http://localhost:8080/content/castr/podcasts
```

Getting a specific podcast would be done like this:

```
http://localhost:8080/content/castr/podcasts/my_crazy_podcast
```

Getting an episode would then look something like this:

```
http://localhost:8080/content/castr/podcasts/my_crazy_podcast/episode_1
```

### Curl? 
To make this tutorial work, a tool named curl is needed. On a Mac or Linux machine it's probably already there. For Windows the best bet is use the Git Bash shell to do this tutorial since it's already built into it. If you need it for windows, find it at this [link](https://git-scm.com/download/win). Make sure to install the git bash shell.

This gives us enough to get started. Now we need to learn how to get data into Apache Sling and how to visualize it after we do.

## Priming Some Podcast Data
Posting data to Apache Sling will seem relatively straight forward and limited at first, however looks are deceiving. There is a very polished [servlet](https://sling.apache.org/documentation/bundles/manipulating-content-the-slingpostservlet-servlets-post.html) hanging out behind the scenes that can do some pretty amazing things. But lets start by posting a single field to a URL and watch what happens. In the command prompt (with curl installed) paste in the following line:

```
curl "http://localhost:8080/content/castr/podcasts/programmer_vs_world" -F"title=Programmer Versus World" -u "admin:admin"
```

_Note: Here we posted to our ```content -> castr -> podcasts``` folder a new post named ```programmer_vs_world``` and gave it one field. The -u flag is our login credentials which is needed to manipulate data._

In the HTML response notice the following two table entries:

```xml
<tr>
    <td>Status</td>
    <td><div id="Status">201</div></td>
</tr>
<tr>
    <td>Message</td>
    <td><div id="Message">Created</div></td>
</tr>
```

_Note: A HTML response comes back from this as the default reply because most POSTs are coming from web forms and this would be seen by the browser after a successful post._

### Viewing Our New Post
To view the new post we will use the default JSON renderer. You'll notice we will use the form ```somepage.<number>.json``` a lot. The number in this format tells Sling how far down to recursively render. In most cases we will use 4 but feel free to experiment. The phrase "infinity" can also be used if you want to really have some fun later.

Navigate to:

[http://localhost:8080/content.4.json](http://localhost:8080/content.4.json)

That command, did indeed create the full path we wanted. Notice how, even if the nodes didn't exist, Apache Sling just created them for us. 

We can further narrow out display of the data by using URLS like:
 
* [http://localhost:8080/content/castr.4.json](http://localhost:8080/content/castr.4.json)
* [http://localhost:8080/content/castr/podcasts.4.json](http://localhost:8080/content/castr/podcasts.4.json), * [http://localhost:8080/content/castr/podcasts/programmer_vs_world.json](http://localhost:8080/content/castr/podcasts/programmer_vs_world.4.json).

Because we only added one field (title) when we made that final node it ended up like this:

```javascript
{
	"title": "Programmer Versus World",
	"jcr:primaryType": "nt:unstructured"
}
```

_Note: The primaryType property is automatically added in our case because it's mandatory. The nt:unstructured type just means it's schema-less._ 

### Adding Another Node
Let's add two more podcast nodes and see what happens:

```
curl "http://localhost:8080/content/castr/podcasts/some_other_podcast" -F"title=The Other Podcast" -u "admin:admin"

curl "http://localhost:8080/content/castr/podcasts/some_other_podcast2" -F"title=The Deletable Podcast" -u "admin:admin"
```
Now if we go to:

```
http://localhost:8080/content/castr/podcasts.4.json
```

We notice we have three podcasts nodes now:

```javascript
{
	{
		"jcr:primaryType": "nt:unstructured",
		"programmer_vs_world": {
		"title": "Programmer Versus World",
		"jcr:primaryType": "nt:unstructured"
	},
		"some_other_podcast": {
		"title": "The Other Podcast",
		"jcr:primaryType": "nt:unstructured"
	},
		"some_other_podcast2": {
		"title": "The Deletable Podcast",
		"jcr:primaryType": "nt:unstructured"
	}
}
```

## Deleting A Node
That *some_other_podcast2* entry was just an extra. Lets remove it. To do this we need to use a special field that the Apache Sling post servlet understands. It's named ```:operation```.

```
curl "http://localhost:8080/content/castr/podcasts/some_other_podcast2" -F":operation=delete" -u "admin:admin"
```

Notice how this is still a *HTTP POST*, just with a field named ":operation" set to "delete". This will delete whatever node is specified in the URL. Be careful with this one. 

Now our [podcasts node](http://localhost:8080/content/castr/podcasts.4.json) shows:

```
{
	{
		"jcr:primaryType": "nt:unstructured",
		"programmer_vs_world": {
		"title": "Programmer Versus World",
		"jcr:primaryType": "nt:unstructured"
	},
		"some_other_podcast": {
		"title": "The Other Podcast",
		"jcr:primaryType": "nt:unstructured"
	}
}
``` 

## Updating Data
To update data in Apache Sling all we need to do is post again to the same area with the changes we want to see. If we don't want a specific field changed, we just leave it out. The first podcast entry we created only had a title field and a primaryType field. Let's add a description to it by copying the following command to the terminal:

```
curl "http://localhost:8080/content/castr/podcasts/programmer_vs_world" -F"description=Programmer vs World is a podcast centered around asking the hard coding, programming, engineering and software architecture related questions but with a fun and humorous touch." -u "admin:admin"
```

Now when we view the [podcasts](http://localhost:8080/content/castr/podcasts.4.json) node we get:

```javascript
{
	{
		"jcr:primaryType": "nt:unstructured",
		"programmer_vs_world": {
		"description": "Programmer vs World is a podcast centered around asking the hard coding, programming, engineering and software architecture related questions but with a fun and humorous touch.",
		"title": "Programmer Versus World",
		"jcr:primaryType": "nt:unstructured"
	},
		"some_other_podcast": {
		"title": "The Other Podcast",
		"jcr:primaryType": "nt:unstructured"
	}
}
```

Let's go ahead and add a description to the other one too:

```
curl "http://localhost:8080/content/castr/podcasts/some_other_podcast" -F"description=Current this podcast doesn't really exit so it doesn't really have a good description, however, if it did exist that description would go here." -u "admin:admin"
```

## Adding Child Nodes
Adding children is really just as simple as adding new fields except we need to be more specific on the URL that we give curl. Let's add some podcasts to our first node just to demonstrate the point:

```
curl "http://localhost:8080/content/castr/podcasts/programmer_vs_world/episode_1" -F"title=The Beginnings Of Things To Come" -F"description=In this episode Jeff and Gabe, along with special guest Zack, talk about certifications, becoming architects and should developers be the stakeholders or architecture." -F"link=https://dl.dropboxusercontent.com/u/16986127/Podcasts/Podcast%20Session%201%20Mastered.mp3" -u "admin:admin"
```
and
```
curl "http://localhost:8080/content/castr/podcasts/programmer_vs_world/episode_2" -F"title=Build 2015" -F"description=In this episode the crew recaps a few of the big announcements from Microsofts Build 2015 conference." -F"link=https://dl.dropboxusercontent.com/u/16986127/Podcasts/PvW_Episode2_Final.mp3" -u "admin:admin"
```

Now if we checkout the root node we should see:

```javascript
{
	{
		"jcr:primaryType": "nt:unstructured",
		"programmer_vs_world": {
			"description": "Programmer vs World is a podcast centered around asking the hard coding, programming, engineering and software architecture related questions but with a fun and humorous touch.",
			"title": "Programmer Versus World",
			"jcr:primaryType": "nt:unstructured",
			"episode_1": {
				"description": "In this episode Jeff and Gabe, along with special guest Zack, talk about certifications, becoming architects and should developers be the stakeholders or architecture.",
				"link": "https://dl.dropboxusercontent.com/u/16986127/Podcasts/Podcast%20Session%201%20Mastered.mp3",
				"title": "The Beginnings Of Things To Come",
				"jcr:primaryType": "nt:unstructured"
			},
			"episode_2": {
				"description": "In this episode the crew recaps a few of the big announcements from Microsofts Build 2015 conference.",
				"link": "https://dl.dropboxusercontent.com/u/16986127/Podcasts/PvW_Episode2_Final.mp3",
				"title": "Build 2015",
				"jcr:primaryType": "nt:unstructured"
			}
		},
		"some_other_podcast": {
			"description": "Current this podcast doesn't really exit so it doesn't really have a good description, however, if it did exist that description would go here.",
			"title": "The Other Podcast",
			"jcr:primaryType": "nt:unstructured"
	}
}
```

## Some Other Fun Stuff
Now we have some primed data we can build off of on a future tutorial. But before we fully abandon this one let's try a few more URL's before we move on. Enter this one in your browser:

```
http://localhost:8080/content/castr/podcasts.xml
```

Apache Sling also has a default XML renderer as well and we don't need to specify a depth when we use it either. As you can imagine using this in conjunction with some XSLT would make things easy.

Now let's try another one:

```
http://localhost:8080/content/castr/podcasts/some_other_podcast.txt
```

Notice how we got the content of the some_other_podcast node back as text. So what happens when we use:

```
http://localhost:8080/content/castr/podcasts/some_other_podcast.html
```

Notice how we received a default response that looks like the text servlet just in an HTML form. This is the default HTML renderer and this is how things will look if we don't create pages for them.

## Summary
In this tutorial we used the sling post servlet to prime some content into our content repository. In the next tutorial we will abandon the default HTML renderer and make some pages that will render our content.









