# Focused Crawler
Focused Crawler wraps the video facet search facility a predefined set of websites (RBB Mediathek, ARD Mediathek, Avrotros.nl). Using the on site search, the crawler identifies web pages embedding video that are relevant to the query issued to IRAPI. These are crawled in a priority queue, which indexes them typically within minutes of the original user query.

The [Ex](http://eso.vse.cz/~labsky/ex/) has been incorporated into the focused video crawler module. The purpose of the focused video crawler is to index documents that are relevant to queries issued to IRAPI.

Focused crawler is implemented as an application with interface using ZeroMQ messaging protocol. Input and output messages are in JSON format.

> Created within the project [LinkedTV](http://linkedtv.eu/) at [The University of Economics, Prague (VŠE)](http://www.vse.cz/english/).

# Examples
```
Status:
-> {"operation":"status"}
<- {"queueSize": 3, "queue": [ ... ], "historySize": 5, "history": [...]}

Add new task (Search for candidates, extract  metadata, update solr index):
-> {"operation":"add", "domain_source":"RBB", "query": "Berlin"}
<- {}

Candidates for crawling (on site search on a predefined set of websites):
-> {"operation":"candidates", "domain_source":"RBB", "query": "Berlin"}
-> {"candidates": [...]}

Extract metadata from url:
-> {"operation":"metadata", "url":"http://..."}
-> {"title": "...", "description": "...", "MES": { ... }}
```

# Installation
1. Clone project
2. Install dependencies to your local Maven directory (./lib/install.sh)
3. Update settings in properties files
4. Build with Maven
5. Run focusedcrawler from target directory

