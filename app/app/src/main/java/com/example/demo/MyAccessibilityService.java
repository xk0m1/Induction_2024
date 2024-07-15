package com.example.demo;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {

    private final Set<String> loggedNodes = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();
        List<CharSequence> s = event.getText();

        try {
            handleNodeInfo(getRootInActiveWindow(), 0, event.getPackageName().toString());
        } catch (Exception e) {
            Log.d("nodeinfo", "Error: " + e.getMessage());
        }

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                logEvent(s, "single click", event.getPackageName().toString());
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                logEvent(s, "view focused", event.getPackageName().toString());
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                logEvent(s, "long click", event.getPackageName().toString());
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                logEvent(s, "view scrolled", event.getPackageName().toString());
                logScrollEvent(event);
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                logEvent(s, "text changed", event.getPackageName().toString());
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotificationStateChanged(event);
                break;
            default:
                Log.d("data", "Unhandled event type: " + eventType);
                break;
        }
    }

    public void handleNodeInfo(AccessibilityNodeInfo node, int depth, String packageName) {
        if (node == null) {
            return;
        }

        StringBuilder indentation = new StringBuilder();
        for (int j = 0; j < depth; j++) {
            indentation.append("  ");
        }

        CharSequence nodeText = node.getText();
        CharSequence nodeContentDescription = node.getContentDescription();

        String nodeDescription = packageName + " " + node.getClassName() + " " +
                (nodeText != null ? nodeText : "") + " " +
                (nodeContentDescription != null ? nodeContentDescription : "");

        if ((!isEmpty(nodeText) || !isEmpty(nodeContentDescription)) && loggedNodes.add(nodeDescription)) {
            StringBuilder sb = new StringBuilder(indentation);
            sb.append(nodeDescription);

            Log.d("nodeinfo", sb.toString());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            handleNodeInfo(node.getChild(i), depth + 1, packageName);
        }
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private void logEvent(List<CharSequence> text, String action, String packageName) {
        if (text != null && !text.isEmpty()) {
            for (CharSequence t : text) {
                Log.d("data", "App name: " + packageName + ", " + action + ": " + t.toString());
            }
        } else {
            Log.d("data", "App name: " + packageName + ", " + action + ": No text");
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
        } else {
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
