package com.zero.lib.contactlistener.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zero.lib.contactlistener.ContactHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onContactCheck(View view) {
        final HashMap<Integer, HashSet<Integer>> update = ContactHelper.getInstance(this).update();
        final Iterator<Map.Entry<Integer, HashSet<Integer>>> iterator = update.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<Integer, HashSet<Integer>> next = iterator.next();
            final Integer key = next.getKey();
            String action_name = "";
            if (key == ContactHelper.CONTACT_ADDED) {
                action_name = "added";
            } else if (key == ContactHelper.CONTACT_DELETED) {
                action_name = "deleted";
            } else if (key == ContactHelper.CONTACT_UPDATED) {
                action_name = "updated";
            }
            final Iterator<Integer> iterator1 = next.getValue().iterator();
            while (iterator1.hasNext()) {
                Log.d("MainActivity", "onContactCheck: " + action_name + ":" + iterator1.next());
            }
        }
    }
}
