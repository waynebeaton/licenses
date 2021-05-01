package org.eclipse.dash.licenses.npmjs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ExtendedContentData {
	private String title;
	private String url;
	private List<ExtendedContentDataItem> items = new ArrayList<>();

	public ExtendedContentData(String title, String url) {
		this.title = title;
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public Stream<ExtendedContentDataItem> getItems() {
		return items.stream();
	}

	public void addItem(String key, String value) {
		if (value == null)
			return;
		items.add(new ExtendedContentDataItem(key, value));
	}
}
