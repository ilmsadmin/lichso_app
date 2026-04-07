# Lịch Số - Widget Feature Documentation

## Overview
This document describes the widget implementation for the Lịch Số (Perpetual Calendar) application. Two widgets have been implemented:

1. **Calendar Widget** (Lịch Vạn Niên Widget) - Displays daily calendar information with lunar date
2. **AI Horoscope Widget** (AI Tử Vi Widget) - Displays daily fortune predictions based on user profile

## Features

### 1. Calendar Widget (Lịch Vạn Niên)

**Display Information:**
- Current month and year (Vietnamese format)
- Current year Can Chi (Năm Can Chi)
- Today's solar date (large display)
- Day of week (Vietnamese)
- Lunar date (Ngày âm lịch)
- Day Can Chi (Thiên Can Địa Chi)
- Day rating (Ngày tốt/xấu) with color coding:
  - Green: Very good/Good days
  - Orange: Average days
  - Red: Bad days
- Moon phase with emoji and name
- Current solar term (Tiết khí)

**Widget Characteristics:**
- Minimum size: 250dp × 180dp
- Resizable: Both horizontal and vertical
- Updates: Every hour (3600000ms)
- Click action: Opens main app

**Files:**
- Layout: `res/layout/widget_calendar.xml`
- Widget info: `res/xml/calendar_widget_info.xml`
- Provider: `com.lichso.app.widget.CalendarWidgetProvider`
- Background: `res/drawable/widget_background.xml`

### 2. AI Horoscope Widget (AI Tử Vi)

**Display Information:**
- Current date
- User profile information:
  - Display name
  - Zodiac animal (Con giáp)
  - Year Can Chi
- Daily fortune prediction based on:
  - Day quality score (1-10)
  - Day Can Chi
  - Personalized recommendations

**Fortune Predictions:**
- ⭐⭐⭐ Very Good Days (Score ≥ 8): Excellent luck, good for important decisions
- ⭐⭐ Good Days (Score ≥ 6): Favorable for regular activities
- ⭐ Average Days (Score ≥ 4): Moderate, avoid big decisions
- ⚠️ Bad Days (Score < 4): Unfavorable, be cautious

**Widget Characteristics:**
- Minimum size: 250dp × 200dp
- Resizable: Both horizontal and vertical
- Updates: Every 4 hours (14400000ms)
- Click action: Opens app and navigates to AI Chat screen

**Files:**
- Layout: `res/layout/widget_ai_horoscope.xml`
- Widget info: `res/xml/ai_horoscope_widget_info.xml`
- Provider: `com.lichso.app.widget.AiHoroscopeWidgetProvider`

## Technical Implementation

### Widget Architecture

```
CalendarWidgetProvider / AiHoroscopeWidgetProvider (AppWidgetProvider)
    ↓
Updates widget UI using RemoteViews
    ↓
CalendarWidgetUpdateWorker (Background Worker)
    ↓
Scheduled by CalendarWidgetScheduler using WorkManager
```

### Key Components

1. **Widget Providers:**
   - `CalendarWidgetProvider.kt` - Handles calendar widget updates
   - `AiHoroscopeWidgetProvider.kt` - Handles AI horoscope widget updates
   - Both extend `AppWidgetProvider`

2. **Background Services:**
   - `CalendarWidgetUpdateWorker.kt` - CoroutineWorker for background updates
   - `CalendarWidgetScheduler.kt` - Schedules periodic updates using WorkManager

3. **Update Schedule:**
   - Periodic updates: Every 1 hour with 15-minute flex interval
   - Triggered on:
     - App launch (in `LichSoApp.onCreate()`)
     - Device boot (can be extended)
     - Manual update requests

4. **Data Sources:**
   - `DayInfoProvider` - Provides all calendar calculations
   - `SettingsDataStore` - User profile information (for AI widget)
   - Lunar calendar calculations
   - Can Chi calculations
   - Day rating algorithms

### Update Mechanism

The widgets use a two-tier update system:

1. **System Updates:**
   - Configured in widget info XML (`updatePeriodMillis`)
   - Calendar Widget: Every hour (3600000ms)
   - AI Horoscope Widget: Every 4 hours (14400000ms)

2. **WorkManager Updates:**
   - More reliable for background updates
   - Scheduled in `LichSoApp.onCreate()`
   - Persists across device reboots (with proper configuration)

### Adding Widgets to Home Screen

Users can add widgets through:
1. Long-press on home screen
2. Select "Widgets"
3. Find "Lịch Số" app
4. Choose either:
   - "Widget hiển thị lịch vạn niên và thông tin ngày" (Calendar)
   - "Widget hiển thị lời khuyên tử vi từ AI" (AI Horoscope)
