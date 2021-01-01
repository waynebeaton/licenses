package org.eclipse.dash.licenses.review;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.dash.licenses.LicenseData;

public class IPZillaSearchBuilder {
	private static final String[] COMMON_TERMS = new String[] { "apache", "eclipse", "source", "platform", "plugin",
			"parent", "client", "server" };
	private static final int SEARCH_TERM_MINIMUM_LENGTH = 5;

	Set<String> terms = new LinkedHashSet<>();

	public static String build(LicenseData licenseData) {
		return new IPZillaSearchBuilder().get(licenseData);
	}

	private void add(String term) {
		// Break the name into segments (non-word characters) and add the segments from
		// the name to the search terms. We arbitrarily decide that terms that are
		// "short" aren't interesting and skip them. The logic being that shorter words
		// are more likely to be common, and common words will clutter up our search.
		if (Arrays.stream(COMMON_TERMS).anyMatch(each -> each.equalsIgnoreCase(term)))
			return;
		if (term.length() >= SEARCH_TERM_MINIMUM_LENGTH)
			terms.add(term);
	}

	private String get(LicenseData licenseData) {
		if (!licenseData.getId().isValid())
			return null;

		String namespace = licenseData.getId().getNamespace();
		String name = licenseData.getId().getName();

		// Assemble terms from the content data that might result
		// in an interesting search.
		terms.add(namespace);
		terms.add(name);

		// Break the name into segments (non-word characters) and add the segments from
		// the name to the search terms.
		for (String segment : name.split("\\W"))
			add(segment);

		for (String segment : namespace.split("\\W"))
			add(segment);

		if (terms.isEmpty())
			return null;

		var builder = new StringBuilder();
		builder.append("https://dev.eclipse.org/ipzilla/buglist.cgi");
		builder.append("?short_desc_type=anywords");
		builder.append("&short_desc=");
		builder.append(String.join("+", terms));
		builder.append("&long_desc_type=substring");

		return builder.toString();
	}
}