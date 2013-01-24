android
=======

Digipost for Android

First commit
------
**Dag 6** Dette prosjektet er hittil veldig beta, så vi trenger ikke så mye tilbakemeldinger før vi får rettet opp i alt det vi har på planen. Når vi kommer skikkelig i gang blir det morsomt med mye god konstruktiv kritikk, men før det vil det nok bare sinke fremdriften. :-) Gleder oss!

Viktig:
------
For å få tilgang til Digipost sitt api vil du trenge client id og client secret. Hvordan du får tak i det kan du lese mer om på https://www.digipost.no/plattform/privat/ 
Når du har fått client id og client secret er du nødt til å opprette en fil kalt Secret.java og legge den i mappen src/no/digipost/android/authentication og gi den følgende innhold:

package no.digipost.android.authentication;

public class Secret {

public static final String CLIENT_ID = "";

public static final String CLIENT_SECRET = "";

public static final String REDIRECT_URI = "http://localhost:1979";

public static String ACCESS_TOKEN = "";

}
