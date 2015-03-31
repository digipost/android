package no.digipost.android.model;

public enum Origin {
    CORPORATION,
    PRIVATE_PERSON,
    PUBLIC_ENTITY,
    UPLOADED;

    public static Origin parse(String origin) {
        try {
            return Origin.valueOf(origin);
        } catch (IllegalArgumentException e) {
            // Fall back to CORPORATION if Digipost has added a new origin-type that this app doesn't understand
            return CORPORATION;
        }
    }
}
