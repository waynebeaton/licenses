/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse an ID provided in the NPM JS repository format.
 *
 * <p>
 * NPM JS coordinates take the form &quot;[@scope/]name@version&quot; used to
 * identify content in the npmjs.com repository. This implementation is intended
 * to parse identifiers that point to very specific content, so version ranges
 * (which are supported by the format) are not supported.
 *
 * <p>
 * See the <a href="https://docs.npmjs.com/files/package.json">npm-package.json
 * specification</a> for more information.
 */
public class NpmJsIdParser implements ContentIdParser {

	private static Pattern pattern = Pattern.compile(
	// @formatter:off
			"^(?:(?<namespace>@?[^\\/]+)\\/)?"
			+ "(?<name>[^\\/]+)"
			+ "@(?<version>\\d+(?:\\.\\d+){2}[^\\/]*)$"
	// @formatter:on
	);

	@Override
	public IContentId parseId(String value) {
		Matcher matcher = pattern.matcher(value.trim());
		if (!matcher.matches())
			return new InvalidContentId(value);

		String namespace = matcher.group("namespace");
		if (namespace == null)
			namespace = "-";
		String name = matcher.group("name");
		String version = matcher.group("version");

		return ContentId.getContentId("npm", "npmjs", namespace, name, version);
	}
}
