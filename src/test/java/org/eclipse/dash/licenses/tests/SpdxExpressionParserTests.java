/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the Eclipse Public License 2.0 which accompanies this 
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.dash.licenses.spdx.SpdxExpressionParser;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpdxExpressionParserTests {

	@Nested
	class TestParsing {

		@Test
		void testSimple() {
			assertEquals("EPL-1.0", new SpdxExpressionParser().parse("EPL-1.0").toString());
		}

		@Test
		void testConjunction() {
			assertEquals("(EPL-1.0 AND Apache-2.0)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0").toString());
		}

		@Test
		void testMultipleConjunction() {
			assertEquals("((EPL-1.0 AND Apache-2.0) AND MIT)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 AND MIT").toString());
		}

		@Test
		void testDisjunction() {
			assertEquals("(EPL-1.0 OR Apache-2.0)",
					new SpdxExpressionParser().parse("EPL-1.0 OR Apache-2.0").toString());
		}

		@Test
		void testPrecedence1() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND MIT))",
					new SpdxExpressionParser().parse("EPL-1.0 OR Apache-2.0 AND MIT").toString());
		}

		@Test
		void testPrecedence2() {
			assertEquals("((Apache-2.0 AND MIT) OR EPL-1.0)",
					new SpdxExpressionParser().parse("Apache-2.0 AND MIT OR EPL-1.0").toString());
		}

		@Test
		void testPrecedence3() {
			assertEquals("((Apache-2.0 AND MIT) OR (EPL-1.0 AND BSD0))",
					new SpdxExpressionParser().parse("Apache-2.0 AND MIT OR EPL-1.0 and BSD0").toString());
		}

		@Test
		void testPrecedence5() {
			assertEquals("((EPL-1.0 AND Apache-2.0) OR MIT)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 OR MIT").toString());
		}

		@Test
		void testPrecedence6() {
			assertEquals("((Apache-2.0 AND (BSD0 OR MIT)) OR EPL-2.0)",
					new SpdxExpressionParser().parse("Apache-2.0 AND (BSD0 OR MIT) OR EPL-2.0").toString());
		}

		@Test
		void testExceptionPrecedence6() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND MIT))",
					new SpdxExpressionParser().parse("EPL-1.0 OR Apache-2.0 AND MIT").toString());
		}

		@Test
		void testNestedPrecedence() {
			assertEquals("(((Apache-2.0 AND MIT) OR BSD0) OR EPL-1.0)",
					new SpdxExpressionParser().parse("(Apache-2.0 AND MIT OR BSD0) OR EPL-1.0").toString());
		}

		@Test
		void testLeadingParentheses() {
			assertEquals("((Apache-2.0 OR MIT) AND EPL-1.0)",
					new SpdxExpressionParser().parse("(Apache-2.0 OR MIT) AND EPL-1.0").toString());
		}

		@Test
		void testFollowingParentheses() {
			assertEquals("(MIT AND (EPL-1.0 OR Apache-2.0))",
					new SpdxExpressionParser().parse("MIT AND (EPL-1.0 OR Apache-2.0)").toString());
		}

		@Test
		void testNestedParentheses1() {
			assertEquals("(EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT)))",
					new SpdxExpressionParser().parse("EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT))").toString());
		}

		@Test
		void testNestedParentheses2() {
			assertEquals("(EPL-1.0 OR ((Apache-2.0 AND (BSD0 OR MIT)) OR EPL-2.0))", new SpdxExpressionParser()
					.parse("EPL-1.0 OR (Apache-2.0 AND (BSD0 OR MIT) OR EPL-2.0)").toString());
		}

		@Test
		void testArbitraryParentheses() {
			assertEquals("((EPL-1.0 AND MPL) OR (Apache-2.0 AND (BSD0 OR MIT)))",
					new SpdxExpressionParser().parse("(EPL-1.0 AND MPL) OR (Apache-2.0 AND (BSD0 OR MIT))").toString());
		}

		@Test
		void testArbitraryParentheses2() {
			assertEquals("((EPL-1.0 AND MPL) OR ((Apache-2.0 AND BSD0) OR MIT))", new SpdxExpressionParser()
					.parse("(EPL-1.0 AND (MPL)) OR ((Apache-2.0 AND BSD0) OR MIT)").toString());
		}

		@Test
		void testException() {
			assertEquals("(EPL-1.0 WITH Exception)",
					new SpdxExpressionParser().parse("EPL-1.0 with Exception").toString());
		}

		@Test
		void testExceptionPrecedence1() {
			assertEquals("(MIT OR (EPL-1.0 WITH Exception))",
					new SpdxExpressionParser().parse("MIT OR EPL-1.0 WITH Exception").toString());
		}

		@Test
		void testExceptionPrecedence2() {
			assertEquals("((EPL-1.0 WITH Exception) OR MIT)",
					new SpdxExpressionParser().parse("EPL-1.0 WITH Exception OR MIT").toString());
		}

		@Test
		void testExceptionPrecedence3() {
			assertEquals("(((EPL-1.0 AND Apache-2.0) OR MIT) OR BSD0)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 OR MIT OR BSD0").toString());
		}

		@Test
		void testExceptionPrecedence4() {
			assertEquals("(((EPL-1.0 AND Apache-2.0) OR (MIT WITH STUFF)) OR BSD0)",
					new SpdxExpressionParser().parse("EPL-1.0 AND Apache-2.0 OR MIT WITH STUFF OR BSD0").toString());
		}

		@Test
		void testPlus() {
			assertEquals("(EPL-1.0)+", new SpdxExpressionParser().parse("EPL-1.0+").toString());
		}

		@Test
		void testPlusInExpression1() {
			assertEquals("(Apache-2.0 OR (EPL-1.0)+)",
					new SpdxExpressionParser().parse("Apache-2.0 OR EPL-1.0+").toString());
		}

		@Test
		void testPlusInExpression2() {
			assertEquals("((BSD0 AND Apache-2.0) OR (EPL-1.0)+)",
					new SpdxExpressionParser().parse("BSD0 AND Apache-2.0 OR EPL-1.0+").toString());
		}
	}

	@Nested
	class TestBroken {
		@Test
		@Disabled
		void testBrokenBinaryExpression() {
			assertFalse(new SpdxExpressionParser().parse("BSD0 AND ").isValid());
		}
	}

	@Nested
	class TestMatching {
		@Test
		void testMatchSimple() {
			assertTrue(new SpdxExpressionParser().parse("EPL-2.0").matchesApproved(Collections.singleton("EPL-2.0")));
		}

		@Test
		void testMatchConjunction() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-2.0 AND Apache-2.0").matchesApproved(approved));
		}

		@Test
		void testMatchConjunctionFail() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertFalse(new SpdxExpressionParser().parse("EPL-2.0 AND BSD0").matchesApproved(approved));
		}

		@Test
		void testMatchDisjunction() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-2.0 OR BSD0").matchesApproved(approved));
		}

		@Test
		void testMatchArbitrary() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-2.0");
			approved.add("Apache-2.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-2.0 AND (Apache-2.0 OR BSD0)").matchesApproved(approved));
		}

		@Test
		void testMatchPlus1() {
			Set<String> approved = new HashSet<>();
			approved.add("EPL-1.0");

			assertTrue(new SpdxExpressionParser().parse("EPL-1.0+").matchesApproved(approved));
		}

//		@Ignore("Not implemented yet.")
//		@Test
//		void testMatchPlus2() {
//			Set<String> approved = new HashSet<>();
//			approved.add("EPL-2.0");
//
//			assertTrue(new SpdxExpressionParser().parse("EPL-1.0+").matchesApproved(approved));
//		}
	}
}
