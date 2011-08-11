/*
 * Copyright 2011 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wagstrom.research.github;

import com.ibm.research.govsci.graph.StringableEnum;

public enum VertexType implements StringableEnum {
	COMMIT("COMMIT"),
	USER("USER"),
	REPOSITORY("REPOSITORY"),
	ORGANIZATION("ORGANIZATION"),
	TEAM("TEAM"),
	GIST("GIST"),
	ISSUE("ISSUE"),
	LABEL("LABEL"),
	COMMENT("COMMENT"),
	GISTFILE("GISTFILE"),
	PULLREQUEST("PULLREQUEST"),
	DISCUSSION("DISCUSSION");
	
	private String text;
	VertexType(String text) {
		this.text = text;
	}
	public String toString() {
		return this.text;
	}
	
	public static VertexType fromString(String text) {
		if (text != null) {
			for (VertexType d : VertexType.values()) {
				if (text.equals(d.text)) { return d; }
			}
		}
		throw new IllegalArgumentException("VertexType: '" + text + "' not valid");
	}
}
