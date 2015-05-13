# Dashboard
A dashboard is a web application which helps to monitor results of the crawling and indexing processes. The main purpose of the dashboard is to offer detailed and up-to-date statistics for the data stored in the index.

It provides an overview of how many media objects have been identiﬁed in the crawled web pages. The dashboard distinguishes between the types of documents stored (web page, image, video, podcast). Data which are shown in the dashboard are retrieved directly from the index using appropriate queries, therefore the dashboard displays “live” index status.

There are several other functionalities. It is possible to monitor the server availability, to check the last modiﬁcation date and day increases of the index, to determine the number of stored documents, to ﬁlter and to sort results or make several data exports. Supported data formats are CSV and XML. It also gives the content partners the ability to control the white list.

A brief overview of the index statistics is also given as a pie chart.

![Total numbers in chart](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/dashboard/ltv_dashboard_graph.png)

The statistics displayed by the dashboard are generated using Lucene queries, separate queries are issues for individual media media types stored in index, the returned results are processed and counts of media types are used to populate data tables and charts.

![Table stats for whitelisted hosts](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/dashboard/ltv_dashboard_stats.png)

There could be several web sites which do not have any media crawled and indexed. These web sites are indicated by "red zero" column values in the row dedicated to the web site. If there is a web site with row full of zeroes, it means that crawler has no access to the site, typically due to exclusion of the Nutch robot in the robots.txt ﬁle or due to another restriction from the web site provider.

![Day increases by document type stored to index](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/dashboard/ltv_dashboard_increases.png)

Whitelist administration (adding of URL) is realized by remote modifying conﬁguration ﬁles, which are part of the Nutch crawling process.

a. Seed ﬁle which is the main ﬁle to add URL to be processed.
b. Regex-urlﬁlter to restrict URL by regular expression pattern.
c. Whitelist-urlﬁlter to indicate which type of whitelisted URL is stored in index.

![Process to apply all changes](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/dashboard/ltv_dashboard_process.png)

The dashboard allows to manage both whitelists. The user ﬁrst picks up the whitelist, and then there he is presented with option to add new URL.

![Adding URL to whitelist](https://raw.githubusercontent.com/KIZI/IRAPI/master/wikipictures/dashboard/ltv_dashboard_addurl.png)

> Created within the project [LinkedTV](http://linkedtv.eu/) at [The University of Economics, Prague (VŠE)](http://www.vse.cz/english/).

# Installation
1. Clone project
2. Update settings in properties file (application.properties)
3. Create database with prepared SQL script (dashboard_backup.sql or dashboard_backup_insert.sql with example data)
4. Build with Maven and run as a web application
