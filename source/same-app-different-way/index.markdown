---
layout: page
title: "Same App, Different Way"
date: 2015-05-19 23:36
comments: true
sharing: true
footer: true
---
Same App, Different Way is a concept for tutorials that allows us to built the same solution in different frameworks, technologies and/or languages. We did this so that
folks who may not have much development experience can see how a relatively simple app is done in a myriad of different ways. This allows them to learn, examine and possible choose a new framework or technology based on what it takes to create an application.

Since all applications are designed to solve a problem, lets take a look at our problem.

## The Problem
Podcasting is taking over the world. Well, some of the world. Everyday more and more podcasters get their start, but recently iTunes took it to a whole new level. They placed the podcats app as one of their default applications on the phone. This means a whole new world of subscribers just got their first introduction into this dynamic and fascinating world of internet broadcasting. Now for the subscribers this is great, for the podcasters themselves it created a need.

See, iTunes uses an RSS feed to put items in the podcast store for users of iPhones to subscribe to. All a podcaster can do it created a feed and submit to iTunes for acceptance. However, most poscasters are not web designers or developers.

This means there is a need to have a web application that a podcaster can easily deploy that allows them to enter in the URL's to their podcasts and some superflious information and have it dynamically generate their RSS feed for iTunes.

## Our Application Requirements
Our app will allow a user to login (preferably using facebook or twitter). Once logged in the user will be presented with all of the podcasts they have submitted organized by the date. From here they can add/remove/update all of the media for each podcast they run. When they are done, the RSS feed for that specific podcast will reflect the changes.

Some important points:

* The application will only save the URL's to media that is stored somewhere else. We won't be taking the uploads themselves (at least not yet).
* The application will allow a single user to maintain multiple feeds.
* The application will need to have a different feed for each podcast.
* The application will allow for all of the itunes specific elements to be maintained.
* The target user of the application is someone who isn't a developer.

## The Tutorials
Each tutorial will build this app differently, however the core use of the application will remain the same. If you have suggestions on specific frameworks and/or technologies you would like to see, leave them in the comments!