package me.mrgazdag.programs.httpserver;

public class BadRequestException extends Exception {
	private static final long serialVersionUID = 1L;
	public BadRequestException(String msg) {
		super(msg);
	}
	public BadRequestException(Throwable source) {
		super(source);
	}

}
