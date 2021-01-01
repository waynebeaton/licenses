/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import org.eclipse.dash.licenses.LicenseData;
import org.gitlab4j.api.Constants.IssueState;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.IssueFilter;

public class FindExistingReviewCommand {
	private LicenseData licenseData;

	public FindExistingReviewCommand(LicenseData licenseData) {
		this.licenseData = licenseData;
	}

	public String getTitle() {
		return licenseData.getId().toString();
	}

	public Issue find(IssuesApi issuesApi) throws GitLabApiException {
		String title = getTitle();
		IssueFilter filter = new IssueFilter().withSearch(title).withState(IssueState.OPENED);
		return issuesApi.getIssuesStream(getRepositoryPath(), filter).filter(issue -> issue.getTitle().equals(title))
				.findAny().orElse(null);
	}

	private String getRepositoryPath() {
		return System.getProperty("org.eclipse.dash.repository-path", "eclipsefdn/iplab/iplab");
	}
}
