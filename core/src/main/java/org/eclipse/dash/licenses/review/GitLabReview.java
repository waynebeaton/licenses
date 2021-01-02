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

import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseData;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedContentData;

public class GitLabReview {
	private LicenseData licenseData;

	public GitLabReview(LicenseData licenseData) {
		this.licenseData = licenseData;
	}

	public String getTitle() {
		return getContentId().toString();
	}

	public String getLabels() {
		return "Review Needed";
	}

	public String getDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%s\n", licenseData.getId()));
		licenseData.contentData().forEach(data -> describeItem(data, builder));
		String searchUrl = IPZillaSearchBuilder.build(licenseData);
		if (searchUrl != null) {
			builder.append(String.format("  - [Search IPZilla](%s)\n", searchUrl));
		}

		// FIXME This is clunky
		IContentId id = licenseData.getId();
		if ("maven".equals(id.getType()) && "mavencentral".equals(id.getSource())) {
			builder.append(String.format("  - [Maven Central](https://search.maven.org/artifact/%s/%s/%s/jar)\n",
					id.getNamespace(), id.getName(), id.getVersion()));
			var source = getVerifiedMavenSourceUrl();
			if (source != null) {
				builder.append(String.format("  - [Source](%s) from Maven Central\n", source));
			}
		}
		if ("npm".equals(id.getType()) && "npmjs".equals(id.getSource())) {
			var npmId = new StringBuilder();
			if (!"-".equals(id.getNamespace())) {
				npmId.append(id.getNamespace());
				npmId.append('/');
			}
			npmId.append(id.getName());
			builder.append(String.format("  - [npmjs.com](https://www.npmjs.com/package/%s/v/%s)\n", npmId.toString(),
					id.getVersion()));
		}
		return builder.toString();
	}

	/**
	 * THis method writes potentially helpful information to make the intellectual
	 * review process as easy as possible to the output writer.
	 * 
	 * @param data
	 */
	private void describeItem(IContentData data, StringBuilder output) {
		// FIXME This is clunky

		String authority = data.getAuthority();
		if (data.getUrl() != null)
			authority = String.format("[%s](%s)\n", authority, data.getUrl());
		output.append(String.format("  - %s %s (%d)\n", authority, data.getLicense(), data.getScore()));
		switch (data.getAuthority()) {
		case ClearlyDefinedContentData.CLEARLYDEFINED:
			((ClearlyDefinedContentData) data).discoveredLicenses()
					.forEach(license -> output.append("    - " + license).append('\n'));
		};
	}

	public String getVerifiedMavenSourceUrl() {
		var url = getMavenSourceUrl();
		if (url == null)
			return null;

		if (remoteFileExists(url)) {
			return url;
		}

		return null;
	}

	public String getMavenSourceUrl() {
		var id = getContentId();
		if (!id.isValid())
			return null;

		// FIXME Validate that this file pattern is correct.
		// This pattern was observed and appears to be accurate.
		var url = "https://search.maven.org/remotecontent?filepath={groupPath}/{artifactid}/{version}/{artifactid}-{version}-sources.jar";
		url = url.replace("{groupPath}", id.getNamespace().replace('.', '/'));
		url = url.replace("{artifactid}", id.getName());
		url = url.replace("{version}", id.getVersion());

		return url;
	}

	private IContentId getContentId() {
		return licenseData.getId();
	}

	private static boolean remoteFileExists(String url) {
		// FIXME This method doesn't belong here.
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("HEAD");
			return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
