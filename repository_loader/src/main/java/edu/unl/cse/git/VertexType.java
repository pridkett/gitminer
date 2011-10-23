package edu.unl.cse.git;

import com.ibm.research.govsci.graph.StringableEnum;

public enum VertexType implements StringableEnum {
	COMMIT("COMMIT"),
	REPOSITORY("REPOSITORY"),
	FILE("FILE"),
	GIT_USER("GIT_USER"),
	NAME("NAME"),
	EMAIL("EMAIL");
	
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

