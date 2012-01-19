package edu.unl.cse.git;

import com.ibm.research.govsci.graph.StringableEnum;

public enum EdgeType implements StringableEnum {
	REPOSITORY( "REPOSITORY" ),
	PARENT( "PARENT" ),
	AUTHOR( "AUTHOR" ),
	COMMITTER( "COMMITTER" ),
	CHANGED( "CHANGED" ),
	NAME( "NAME" ),
	EMAIL( "EMAIL" );
	
	private String text;
	
	EdgeType(String text) {
		this.text = text;
	}
	
	public String toString() {
		return this.text;
	}
	
	public static EdgeType fromString(String text) {
		if (text != null) {
			for (EdgeType d : EdgeType.values()) {
				if (text.equals(d.text)) { return d; }
			}
		}
		throw new IllegalArgumentException("EdgeType: '" + text + "' not valid");
	}
}
