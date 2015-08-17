package app.morningsignout.com.morningsignoff;

// It is used by FetchListArticlesTask class and FetchHeadLineArticles class to parse HTML elements
public class Parser {
	public String getTitle(String s) {
		int tagA = s.indexOf("<a");
		
		if (tagA != -1) {
			int titleI = s.indexOf(">", tagA + 1) + 1;
    		int titleII = s.indexOf("</a>");

    		return replaceUnicode(s.substring(titleI, titleII));
		}
		
		return null;
	}
	public String getLink(String s1) {
		String s = s1.trim();
		int link = s.indexOf("http");
		int linkDelim = s.indexOf("\"", link);
		
		if (link != -1) {
    		return replaceUnicode(s.substring(link, linkDelim));
		}
		
		return null;
	}
	public String getDescription(String s) {
		int beginInd = s.indexOf("<p>") + 3, endInd = s.indexOf("<a class");
		
		if (beginInd != -1 && endInd != -1) return replaceUnicode(s.substring(beginInd, endInd));
		return null;
	}
    public String getImageURL(String s) {
        int beginInd = s.indexOf("src=\"") + 5, endInd = s.indexOf("\"", beginInd);

        if (beginInd != -1 && endInd != -1) return replaceUnicode(s.substring(beginInd, endInd));
        return null;
    }
	public String getAuthor(String s) {
		int beginInd = s.indexOf("rel=\"author\">") + 13, endInd = s.indexOf("</a>", beginInd);

		if (beginInd != -1 && endInd != -1) return replaceUnicode(s.substring(beginInd, endInd));
		return null;
	}
	public static String replaceUnicode(String s) {
		String ret = s;
        ret = ret.replace("&#038;", "\u0026"); // ampersand
        ret = ret.replace("&amp;", "\u0026"); // ampersand
        ret = ret.replace("&#8211;", "\u2013"); // en dash
		ret = ret.replace("&#8216;", "\u2018"); // left quotation mark
		ret = ret.replace("&#8217;", "\u2019");
		ret = ret.replace("&#8220;", "\u201C");
		ret = ret.replace("&#8221;", "\u201D");
		ret = ret.replace("&#8230;", "\u2026");
		ret = ret.replace("&It;", "<");

		return ret;
	}
}
