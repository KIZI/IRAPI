package eu.linkedtv.irapi.search.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IrAPIConstants {

	public static final String IMAGE = "image";
	public static final String VIDEO = "video";
	public static final String AUDIO = "audio";
	public static final String WEBPAGE = "webpage";
	public static final Set<String> ALLOWED_MEDIA_TYPES = new HashSet<>(Arrays.asList("webpage", "image", "audio",
			"video"));

	public static final String SUPPLEMENTAL_PROVENANCE = "supplemental";
	public static final String MAIN_PROVENANCE = "main";

	public static final int PHRASE_SLOP = 4;
	public static final String QUERY_PRECISSION_LEVEL = "query_precission_level";
	public static final int PRECISSION_MAIN_LEVEL_1 = 1;
	public static final int PRECISSION_MAIN_LEVEL_2 = 2;
	public static final int PRECISSION_MAIN_LEVEL_3 = 3;

	public static final int PRECISSION_SUPPL_LEVEL_1 = 4;
	public static final int PRECISSION_SUPPL_LEVEL_2 = 5;
	public static final int PRECISSION_SUPPL_LEVEL_3 = 6;

	public static final String PROVENANCE = "provenance";
	public static final String SCORE = "score";
	public static final String USED_FIELDS = "used_fields";
	public static final String USED_QUERY = "used_query";

}
