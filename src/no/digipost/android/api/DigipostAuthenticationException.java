package no.digipost.android.api;

public class DigipostAuthenticationException extends Exception {
	private static final long serialVersionUID = 4361342932572546766L;

	public DigipostAuthenticationException(final String errorMessage) {
		super(errorMessage);
	}
}
