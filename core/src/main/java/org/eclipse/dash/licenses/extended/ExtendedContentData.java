package org.eclipse.dash.licenses.extended;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ExtendedContentData {
	private String title;
	private String url;
	private List<ExtendedContentDataItem> items = new ArrayList<>();

	private static ExtendedContentDataItem EMPTY = new ExtendedContentDataItem(null, null);

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

	public String get(String key) {
		return getItems().filter(each -> each.getLabel().equals(key)).findAny().orElse(EMPTY).getValue();
	}
}
