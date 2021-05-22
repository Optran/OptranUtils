package com.github.optran.utils.exceptions;

public class NotEnoughMemoryException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public NotEnoughMemoryException() {
	}
	public NotEnoughMemoryException(Throwable cause) {
		super(cause);
	}
	public NotEnoughMemoryException(String message) {
		super(message);
	}
	public NotEnoughMemoryException(String message, Throwable cause) {
		super(message, cause);
	}
}
