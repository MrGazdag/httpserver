package me.mrgazdag.programs.httpserver;

public enum HTTPVersion {
	VERSION_0_9("0.9"),
	VERSION_1_0("1.0"),
	VERSION_1_1("1.1"),
	VERSION_2_0("2.0"),
	UNKNOWN("UNKNOWN");

	private String s;
	private HTTPVersion(String s) {
		this.s = "HTTP/" + s;
	}
	public String getMessage() {
		return s;
	}
	public static HTTPVersion of(String input) {
		for (HTTPVersion v : values()) {
			if (v.s.equalsIgnoreCase(input)) return v;
		}
		return UNKNOWN;
	}
}
