/* support functions for the media metadata extraction model */

/* returns the maximum portion of words contained in an alt or title attribute that also appear in text.
// media = string containing an extracted XML snippet that represents a media object, 
// out of which an alt or title attribute are extracted and compared to text
// text = text to potentially descextracted XML snippet that represents a media object */
function altSimilarToText(media, text) {
    var rc = -1;
    
    var alt = getAttValue(media, "alt");
    if(alt != null) {
	    rc = portionOfWordsContained(alt, text);
    	LG(INF,"comparing:"+alt+" vs. "+text+" portionContained="+rc);
    }
    var tit = getAttValue(media, "title");
    if(tit != null) {
	    var rc2 = portionOfWordsContained(tit, text);
    	LG(INF,"comparing:"+tit+" vs. "+text+" portionContained="+rc2);
    	rc = Math.max(rc, rc2);
    }
    
    if(alt == null && tit == null) {
    	rc = 0.5; // when neither title nor alt is present, assume neither similar nor dissimilar
    }
    
	return rc;
}

/* extracts value of an attribute given by name */
function getAttValue(xmlString, attName) {
    var val = null;
    var attIdx = xmlString.indexOf(attName+"=\"");
    if(attIdx != -1) {
		attIdx += attName.length + 2;
    	var endIdx = xmlString.indexOf("\"", attIdx);
     	val = xmlString.substring(attIdx, endIdx);
    }
    return val;
}

/* preprocesses a sentence string by removing any punctuation and extra whitespace */
function prepro(str) {
	str = str.toLowerCase().replace(/[\.\,\;\-\/\s\r\n \t!?'\"]+/g, " ");
	str = str.replace(/^\s/g, "");
	str = str.replace(/\s$/g, "");
	return str;
}

/* converts a sentence string into a map of ones keyed by words */
function str2map(str) {
    var map = {};
    var toks = str.split(" ");
   	for(var i=0; i<toks.length; i++) { map[toks[i]] = 1; } 
    return map;
}

/* returns the portion of words contained in string phrase p1 that are also contained in phrase p2.
// returns 0.5 if p1 is empty */
function portionOfWordsContained(p1, p2) {
	var pp1 = prepro(p1);
    var pp2 = prepro(p2);
	
    var m1 = str2map(pp1);
    var m2 = str2map(pp2);
    
	rc = portionContained(m1, m2);
	return rc;
}

/* returns the portion of keys contained in map m1 that are also contained in map m2.
// returns 0.5 if m1 is empty */
function portionContained(m1, m2) {
	var hits = 0;
	var cnt = 0;
	for(x in m1) {
		if(m2[x]) {
			hits++;
		}
		cnt++;
	}
	var portion = 0.5;
	if(cnt>0) {
		portion = hits/cnt;
	}
	return portion;
}

/* return true if the xmlString represents an image/object of dimensions unlikely 
// to correspond to semantic content, false otherwise. */
function isSmallImage(xmlString) {
   	var rc = false;
    var h = getAttValue(xmlString, "height");
    var w = getAttValue(xmlString, "width");
    if(h != null) {
    	h = 1 * h;
    	rc  = (h <= 90);
    }
    if(rc && w != null) {
    	w = 1 * w;
    	rc  = (w <= 110);
    }
    LG(INF,"isSmallImage:"+xmlString+"="+rc);
    // todo: could use a 2d prob density function learnt in an older project
	return rc;
}

var g_reAuxImg = /(logo|src="")/i;

/* return true if the xmlString represents an image/object of dimensions unlikely 
// to correspond to semantic content, false otherwise. */
function isAuxImage(xmlString) {
   	var rc = false;
   	rc = g_reAuxImg.test(xmlString);
   	return rc;
}

/* return the frequency with which the media resource appears within the given document, 
//  as pre-annotated during preprocessing. Returns -1 if not known. */
function occurrenceCount(xmlString) {
   	var rc = -1;
    var fr = getAttValue(xmlString, "pagefrequency");
    if(fr != null) {
    	rc = 1 * fr;
    }
    LG(INF,"occurrenceCount:"+xmlString+"="+rc);
    return rc; 
}

var g_reBlack = /(Ihre Bewertung|Weiterempfehlen|Clip empfehlen|Sendung vom|Senden Sie diesen|Nutzerbewertung|Weitere Clips|Alle Clips|zu diesem Stichwort|rbb aktuell|rbb Online|rbb Fernsehen|livestream|Diese Seite|Impressum|^Mehr (aus|von|zu|zur)|^Service|^Der rbb|^Nachrichten|^\w+ Nachrichten|^Meine Wertung|^Ihre Meinung|^Vielen Dank$|^\w+ empfehlen|Um d\w+ \w+ abspielen|Ihre (E-Mail|Email|Addresse)|Bitte beachten Sie|Wertung abgeben|^Übersicht|^Hinweis|^Information$|^Meer avro|^Best bekeken|^video$|^article$|^AVRO|Meer NPO|zum Thema|op social media|Pflichtfelder|Senden Sie diesen|Flash plug|werden geladen|Daten werden)/i;

LG(WRN, g_reBlack.toSource());

function isBlacklisted(text) {
	return g_reBlack.test(text);
}

var s_blacklist = [
'cookie','cookies','admin','browser','support','upgrade','sendung','logo',
'rbb', 'aktuell', 'fernsehen', 'clips', 'geladen', 'stichwort', 'zeigen',
'wetter', 'nutzerbewertung', 'bewertet', 'flash', 'plugin', 'plug', 'abspielen','links','link',
'Übersicht','livestream','rundfunk','mediathek','bewerten','facebook', 'twitter', 'GooglePlus', 'Webnews',
'Digg','Yigg','Pflichtfelder','Pflichtfeld','ausfüllen','senden','Flash','version','geladen','herunterladen'
];
var s_blackmap = {};

/* called once to init black map */
function initBlackMap() {
	for(var i=0; i<s_blacklist.length; i++) {
    	s_blackmap[prepro(s_blacklist[i])] = 1;
    }
}

// call it
initBlackMap();

/* returns count of word occurrences that match a blacklisted word */
function getBlacklistedWordCnt(text) {
	var ptext = prepro(text);
    var toks = ptext.split(" ");
    var bcnt = getBlacklistedWordCntInList(toks);
    LG(INF,"blackCount:"+text+"="+bcnt);
	return bcnt;
}

/* return number of occurrences of blacklisted words in array tokList */
function getBlacklistedWordCntInList(tokList) {
    var bcnt = 0;
    for(var i=0; i<tokList.length; i++) {
    	if(s_blackmap[tokList[i]]) {
    		bcnt++;
    	}
    }
    return bcnt;
}

/* returns portion of word occurrences that match a blacklisted word */
function getBlacklistedWordPortion(text) {
	var portion = 0;
	var ptext = prepro(text);
    var toks = ptext.split(" ");
    var bcnt = getBlacklistedWordCntInList(toks);
    if(toks.length > 0) {
    	portion = bcnt / toks.length;
    }
    LG(INF,"blackPortion:"+text+"="+portion);
	return portion;
}


var g_reFormObject = /<(label|input|form|option|select|textarea)/i;

function containsFormObjects(text) {
	return g_reFormObject.test(text);
}
