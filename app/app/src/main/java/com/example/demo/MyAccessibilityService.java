package com.example.demo;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();
        List<CharSequence> s = event.getText();

        try{
            handleNodeInfo(getRootInActiveWindow(),0);
        }catch (Exception e){
            Log.d("nodeinfo", "Error: " + e.getMessage());
        }

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                logEvent(s, "single click");
                Log.d("data", "App name: " + event.getContentDescription());
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                logEvent(s, "view focused");
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                logEvent(s, "long click");
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                logEvent(s, "view scrolled");
                logScrollEvent(event);
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                logEvent(s, "text changed");
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotificationStateChanged(event);
                break;
            default:
                Log.d("data", "Unhandled event type: " + eventType);
                break;
        }
    }

    public void handleNodeInfo(AccessibilityNodeInfo n,int i) {
        if (n == null) {
            Log.d("nodeinfo", "Node is null");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append("  ");
        }

        sb.append(n.getClassName());
        sb.append(" ");
        sb.append(n.getText());
        sb.append(" ");
        sb.append(n.getContentDescription());
        sb.append(" ");

        Log.d("nodeinfo", sb.toString());

        for (int j = 0; j < n.getChildCount(); j++) {
            handleNodeInfo(n.getChild(j), i + 1);
        }
    }

    private void logEvent(List<CharSequence> text, String action) {
        if (text != null && !text.isEmpty()) {
            for (CharSequence t : text) {
                Log.d("data", t.toString());
                Log.d("data", action);
            }
        }
    }

    private void logScrollEvent(AccessibilityEvent event) {
        Log.d("ScrollEvent", "Event Type: " + AccessibilityEvent.eventTypeToString(event.getEventType()));
        Log.d("ScrollEvent", "Package Name: " + event.getPackageName());
        Log.d("ScrollEvent", "Class Name: " + event.getClassName());
        Log.d("ScrollEvent", "Item Count: " + event.getItemCount());
        for (CharSequence text : event.getText()) {
            Log.d("ScrollEvent", "Event Text: " + text);
        }
    }

    private void handleNotificationStateChanged(AccessibilityEvent event) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            CharSequence notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

            Log.d("Notification", "Notification Title: " + notificationTitle);
            Log.d("Notification", "Notification Text: " + notificationText);
        }

        else {
            List<CharSequence> notificationText = event.getText();
            if (!notificationText.isEmpty()) {
                for (CharSequence t : notificationText) {
                    Log.d("Notification", "Notification Text: " + t.toString());
                }
            } else {
                Log.d("Notification", "Notification state changed but no text available.");
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("MyAccessibilityService", "Service interrupted");
    }


    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d("MyAccessibilityService", "Service connected");
    }

}