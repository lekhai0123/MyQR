# Android Home Screen Widget Setup

To complete the home screen widget integration using `home_widget`, please follow these steps for the Android codebase (`android/app/src/main/`):

### 1. `AndroidManifest.xml`

Update your `android/app/src/main/AndroidManifest.xml`. Add the widget receiver definition inside your `<application>` tag:

```xml
<receiver android:name="HomeWidgetProvider" android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data android:name="android.appwidget.provider"
               android:resource="@xml/app_widget_provider" />
</receiver>
```

Also, add a `<meta-data>` tag in your `MainActivity` to allow deep linking from the widget:

```xml
<activity android:name=".MainActivity">
    <!-- Existing code... -->
    
    <!-- Deep linking for Home Widget -->
    <meta-data android:name="home_widget_action" android:value="quick_generate" />
</activity>
```

### 2. `app_widget_provider.xml`

Create a new folder `res/xml` (if it doesn't exist) and add `res/xml/app_widget_provider.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp"
    android:minHeight="40dp"
    android:updatePeriodMillis="86400000"
    android:initialLayout="@layout/widget_layout"
    android:resizeMode="horizontal"
    android:widgetCategory="home_screen" />
```

### 3. `widget_layout.xml`

Create a new folder `res/layout` (if it doesn't exist) and add `res/layout/widget_layout.xml` to design the search-bar-like widget:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="@drawable/widget_bg"
    android:orientation="horizontal"
    android:padding="12dp"
    android:id="@+id/widget_container"
    android:gravity="center_vertical">

    <ImageView
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:src="@android:drawable/ic_menu_edit"
        android:tint="#888888" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:text="Quick Transfer..."
        android:textColor="#888888"
        android:textSize="16sp" />
</LinearLayout>
```

### 4. Background Drawable (`widget_bg.xml`)

Create `res/drawable/widget_bg.xml` for the rounded 'search bar' style background:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#2A2A2A" />
    <corners android:radius="24dp" />
    <stroke android:width="1dp" android:color="#444444" />
</shape>
```

### 5. `HomeWidgetProvider.kt`

Since you are using `home_widget`, create/update `HomeWidgetProvider.kt` inside `android/app/src/main/kotlin/com/your/package/`:

```kotlin
package com.your.package // Replace with your actual package

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetBackgroundIntent
import es.antonborri.home_widget.HomeWidgetLaunchIntent
import es.antonborri.home_widget.HomeWidgetProvider

class HomeWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences
    ) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_layout).apply {
                // Clicking the widget launches the app
                val pendingIntent = HomeWidgetLaunchIntent.getActivity(
                    context,
                    MainActivity::class.java
                )
                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
```

Now, tapping the widget on the Android home screen will instantly open the app focusing on the numeric text input.
