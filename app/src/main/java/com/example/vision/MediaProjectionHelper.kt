package com.example.vision

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager

/**
 * MediaProjection Screen Capture pipeline helper for J.A.R.V.I.S.
 * Sets up screen recording / projection authorization intent.
 */
class MediaProjectionHelper(private val context: Context) {

    private val mediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager

    fun createScreenCaptureIntent(): Intent? {
        return mediaProjectionManager?.createScreenCaptureIntent()
    }
}
