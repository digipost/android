Digipost for Android
=======

Digipost for Android er en app for Android som gir tilgang til brukerens sikre digitale postkasse i Digipost. Bruker må opprette en konto på https://www.digipost.no/.

App-en er tilgjengelig på Google Play, trykk nedenfor for å laste den ned.  
<a href='https://play.google.com/store/apps/details?id=no.digipost.android'>
    <img alt='Tilgjengelig på Google Play'
         height="80"
         src='https://play.google.com/intl/en_us/badges/images/generic/no_badge_web_generic.png'/>
</a>

Kildekoden som er skrevet av Posten er her tilgjengelig som fri programvare under lisensen *Apache License, Version 2.0*, som beskrevet i [lisensfilen](https://github.com/digipost/android/blob/master/LICENSE.txt "LICENSE").

Viktig:
------
For å kunne kjøre applikasjonen og få tilgang til Digipost applikasjonen sitt API trenger du en client id, client secret, og redirect uri. Hvordan du får tak i disse verdiene kan leses mer om på https://www.digipost.no/plattform/annet/oauth/.

Når man har fått tak i verdiene må det legges inn i Secret.java klassen. Den ligger under _src/main/java/no/digipost/android/authentication/Secret.java_, eller søk etter _TODO_.
