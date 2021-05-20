/*************************************************************************
 * Copyright (c) 2019, The Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution, and is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *************************************************************************/
package org.eclipse.dash.licenses.clearlydefined;

import java.util.stream.Stream;

import org.eclipse.dash.licenses.ContentId;
import org.eclipse.dash.licenses.IContentData;
import org.eclipse.dash.licenses.IContentId;
import org.eclipse.dash.licenses.LicenseSupport.Status;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class ClearlyDefinedContentData implements IContentData {

	public static final String CLEARLYDEFINED = "clearlydefined";

	/**
	 * This field holds data that presents a single unit of content answered by a
	 * query to the ClearlyDefined service. The value is in JSON format as in this
	 * (somewhat abridged) example:
	 *
	 * <pre>
	 * 	{
	 *	    "_id": "npm/npmjs/-/write/1.0.3",
	 *	    "coordinates": {
	 *	        "name": "write",
	 *	        "provider": "npmjs",
	 *	        "revision": "1.0.3",
	 *	        "type": "npm"
	 *	    },
	 *	    "licensed": {
	 *	        "declared": "MIT",
	 *	        "facets": {
	 *	            "core": {
	 *	                "attribution": {
	 *	                    "parties": [
	 *	                        "Copyright (c) 2014-2017, Jon Schlinkert.",
	 *	                        "Copyright (c) 2017, Jon Schlinkert (https://github.com/jonschlinkert)."
	 *	                    ],
	 *	                    "unknown": 1
	 *	                },
	 *	                "discovered": {
	 *	                    "expressions": [
	 *	                        "MIT"
	 *	                    ],
	 *	                    "unknown": 0
	 *	                },
	 *	                "files": 4
	 *	            }
	 *	        },
	 *	        "score": {
	 *	            "consistency": 15,
	 *	            "declared": 30,
	 *	            "discovered": 19,
	 *	            "spdx": 15,
	 *	            "texts": 15,
	 *	            "total": 94
	 *	        },
	 *	        "toolScore": {
	 *	            "consistency": 15,
	 *	            "declared": 30,
	 *	            "discovered": 19,
	 *	            "spdx": 15,
	 *	            "texts": 15,
	 *	            "total": 94
	 *	        }
	 *	    },
	 *	    "scores": {
	 *	        "effective": 97,
	 *	        "tool": 97
	 *	    }
	 *	}
	 * </pre>
	 */
	private JsonObject data;
	private String id;
	private Status status;

	public ClearlyDefinedContentData(String id, JsonObject data) {
		this.id = id;
		this.data = data;
	}

	@Override
	public String getLicense() {
		// FIXME Should combine the declared and discovered licenses.
		return getDeclaredLicense();
	}

	public String getDeclaredLicense() {
		// @formatter:off
		JsonString license = data
			.getOrDefault("licensed", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getJsonString("declared");
		// @formatter:on
		return license == null ? "" : license.getString();
	}

	@Override
	public int getScore() {
		return getLicenseScore();
	}

	public int getEffectiveScore() {
		// @formatter:off
		JsonNumber score = data
			.getOrDefault("scores", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getJsonNumber("effective");
		// @formatter:on
		return score == null ? 0 : score.intValue();
	}

	public int getLicenseScore() {
		// @formatter:off
		JsonNumber score = data
			.getOrDefault("licensed", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getOrDefault("score", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getJsonNumber("total");
		// @formatter:on
		return score == null ? 0 : score.intValue();
	}

	@Override
	public IContentId getId() {
		return ContentId.getContentId(id);
	}

	@Override
	public String getAuthority() {
		return CLEARLYDEFINED;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * Answer the discovered licenses as a stream.
	 *
	 * @return
	 */
	public Stream<String> discoveredLicenses() {
		// @formatter:off
		return data
			.getOrDefault("licensed", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getOrDefault("facets", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getOrDefault("core", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getOrDefault("discovered", JsonValue.EMPTY_JSON_OBJECT).asJsonObject()
			.getOrDefault("expressions", JsonValue.EMPTY_JSON_ARRAY).asJsonArray()
			.getValuesAs(JsonString.class).stream().map(JsonString::getString);
		// @formatter:on
	}

	@Override
	public String getUrl() {
		return "https://clearlydefined.io/definitions/" + getId();
	}
}
