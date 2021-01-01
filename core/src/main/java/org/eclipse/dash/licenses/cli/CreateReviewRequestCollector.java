/*************************************************************************
 * Copyright (c) 2020, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.review.CreateReviewCommand;
import org.eclipse.dash.licenses.review.FindExistingReviewCommand;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.IssuesApi;
import org.gitlab4j.api.models.Issue;

/**
 * The "Create Review Request" collector tracks the results that likely require
 * some review from the IP Team. The gathered information is output to an
 * {@link OutputStream} in Markdown format, suitable for use as a description in
 * a review request.
 */
public class CreateReviewRequestCollector implements IResultsCollector {

	private PrintWriter output;
	private List<LicenseData> needsReview = new ArrayList<>();
	private Project project;

	public CreateReviewRequestCollector(Project project, OutputStream out) {
		this.project = project;
		output = new PrintWriter(out);
	}

	@Override
	public void accept(LicenseData data) {
		if (data.getStatus() != Status.Approved) {
			needsReview.add(data);
		}
	}

	@Override
	public void close() {
		if (needsReview.isEmpty()) {
			output.println(
					"Vetted license information was found for all content. No further investigation is required.");
		} else {
			if (project != null) {
				output.println(String.format("Project: [%s](%s)", project.getName(), project.getUrl()));
				output.println();
			}

			try (GitLabApi gitLabApi = new GitLabApi(getHostUrl(), getAccessToken())) {
				IssuesApi issuesApi = gitLabApi.getIssuesApi();
				for (LicenseData data : needsReview) {
					if (!data.getId().isValid())
						continue;
					Issue existing = new FindExistingReviewCommand(data).find(issuesApi);
					if (existing != null) {
						output.println(String.format("A review has already been requested for %s\n - %s",
								data.getId().toString(), existing.getWebUrl()));
						continue;
					}
					Issue created = new CreateReviewCommand(data).create(issuesApi);
					if (created == null) {
						output.println(String.format(
								"An error occurred while attempting to create a review request for %s", data.getId()));
						// TODO If we break creating a review, then don't try to create any more.
						break;
					}
					output.println(String.format("A new review request was created for %s\n - %s",
							data.getId().toString(), created.getWebUrl()));

					break;
				}
			} catch (GitLabApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		output.flush();
	}

	private String getHostUrl() {
		return System.getProperty("org.eclipse.dash.repository-host", "https://gitlab.eclipse.org");
	}

	private String getAccessToken() {
		return System.getProperty("org.eclipse.dash.token");
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}