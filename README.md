![LinkedTV project](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/linkedtvlogo.png)
# IRAPI - media search engine

IRAPI is a repository that holds all parts of builded MEDIA SEARCH ENGINE for the LinkedTV project.

## Folder description

* **"dashboard"**     : contains web application, which displays detailed statistics for Apache Solr index and tools for provide operation with data.

* **"nutch-plugin"**     : contains plugin for Apache Nutch 2, which purpose is to extract media from webpages - important for crawling task
	* instalation and usage : [/wiki/Media-extractor-plugin---installation&usage](https://github.com/KIZI/IRAPI/wiki/Media-extractor-plugin---installation&usage)
	* principle and class documentation : [wiki/Media-extractor-plugin,-principle-and-class-documentation](https://github.com/KIZI/IRAPI/wiki/Media-extractor-plugin,-principle-and-class-documentation)

* **"solr-example-conf/cores"** : contains example configuration for Apache Solr index, which corresponds to crawled data from media-extractor (nutch-plugin)

* **"search"**            : contains web application, which provides an endpoint for searching over indexed media data
	* wiki page:[wiki/Searching---How-the-search-over-indexed-data-is-done](https://github.com/KIZI/IRAPI/wiki/Searching---How-the-search-over-indexed-data-is-done) 

* **"focusedcrawler, focusedcrawler client"** : special appliaction for focused video extracting

> **Note:** The project is customized for LinkedTV purposes, rather it is assumed that it will be used for inspiration.

More information about usage and instalations to individual application on related [wiki pages](https://github.com/KIZI/IRAPI/wiki) or in folders README.
