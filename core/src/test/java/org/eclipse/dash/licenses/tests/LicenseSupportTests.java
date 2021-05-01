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

import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.tests.util.TestLicenseToolModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

class LicenseSupportTests {

	private LicenseSupport licenseSupport;

	@BeforeEach
	void setup() {
		Injector injector = Guice.createInjector(new TestLicenseToolModule());
		licenseSupport = injector.getInstance(LicenseSupport.class);
	}

	/**
	 * Basic test to confirm that we can look up a single license by SPDX code.
	 * Confirm that look ups are case insensitive (i.e., "epl-2.0" and "EPL-2.0" are
	 * considered equivalent).
	 */
	@Test
	void testApprovedLicense() {
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("EPL-2.0"));
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("epl-2.0"));
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("BSD-2-Clause"));
	}

	@Test
	void testRestrictedLicense() {
		assertEquals(LicenseSupport.Status.Restricted, licenseSupport.getStatus("GPL-2.0"));
	}

	@Test
	void testEmptyLicense() {
		assertEquals(LicenseSupport.Status.Restricted, licenseSupport.getStatus(""));
	}

	/**
	 * In SPDX, an "AND" condition indicates that the consumer must accept both
	 * licenses. Generally, this sort of condition means that some of the content is
	 * under one license and some is under the other.
	 */
	@Test
	void testConjunctionRestricted() {
		assertEquals(LicenseSupport.Status.Restricted, licenseSupport.getStatus("EPL-2.0 AND GPL-2.0"));
	}

	@Test
	void testConjunctionApproved() {
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("EPL-2.0 AND Apache-2.0"));
	}

	/**
	 * In SPDX, an "OR" condition indicates that the consumer may accept either of
	 * the licenses (dual licensing). If all we have is ORs, then we're probably
	 * okay, and so the result should be "accepted".
	 */
	@Test
	void testDisjunction() {
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("EPL-2.0 OR GPL-2.0"));
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("EPL-2.0 OR MIT OR GPL-2.0"));
	}

	@Test
	void testComplexDisjunction() {
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("BSD-2-Clause OR (Apache-2.0 OR MIT)"));
	}

	/**
	 * SPDX supports arbitrary combinations. Technically, the tested combination,
	 * "(EPL-2.0 AND MIT) OR GPL-2.0" should answer an "accepted"result.
	 */
	@Test
	void testComplexApproved() {
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("(EPL-2.0 AND MIT) OR GPL-2.0"));
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("(EPL-2.0 OR MIT) OR GPL-2.0"));
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("(EPL-2.0 OR MIT) OR GPL-2.0"));
		assertEquals(LicenseSupport.Status.Approved, licenseSupport.getStatus("(EPL-2.0 AND (MIT OR GPL-2.0))"));
	}

	@Test
	void testComplexRestricted() {
		assertEquals(LicenseSupport.Status.Restricted, licenseSupport.getStatus("(EPL-2.0 OR MIT) AND GPL-2.0"));
	}
}
