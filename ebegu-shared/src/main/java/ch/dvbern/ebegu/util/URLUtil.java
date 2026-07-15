package ch.dvbern.ebegu.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public abstract class URLUtil {
	public static boolean isValidHttpOrHttpsURL(String url) {
		try {
			var uri = new URL(url).toURI();
			return Objects.equals(
				uri.getScheme().toLowerCase(Constants.DEFAULT_LOCALE),
				"https"
			)
				|| Objects.equals(
					uri.getScheme().toLowerCase(Constants.DEFAULT_LOCALE),
					"http"
				);
		} catch (MalformedURLException | URISyntaxException e) {
			return false;
		}
	}
}
