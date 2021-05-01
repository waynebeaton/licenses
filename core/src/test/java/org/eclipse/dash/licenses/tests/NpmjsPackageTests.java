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

import java.io.InputStream;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.context.IContext;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.npmjs.INpmjsPackageService;
import org.eclipse.dash.licenses.npmjs.NpmjsPackage;
import org.eclipse.dash.licenses.npmjs.NpmjsExtendedContentDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class NpmjsPackageTests {

	private IContext context;

	@BeforeEach
	void setUp() {
		context = new IContext() {
			@Override
			public IHttpClientService getHttpClientService() {
				return new IHttpClientService() {
					@Override
					public int get(String url, String contentType, Consumer<InputStream> handler) {
						handler.accept(this.getClass().getResourceAsStream("/chalk.json"));
						return 200;
					}

					@Override
					public boolean remoteFileExists(String url) {
						return true;
					}
				};
			};
		};
	}

	@Test
	void testValid() {
		INpmjsPackageService service = new NpmjsExtendedContentDataProvider(context);
		IContentId id = ContentId.getContentId("npm/npmjs/-/chalk/0.1.0");
		var thing = service.getPackage(id);

		assertEquals("git://github.com/sindresorhus/chalk.git", thing.getRepositoryUrl());
		assertEquals("MIT", thing.getLicense());
		assertEquals("https://github.com/sindresorhus/chalk/archive/v0.1.0.zip", thing.getSourceUrl());
		assertEquals("https://registry.npmjs.org/chalk/-/chalk-0.1.0.tgz", thing.getDistributionUrl());
	}

	@Test
	@Disabled
	void testMissingMetadata() {
		IContentId id = ContentId.getContentId("npm/npmjs/-/chalk/0.1.0");
		NpmjsPackage thing = new NpmjsPackage(id);
		assertNull(thing.getRepositoryUrl());
	}
}
