package me.mrgazdag.programs.httpserver;

public class BadRequestFormatException extends Exception {
	private static final long serialVersionUID = 1L;
	public BadRequestFormatException(String msg) {
		super(msg);
	}
	public BadRequestFormatException(Throwable source) {
		super(source);
	}

}
