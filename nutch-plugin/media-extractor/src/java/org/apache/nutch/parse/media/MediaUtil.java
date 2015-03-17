package org.apache.nutch.parse.media;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.nutch.media.model.Media;

public class MediaUtil {

	/**
	 * When using different extractors there is a chance that two of them will find the same media,
	 * but can find relevant information. Thus we merge information about the media with same id.
	 *
	 * @param extractedMedia
	 * @return <tt>List<Media></tt> list of media without duplicates
	 */
	public static List<Media> mergeDuplicates(final List<Media> extractedMedia) {
		Collections.sort(extractedMedia, Media.ID_COMPARATOR);
		List<Media> mergedMedia = new ArrayList<>();

		String previousID = null;
		List<Media> tempToMegre = new ArrayList<>();

		for (Media media : extractedMedia) {
			if (previousID != null && !previousID.equals(media.getId())) {// new different
				mergedMedia.add(merge(tempToMegre));
				tempToMegre.clear();
			}
			tempToMegre.add(media);
			previousID = media.getId();
		}
		// last media could not be resolved in loop
		if (!tempToMegre.isEmpty()) {
			mergedMedia.add(merge(tempToMegre));
		}
		return mergedMedia;
	}

	/**
	 * Merges media by this strategy:
	 * <ul>
	 * <li>one ID, one URL, one type</li>
	 * <li>description, title: concatenate</li>
	 * <li>solverClass: concatenate solvers name, so the information is preserved</li>
	 * <li>others: wins with more occurrences or first from two</li>
	 * </ul>
	 *
	 * @param mediaToMegre
	 *            temporary list of media, which should be merged
	 * @return
	 */
	private static Media merge(final List<Media> mediaToMegre) {
		Media mergedMedia = new Media();
		Set<String> keySet = new HashSet<>();
		for (Media media : mediaToMegre) {
			keySet.addAll(media.getMetaFields().keySet());
		}
		for (String key : keySet) {
			if (key.equals(Media.ID)) {
				mergedMedia.setId(mediaToMegre.get(0).getId());
				continue;
			}
			if (key.equals(Media.URL)) {
				mergedMedia.setUrl(mediaToMegre.get(0).getUrl());
				continue;
			}
			if (key.equals(Media.TYPE)) {
				mergedMedia.setType(mediaToMegre.get(0).getType());
				continue;
			}
			// description and solver class cumulate info add only unique text
			if (key.equals(Media.DESCRIPTION) || key.equals(Media.SOLVER_CLASS) || key.equals(Media.TITLE)) {
				Set<String> cumulateInfo = new HashSet<>();
				for (Media media : mediaToMegre) {
					cumulateInfo.add(media.getValue(key));
				}
				String cumulateInfoText = "";
				for (String string : cumulateInfo) {
					cumulateInfoText += string + " ";
				}
				mergedMedia.setInfo(key, cumulateInfoText);
				continue;
			}
			// other resolve stronger--------------------------------------------
			Map<String, Integer> possibleValues = new HashMap<>();
			// prepare how many values of the filed are different
			for (Media media : mediaToMegre) {
				String valueOfField = media.getValue(key);
				if (valueOfField == null || valueOfField.equals("")) continue;
				if (possibleValues.containsKey(valueOfField)) {
					Integer integer = possibleValues.get(valueOfField);
					possibleValues.put(valueOfField, integer + 1);
				} else {// new
					possibleValues.put(valueOfField, 1);
				}
			}
			// resolve
			int maxCount = 0;
			Entry<String, Integer> winningEntry = null;
			for (Entry<String, Integer> entry : possibleValues.entrySet()) {
				int count = entry.getValue();
				if (count > maxCount) {// note <test1,2>, <test2,2> => wins "test1"
					maxCount = count;
					winningEntry = entry;
				}
			}
			mergedMedia.setInfo(key, winningEntry.getKey());
		}
		return mergedMedia;
	}

	/**
	 * Removes all Media from given list, that have field ID equal null.
	 *
	 * @param extractedMedia
	 *            all media that was extracted
	 * @return <tt>List<Media></tt> list of Media, removed those with null in ID
	 */
	public static List<Media> cleanNullId(final List<Media> extractedMedia) {
		List<Media> result = new ArrayList<>();
		for (Media media : extractedMedia) {
			if (media.getId() != null) {
				if (media.getUrl() != null) {
					result.add(media);
				}
			}
		}
		return result;
	}

	public static boolean isValidURL(final String urlStr) {
		try {
			new URL(urlStr);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

}
