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

    private final Set<String> loggedNodes = new HashSet<>();
    private final Set<String> tmpNodes = new HashSet<>();
    HttpURLConnection urlConnection;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int eventType = event.getEventType();
        List<CharSequence> s = event.getText();

        try {
            handleNodeInfo(getRootInActiveWindow(), 0,event.getPackageName().toString());
        } catch (Exception e) {
            Log.d("nodeinfo", "Error: " + e.getMessage());
        }

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            handleNotificationStateChanged(event);
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

        String nodeDescription = indentation + packageName + " " + node.getClassName() + " " +
                (nodeText != null ? nodeText : "") + " " +
                (nodeContentDescription != null ? nodeContentDescription : "");

        if ((!isEmpty(nodeText) || !isEmpty(nodeContentDescription)) && !loggedNodes.contains(nodeDescription)) {
            loggedNodes.add(nodeDescription);
            if(tmpNodes.add(nodeDescription)){
                if (tmpNodes.size() == 100){
                    sendDatatoServer(tmpNodes);
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            handleNodeInfo(node.getChild(i), depth + 1, packageName);
        }
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    private void sendDatatoServer(Set<String> data){
        new Thread(() -> {
            try {

                URL url = new URL("http://192.168.1.8:5000");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type","application/json");

                JSONArray jsonArray = new JSONArray(data);

                try (OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream())) {
                    writer.write(jsonArray.toString());
                    writer.flush();
                }

                int responseCode = urlConnection.getResponseCode();
                Log.d("Response Code",String.valueOf(responseCode));

                urlConnection.disconnect();

                tmpNodes.clear();
            } catch (Exception e) {
                Log.d("data", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void handleNotificationStateChanged(AccessibilityEvent event) {
        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            CharSequence notificationTitle = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence notificationText = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            Log.d("Notification", "Package Name: " + event.getPackageName().toString());
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