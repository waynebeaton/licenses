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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.LicenseSupport.Status;
import org.eclipse.dash.licenses.cli.IResultsCollector;

/**
 * The "Create Review Request" collector tracks the results that likely require
 * some review from the IP Team. The gathered information is output to an
 * {@link OutputStream} in Markdown format, suitable for use as a description in
 * a review request.
 */
public class CreateReviewRequestCollector implements IResultsCollector {
	private PrintWriter output;
	private List<LicenseData> needsReview = new ArrayList<>();
	private ISettings settings;

	public CreateReviewRequestCollector(ISettings settings, OutputStream out) {
		this.settings = settings;
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
			var gitlab = new GitLabSupport(settings.getIpLabHostUrl(), settings.getIpLabToken(),
					settings.getIpLabRepositoryPath());
			gitlab.createReviews(needsReview, output);
		}
		output.flush();
	}

	@Override
	public int getStatus() {
		return needsReview.size();
	}
}