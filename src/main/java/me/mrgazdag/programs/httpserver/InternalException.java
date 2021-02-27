package me.mrgazdag.programs.httpserver;

public class InternalException extends Exception {
	private static final long serialVersionUID = 1L;
	public InternalException(String msg) {
		super(msg);
	}
	public InternalException(Throwable source) {
		super(source);
	}

}
