package org.eclipse.dash.licenses.npmjs;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.ISettings;

public class ExtendedContentDataService {
	@Inject
	ISettings settings;

	@Inject
	Set<IExtendedContentDataProvider> providers;

	public Stream<ExtendedContentData> findFor(IContentId contentId) {
		return providers.stream().map(provider -> provider.getExtendedContentData(contentId)).filter(Objects::nonNull);
	}
}
