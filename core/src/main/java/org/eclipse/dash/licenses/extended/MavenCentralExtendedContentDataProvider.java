/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.extended;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.http.IHttpClientService;

public class MavenCentralExtendedContentDataProvider implements IExtendedContentDataProvider {
	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;

	@Override
	public ExtendedContentData getExtendedContentData(IContentId id) {
		if (!"maven".equals(id.getType()))
			return null;
		if (!"mavencentral".equals(id.getSource()))
			return null;

		return new MavenCentralPackageBuilder(id).build();
	}

	class MavenCentralPackageBuilder {
		private IContentId id;

		public MavenCentralPackageBuilder(IContentId id) {
			this.id = id;
		}

		public ExtendedContentData build() {
			var thing = new ExtendedContentData("Maven Central", getUrl());
			thing.addItem("Source", getSourceUrl());

			return thing;
		}

		public String getUrl() {
			return String.format("https://search.maven.org/artifact/%s/%s/%s/jar", id.getNamespace(), id.getName(),
					id.getVersion());
		}

		public String getSourceUrl() {
			var url = getMavenCentralSourceUrl();
			if (url == null) {
				return null;
			}

			if (httpClientService.remoteFileExists(url)) {
				return url;
			}

			return null;
		}

		public String getMavenCentralSourceUrl() {

			// FIXME Validate that this file pattern is correct.
			// This pattern was observed and appears to be accurate.
			var url = "https://search.maven.org/remotecontent?filepath={groupPath}/{artifactid}/{version}/{artifactid}-{version}-sources.jar";
			url = url.replace("{groupPath}", id.getNamespace().replace('.', '/'));
			url = url.replace("{artifactid}", id.getName());
			url = url.replace("{version}", id.getVersion());

			return url;
		}
	}
}
