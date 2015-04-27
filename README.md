![LinkedTV project](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/linkedtvlogo.png) <br/>Supported by LinkedTV
# IRAPI - media search engine

IRAPI is a repository that holds all parts of builded MEDIA SEARCH ENGINE, which was initially developed for the <a href="http://linkedtv.eu">LinkedTV project</a>.

## Folder description

* **"dashboard"**     : contains web application which displays detailed statistics for Apache Solr index and  allows to edit the seed list.

* **"nutch-plugin"**     : contains plugin for Apache Nutch 2. Its purpose is to extract media from webpages 
	* instalation and usage : [/wiki/Media-extractor-plugin---installation&usage](https://github.com/KIZI/IRAPI/wiki/Media-extractor-plugin---installation&usage)
	*  class documentation : [wiki/Media-extractor-plugin,-principle-and-class-documentation](https://github.com/KIZI/IRAPI/wiki/Media-extractor-plugin,-principle-and-class-documentation)

* **"solr-example-conf/cores"** : example configuration for Apache Solr index compatible with data structure required by the media-extractor (nutch-plugin)

* **"search"**            : contains web application providing  endpoint for searching over indexed media data
	* wiki page:[wiki/Searching---How-the-search-over-indexed-data-is-done](https://github.com/KIZI/IRAPI/wiki/Searching---How-the-search-over-indexed-data-is-done) 

* **"focusedcrawler, focusedcrawler client"** : application for focused on-demand video crawling (wraps on-line search of several news websites) Within IRAPI, the focused crawl is triggered by query issues against the search web application.

> **Note:** While the project is customized for LinkedTV purposes,  it can serve as inpiration or template for other related uses.

More information about usage and instalations to individual application on related [wiki pages](https://github.com/KIZI/IRAPI/wiki) or in folders README.
