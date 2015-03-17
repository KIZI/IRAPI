Media-extractor plugin for Apache Nutch README

Media-extractor plugin is plugin for Apache Nutch 2.3, created within the project LinkedTV at The University of Economics, Prague (VŠE).

It purpose is to extract media informations (url,title,description,width,...) from webpage and respect the links between the page and the media (M:1 binding). As a media is considered image, video, audio.

Use of this plugin includes modification of Nutch source code.

1) copy all java files in folder "media" under package $NUTCH_HOME/src/java/org/apache/nutch/media

     cp media $NUTCH_HOME/src/java/org/apache/nutch/media

2) modifiy org.apache.nutch.indexer.NutchDocument -> same as "modified_NutchDocument/NutchDocument.java" 

In fact the modification is only about adding this code:

private List<Media> media;

	public void addMedia(List<Media> media) {
		this.media = media;
	}

	public List<Media> getMedia() {
		return media;
	} 

These modifications are necessary for indexing part of crawling proces.

For more informations see the wiki page: https://github.com/KIZI/IRAPI/wiki

For more information about the project LinkedTV see: http://www.linkedtv.eu/
For more information about The University of Economics, Prague (VŠE), see: http://www.vse.cz/english/


