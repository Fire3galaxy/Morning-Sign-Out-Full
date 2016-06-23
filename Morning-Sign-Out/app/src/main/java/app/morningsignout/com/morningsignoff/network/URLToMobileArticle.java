package app.morningsignout.com.morningsignoff.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

public class URLToMobileArticle extends AsyncTask<String, Void, String> {
    static final String LOG_NAME = "URLToMobileArticle";
    static final int MINYEAR = 2014;
    final int CURRENTYEAR = Calendar.getInstance().get(Calendar.YEAR);

    WebView wb;
    String link;
    boolean isAuthorMTT;

    public URLToMobileArticle(WebView webview) {
        this.wb = webview;
        this.isAuthorMTT = false;
    }
    public URLToMobileArticle(WebView webView, boolean isAuthorMTT) {
        this.wb = webView;
        this.isAuthorMTT = isAuthorMTT;
    }

    @Override
    protected String doInBackground(String... params) {
        link = params[0];
        Uri requestUrl = Uri.parse(link);

        Log.d(LOG_NAME, link);

        // Special request: should change authorMTT to a boolean, since isOther boolean isn't used.
        if (isAuthorMTT) {
            Log.d(LOG_NAME, "changing webresponse to author (mtt) page");
            try {
                return getAuthorMTT(requestUrl.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Article Page
        if (requestUrl.getPathSegments().size() == 1) {
            Log.d(LOG_NAME, "changing webresponse to article page");
            return getArticleRevised(requestUrl.toString());
        }
        // Author, Tag, Date pages
        else if (requestUrl.getPathSegments().size() == 2) {
            String pathSeg0 = requestUrl.getPathSegments().get(0);
            int pathYear = -1;
            try {
                pathYear = Integer.parseInt(pathSeg0);
            } catch (NumberFormatException nfe) {
                // do nothing, basically throw the exception
            }

            if (pathSeg0.equals("author") ||
                    pathSeg0.equals("tag") ||
                    (pathYear >= MINYEAR && pathYear <= CURRENTYEAR)) {
                Log.d(LOG_NAME, "changing webresponse to other kind of page");
                try {
                    return getOther(requestUrl.toString());
                } catch (IOException e) {
                    String m = e.getMessage();

                    if (m == null) m = "IOException in getOther()";

                    Log.e(LOG_NAME, m);
                }
            }
        }
        // Search
        else if (link.contains("/?s=")) {
            try {
                return getOther(requestUrl.toString());
            } catch (IOException e) {
                Log.e(LOG_NAME, "IOException in Fetching Search page");
            }
        }

        return null;

//		if (!isOther) return getArticle(params[0]);
//        else try {
//            return getOther(params[0]);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        return null;
    }

    @Override
    protected void onPostExecute(final String html) {
//        wb.loadData(html, "text/html; charset=UTF-8", null);
        if (html != null)
            wb.loadDataWithBaseURL(link, html, "text/html", "UTF-8", link);
        else
            wb.loadUrl(link);
    }

    // Sections of html that the content post will specifically be in
    // o = open tag, c = close tag
    static final String body_o = "<body>", body_c = "</body>";
    static final String divContainer_o = "<div class=\"container\">", divContainer_c = "</div>";
    static final String divContent_o = "<div class=\"content content--single\">", divContent_c = "</div>";
    static final String divPost_o = "<div class=\"content__post\">", divPost_c = "</div>";
    static final String category_o = "<h4>", category_c = "</h4>";

    // Trimming these elements
    static final String header_o = "<header>", header_c = "</header>";
    static final String footer_o = "<footer>", footer_c = "</footer>";
    static final String divSSBA_o = "<div class=\"ssba ssba-wrap\">", divSSBA_c = "</div>";
    static final String divRelated_o = "<div class=\"content__related\">", divRelated_c = "</div>";
    static final String divDisqusThread_o = "<div id=\"disqus_thread\">", divDisqusThread_c = "</div>";
    static final String noComment_o = "<p class=\"nocomments\">", noComment_c = "</p>";
    static final String divPostNav_o = "<div class=\"post-nav\">", divPostNav_c = "</div>";
    static final String href_o = "href=\"", href_c = "\"";

    public static String getArticle(String link) {
        URL url;
        String html_new;
        try {
            url = new URL(link);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();

            // Return if failed
            int statusCode = c.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(c.getInputStream(), "utf-8"));
            String input;
            String html = "";

            while ((input = in.readLine()) != null) html += (input + "\n");

            // Find body substring
            int b_o = html.indexOf(body_o); 						// start of body
            int b_c = html.lastIndexOf(body_c) + body_c.length(); 	// end of body
            String sub_b = html.substring(b_o, b_c); 				// body of html

            // Find divContainer substring
            int a_divC_o = sub_b.indexOf(divContainer_o); 			// start of container
            int a_divC_c = sub_b.lastIndexOf(divContainer_c); 		// "Back to top" <div> </div>
            int b_divC_c = sub_b.lastIndexOf(divContainer_c, a_divC_c - 1) + divContainer_c.length(); 	// end of container
            String sub_divC = sub_b.substring(a_divC_o, b_divC_c);	// container html (in body)

            // Trim header
            int h_o = sub_divC.indexOf(header_o);						// header open tag
            int h_c = sub_divC.indexOf(header_c) + header_c.length(); 	// header close tag
            String sub_divC_1 = deleteSub(sub_divC, h_o, h_c);

            // Trim footer
            int f_o = sub_divC_1.indexOf(footer_o);
            int f_c = sub_divC_1.indexOf(footer_c) + header_c.length();
            String sub_divC_2 = deleteSub(sub_divC_1, f_o, f_c);

            // Find divContent substring
            int a_divCt_o = sub_divC_2.indexOf(divContent_o);					// open div of divContent
            int a_divCt_c = sub_divC_2.lastIndexOf(divContent_c);		 		// close div of divContent
            String sub_divCt = sub_divC_2.substring(a_divCt_o, a_divCt_c);

            // Trim divRelated
//	        int divR_o = sub_divCt.indexOf(divRelated_o);												// open div of content__related
//	        int a_divR_c = sub_divCt.lastIndexOf(divRelated_c);											// close div of divContent
//	        int b_divR_c = sub_divCt.lastIndexOf(divRelated_c, a_divR_c - 1) + divRelated_c.length();	// close div of divRelated
//	        String sub_divCt_1 = deleteSub(sub_divCt, divR_o, b_divR_c);

            // Trim divRelated temporarily
            int divR_o = sub_divCt.indexOf(divRelated_o);												// open div of content__related
            int a_divR_c = sub_divCt.lastIndexOf(divRelated_c);											// close div of divContent
            int b_divR_c = sub_divCt.lastIndexOf(divRelated_c, a_divR_c - 1) + divRelated_c.length();	// close div of divRelated
            String sub_divR = sub_divCt.substring(divR_o, b_divR_c);
            String sub_divCt_1 = deleteSub(sub_divCt, divR_o, b_divR_c);

            // Find divPost substring
            int a_divP_o = sub_divCt_1.indexOf(divPost_o);											// open div of content__post
            int a_divP_c = sub_divCt_1.lastIndexOf(divPost_c);										// close div of divContent
            int b_divP_c = sub_divCt_1.lastIndexOf(divPost_c, a_divP_c - 1) + divPost_c.length();		// close div of divPost
            String sub_divP = sub_divCt_1.substring(a_divP_o, b_divP_c);

            // Trim divDisqusThread
            String sub_divP_1;
            if (sub_divP.contains(divDisqusThread_o)) {
                int divDT_o = sub_divP.indexOf(divDisqusThread_o);														// open div of disqus_thread
                int a_divDT_c = sub_divP.lastIndexOf(divDisqusThread_c);												// close div of divPost
                int b_divDT_c = sub_divP.lastIndexOf(divDisqusThread_c, a_divDT_c - 1) + divDisqusThread_c.length();	// close div of divDT

//		        System.out.println(sub_divP.substring(divDT_o));

                sub_divP_1 = deleteSub(sub_divP, divDT_o, b_divDT_c);
            }
            // Trim no comment section
            else {
                int divNC_o = sub_divP.indexOf(noComment_o);
                int divNC_c = sub_divP.indexOf(noComment_c, divNC_o) + noComment_c.length();
                sub_divP_1 = deleteSub(sub_divP, divNC_o, divNC_c);
            }

            // Trim divPostNav
            int divPN_o = sub_divP_1.indexOf(divPostNav_o);													// open div of post-nav
//	        int a_divPN_c = sub_divP_1.lastIndexOf(divPostNav_c);											// close div of divPost
//	        int b_divPN_c = sub_divP_1.lastIndexOf(divPostNav_c, a_divPN_c - 1) + divPostNav_c.length();	// close div of divPN
//	        String sub_divP_2 = deleteSub(sub_divP_1, divPN_o, b_divPN_c);

            // Trim divSSBA2
            int divSSBA2_o = sub_divP_1.lastIndexOf(divSSBA_o);												// open div of ssba-wrap (2)
            int a_divSSBA2_c = sub_divP_1.lastIndexOf(divSSBA_c, divPN_o) + divSSBA_c.length();				// close div of ssba-wrap (2)
//	        int b_divSSBA2_c = sub_divP_2.lastIndexOf(divSSBA_c, a_divSSBA2_c) + divSSBA_c.length();
            String sub_divP_2 = deleteSub(sub_divP_1, divSSBA2_o, a_divSSBA2_c);

            // Trim divSSBA1
            int divSSBA1_o = sub_divP_2.indexOf(divSSBA_o);													// open div of ssba-wrap (1)
            int a_divSSBA1_c = sub_divP_2.lastIndexOf(divSSBA_c);											// close div of divPost
            int b_divSSBA1_c = sub_divP_2.lastIndexOf(divSSBA_c, a_divSSBA1_c - 1);							// close div of divPN
            int c_divSSBA1_c = sub_divP_2.lastIndexOf(divSSBA_c, b_divSSBA1_c - 1) + divSSBA_c.length();	// close div of ssba-wrap (1)
            String sub_divP_3 = deleteSub(sub_divP_2, divSSBA1_o, c_divSSBA1_c);

            // Trim href (category links)
            int cat_o = sub_divP_3.indexOf(category_o);														// open tag for category
            int cat_c = sub_divP_3.indexOf(category_c, cat_o) + category_c.length();												// close tag for category
            String sub_divP_4 = deleteSub(sub_divP_3, cat_o, cat_c);

            String sub_divP_5 = shrinkAllImages(sub_divP_4);

            // -------------------------Done Trimming and Subdividing-------------------------------

            // Put divPost substring back into divContent substring
            String sub_divCt_new = replaceSub(sub_divCt_1, sub_divP_5, a_divP_o, b_divP_c);

//            // Put divRelated substring back into divContent (appending)
//            String sub_divCt_new_2 = sub_divCt_new + sub_divR;

            // Put divContent substring back into container substring
            String sub_divC_new = replaceSub(sub_divC_2, sub_divCt_new, a_divCt_o, a_divCt_c);

            // Put divContainer substring back into body substring
            String sub_b_new = replaceSub(sub_b, sub_divC_new, a_divC_o, b_divC_c);

            // Put body substring into html
            html_new = replaceSub(html, sub_b_new, b_o, b_c);
        } catch (MalformedURLException e) {
            Log.e(e.getLocalizedMessage(), e.getMessage());

            return null;
        } catch (IOException e) {
            Log.e(e.getLocalizedMessage(), e.getMessage());

            return null;
        }

        return html_new;
    }
    static String replaceSub(String s, String substring, int start, int end) {
        return s.substring(0, start) + substring + s.substring(end);
    }
    static String deleteSub(String s, int start, int end) {
        return s.substring(0, start) + s.substring(end);
    }

    static String widthAttr = "width=\"",
            heightAttr = "height=\"",
            styleAttr1 = " style=\"height:88%;width:88%;padding-left:6%\">", // with padding (for pics with hyperlinks)
            styleAttr2 = " style=\"height:88%;width:88%\">";                // without padding (for pics without hyperlinks), they act differently
    static String shrinkAllImages(final String html) {
        String newHtml = html;

        int imgStart, imgEnd,
                widthStart, widthEnd,
                heightStart, heightEnd;

        imgStart = newHtml.indexOf("<img");
        imgStart = newHtml.indexOf("<img", imgStart + 1); // ignore featured imageViewReference

        while (imgStart != -1) {
            // Remove width attr (if present)
            widthStart = newHtml.indexOf(widthAttr, imgStart);
            if (widthStart != -1) {
                widthEnd = newHtml.indexOf("\"", widthStart + widthAttr.length());
                newHtml = deleteSub(newHtml, widthStart, widthEnd + 1); // include "
            }

            // Remove height attr (if present)
            heightStart = newHtml.indexOf(heightAttr, imgStart);
            if (heightStart != -1) {
                heightEnd = newHtml.indexOf("\"", heightStart + heightAttr.length());
                newHtml = deleteSub(newHtml, heightStart, heightEnd + 1);
            }

            imgEnd = newHtml.indexOf(">", imgStart);
            if (isHyperlinked(newHtml, imgStart))
                newHtml = replaceSub(newHtml, styleAttr1, imgEnd, imgEnd + 1); // with padding-left
            else
                newHtml = replaceSub(newHtml, styleAttr2, imgEnd, imgEnd + 1); // without padding-left

            imgStart = newHtml.indexOf("<img", imgStart + 1);
        }

        return newHtml;
    }
    static boolean isHyperlinked(final String html, int imgStart) {
        int lastTag = html.lastIndexOf('<', imgStart - 1);
        if (lastTag == -1) {
            Log.e(LOG_NAME, "Failed to identify previous tag");
            return false;
        }

        // <a href="...."> or <figure
        return html.charAt(lastTag + 1) == 'a';
    }

    public static String getOther(final String urlname) throws IOException {
        // Increased timeout because it could be search page request
        Document doc = Jsoup.connect(urlname).timeout(6 * 1000).get();
        doc.select("header").remove();
        doc.select("footer").remove();
        doc.select(".page-title--tag > span").remove();
        doc.select("h4:containsOwn(Category)").remove();
        doc.select(".page-title").attr("style", "margin: 0px 0 1px");
        doc.select(".author-information h1, .author-information h2").attr("style", "text-align: center");
        doc.select(".author-bio").attr("style", "margin-top: 5px");
        doc.select(".content__post").attr("style", "margin: 0px 5px 15px");
        doc.select(".author-posts > h1").attr("style", "margin: 15px 0px");
        Elements imgElems = doc.select(".attachment-post-thumbnail, .wp-post-imageViewReference");

        // Hyperlink images to articles
        for (Element img : imgElems) {
            img.wrap(String.format("<a href=%s> </a>", img.parent().select("a").attr("href")));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            imgElems.attr("style", "object-fit: cover");            // Maintain aspect ratio and crop
        else
            imgElems.attr("style", "width: 100%; height: auto");    // Compromise, do not crop image

        return doc.toString();
    }

    static String getAuthorMTT(final String urlname) throws IOException {
        Document doc = Jsoup.connect(urlname).get();

        // Remove only most of header, leave logo
        Element header = doc.select("header").first();
        header.child(0).remove();                   // remove header_upper
        Element header_lower = header.child(0);
        header_lower.select("a").unwrap();          // remove hyperlink from logo
        header_lower.child(1).remove();             // remove header_lower_nav

        doc.select("footer").remove();
        doc.select(".page-title--tag > span").remove();
        doc.select("h4:containsOwn(Category)").remove();
        doc.select(".page-title").attr("style", "margin-bottom: 25px");
        doc.select(".author-bio").attr("style", "margin-top: 5px");
        doc.select(".content__post").attr("style", "margin: 0px 5px 15px");
        doc.select(".author-posts > h1").attr("style", "margin: 15px 0px");
        Elements imgElems = doc.select(".attachment-post-thumbnail, .wp-post-image");
        for (Element img : imgElems) {
            img.wrap(String.format("<a href=%s></a>", img.parent().select("a").attr("href")));
        }
        return doc.toString();
    }

    public static String getArticleRevised(final String urlname) {
        // Increased timeout because it could be search page request
        Document doc = null;
        try {
            doc = Jsoup.connect(urlname).timeout(6 * 1000).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        doc.select("header").remove();              // Top of page
        doc.select("footer").remove();              // Bottom of page
        doc.select("div.ssba-wrap").remove();       // Social media addon
        doc.select(".content__related").remove();   // Related articles
        doc.select("#disqus_thread").remove();      // Comments section
        doc.select(".nocomments").remove();         // No comments section
        doc.select(".post-nav").remove();           // Previous/Next article
        doc.select("h4").first().remove();          // Category
        Element post = doc.select(".content__post").first();
        post.attr("style", "margin-top: 0px; padding-top: 20px");   // Padding of page
        doc.select("figure").attr("style", "display: block");       // Fix stretched images

        return doc.toString();
    }
}
 
