# IRAPI search demo

## Purpose
IRAPI search is a demo web appliaction based on Jersey framework, which handles searching over crawled media data in SOLR indexes. It provides only one endpoint and returns results in JSON format.

> **Note:** The project is customized for [LinkedTV](http://linkedtv.eu/) project purposes, rather it is assumed that it will be used for inspiration how to build searching part of search engine.

## Installation
### Prerequisites:
* JDK 1.7
* Maven 3.*

1. download/clone the project
2. prepare indexes [example SOLR index configurations](https://github.com/KIZI/IRAPI/tree/master/solr-example-conf/cores)
3. in class [SolrIndexPool](https://github.com/KIZI/IRAPI/blob/master/search/src/main/java/eu/linkedtv/irapi/search/solr/SolrIndexPool.java) are hardcoded indexes, change the urls or change the class according your needs
4. do the standart web application build and deploy (mvn install, deploy .war file on server)

## Wiki
[IRAPI search wiki](https://github.com/KIZI/IRAPI/wiki/IRAPI-search) 

## License

[Apache 2.0](https://github.com/KIZI/IRAPI/blob/master/search/LICENCE.TXT)


## Example usage
QUERY: *http://localhost:8080/search/media-server/?q="sonne"&media_type=image*

![example IRAPI result](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/search_json_output.png)
