package com.innovate.crashlogger

import io.sentry.core.SentryClient
import io.sentry.core.SentryEvent

class CrashExceptionHandler(var client: SentryClient) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        client.captureEvent(SentryEvent(e))
    }
}