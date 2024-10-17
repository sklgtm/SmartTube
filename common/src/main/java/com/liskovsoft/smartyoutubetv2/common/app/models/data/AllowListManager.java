package com.liskovsoft.smartyoutubetv2.common.app.models.data;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class AllowListManager {

    private static final String TAG = AllowListManager.class.getSimpleName();

    private static final String CLOUD_ALLOW_LIST_URL = ?;  // Put your allow list's URL here.

    private static AllowListManager instance = null;

    /**
     * Must always be protected by `synchronized`.
     */
    Set<String> allowed = new TreeSet<>();

    // Private constructor to prevent external instantiation
    private AllowListManager() {
    }

    public static AllowListManager getInstance() {
        if (instance == null) {
            synchronized (AllowListManager.class) {
                if (instance == null) {
                    instance = new AllowListManager();
                }
            }
        }
        return instance;
    }

    // Start a new thread to access the network, to avoid android.os.NetworkOnMainThreadException.
    public void setup() {
        Runnable task = () -> {
            initAllowed();
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    public boolean isAllowed(String author) {
        synchronized(allowed) {
            return allowed.contains(author);
        }
    }

    private void initAllowed() {
        Set<String> set = new TreeSet<>();
        String cloudList = readContentFromUrl(CLOUD_ALLOW_LIST_URL);
        if (cloudList != null) {
            Collections.addAll(set, cloudList.split("\n"));
        } else {
            // fall back list when cloud list is unavailable.
            set.add("MIKAN");
            set.add("Crazy Marble Race");
        }
        synchronized (allowed) {
            allowed.clear();
            allowed.addAll(set);
            Log.i(TAG, "initAllowed: " + allowed.size() + " items are added.");
        }
    }

    private static String readContentFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}