package no.digipost.android.api.exception;

public class DigipostInvalidTokenException extends Exception {
	private static final long serialVersionUID = -4399648205742784466L;

	public DigipostInvalidTokenException() {
		super();
	}

	public DigipostInvalidTokenException(final String errorMessage) {
		super(errorMessage);
	}
}
