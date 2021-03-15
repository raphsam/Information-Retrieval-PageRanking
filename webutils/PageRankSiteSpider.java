package ir.webutils;

import java.util.*;
import java.net.*;
import java.io.*;

/**
 * A spider that limits itself to a given site.
 *
 * @author Ray Mooney
 */

public class PageRankSiteSpider extends PageRankSpider{
    public List<Link> getNewLinks(HTMLPage page) {
        List<Link> links = new LinkExtractor(page).extractLinks();
        URL url = page.getLink().getURL();
        ListIterator<Link> iter = links.listIterator();
        while (iter.hasNext()) {
            Link link = iter.next();
        // Only add new links that belong to same host as current
            if (!url.getHost().equals(link.getURL().getHost()))
                iter.remove();			
		}
		return links;
    }
	public static void main(String args[]) {
		new PageRankSiteSpider().go(args);
	}

}