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

public interface IContentId extends Comparable<IContentId> {

	String getNamespace();

	String getName();

	String getVersion();

	String getType();

	String getSource();

	boolean isValid();

//	boolean isSupported();

	@Override
	default int compareTo(IContentId o) {
		return toString().compareTo(o.toString());
	}

}