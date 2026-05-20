package com.example.obrona

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlin.random.Random


class MyWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        CoroutineScope(Dispatchers.IO).launch {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "REFRESH_ACTION") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MyWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            CoroutineScope(Dispatchers.IO).launch {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val question = getRandomQuestion(context)
    val views = RemoteViews(context.packageName, R.layout.my_widget)

    views.setTextViewText(R.id.appwidget_text, question?.title ?: "Błąd")
    views.setTextViewText(R.id.appwidget_desc, question?.shortDesc ?: "Kliknij, aby wylosować")

    val intent = Intent(context, MyWidget::class.java).apply {
        action = "REFRESH_ACTION"
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun getRandomQuestion(context: Context): Question? {
    return try {
        // Losujemy jeden z 3 arkuszy
        val arkuszNum = Random.nextInt(1, 4)
        val fileName = "arkusz$arkuszNum.txt"

        val lines = context.assets.open(fileName).bufferedReader(Charsets.UTF_8).readLines()
        val cleanLines = lines.filter { it.trim().isNotEmpty() }

        if (cleanLines.isNotEmpty()) {
            val randomLine = cleanLines.random().replace("\uFEFF", "")
            val parts = randomLine.split("\t")
            if (parts.size >= 3) {
                Question(parts[0].trim(), parts[1].trim(), parts[2].trim())
            } else null
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}