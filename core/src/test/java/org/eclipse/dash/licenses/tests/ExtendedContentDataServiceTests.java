/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.InvalidContentId;
import org.eclipse.dash.licenses.extended.ExtendedContentData;
import org.eclipse.dash.licenses.extended.ExtendedContentDataItem;
import org.eclipse.dash.licenses.extended.ExtendedContentDataService;
import org.eclipse.dash.licenses.review.IPZillaSearchBuilder;
import org.eclipse.dash.licenses.tests.util.TestLicenseToolModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExtendedContentDataServiceTests {
	private ExtendedContentDataService dataService;

	@BeforeEach
	void setup() {
		Injector injector = Guice.createInjector(new TestLicenseToolModule());
		dataService = injector.getInstance(ExtendedContentDataService.class);
	}

	@Nested
	class GitLabSupportTests {
		@Test
		@Disabled
		void testCreateReview() {
		}
	}

	@Nested
	class MavenCentralExtendedContentDataTests {

		@Test
		void testTitle() {
			ExtendedContentData data = dataService
					.findFor(ContentId.getContentId("maven/mavencentral/group.path/artifact/1.0")).findAny().get();
			assertEquals("Maven Central", data.getTitle());
			assertEquals("https://search.maven.org/artifact/group.path/artifact/1.0/jar", data.getUrl());
			ExtendedContentDataItem item = data.getItems().findFirst().get();
			assertEquals("Source", item.getLabel());
			assertEquals(
					"https://search.maven.org/remotecontent?filepath=group/path/artifact/1.0/artifact-1.0-sources.jar",
					item.getValue());
		}
	}

	@Nested
	class NpmjsExtendedContentDataTests {
		@Test
		void testValid() {

			ExtendedContentData thing = dataService.findFor(ContentId.getContentId("npm/npmjs/-/chalk/0.1.0")).findAny()
					.get();
			assertEquals("npmjs", thing.getTitle());
			assertEquals("git://github.com/sindresorhus/chalk.git", thing.get("Repository"));
			assertEquals("MIT", thing.get("License"));
			assertEquals("https://github.com/sindresorhus/chalk/archive/v0.1.0.zip", thing.get("Source"));
			assertEquals("https://registry.npmjs.org/chalk/-/chalk-0.1.0.tgz", thing.get("Distribution"));
		}
	}

	@Nested
	class IPZillaSearchBuilderTests {
		@Test
		void testBasic() throws Exception {
			String url = IPZillaSearchBuilder.build(ContentId.getContentId("type/source/namespace/component/1.0"));
			Matcher matcher = Pattern.compile("&short_desc=([^&]+)&").matcher(url);
			matcher.find();
			String group = matcher.group(1);
			assertEquals("namespace+component", group);
		}

		@Test
		void testExcludeCommon() throws Exception {
			String url = IPZillaSearchBuilder
					.build(ContentId.getContentId("type/source/namespace/eclipse.component/1.0"));
			Matcher matcher = Pattern.compile("&short_desc=([^&]+)&").matcher(url);
			matcher.find();
			String group = matcher.group(1);
			assertEquals("namespace+eclipse.component+component", group);
		}

		@Test
		void testNoSearchableTerms() throws Exception {
			String url = IPZillaSearchBuilder.build(ContentId.getContentId("type/source/x/y/1.0"));
			assertNull(url);
		}

		@Test
		void testInvalidId() throws Exception {
			String url = IPZillaSearchBuilder.build(new InvalidContentId("invalid"));
			assertNull(url);
		}

	}
}
