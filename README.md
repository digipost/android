android
=======

Digipost for Android

Viktig:
------
For å kunne bruke koden og få tilgang til Digipost sitt api vil du trenge client id og client secret. Hvordan du får tak i det kan du lese mer om på https://www.digipost.no/plattform/privat/ 
Når du har fått client id og client secret er du nødt til å opprette en fil kalt Secret.java,legge den i mappen src/no/digipost/android/authentication og gi den følgende innhold:

    package no.digipost.android.authentication;
    public class Secret {
      public static final String CLIENT_ID = "";
      public static final String CLIENT_SECRET = "";
      public static final String REDIRECT_URI = "http://localhost:1979";
      public static String ACCESS_TOKEN = "";
    }
