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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.MavenIdParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MavenIdParserTests {
	private MavenIdParser parser;

	@BeforeEach
	public void setup() {
		parser = new MavenIdParser();
	}

	@Test
	public void testCreation1() {
		IContentId value = parser.parseId("groupid:artifactid:1.2.3");
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testWithExt() {
		IContentId value = parser.parseId("groupid:artifactid:jar:1.2.3");
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testMissingPhase() {
		IContentId value = parser.parseId("groupid:artifactid:jar:test:1.2.3");
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testWithPhase() {
		IContentId value = parser.parseId("groupid:artifactid:jar:1.2.3:compile");
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testExtraPadding() {
		IContentId value = parser.parseId("  groupid:artifactid:jar:1.2.3:compile  ");
		assertEquals("groupid", value.getNamespace());
		assertEquals("artifactid", value.getName());
		assertEquals("1.2.3", value.getVersion());
	}

	@Test
	public void testOrbitBundle() {
		IContentId value = parser.parseId("p2.eclipse-plugin:org.eclipse.core.jobs:jar:3.8.0.v20160509-0411");
		assertEquals("p2", value.getType());
		assertEquals("orbit", value.getSource());
		assertEquals("p2.eclipse-plugin", value.getNamespace());
		assertEquals("org.eclipse.core.jobs", value.getName());
		assertEquals("3.8.0.v20160509-0411", value.getVersion());
	}

	@Test
	public void testRetainQualifier1() {
		IContentId value = parser.parseId("p2.eclipse-plugin:org.eclipse.core.jobs:jar:3.8.0.20160509");
		assertEquals("p2", value.getType());
		assertEquals("orbit", value.getSource());
		assertEquals("p2.eclipse-plugin", value.getNamespace());
		assertEquals("org.eclipse.core.jobs", value.getName());
		assertEquals("3.8.0.20160509", value.getVersion());
	}

	@Test
	public void testRetainQualifier2() {
		IContentId value = parser.parseId("com.google.guava:guava:jar:28.0-jre:compile");

		assertEquals("28.0-jre", value.getVersion());
	}

	@Test
	public void testEclipseFeature() {
		IContentId value = parser.parseId(
				"org.eclipse.acceleo.features:org.eclipse.acceleo.doc:eclipse-feature:3.7.10-SNAPSHOT:provided");
		// TODO The default values for type and source are obviously bogus in this case
		assertEquals("maven", value.getType());
		assertEquals("mavencentral", value.getSource());
		assertEquals("org.eclipse.acceleo.features", value.getNamespace());
		assertEquals("org.eclipse.acceleo.doc", value.getName());
		assertEquals("3.7.10-SNAPSHOT", value.getVersion());
	}

	@Test
	public void testMavenP2() {
		IContentId value = parser
				.parseId("p2.eclipse-plugin:org.apache.ant:jar:lib/ant-commons-net.jar:1.10.8.v20200515-1239:system");
		assertEquals("maven", value.getType());
		assertEquals("mavencentral", value.getSource());
		assertEquals("org.apache.ant", value.getNamespace());
		assertEquals("ant-commons-net", value.getName());
		assertEquals("1.10.8", value.getVersion());
	}

	@Test
	@Disabled
	public void testWithVersionInNestedJar() {
		IContentId value = parser.parseId(
				"p2.eclipse-plugin:org.eclipse.wst.jsdt.chromium:jar:lib/json_simple/json_simple-1.1.jar:0.5.200.v201610211901:system");
		assertEquals("p2", value.getType());
		assertEquals("orbit", value.getSource());
		assertEquals("org.eclipse.wst.jsdt.chromium", value.getNamespace());
		assertEquals("json_simple", value.getName());
		assertEquals("1.1", value.getVersion());
	}

	/**
	 * "p2.eclipse-plugin:org.jaxen:jar:lib/jaxen-1.1.6.jar:1.1.6.201804090728:system"
	 * maps to maven/mavencentral/org.jaxen/jaxen/1.1.6
	 */
	@Disabled
	@Test
	public void testWithNestedJar() {
		IContentId value = parser
				.parseId("p2.eclipse-plugin:org.jaxen:jar:lib/jaxen-1.1.6.jar:1.1.6.201804090728:system");

		assertEquals("jaxen", value.getName());
		assertEquals("org.jaxen", value.getNamespace());
		assertEquals("1.1.6", value.getVersion());
		assertEquals("maven", value.getType());
		assertEquals("mavencentral", value.getSource());
	}

	@Test
	public void testWithNonNumericVersionWithPhase() {
		IContentId value = parser.parseId("com.google.javascript:closure-compiler-externs:jar:v20160315:compile");

		assertEquals("v20160315", value.getVersion());
	}

	@Test
	public void testWithNonNumericVersionWithoutPhase() {
		IContentId value = parser.parseId("com.google.javascript:closure-compiler-externs:jar:v20160315");

		assertEquals("v20160315", value.getVersion());
	}
}
