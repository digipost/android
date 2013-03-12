package no.digipost.android.api;

public class DigipostApiException extends Exception {
	private static final long serialVersionUID = 2319082881339067458L;

	public DigipostApiException(final String errorMessage) {
		super(errorMessage);
	}
}
