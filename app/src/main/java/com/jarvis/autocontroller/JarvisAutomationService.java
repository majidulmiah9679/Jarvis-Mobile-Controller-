package com.jarvis.autocontroller;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.HashSet;
import java.util.Set;

public class JarvisAutomationService extends AccessibilityService {

    private static JarvisAutomationService instance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static JarvisAutomationService getInstance() {
        return instance;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String currentPackage = event.getPackageName().toString();

        SharedPreferences prefs = getSharedPreferences("JarvisSettings", MODE_PRIVATE);
        Set<String> selectedApps = prefs.getStringSet("selected_apps", new HashSet<String>());

        // ইউজার সেটিংসে সিলেক্ট করা অ্যাপ চেক করা
        if (selectedApps != null && selectedApps.contains(currentPackage)) {
            // উইন্ডোর কন্টেন্ট পরিবর্তন হলে নোড রিড করা বা অটো কাজ চালানো সম্ভব
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                // এক্সাম্পল: নির্দিষ্ট কোনো বাটনে ক্লিক বা কাজ
                rootNode.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {}

    // স্ক্রিনের যেকোনো কোঅর্ডিনেটে অটো ট্যাপ করার মেথড
    public void autoClick(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path clickPath = new Path();
            clickPath.moveTo(x, y);
            GestureDescription.StrokeDescription clickStroke =
                    new GestureDescription.StrokeDescription(clickPath, 0, 50);
            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(clickStroke);
            dispatchGesture(gestureBuilder.build(), null, null);
        }
    }

    // স্ক্রল ডাউন অটোমেশন মেথড
    public void autoScrollDown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path scrollPath = new Path();
            // স্ক্রিনের মাঝখান থেকে নিচ থেকে উপরে সোয়াইপ (স্ক্রল ডাউন)
            scrollPath.moveTo(500, 1500);
            scrollPath.lineTo(500, 500);
            GestureDescription.StrokeDescription scrollStroke =
                    new GestureDescription.StrokeDescription(scrollPath, 0, 300);
            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(scrollStroke);
            dispatchGesture(gestureBuilder.build(), null, null);
        }
    }

    // নোড টেক্সট বা আইডি দিয়ে ক্লিক
    public void clickByText(String text) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            for (AccessibilityNodeInfo node : rootNode.findAccessibilityNodeInfosByText(text)) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            rootNode.recycle();
        }
    }
}
