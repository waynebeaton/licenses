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

import java.util.function.Consumer;

import org.gitlab4j.api.GitLabApi;

public class GitLabSupport {

	private String host;
	private String token;
	private String path;

	public GitLabSupport(String host, String token, String path) {
		this.host = host;
		this.token = token;
		this.path = path;
	}

	public void execute(Consumer<GitLabConnection> callable) {
		try (GitLabApi gitLabApi = new GitLabApi(getHostUrl(), getAccessToken())) {
			callable.accept(new GitLabConnection(this, gitLabApi));
		}
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
}
