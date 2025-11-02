# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# Reference: https://github.com/Kotlin/kotlinx.coroutines/blob/13f27f729547e5c22d17d5b5de3582d450b037b4/kotlinx-coroutines-core/jvm/resources/META-INF/com.android.tools/proguard/coroutines.pro

-printmapping build/proguard-mapping.txt

# Keep Ktor classes
-keep class io.ktor.** { *; }
-dontnote io.ktor.**
-dontwarn io.ktor.**
-keep class kotlinx.io.** { *; }
-dontwarn kotlinx.io.**

# Keep all DTO classes in the package
-keep class com.meet.dev.analyzer.data.models.** { *; }
-dontnote com.meet.dev.analyzer.data.models.**

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
-keepnames class kotlinx.serialization.internal.** { *; }

# Do not warn about missing annotations and metadata
-dontwarn kotlin.Metadata
-dontwarn kotlin.jvm.internal.**
-dontwarn kotlin.reflect.jvm.internal.**

# Keep necessary Kotlin attributes
-keepattributes Signature, *Annotation*

# Logging classes, if logging is required
-keep class org.slf4j.** { *; }
-keep class org.slf4j.impl.** { *; }
-keep class ch.qos.logback.** { *; }
-dontwarn org.slf4j.**

# Keep the entire MacOSThemeDetector class and its nested classes
-keep class com.jthemedetecor.** { *; }
-keep class com.jthemedetecor.MacOSThemeDetector$* { *; }

# Annotated interfaces (including methods which are also kept in implementing classes)
-keepattributes Annotation
-keepattributes *Annotation*


# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# These classes are only required by kotlinx.coroutines.debug.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn sun.misc.SignalHandler
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.Signal

# Only used in `kotlinx.coroutines.internal.ExceptionsConstructor`.
# The case when it is not available is hidden in a `try`-`catch`, as well as a check for Android.
-dontwarn java.lang.ClassValue

# Additional dontwarn rules for common issues
-dontwarn java.awt.**
-dontwarn javax.annotation.**