5. Drag to home screen
6. Resize as needed

## Integration Points

### AndroidManifest.xml
Both widgets are registered as receivers:

```xml
<!-- Calendar Widget -->
<receiver android:name=".widget.CalendarWidgetProvider" android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/calendar_widget_info" />
</receiver>

<!-- AI Horoscope Widget -->
<receiver android:name=".widget.AiHoroscopeWidgetProvider" android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/ai_horoscope_widget_info" />
</receiver>
```

### LichSoApp.kt
Widget scheduler initialized on app launch:

```kotlin
override fun onCreate() {
    super.onCreate()
    NotificationHelper.createChannels(this)
    scheduleWorkersFromSettings()
    // Schedule widget updates
    CalendarWidgetScheduler.scheduleWidgetUpdates(this)
}
```

## User Experience Features

### Calendar Widget
- **Visual Design:**
  - Red header with month/year and Can Chi
  - Large solar date number in red
  - Lunar information in smaller text
  - Color-coded day rating badge
  - Moon phase emoji
  - Solar term information

- **Interaction:**
  - Single tap opens main app
  - Shows today's information at a glance
  - No configuration needed

### AI Horoscope Widget
- **Visual Design:**
  - Red header with icon and date
  - User profile section with zodiac info
  - Daily prediction text (6 lines max with ellipsis)
  - Red footer button to open AI chat

- **Interaction:**
  - Tap user section or footer to open AI chat
  - Automatically loads user profile from settings
  - Shows placeholder text if profile not configured
  - Encourages user to complete profile setup

## Future Enhancements

Potential improvements for future versions:

1. **Widget Configuration:**
   - Allow users to customize displayed information
   - Color theme selection
   - Update frequency preferences

2. **Premium Features:**
   - Extended monthly calendar view widget
   - Lucky hours display
   - Multiple zodiac system support

3. **Interactive Elements:**
   - Refresh button
   - Previous/next day navigation
   - Quick access to specific features

4. **Performance:**
   - Cache day information
   - Optimize battery usage
   - Reduce update frequency during night hours

5. **AI Integration:**
   - Real-time AI predictions (requires API calls)
   - Personalized advice based on user history
   - Integration with chat message history

## Testing

To test the widgets:

1. Build and install the app:
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. Add widgets to home screen (see "Adding Widgets to Home Screen" section)

3. Verify display information:
   - Check lunar date accuracy
   - Verify Can Chi calculations
   - Test day rating display
   - Confirm moon phase emoji

4. Test interactions:
   - Tap widgets to open app
   - Verify navigation to correct screens
   - Check AI widget opens chat screen

5. Test updates:
   - Wait for scheduled updates
   - Restart device and check widget persistence
   - Change date/time and verify widget updates

## Dependencies

The widget implementation uses:
- AndroidX AppWidget libraries
- WorkManager for background updates
- Kotlin Coroutines for asynchronous operations
- DataStore for user preferences
- Existing domain models (DayInfo, DayInfoProvider)
- Lunar calendar utilities

## Troubleshooting

Common issues and solutions:

1. **Widget not updating:**
   - Check WorkManager status
   - Verify app has background permissions
   - Check battery optimization settings

2. **Widget shows "Chưa có thông tin":**
   - User profile not configured
   - Open app and set up profile in settings

3. **Widget crashes on tap:**
   - Verify MainActivity intent configuration
   - Check PendingIntent flags

4. **Layout issues:**
   - Check widget size constraints
   - Test on different launcher apps
   - Verify RemoteViews compatibility

## Related Files

**Widget Package** (`com.lichso.app.widget`):
- `CalendarWidgetProvider.kt`
- `AiHoroscopeWidgetProvider.kt`
- `CalendarWidgetUpdateWorker.kt`
- `CalendarWidgetScheduler.kt`

**Resources**:
- `res/layout/widget_calendar.xml`
- `res/layout/widget_ai_horoscope.xml`
- `res/xml/calendar_widget_info.xml`
- `res/xml/ai_horoscope_widget_info.xml`
- `res/drawable/widget_background.xml`
- `res/values/strings.xml` (widget descriptions)

**Domain**:
- `domain/DayInfoProvider.kt`
- `domain/model/DayInfo.kt`
- All calculator utilities (Can Chi, Lunar, etc.)

**Settings**:
- `ui/screen/settings/SettingsKeys.kt`
- `ui/screen/settings/SettingsViewModel.kt`

## Conclusion

The widget implementation provides users with quick access to essential calendar and horoscope information directly from their home screen. The implementation follows Android best practices for widget development and integrates seamlessly with the existing app architecture.
