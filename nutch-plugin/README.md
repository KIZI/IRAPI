# Media-extractor plugin for Apache Nutch 2 README

Media-extractor plugin is plugin for Apache Nutch 2.3, created within the project [LinkedTV](http://linkedtv.eu/) at the [University of Economics, Prague (VŠE)](http://www.vse.cz/english/).

Its purpose is to extract media information (url,title,description,width,...) from webpage preserving the links between the page and the media (M:1 binding). The following media types are recognized: image, video (multiple formats), audio.

## Installation
### Prerequisites:
* JDK 1.7
* ant

Use of this plugin requires modification of the Nutch source code.

1. copy all java files in folder *"org/apache/nutch/media/model/"* under package *$NUTCH_HOME/src/java/org/apache/nutch/media*

```
cp media $NUTCH_HOME/src/java/org/apache/nutch/media

```

2. modifiy *org.apache.nutch.indexer.NutchDocument* -> same as in folder [modified_NutchDocument](https://github.com/KIZI/IRAPI/tree/master/nutch-plugin/modified_NutchDocument).

In fact the modification is only about adding this code:

```Java
        private List<Media> media;

	public void addMedia(List<Media> media) {
		this.media = media;
	}

	public List<Media> getMedia() {
		return media;
	}
```

These modifications are necessary for the indexing part of crawling proces.

### plugin configuration
See [media extractor plugin instalation and configuration](https://github.com/KIZI/IRAPI/wiki/Media-extractor-plugin---installation&usage).

## Wiki - principle, developer perspective
[Media extractor wiki pages](https://github.com/KIZI/IRAPI/wiki/Media-extractor-plugin----developer-perspective)

## License

[Apache 2.0](https://github.com/KIZI/IRAPI/blob/master/nutch-plugin/LICENSE.txt)
