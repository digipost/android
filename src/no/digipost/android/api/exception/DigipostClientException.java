package no.digipost.android.api.exception;

public class DigipostClientException extends Exception {
	private static final long serialVersionUID = 1759556386425322519L;

	public DigipostClientException(final String errorMessage) {
		super(errorMessage);
	}
}
