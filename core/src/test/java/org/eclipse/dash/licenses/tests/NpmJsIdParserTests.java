/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.dash.licenses.NpmJsIdParser;
import org.junit.jupiter.api.Test;

class NpmJsIdParserTests {

	@Test
	void testBasic() {
		assertEquals("npm/npmjs/-/highlight/7.5.0", new NpmJsIdParser().parseId("highlight@7.5.0").toString());
	}

	@Test
	void testWithScope() {
		assertEquals("npm/npmjs/@babel/highlight/7.5.0",
				new NpmJsIdParser().parseId("@babel/highlight@7.5.0").toString());
	}

	@Test
	void testWithDashScope() {
		assertEquals("npm/npmjs/-/highlight/7.5.0", new NpmJsIdParser().parseId("-/highlight@7.5.0").toString());
	}

	@Test
	void testWithDashes() {
		assertEquals("npm/npmjs/-/high-light/7.5.0", new NpmJsIdParser().parseId("-/high-light@7.5.0").toString());
	}

	@Test
	void testWithFile() {
		assertFalse(new NpmJsIdParser()
				.parseId("vscode-css-languageserver@file:target/vscode-css-languageserver-1.0.0.tgz").isValid());
	}

	@Test
	void testWithPartialVersion() {
		assertFalse(new NpmJsIdParser().parseId("cheerio@1.0").isValid());
	}

	@Test
	void testWithInvalidVersion() {
		assertFalse(new NpmJsIdParser().parseId("cheerio@1.0.").isValid());
		assertFalse(new NpmJsIdParser().parseId("cheerio@a.0.").isValid());
		assertFalse(new NpmJsIdParser().parseId("cheerio@.0.").isValid());
	}

	@Test
	void testWithValidVersion() {
		assertEquals("1.0.0", new NpmJsIdParser().parseId("cheerio@1.0.0").getVersion());
		assertEquals("12.34.56", new NpmJsIdParser().parseId("cheerio@12.34.56").getVersion());
		assertEquals("1.0.0-rc.3", new NpmJsIdParser().parseId("cheerio@1.0.0-rc.3").getVersion());
	}

	@Test
	void testMissingName() {
		assertFalse(new NpmJsIdParser().parseId("@1.0.0-rc.3").isValid());
	}

	@Test
	void testMissingVersion() {
		assertFalse(new NpmJsIdParser().parseId("cheerio@").isValid());
	}

	@Test
	void testExtraInformationFails() {
		assertFalse(new NpmJsIdParser().parseId("blah/blah/blah@1.2.3").isValid());
	}
}
