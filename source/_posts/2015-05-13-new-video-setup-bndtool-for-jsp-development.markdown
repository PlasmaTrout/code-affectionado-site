---
layout: post
title: "New Video: Setup BndTool For JSP Development"
date: 2015-05-13 17:19
comments: true
categories: 
---
Today I worked on a video tutorial on how to setup BndTools with Pax-Web so you can develop JSP applications on it.
I originally found out how to get this accomplished by looking at the imports that Karaf uses when you turn the
features on for it. Translating them to felix wasn't hard but it was time consuming and tedious. In the end you only
need a [handful of bundles](https://app.box.com/s/xwvcb9ai5v6ht43tqnnsn9ih706c6b2q) to make it work. I've zipped them all up and put them on Box.net to make them easier to get started with. Here's the video, enjoy!

<iframe width="625" height="385" src="https://www.youtube.com/embed/_q7_8yGJ1nM" frameborder="0" allowfullscreen></iframe>

It took some trial and error, but after I made it work once, I captured the launch descriptor in a gist which you can see as well:

<script src="https://gist.github.com/PlasmaTrout/7dd5646a258a664ce8bd.js"></script>

The video link to youtube is on the [labs and tutorials](/labs-and-tutorials]) page.