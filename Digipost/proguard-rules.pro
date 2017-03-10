
# general
-dontwarn InnerClasses, EnclosingMethod, Signature, Exceptions
-keepattributes SetJavaScriptEnabled
-keepattributes JavascriptInterface
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#digipost
-keep class no.digipost.android.** {*;}

# android
-keep class android.support.v4.** {*;}
-dontnote android.support.v4.**
-keep class android.support.v7.** {*;}
-dontnote android.support.v7.**

-keep class android.support.graphics.drawable.** {*;}
-dontnote android.support.graphics.drawable.**

-keep class android.webkit.** {*;}
-dontnote android.webkit.**

-dontnote android.net.http.HttpResponseCache
-dontnote android.net.http.SslCertificate$DName
-dontnote android.net.http.SslError
-dontnote android.net.http.SslCertificate

# java/javax
-keep class javax.imageio.** {*;}
-keep class javax.activation.** {*;}
-dontwarn javax.activation.**

-keep class javax.ws.rs.core.** {*;}
-keep class javax.ws.rs.ext.** {*;}

-keep class java.beans.Introspector
-dontwarn java.beans.Introspector
-dontnote java.beans.Introspector

-keep class javax.xml.stream.** {*;}
-dontwarn javax.xml.stream.**

-keep class javax.xml.bind.** {*;}
-dontwarn javax.xml.bind.**

-keep class javax.mail.** {*;}
-dontwarn javax.mail.**

-keep class javax.imageio.** {*;}
-dontwarn javax.imageio.**

-keep class java.awt.image.** {*;}
-dontwarn java.awt.image.**

# apache
-keep class org.apache.http.** {*;}
-dontwarn org.apache.http.**
-dontnote org.apache.http.**

-keep class org.apache.commons.lang.exception.ExceptionUtils {*;}
-dontnote org.apache.commons.lang.exception.ExceptionUtils

# jersey
-keep class com.sun.jersey.** {*;}
-keep class com.sun.jersey.json.impl.writer.DefaultXmlStreamWriter {*;}
-keep class com.sun.jersey.core.impl.provider.entity.MimeMultipartProvider {*;}
-keep class com.sun.jersey.core.impl.provider.entity.RenderedImageProvider {*;}
-dontwarn com.sun.jersey.json.impl.provider.entity.**
-dontwarn com.sun.jersey.json.impl.reader.JsonXmlStreamReader
-dontwarn com.sun.jersey.json.impl.writer.DefaultXmlStreamWriter
-keep class com.sun.xml.** {*;}
-dontwarn com.sun.xml.**
-keep class com.sun.ws.** {*;}
-dontnote com.sun.**

# osgi
-keep class org.osgi.framework.Bundle {*;}
-keep class org.osgi.framework.BundleContext
-keep class org.osgi.framework.FrameworkUtil
-keep class org.osgi.framework.BundleReference
-keep class org.osgi.framework.BundleEvent
-keep class org.osgi.framework.SynchronousBundleListener
-dontwarn org.osgi.framework.**
-dontnote org.osgi.framework.**

# jackson
-keep class org.codehaus.jackson.** {*;}
-dontnote org.codehaus.jackson.map.deser.BasicDeserializerFactory

# google
-keep class com.google.android.gms.measurement.** {*;}
-keep class com.google.android.gms.internal.** {*;}
-keep class com.google.firebase.** {*;}
-dontnote com.google.firebase.**
-keep class com.google.android.gms.dynamic.IObjectWrapper {*;}
-dontnote com.google.android.gms.**
-dontwarn com.google.android.gms.**
-keep class com.google.gson.** {*;}
-dontnote com.google.gson.**

# facebook
-keep class com.facebook.crypto.** {*;}
-dontnote com.facebook.crypto.**
-dontnote com.facebook.android.crypto.keychain.SecureRandomFix

# eclipse
-keep class org.eclipse.persistence.** {*;}
-dontwarn org.eclipse.persistence.**

# jodatime
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**

# httpclient
-keep class cz.msebera.android.httpclient.** {*;}
-dontnote cz.msebera.android.httpclient.**

# others
-keep class com.terlici.dragndroplist.** {*;}
-keep class com.nostra13.universalimageloader.** {*;}
-keep class com.loopj.android.** {*;}
-keep class org.w3c.dom.bootstrap.** {*;}
-dontwarn org.w3c.dom.bootstrap.**
