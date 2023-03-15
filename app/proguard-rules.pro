-dontskipnonpubliclibraryclassmembers
-printmapping proguard.map
-renamesourcefileattribute ProGuard
-keepattributes SourceFile,LineNumberTable
-dontwarn oauth.signpost.signature.**
-ignorewarnings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 排除所有注解类
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
#Keep
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# 避免混淆泛型
-keepattributes Signature
-keepattributes *JavascriptInterface*

-keep class android.webkit.**


# 保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
# 枚举类不能被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Parcelable序列化的类不能被混淆
-keep class * implements android.os.Parcelable{
    public static final android.os.Parcelable$Creator *;
}
#保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#去掉log
-assumenosideeffects class android.util.Log{
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

#EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# ------------------jetpack--------------------------------
# Lifecycle
# LifecycleObserver的空构造函数被proguard认为是未使用的
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
# 保留生命周期状态和事件枚举值
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}

# ViewModel
# ViewModel的空构造函数被proguard视为未使用
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-keepclassmembers class android.arch.** { *; }
-keep class android.arch.** { *; }
-dontwarn android.arch.**

# paging
-dontwarn android.arch.paging.DataSource

# Room 数据库
-dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource

# databinding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }
-dontwarn com.android.databinding.**
-keep class com.android.databinding.** { *; }

# okhttp
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn okio.**

#androidJunkCode
-keep class lk.gbw.sw.** { *; }