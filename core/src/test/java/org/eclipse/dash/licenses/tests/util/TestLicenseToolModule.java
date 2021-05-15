/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.tests.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.LicenseSupport;
import org.eclipse.dash.licenses.clearlydefined.ClearlyDefinedSupport;
import org.eclipse.dash.licenses.extended.ExtendedContentDataService;
import org.eclipse.dash.licenses.extended.IExtendedContentDataProvider;
import org.eclipse.dash.licenses.extended.MavenCentralExtendedContentDataProvider;
import org.eclipse.dash.licenses.extended.NpmjsExtendedContentDataProvider;
import org.eclipse.dash.licenses.foundation.EclipseFoundationSupport;
import org.eclipse.dash.licenses.http.IHttpClientService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;

public class TestLicenseToolModule extends AbstractModule {

	private ISettings settings;

	public TestLicenseToolModule() {
		this.settings = new ISettings() {
		};
	}

	@Override
	protected void configure() {
		bind(ISettings.class).toInstance(settings);
		bind(IHttpClientService.class).toInstance(getHttpClientService());
		bind(LicenseChecker.class).toInstance(new LicenseChecker());
		bind(EclipseFoundationSupport.class).toInstance(new EclipseFoundationSupport());
		bind(ClearlyDefinedSupport.class).toInstance(new ClearlyDefinedSupport());
		bind(LicenseSupport.class).toInstance(new LicenseSupport());
		bind(ExtendedContentDataService.class).toInstance(new ExtendedContentDataService());

		Multibinder<IExtendedContentDataProvider> classifierBinder = Multibinder.newSetBinder(binder(),
				IExtendedContentDataProvider.class);
		classifierBinder.addBinding().to(NpmjsExtendedContentDataProvider.class);
		classifierBinder.addBinding().to(MavenCentralExtendedContentDataProvider.class);
		// classifierBinder.addBinding().to(GithubDataProvider.class);
	}

	public IHttpClientService getHttpClientService() {
		return new IHttpClientService() {
			@Override
			public int post(String url, String contentType, String payload, Consumer<String> handler) {
				if (url.equals(settings.getClearlyDefinedDefinitionsUrl())) {
					// The file contains only the information for the one record; the
					// ClearlyDefined service expects a Json collection as the response,
					// so insert the file contents into an array and pass that value to
					// the handler.
					JsonReader reader = Json.createReader(new StringReader(payload));
					JsonArray items = (JsonArray) reader.read();

					var builder = new StringBuilder();
					builder.append("{");
					for (int index = 0; index < items.size(); index++) {
						if (index > 0)
							builder.append(",");
						var id = items.getString(index);
						builder.append("\"");
						builder.append(id);
						builder.append("\" :");

						switch (id) {
						case "npm/npmjs/-/write/1.0.3":
							builder.append(new BufferedReader(new InputStreamReader(
									this.getClass().getResourceAsStream("/write-1.0.3.json"), StandardCharsets.UTF_8))
											.lines().collect(Collectors.joining("\n")));
							break;
						case "npm/npmjs/@yarnpkg/lockfile/1.1.0":
							builder.append(new BufferedReader(
									new InputStreamReader(this.getClass().getResourceAsStream("/lockfile-1.1.0.json"),
											StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")));
							break;

						default:
							builder.append("{}");
						}
					}
					builder.append("}");

					handler.accept(builder.toString());

					return 200;
				}

				if (url.equals(settings.getLicenseCheckUrl())) {

					if (payload.startsWith("json=")) {
						var json = URLDecoder.decode(payload.substring("json=".length()), StandardCharsets.UTF_8);

						var approved = Json.createObjectBuilder();
						var rejected = Json.createObjectBuilder();

						var reader = Json.createReader(new StringReader(json));
						reader.readArray().forEach(key -> {
							var id = ((JsonString) key).getString();
							var item = Json.createObjectBuilder();
							switch (id) {
							case "npm/npmjs/-/write/0.2.0":
								item.add("authority", "CQ7766");
								item.add("confidence", 100);
								item.add("id", id);
								item.add("license", "Apache-2.0");
								item.addNull("sourceUrl");
								item.add("status", "approved");

								approved.add(id, item);
								break;

							case "npm/npmjs/@yarnpkg/lockfile/1.1.0":
								item.add("authority", "CQ7722");
								item.add("confidence", 100);
								item.add("id", id);
								item.add("license", "GPL-2.0");
								item.addNull("sourceUrl");
								item.add("status", "rejected");

								rejected.add(id, item);
								break;
							}
						});

						var parent = Json.createObjectBuilder();
						parent.add("approved", approved);
						parent.add("rejected", rejected);

						handler.accept(parent.build().toString());

						return 200;
					}
				}
				return 404;
			}

			@Override
			public int get(String url, String contentType, Consumer<InputStream> handler) {
				if (url.equals(settings.getApprovedLicensesUrl())) {
					handler.accept(this.getClass().getResourceAsStream("/licenses.json"));
					return 200;
				}

				switch (url) {
				case "https://registry.npmjs.org/chalk":
					handler.accept(this.getClass().getResourceAsStream("/chalk.json"));
					return 200;
				}

				return 404;
			}

			@Override
			public boolean remoteFileExists(String url) {
				switch (url) {
				case "https://search.maven.org/artifact/group.path/artifact/1.0/jar":
				case "https://search.maven.org/remotecontent?filepath=group/path/artifact/1.0/artifact-1.0-sources.jar":
				case "https://github.com/sindresorhus/chalk/archive/v0.1.0.zip":
				case "https://registry.npmjs.org/chalk/-/chalk-0.1.0.tgz":
					return true;
				}
				return IHttpClientService.super.remoteFileExists(url);
			}
		};
	}
}
