/*************************************************************************
 * Copyright (c) 2019,2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.ClearlyDefinedIdParser;
import org.eclipse.dash.licenses.ContentIdParser;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.InvalidContentId;
import org.eclipse.dash.licenses.MavenIdParser;
import org.eclipse.dash.licenses.NpmJsIdParser;

public class FlatFileReader implements IDependencyListReader {

	// TODO Dependency injection opportunity
	List<ContentIdParser> parsers = new ArrayList<>();

	private BufferedReader reader;

	public FlatFileReader(Reader input) {
		reader = new BufferedReader(input);
		parsers.add(new MavenIdParser());
		parsers.add(new NpmJsIdParser());
		parsers.add(new ClearlyDefinedIdParser());
	}

	@Override
	public List<IContentId> getContentIds() {
		// @formatter:off
		return reader.lines()
			.filter(FlatFileReader::isLineThatWeShouldBotherLookingAt)
			.filter(line -> !line.isBlank())
			.map(this::getContentId)
			.distinct()
			.collect(Collectors.toList());
		// @formatter:on
	}

	public IContentId getContentId(String value) {
		// @formatter:off
		return parsers.stream()
			.map(parser -> parser.parseId(value))
			.filter(Objects::nonNull)
			.findFirst()
			.orElseGet(() -> new InvalidContentId(value));
		// @formatter:on
	}

	/**
	 * Answers whether or not a particular line from the flat file is worth trying
	 * to extract an ID from (effectively, this is the opposite of "should ignore").
	 * Skip lines that are headers/annotations/etc.
	 * 
	 * This is different from invalid lines. We want to actually communicate when
	 * content is specified incorrectly. This method filters out lines that we
	 * really just don't need to even think about.
	 * 
	 * @param line
	 * @return
	 */
	private static boolean isLineThatWeShouldBotherLookingAt(String line) {
		switch (line.trim()) {
		// Maven's dependency plugin prints "none" when there are no dependencies
		case "none":
			return false;

		// Maven's dependency plugin prints a header for each target.
		case "The following files have been resolved:":
			return false;
		}
		return true;
	}
}
