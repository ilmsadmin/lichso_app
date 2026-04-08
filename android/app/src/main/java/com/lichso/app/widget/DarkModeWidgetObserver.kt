package com.lichso.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentCallbacks2
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Singleton that detects dark-mode changes and instantly updates ClockWidget2.
 *
 * Uses TWO mechanisms for reliability:
 *  1. ComponentCallbacks2 — fires instantly when configuration changes while process alive.
 *  2. SharedPreferences persistence — survives process death. On ensureRegistered(),
 *     compares current night mode with the last saved value. If they differ (process was
 *     killed then restarted in a new mode), triggers an immediate widget refresh.
 */
object DarkModeWidgetObserver {

    private const val TAG = "DarkModeObserver"
    private const val PREFS_NAME = "widget_dark_mode"
    private const val KEY_LAST_NIGHT = "last_night_mode"

    @Volatile private var registered = false
    private var lastNightMode: Int = -1

    private val callbacks = object : ComponentCallbacks2 {
        override fun onConfigurationChanged(newConfig: Configuration) {
            val newNight = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
            Log.d(TAG, "onConfigurationChanged: night=0x${Integer.toHexString(newNight)}, last=0x${Integer.toHexString(lastNightMode)}")
            if (newNight != lastNightMode) {
                lastNightMode = newNight
                saveNightMode(newNight)
                val isDark = newNight == Configuration.UI_MODE_NIGHT_YES
                Log.d(TAG, "Dark mode changed → isDark=$isDark, refreshing widgets")
                refreshClockWidget2(isDark)
                refreshCalendarWidget(isDark)
                refreshAiWidget(isDark)
                refreshMonthCalendarWidget(isDark)
                // Also schedule a delayed retry in case the first update was too early
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d(TAG, "Delayed retry refresh, isDark=$isDark")
                    refreshClockWidget2(isDark)
                    refreshCalendarWidget(isDark)
                    refreshAiWidget(isDark)
                    refreshMonthCalendarWidget(isDark)
                }, 500)
            }
        }

        override fun onLowMemory() {}
        override fun onTrimMemory(level: Int) {}
    }

    private var appContext: Context? = null

    /**
     * Call this to ensure the observer is active. Safe to call multiple times;
     * it will only register once per process.
     *
     * Also checks if dark mode changed while process was dead (by comparing
     * current config with the value persisted in SharedPreferences).
     */
    @Synchronized
    fun ensureRegistered(context: Context) {
        val appCtx = context.applicationContext
        appContext = appCtx

        val currentNight = appCtx.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK

        // Check persisted value — detects changes that happened while process was dead
        val prefs = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedNight = prefs.getInt(KEY_LAST_NIGHT, -1)

        if (savedNight != -1 && savedNight != currentNight) {
            // Dark mode changed while we were dead → update widgets NOW
            Log.d(TAG, "Night mode changed while dead: saved=0x${Integer.toHexString(savedNight)} → current=0x${Integer.toHexString(currentNight)}")
            val isDark = currentNight == Configuration.UI_MODE_NIGHT_YES
            refreshClockWidget2(isDark)
            refreshCalendarWidget(isDark)
            refreshAiWidget(isDark)
            refreshMonthCalendarWidget(isDark)
        }

        // Always persist current state
        lastNightMode = currentNight
        saveNightMode(currentNight)

        if (!registered) {
            try {
                appCtx.registerComponentCallbacks(callbacks)
                registered = true
                Log.d(TAG, "Registered ComponentCallbacks2, night=0x${Integer.toHexString(currentNight)}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register callbacks", e)
            }
        }
    }

    private fun saveNightMode(nightMode: Int) {
        val ctx = appContext ?: return
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_LAST_NIGHT, nightMode)
            .apply()
    }

    private fun refreshClockWidget2(isDark: Boolean) {
        val ctx = appContext ?: return
        try {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, ClockWidget2::class.java))
            if (ids.isNotEmpty()) {
                Log.d(TAG, "Updating ${ids.size} ClockWidget2 instance(s), isDark=$isDark")
                for (id in ids) {
                    ClockWidget2.updateWidget(ctx, mgr, id, forceDark = isDark)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error refreshing ClockWidget2", e)
        }
    }

    private fun refreshCalendarWidget(isDark: Boolean) {
        val ctx = appContext ?: return
        try {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, CalendarWidget::class.java))
            if (ids.isNotEmpty()) {
                Log.d(TAG, "Updating ${ids.size} CalendarWidget instance(s), isDark=$isDark")
                for (id in ids) {
                    CalendarWidget.updateWidget(ctx, mgr, id, forceDark = isDark)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error refreshing CalendarWidget", e)
        }
    }

    private fun refreshAiWidget(isDark: Boolean) {
        val ctx = appContext ?: return
        try {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, AiWidget::class.java))
            if (ids.isNotEmpty()) {
                Log.d(TAG, "Updating ${ids.size} AiWidget instance(s), isDark=$isDark")
                for (id in ids) {
                    AiWidget.updateWidget(ctx, mgr, id, forceDark = isDark)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error refreshing AiWidget", e)
        }
    }

    private fun refreshMonthCalendarWidget(isDark: Boolean) {
        val ctx = appContext ?: return
        try {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, MonthCalendarWidget::class.java))
            if (ids.isNotEmpty()) {
                Log.d(TAG, "Updating ${ids.size} MonthCalendarWidget instance(s), isDark=$isDark")
                for (id in ids) {
                    MonthCalendarWidget.updateWidget(ctx, mgr, id, forceDark = isDark)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error refreshing MonthCalendarWidget", e)
        }
    }
}
