/*************************************************************************
 * Copyright (c) 2021 The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.eclipse.dash.licenses.ISettings;

public class HttpClientService implements IHttpClientService {

	@Inject
	ISettings settings;

	@Override
	public int post(String url, String contentType, String payload, Consumer<String> handler) {
		try {
			Duration timeout = Duration.ofSeconds(settings.getTimeout());
			HttpRequest request = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", contentType)
					.POST(BodyPublishers.ofString(payload, StandardCharsets.UTF_8)).timeout(timeout).build();

			HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				handler.accept(response.body());
			}
			return response.statusCode();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean remoteFileExists(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("HEAD");
			return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int get(String url, String contentType, Consumer<InputStream> handler) {
		try {
			Duration timeout = Duration.ofSeconds(settings.getTimeout());
			HttpRequest request = HttpRequest.newBuilder(URI.create(url)).header("Content-Type", contentType).GET()
					.timeout(timeout).build();

			HttpClient httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
			HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());
			if (response.statusCode() == 200) {
				handler.accept(response.body());
			}
			return response.statusCode();
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
