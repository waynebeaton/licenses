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

import java.util.Collections;
import java.util.Map;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.tests.util.TestLicenseToolModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

class LicenseCheckerTests {

	private LicenseChecker licenseChecker;

	@BeforeEach
	void setup() {
		Injector injector = Guice.createInjector(new TestLicenseToolModule());
		licenseChecker = injector.getInstance(LicenseChecker.class);
	}

	@Test
	void testSingleApprovedLicense() {
		IContentId contentId = ContentId.getContentId("npm/npmjs/-/write/1.0.3");
		Map<IContentId, LicenseData> licenseData = licenseChecker.getLicenseData(Collections.singleton(contentId));

		LicenseData data = licenseData.get(contentId);
		assertEquals("MIT", data.getLicense());
		assertEquals(LicenseSupport.Status.Approved, data.getStatus());
	}

	@Test
	void testSingleUnapprovedLicense() {
		IContentId contentId = ContentId.getContentId("npm/npmjs/@yarnpkg/lockfile/1.1.0");
		Map<IContentId, LicenseData> licenseData = licenseChecker.getLicenseData(Collections.singleton(contentId));

		LicenseData data = licenseData.get(contentId);
		assertEquals("BSD-2-Clause", data.getLicense());
		assertEquals(LicenseSupport.Status.Restricted, data.getStatus());
	}

	@Test
	void testWithUnsupported() {
		IContentId contentId = ContentId.getContentId("p2/eclipseplugin/-/write/0.2.0");
		Map<IContentId, LicenseData> licenseData = licenseChecker.getLicenseData(Collections.singleton(contentId));

		LicenseData data = licenseData.get(contentId);
		assertNull(data.getLicense());
		assertEquals(LicenseSupport.Status.Restricted, data.getStatus());
	}
}
