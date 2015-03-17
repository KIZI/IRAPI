package org.apache.nutch.parse.media.nodewalkextractors;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.media.model.ImageMedia;
import org.apache.nutch.media.model.Media;
import org.apache.nutch.parse.media.MediaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Class {@code ImageNodeExtractor} is used for extracting information about images. This is example
 * of extracting without JSOUP library.
 *
 * @author babu
 *
 */
public class ImageNodeExtractor implements NodeWalkExtractor {
	public static final Logger LOG = LoggerFactory.getLogger(ImageNodeExtractor.class);
	public static final String BAD_PATTERNS = "(.*)(banner|img/tooltipp|img/download|content_top|view/add-to-playlist|btn-|/logos/|logo_home|/static/|button|/ico/|/ico_|footer|header|icon)(.*)";
	private static final int MIN_WIDTH = 40;
	private static final int MIN_HEIGHT = 40;

	protected List<Media> mediaEntries = new ArrayList<>();

	@Override
	public List<Media> getMedia() {
		return this.mediaEntries;
	}

	@Override
	public void clear() {
		mediaEntries.clear();
	}

	@Override
	public void matchAndParseMetaData(final Node node, final String pageUrl) {
		if (node.getNodeType() != Node.ELEMENT_NODE || !!!("img".equalsIgnoreCase(node.getNodeName()))) {
			return;
		}
		String srcUrl = null, alt = "", title = "", width = "", height = "";
		NamedNodeMap attrs = node.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attrnode = attrs.item(i);
			if ("src".equalsIgnoreCase(attrnode.getNodeName())) {
				srcUrl = getValidFormatSrc(attrnode.getNodeValue(), pageUrl);
			}
			if ("alt".equalsIgnoreCase(attrnode.getNodeName())) {
				alt = attrnode.getNodeValue();
			}
			if ("title".equalsIgnoreCase(attrnode.getNodeName())) {
				title = attrnode.getNodeValue();
			}
			if ("width".equalsIgnoreCase(attrnode.getNodeName())) {
				width = attrnode.getNodeValue();
			}
			if ("height".equalsIgnoreCase(attrnode.getNodeName())) {
				height = attrnode.getNodeValue();
			}
		}
		// prepare width and height--------------------------------------------------------------
		ImageSize imageSize;
		if (width == null || !StringUtils.isNumeric(width) || height == null || !StringUtils.isNumeric(height)) {
			imageSize = extractWidthAndHeightDirectly(srcUrl);
		} else {
			try {
				int widthINT = Integer.parseInt(width);
				int heightINT = Integer.parseInt(height);
				imageSize = new ImageSize(widthINT, heightINT);
			} catch (NullPointerException | NumberFormatException e) {
				imageSize = extractWidthAndHeightDirectly(srcUrl);
			}
		}

		// FILTER icons,header images etc. ==============================================
		if (srcUrl == null || srcUrl.matches(BAD_PATTERNS) || isTooSmallImage(imageSize)) {
			return;
		}
		// ==============================================================================
		ImageMedia image = new ImageMedia(srcUrl, this);
		image.setUrl(srcUrl);
		if (alt != null) {
			image.setDescription(alt);
			image.setAlt(alt);
		}
		if (title != null) {
			image.setTitle(title);
		}
		if (imageSize != null) {
			image.setImageSize(imageSize.getWidth(), imageSize.getHeight());
		}
		this.mediaEntries.add(image);
	}

	/**
	 * Methods returns expected page independent URL of image. If it is not directed in src
	 * attribute, method tries to complete the URL form web page URL and src path. If path is "" or
	 * cannot be completed for some reason null is returned.
	 *
	 * @param imageSrc
	 *            value form src attribute
	 * @param pageUrl
	 *            web page URL
	 * @return valid format of image src or null
	 */
	private String getValidFormatSrc(String imageSrc, final String pageUrl) {
		if (MediaUtil.isValidURL(imageSrc)) {
			return imageSrc;
		}
		if ("".equals(imageSrc)) {
			return null;
		}
		try {
			URL urlObject = new URL(pageUrl);
			String path = urlObject.getPath().substring(0, urlObject.getPath().lastIndexOf('/') + 1);
			String authority = urlObject.getAuthority();
			if (authority == null) {
				authority = "";
			}
			String base = urlObject.getProtocol() + "://" + authority;
			if (imageSrc.charAt(0) == '/') {
				imageSrc = base + imageSrc;
			} else {
				imageSrc = base + path + imageSrc;
			}
		} catch (Exception e) {
			LOG.warn("Not valid URL of image src : " + imageSrc + ", pageURl: " + pageUrl, e);
			return null;
		}
		return imageSrc;
	}

	// FILTERING -------------------------------------------------------------------------------
	/**
	 * Filter the small images, these images are mostly icons, buttons or any other not wanted types
	 *
	 * @param imageSize
	 * @return
	 */
	private boolean isTooSmallImage(final ImageSize imageSize) {
		if (imageSize == null) return false;// we cannot say anything about image size, better to
		// have it
		return (imageSize.getWidth() < MIN_WIDTH || imageSize.getHeight() < MIN_HEIGHT);
	}

	// IMAGE WIDTH AND HEIGHT -----------------------------------------------------------

	private class ImageSize {
		private final int width;
		private final int height;

		public ImageSize(final int width, final int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}

	/**
	 * partially downloads image and calculate weight and height
	 *
	 * @param image
	 * @param src
	 * @param baseUrl
	 */
	private ImageSize extractWidthAndHeightDirectly(final String src) {
		ImageSize result = null;
		int width = 0, height = 0;
		ImageInputStream in = null;
		try {
			URL imgUrl = new URL(src);
			in = ImageIO.createImageInputStream(imgUrl.openStream());
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					width = reader.getWidth(0);
					height = reader.getHeight(0);
					result = new ImageSize(width, height);
				} finally {
					reader.dispose();
				}
			}
		} catch (Exception e) {
			LOG.warn("Extract Width And Height Directly failed for img src " + src + " with exception "
					+ e.getClass().getName(), e);
		} finally {
			if (in != null) try {
				in.close();
			} catch (IOException e) {
				LOG.warn("Extract width and height Directly failed for img src " + src, e);
			}
		}
		return result;
	}
}
