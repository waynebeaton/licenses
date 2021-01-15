/*************************************************************************
 * Copyright (c) 2021, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.review;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.dash.licenses.LicenseData;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Issue;

public class GitLabSupport {

	private String host;
	private String token;
	private String path;

	public GitLabSupport(String host, String token, String path) {
		this.host = host;
		this.token = token;
		this.path = path;
	}

	private String getAccessToken() {
		return token;
	}

	private String getHostUrl() {
		return host;
	}

	protected String getPath() {
		return path;
	}

	public void createReviews(List<LicenseData> needsReview, PrintWriter output) {
		execute(connection -> {
			for (LicenseData data : needsReview) {
				output.println(String.format("Setting up a review for %s.", data.getId().toString()));

				if (!data.getId().isValid()) {
					output.println(" - Don't know what to do with this.");
					continue;
				}

				/*
				 * Ideally, we need a way to "create if does not already exist" feature in the
				 * GitLab API. But since we don't have that, we'll leverage the expectation that
				 * concurrent requests to review the same content will be relatively rare (there
				 * is some risk that between asking if we have an existing issue for a review
				 * for a particular bit of content and creating a new one, that somebody else
				 * might be doing the same). Our expectation is that the potential additional
				 * churn on the backend should require significantly less effort than that
				 * required to prevent rare duplication.
				 */
				try {
					GitLabReview review = new GitLabReview(data);

					Issue existing = connection.findIssue(review);
					if (existing != null) {
						output.println(String.format(" - Existing: %s", existing.getWebUrl()));
						continue;
					}

					Issue created = connection.createIssue(review);
					if (created == null) {
						output.println(" - An error occurred while attempting to create a review request");
						// TODO If we break creating a review, then don't try to create any more.
						break;
					}

					output.println(String.format(" - Created: %s", created.getWebUrl()));

				} catch (GitLabApiException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	void execute(Consumer<GitLabConnection> callable) {
		try (GitLabApi gitLabApi = new GitLabApi(getHostUrl(), getAccessToken())) {
			callable.accept(new GitLabConnection(this, gitLabApi));
		}
	}
}
