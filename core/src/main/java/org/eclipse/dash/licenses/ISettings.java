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

public interface ISettings {

	int getBatchSize();

	/**
	 * The license check URL is the address of the Eclipse Foundation license data
	 * service.
	 *
	 * @return the URL (String)
	 */
	String getLicenseCheckUrl();

	String getClearlyDefinedDefinitionsUrl();

	String getApprovedLicensesUrl();

	int getConfidenceThreshold();

	/**
	 * How long do we wait for a response from a license data provider?
	 *
	 * @return the timeout in seconds (int).
	 */
	default int getTimeout() {
		return 60;
	}

	/**
	 * Answers whether or not we have the information that we required to create
	 * requests for review by the EF IP Team.
	 * 
	 * @return
	 */
	default boolean canCreateReviews() {
		return getIpLabToken() != null;
	}

	/**
	 * The GitLab authentication token that we need to connect to GitLab to create
	 * requests for review.
	 * 
	 * @return the token or <code>null</code> if no value is available.
	 */
	default String getIpLabToken() {
		// TODO Started as a hack, but might be worth experimenting with
		return System.getProperty("org.eclipse.dash.token");
	}

	/**
	 * The URL of the GitLab host of the "IP Lab" repository.
	 * 
	 * @return A valid URL.
	 */
	default String getIpLabHostUrl() {
		return System.getProperty("org.eclipse.dash.repository-host", "https://gitlab.eclipse.org");
	}

	/**
	 * The path (relative to the GitLab host) of the path of the "IP Lab"
	 * repository.
	 * 
	 * @return A valid path.
	 */
	default String getIpLabRepositoryPath() {
		return System.getProperty("org.eclipse.dash.repository-path", "eclipsefdn/iplab/iplab");
	}
}