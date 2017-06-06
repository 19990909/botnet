# Botnet

A hackable Server that creates a botnet service which is configurable even to the atomic levels.

---

The botnet is organized in the following structure:-

 - The *bin* folder contains necessary binaries to execute the botnet.
 - The *bot_hooks* folder contains some Velocity templates that are used as event hooks.
 - The *conf* folder contains files that configure the Botnet Server.
 - The *src* folder contains weapons of mass destruction.
 
---

![Does it?](https://cdn.meme.am/cache/instances/folder642/65997642.jpg)

Yea ofc!

The Botnet is written on a very neat approach and practically understandable design pattern.

The Botnet Server can contain many *Botnet Services*, that contains a single *Botnet Engine* that is responsible for the management of bots.

The Botnet Server could be configured using the files in *conf* folder.

The standard implementation of Botnet has just only one bot, which can hit some echo service and get the results.

---

# How do I spend my time breaking your app?

Simple. After you build the Botnet Server, it would create a *botnet.jar* file in the *lib* folder. That single file contains the API that you can use to write your own features.

# How do I build it?

Check this project out in a folder, and then run `mvn clean install` command.

---

# Future plans for this project.

 - Add more bots.
 - Implement a webserver to manage bots through a browser.
 - Fully implement the awaitSocket functionality that I dont want to explain.
 - Make it in such way that all bots are individually interactive.
 - Maybe document it.

Cheers: Jay