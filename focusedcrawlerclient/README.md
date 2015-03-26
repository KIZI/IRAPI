# Focused Crawler Client
ZeroMQ client for Focused Crawler that provides HTTP API

> Created within the project [LinkedTV](http://linkedtv.eu/) at [The University of Economics, Prague (VŠE)](http://www.vse.cz/english/).

# Examples
```
/api/v0/status - information about status of focused crawler: tasks in the queue and completed tasks
/api/v0/tasks?domain_source={ds}&query={q} - add new task with specific query and domain_source 
/api/v0/candidates?domain_source={ds}&query={q} - returns a set of urls as a set of candidates for crawling according to query and domain_source
/api/v0/metadata?url={url} - returns extracted metadat form defined url
```

# Installation
1. Clone project
2. Build with Maven and Deploy as a web application 

