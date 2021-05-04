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

import java.net.URI;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.http.IHttpClientService;
import org.eclipse.dash.licenses.util.JsonUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class NpmjsExtendedContentDataProvider implements IExtendedContentDataProvider {
	@Inject
	ISettings settings;
	@Inject
	IHttpClientService httpClientService;

	@Override
	public ExtendedContentData getExtendedContentData(IContentId id) {
		if (!"npmjs".equals(id.getSource()))
			return null;

		var builder = new NpmjsPackageBuilder(id);

		getMetadata(id, data -> {
			builder.setMetadata(data);
		});

		return builder.build();
	}

	private void getMetadata(IContentId id, Consumer<JsonObject> consumer) {
		httpClientService.get(getMetadataUrl(id), "application/json", inputStream -> {
			consumer.accept(JsonUtils.readJson(inputStream));
		});
	}

	private String getMetadataUrl(IContentId id) {
		var builder = new StringBuilder();
		builder.append("https://registry.npmjs.org/");
		if (!"-".equals(id.getNamespace())) {
			builder.append(id.getNamespace());
			builder.append("%2F");
		}
		builder.append(id.getName());

		return builder.toString();
	}

	class NpmjsPackageBuilder {

		private IContentId id;
		private JsonObject metadata = JsonValue.EMPTY_JSON_OBJECT;

		public NpmjsPackageBuilder(IContentId id) {
			this.id = id;
		}

		public ExtendedContentData build() {
			var thing = new ExtendedContentData("npmjs", getUrl());
			thing.addItem("License", getLicense());
			thing.addItem("Distribution", getTarballUrl());
			thing.addItem("Repository", getRepositoryUrl());
			thing.addItem("Source", getSourceUrl());

			return thing;
		}

		String getUrl() {
			var npmId = new StringBuilder();
			if (!"-".equals(id.getNamespace())) {
				npmId.append(id.getNamespace());
				npmId.append('/');
			}
			npmId.append(id.getName());

			return String.format("https://www.npmjs.com/package/%s/v/%s", npmId.toString(), id.getVersion());
		}

		String getRepositoryUrl() {
			return getVersionMetadata().getOrDefault("repository", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
					.getString("url", null);
		}

		String getTarballUrl() {
			return getVersionMetadata().getOrDefault("dist", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
					.getString("tarball", null);
		}

		String getLicense() {
			return getVersionMetadata().getString("license", null);
		}

		void setMetadata(JsonObject data) {
			this.metadata = data;
		}

		JsonObject getVersionMetadata() {
			return metadata.getOrDefault("versions", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
					.getOrDefault(id.getVersion(), JsonValue.EMPTY_JSON_OBJECT).asJsonObject();
		}

		String getSourceUrl() {
			try {
				var uri = URI.create(getRepositoryUrl());
				if (!"github.com".equals(uri.getHost()))
					return null;

				var path = uri.getPath();
				if (path.endsWith(".git"))
					path = path.substring(0, path.length() - 4);

				var url = new StringBuilder();
				url.append("https://");
				url.append(uri.getHost());
				url.append(path);
				url.append("/archive/v");
				url.append(id.getVersion());
				url.append(".zip");
				if (httpClientService.remoteFileExists(url.toString()))
					return url.toString();
			} catch (Exception e) {
				// If we encounter an exception, just skip it and return null.
			}

			return null;
		}
	}
}
