# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
