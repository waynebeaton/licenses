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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.InvalidContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.review.GitLabReview;
import org.eclipse.dash.licenses.review.IPZillaSearchBuilder;
import org.eclipse.dash.licenses.tests.util.TestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GitLabReviewTests {

	@Nested
	class GitLabSupportTests {
		@Test
		@Disabled
		void testCreateReview() {
		}
	}

	@Nested
	class MavenReviewTests {
		private GitLabReview review;

		@BeforeEach
		void setup() {
			review = new GitLabReview("technology.dash",
					new LicenseData(ContentId.getContentId("maven/mavencentral/group.path/artifact/1.0")),
					Stream.empty());
		}

		@Test
		void testTitle() {
			assertEquals("maven/mavencentral/group.path/artifact/1.0", review.getTitle());
		}

		@Test
		void testMavenCentralUrl() {
			assertEquals("https://search.maven.org/artifact/group.path/artifact/1.0/jar", review.getMavenCentralUrl());
		}

		@Test
		void testMavenCentralSourceUrl() {
			assertEquals(
					"https://search.maven.org/remotecontent?filepath=group/path/artifact/1.0/artifact-1.0-sources.jar",
					review.getMavenCentralSourceUrl());
		}

		void testNpmjsUrl() {
			assertNull(review.getNpmjsUrl());
		}
	}

	@Nested
	class NpmReviewTests {
		private GitLabReview review;

		@BeforeEach
		void setup() {
			review = new GitLabReview(new TestContext(),
					new LicenseData(ContentId.getContentId("npm/npmjs/group.path/artifact/1.0")));
		}

		@Test
		void testTitle() {
			assertEquals("npm/npmjs/group.path/artifact/1.0", review.getTitle());
		}

		@Test
		void testNpmjsUrl() {
			assertEquals("https://www.npmjs.com/package/group.path/artifact/v/1.0", review.getNpmjsUrl());
		}

		@Test
		void testMavenSourceUrl() {
			assertNull(review.getMavenCentralSourceUrl());
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
