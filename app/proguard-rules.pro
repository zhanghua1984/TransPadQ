# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\developer\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*
-keep public class * extends android.view.View
-keep public class * extends android.view.ViewGroup
-keep public class * extends android.widget.LinearLayout
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider
-keep class * implements java.io.Serializable
-keep class * extends cn.transpad.transpadui.http.Rst
-keep class * extends cn.transpad.transpadui.http.Rst { *;}
-keep class * implements java.io.Serializable { *;}
-dontwarn cn.transpad.transpadui.view.**
-keep public class cn.transpad.transpadui.view.**
-keep class cn.transpad.transpadui.view.** { *;}

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# Player核心库不能混淆
-dontwarn com.fone.**
-keep public class com.fone.**
-keep class com.fone.** { *; }


# 投放核心库的混淆
-dontwarn cn.transphone.utp.**
-keep public class cn.transphone.utp.**
-keep class cn.transphone.utp.** { *;}
-keep interface cn.transphone.utp.** { *;}


# EventBus 混淆处理
-dontwarn de.greenrobot.event.**
-keep class de.greenrobot.event.** { *; }
-keep class de.greenrobot.event.** { *; }
-keep interface de.greenrobot.event.** { *; }
# EventBus的回调方法不混淆
-keepclassmembers public class * {
  void onEvent*(***);
}

# butterknife 组件混淆处理
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }
# -keep class * { @butterknife.InjectView *;}
-keepnames class * extends android.app.Activity{
    @butterknife.** *;
}
-keepnames class * extends android.app.Fragment{
    @butterknife.** *;
}
-keepnames class * extends android.app.Dialog{
    @butterknife.** *;
}
-keepnames class cn.transpad.transpadui.adapter.**{
    @butterknife.** *;
}
-keepnames class * extends android.app.Activity{
    @butterknife.** *;
}
-keep public class butterknife.**
-keep public class butterknife.internal.**
-keep class butterknife.** { *; }
-keep class butterknife.internal.** { *; }
-keep interface butterknife.internal.**

# retrofit 组件混淆处理
-dontwarn retrofit.**
-keep public class retrofit.**
-keep class retrofit.** { *; }
-keep interface retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# okhttp 组件混淆处理
-dontwarn com.squareup.okhttp.**
-keep public class com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn okio.**
-keep public class okio.**
-keep class okio.** { *; }
-keep interface okio.** { *; }


# jaudiotagger 混淆处理
-dontwarn org.jaudiotagger.**
-keep public class org.jaudiotagger.**
-keep class org.jaudiotagger.**
-keep class org.jaudiotagger.** { *; }
-keep interface org.jaudiotagger.** { *; }

#百度定位 混淆处理
-dontwarn com.baidu.**
-keep public class com.baidu.**
-keep class com.baidu.** { *; }
-keep interface com.baidu.** { *; }

# gson 混淆处理
-dontwarn com.google.**
-keep public class com.google.**
-keep class com.google.** { *; }
-keep interface com.google.** { *; }

#simpleframe xml 混淆处理
-dontwarn org.simpleframework.**
-keep public class org.simpleframework.**
-keep class cn.transpad.transpadui.http.SimpleXMLConverter
-keep class org.simpleframework.** { *; }
-keep interface org.simpleframework.** { *; }

# 云知声混淆处理
-dontwarn cn.yunzhisheng.**
-keep public class cn.yunzhisheng.**
-keep class cn.yunzhisheng.** { *; }
-keep interface cn.yunzhisheng.** { *; }

# 拖动排序组件混淆处理
-dontwarn ca.laplanete.mobile.**
-keep public class ca.laplanete.mobile.**
-keep class ca.laplanete.mobile.** { *; }
-keep interface ca.laplanete.mobile.** { *; }

# all shrare(三星的屏幕共享) 包的混淆处理
-dontwarn android.view.**
-keep public class android.view.**
-keep class android.view.** { *; }
-dontwarn com.sec.android.**
-keep public class com.sec.android.**
-keep class com.sec.android.** { *; }
-keep interface com.sec.android.** { *; }
-keep interface android.view.** { *; }

# HttpclientAndroid 包的混淆处理
-dontwarn com.belladati.httpclientandroidlib.**
-keep public class com.belladati.httpclientandroidlib.**
-keep class com.belladati.httpclientandroidlib.** { *; }
-keep interface com.belladati.httpclientandroidlib.** { *; }

