/*************************************************************************
 * Copyright (c) 2020, Red Hat Inc. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.maven;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.cli.CSVCollector;
import org.eclipse.dash.licenses.cli.IResultsCollector;
import org.eclipse.dash.licenses.cli.NeedsReviewCollector;
import org.eclipse.dash.licenses.review.CreateReviewRequestCollector;

/**
 * Maven goal for running the Dash License Check tool.
 */
@Mojo(name = "license-check", requiresProject = true, aggregator = true, requiresDependencyResolution = ResolutionScope.TEST)
public class LicenseCheckMojo extends AbstractArtifactFilteringMojo {

	/**
	 * Optionally process the request within the context of an Eclipse Foundation
	 * project. (E.g., technology.dash)
	 */
	@Parameter(property = "dash.projectId")
	private String projectId;

	/**
	 * Output a summary to the given file. If not specified, then a dependencies
	 * summary will be generated at the default location within
	 * <code>${project.build.directory}</code>
	 */
	@Parameter(property = "dash.summary", defaultValue = "${project.build.directory}/dash/summary")
	private File summary;

	/**
	 * Output a review request to the given file. If not specified, then a
	 * dependency review will be generated at the default location within
	 * <code>${project.build.directory}</code>
	 */
	@Parameter(property = "dash.review", defaultValue = "${project.build.directory}/dash/review")
	private File review;

	/**
	 * Batch size to use (number of entries sent per API call.)
	 */
	@Parameter(property = "dash.batch", defaultValue = "1000")
	private int batch;

	/**
	 * URL for the Eclipse Foundations's license check API.
	 */
	@Parameter(property = "dash.foundationApi", defaultValue = "https://www.eclipse.org/projects/services/license_check.php")
	private String foundationApi;

	/**
	 * URL for Clearly Defined's license definitions API.
	 */
	@Parameter(property = "dash.clearlyDefinedApi", defaultValue = "https://api.clearlydefined.io/definitions")
	private String clearlyDefinedApi;

	/**
	 * URL that returns the list of approved licenses. This URL should return a JSON
	 * document containing a map of SPDX license identifiers and their descriptions,
	 * for example:
	 * 
	 * <pre>
	 * {
	 *   "approved": {
	 *     "Apache-2.0": "Apache Software License 2.0",
	 *     "CPL-1.0": "Common Public License Version 1.0"
	 *   }
	 * }
	 * </pre>
	 */
	@Parameter(property = "dash.licenses", defaultValue = "https://www.eclipse.org/legal/licenses.json")
	private String licenses;

	/**
	 * Confidence threshold expressed as integer percentage. (0-100)
	 */
	@Parameter(property = "dash.confidence", defaultValue = "75")
	private int confidence;
	
	@Parameter(property = "dash.iplab.token")
	private String iplabToken;

	/**
	 * Skip execution of the Dash License Check mojo.
	 */
	@Parameter(property = "dash.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * The Maven session.
	 */
	@Parameter(defaultValue = "${session}", readonly = true, required = true)
	private MavenSession mavenSession;

	/**
	 * The Maven reactor.
	 */
	@Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
	private List<MavenProject> reactorProjects;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping dependency license check");
			return;
		}

		// Validate the user-given dash license tool settings
		ISettings settings;
		try {
			settings = new MavenSettings(batch, foundationApi, clearlyDefinedApi, licenses, confidence);
		} catch (IllegalArgumentException e) {
			throw new MojoExecutionException("Invalid setting: " + e.getMessage());
		}

		// Get filtered list of project dependencies for all modules in the reactor
		Set<Artifact> filteredArtifacts = new HashSet<>();
		for (MavenProject project : reactorProjects) {
			filteredArtifacts.addAll(filterArtifacts(project.getArtifacts()));
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug("Filtered dependency artifact list:");
			filteredArtifacts.stream().sorted().forEach(a -> {
				getLog().debug("  " + a.toString());
			});
		}

		// Adapt dependency artifacts to dash content IDs
		List<IContentId> deps = new ArrayList<>();
		filteredArtifacts.stream().sorted().forEach(a -> {
			String type = a.getGroupId().startsWith("p2.eclipse-") ? "p2" : "maven";
			// TODO deps are not necessarily from orbit or maven central
			String source = a.getGroupId().startsWith("p2.eclipse-") ? "orbit" : "mavencentral";
			// TODO could get duplicates here if two artifact coords differ only by
			// classifier
			deps.add(ContentId.getContentId(type, source, a.getGroupId(), a.getArtifactId(), a.getVersion()));
		});

		List<IResultsCollector> collectors = new ArrayList<>();

		// This collector generates feedback for the user that the command line tool
		// would always print to stdout, so we collect the output in memory for printing
		// to the maven log later
		ByteArrayOutputStream primaryOut = new ByteArrayOutputStream();
		NeedsReviewCollector needsReviewCollector = new NeedsReviewCollector(primaryOut);
		collectors.add(needsReviewCollector);

		try {
			summary.getParentFile().mkdirs();
			OutputStream summaryOut = new FileOutputStream(summary);
			collectors.add(new CSVCollector(summaryOut));
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Can't write dependency summary file", e);
		}

		if (settings.canCreateReviews()) {
			collectors.add(new CreateReviewRequestCollector(settings, primaryOut));
		}

		LicenseChecker checker = new LicenseChecker(settings);
		checker.getLicenseData(deps).forEach((id, licenseData) -> {
			collectors.forEach(collector -> collector.accept(licenseData));
		});
		collectors.forEach(IResultsCollector::close);

		// Pass the output from the first collector to the maven log
		try (BufferedReader primaryReader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(primaryOut.toByteArray()), StandardCharsets.UTF_8))) {
			String line = null;
			while ((line = primaryReader.readLine()) != null) {
				getLog().info(line);
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

		getLog().info("Summary file was written to: " + summary);
	}
}
