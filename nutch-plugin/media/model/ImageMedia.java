package org.apache.nutch.media.model;

public class ImageMedia extends Media {

	public static final String IMG_ALT = "alt";
	public static final String IMG_WIDTH = "width";
	public static final String IMG_HEIGHT = "height";

	public ImageMedia(final String url, final Object solverClass) {
		super(url, TYPE_IMAGE, solverClass);
	}

	public void setAlt(final String value) {
		setInfo(IMG_ALT, value);
	}

	public void setImageSize(final int width, final int height) {
		setImageSize(width + "", height + "");
	}

	public void setImageSize(final String width, final String height) {
		setInfo(IMG_WIDTH, width);
		setInfo(IMG_HEIGHT, height);
	}

	public void setImageWidth(final String width) {
		setInfo(IMG_WIDTH, width);
	}

	public void setImageHeght(final String height) {
		setInfo(IMG_HEIGHT, height);
	}

}