#PhotoView 包的混淆处理
-dontwarn uk.co.senab.**
-dontwarn com.example.photoview.**
-keep public class uk.co.senab.**
-keep class uk.co.senab.** { *; }
-keep public class com.example.photoview.**
-keep class com.example.photoview.** { *; }
-keep interface com.example.photoview.** { *; }
-keep interface uk.co.senab.** { *; }

#Universal Imageloader 混淆处理
-dontwarn com.nostra13.universalimageloader.**
-keep public class com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.** { *; }
-keep interface com.nostra13.universalimageloader.** { *; }

# 图片模糊模块混淆处理
-dontwarn com.enrique.stackblur.**
-keep public class com.enrique.stackblur.**
-keep class com.enrique.stackblur.** { *; }
-keep interface com.enrique.stackblur.** { *; }


#picasso 混淆处理 (这个包工程里没有，可能是其它的包引用的)
-dontwarn com.squareup.picasso.**
-keep public class com.squareup.picasso.**
-keep class com.squareup.picasso.** { *; }
-keep interface com.squareup.picasso.** { *; }

#LibDLNA 混淆处理
-dontwarn org.kxml2.**
-keep public class org.kxml2.**
-keep class org.kxml2.** { *; }
-keep interface org.kxml2.** { *; }
-dontwarn org.xmlpull.**
-keep public class org.xmlpull.**
-keep class org.xmlpull.** { *; }
-keep interface org.xmlpull.** { *; }
-dontwarn cn.transpad.dlna.**
-keep public class cn.transpad.dlna.**
-keep class cn.transpad.dlna.** { *; }
-keep interface cn.transpad.dlna.** { *; }
-dontwarn fi.iki.elonen.**
-keep public class fi.iki.elonen.**
-keep class fi.iki.elonen.** { *; }
-keep interface fi.iki.elonen.** { *; }
-dontwarn META-INFO.nanohttpd.**
-dontwarn org.cybergarage.**
-keep public class org.cybergarage.**
-keep class org.cybergarage.** { *; }
-keep interface org.cybergarage.** { *; }

#sohu SDK混淆
-dontwarn org.cybergarage.**
-keep public class com.admaster.**
-keep class com.admaster.** { *; }
-keep public class com.google.gson.**
-keep class com.google.gson.** { *; }
-keep public class com.miaozhen.adtracking.sohu.**
-keep class com.miaozhen.adtracking.sohu.** { *; }
-keep public class com.sohu.**
-keep class com.sohu.** { *; }
-dontwarn com.sohuvideo.**
-keep public class com.sohuvideo.**
-keep class com.sohuvideo.** { *; }

#letv SDK混淆
-dontwarn com.letv.**
-keep public class com.avdmg.**
-keep class com.avdmg.** { *; }
-keep public class com.loopj.**
-keep class com.loopj.** { *; }
-keep public class cn.com.iresearch.**
-keep class cn.com.iresearch.** { *; }
-keep public class com.letv.ads.**
-keep class com.letv.ads.** { *; }
-keep public class com.letv.datastatistics.**
-keep class com.letv.datastatistics.** { *; }
-dontwarn com.media.**
-keep public class com.media.**
-keep class com.media.** { *; }
-keep public class cn.mmachina.**
-keep class cn.mmachina.** { *; }
-keep public class cn.com.mma.mobile.tracking.**
-keep class cn.com.mma.mobile.tracking.** { *; }
-keep public class com.letv.adlib.**
-keep class com.letv.adlib.** { *; }
-keep public class com.letv.sdk.onehundredtv.video.**
-keep class com.letv.sdk.onehundredtv.video.** { *; }
-keep public class cn.com.iresearch.mapptracker.**
-keep class cn.com.iresearch.mapptracker.** { *; }
-keep public class com.miaozhen.monitor.**
-keep class com.miaozhen.monitor.** { *; }
-keep public class com.android.letvmanager.**
-keep class com.android.letvmanager.** { *; }
-keep public class com.letv.pp.**
-keep class com.letv.pp.** { *; }
-keep public class android.os.**
-keep class android.os.** { *; }


# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}


# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class * implements android.content.BroadcastReceiver {
  public void *(android.view.View);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**