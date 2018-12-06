Digipost for Android
=======

Digipost for Android er en app for Android som gir tilgang til brukerens sikre digitale postkasse i Digipost. Bruker må opprette en konto på https://www.digipost.no/.

App-en er tilgjengelig på Google Play, trykk nedenfor for å laste den ned.  
<a href='https://play.google.com/store/apps/details?id=no.digipost.android&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
    <img alt='Tilgjengelig på Google Play'
         height="80"
         src='https://play.google.com/intl/en_us/badges/images/generic/no_badge_web_generic.png'/>
</a>

Kildekoden som er skrevet av Posten er her tilgjengelig som fri programvare under lisensen *Apache License, Version 2.0*, som beskrevet i [lisensfilen](https://github.com/digipost/android/blob/master/LICENSE.txt "LICENSE").

Viktig:
------
For å kunne bruke koden og få tilgang til Digipost sitt api vil du trenge client id og client secret. Hvordan du får tak i det kan du lese mer om på https://www.digipost.no/plattform/annet/oauth/ 
Når du har fått client id og client secret er du nødt til å opprette en fil kalt Secret.java,legge den i mappen src/no/digipost/android/authentication og gi den følgende innhold:

    package no.digipost.android.authentication;
    public class Secret {
      public static final String CLIENT_ID = "";
      public static final String CLIENT_SECRET = "";
      public static final String REDIRECT_URI = "";
      public static final String ENV_URL = "https://www.digipost.no/post/";
    }
