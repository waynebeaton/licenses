/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseChecker;
import org.eclipse.dash.licenses.review.CreateReviewRequestCollector;

/**
 * This class provides a CLI entrypoint to determine licenses for content. The
 * tool can be invoked in a few different ways, e.g.
 *
 * <pre>
 * mvn dependency:list -DskipTests -Dmaven.javadoc.skip=true |
 * grep -Poh "\S+:(system|provided|compile) |
 * sort | uniq | java -jar licenses.jar -batch 100 -
 * </pre>
 *
 * or
 *
 * <pre>
 * java -jar licenses.jar -batch 100 package-lock.json
 * </pre>
 *
 * @param args
 */
public class Main {

	public static void main(String[] args) {
		CommandLineSettings settings = CommandLineSettings.getSettings(args);
		if (!settings.isValid()) {
			CommandLineSettings.printUsage(System.out);
			System.exit(1);
		}

		if (settings.isShowHelp()) {
			CommandLineSettings.printHelp(System.out);
			System.exit(0);
		}

		List<IResultsCollector> collectors = new ArrayList<>();

		// TODO Set up collectors based on command line parameters
		IResultsCollector primaryCollector = new NeedsReviewCollector(System.out);
		collectors.add(primaryCollector);

		String summaryPath = settings.getSummaryFilePath();
		if (summaryPath != null) {
			try {
				collectors.add(new CSVCollector(getWriter(summaryPath)));
			} catch (FileNotFoundException e1) {
				System.out.println("Can't write to " + summaryPath);
				System.exit(1);
			}
		}

		if (settings.canCreateReviews()) {
			collectors.add(new CreateReviewRequestCollector(settings, System.out));
		}

		Arrays.stream(settings.getFileNames()).forEach(name -> {
			IDependencyListReader reader = null;
			try {
				reader = getReader(name);
			} catch (FileNotFoundException e) {
				System.out.println(String.format("The file \"%s\" does not exist.", name));
				CommandLineSettings.printUsage(System.out);
				System.exit(1);
			}
			if (reader != null) {
				Collection<IContentId> dependencies = reader.getContentIds();

				LicenseChecker checker = new LicenseChecker(settings);
				checker.getLicenseData(dependencies).forEach((id, licenseData) -> {
					collectors.forEach(collector -> collector.accept(licenseData));
				});
			}
		});

		collectors.forEach(IResultsCollector::close);

		System.exit(primaryCollector.getStatus());
	}

	private static OutputStream getWriter(String path) throws FileNotFoundException {
		if ("-".equals(path))
			return System.out;
		return new FileOutputStream(new File(path));
	}

	@SuppressWarnings("resource")
	private static IDependencyListReader getReader(String name) throws FileNotFoundException {
		if ("-".equals(name)) {
			return new FlatFileReader(new InputStreamReader(System.in));
		} else {
			File input = new File(name);
			if (input.exists()) {
				if ("package-lock.json".equals(input.getName())) {
					return new PackageLockFileReader(new FileInputStream(input));
				}
				return new FlatFileReader(new FileReader(input));
			} else {
				throw new FileNotFoundException(name);
			}
		}
	}
}
