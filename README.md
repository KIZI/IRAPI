[![LinkedTV project](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/linkedtvlogo.png)]
# IRAPI - media search engine


IRAPI is a repository that holds all parts of builded MEDIA SEARCH ENGINE for the LinkedTV project.

## Folder description

* "nutch-plugin"      : contains plugin for Apache Nutch 2, which purpose is to extract media from webpages - important for crawling task

* "solr-example-conf/cores" : contains example configuration for Apache Solr index, which corresponds to crawled data from media-extractor (nutch-plugin)

* "search"            : contains web application, which provides an endpoint for searching over indexed media data

* "focusedcrawler, focusedcrawler client" : special appliaction for focused video extracting

> **Note:** The project is customized for LinkedTV purposes, rather it is assumed that it will be used for inspiration.

More information about usage and instalations to individual application on related [[wiki pages|https://github.com/KIZI/IRAPI/wiki]] or in folders README.
