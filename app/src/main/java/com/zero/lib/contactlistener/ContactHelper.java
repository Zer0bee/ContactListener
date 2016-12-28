package com.zero.lib.contactlistener;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.zero.lib.contactlistener.database.HashDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Manish on 27/12/16.
 */

public class ContactHelper {

    Context context;
    private static final String TAG = "ContactHelper";
    public static final String delimiter = String.valueOf(((char) 007));

    public static int CONTACT_ADDED = 0;
    public static int CONTACT_DELETED = 1;
    public static int CONTACT_UPDATED = 2;

    private static ContactHelper mInstance;

    public static ContactHelper getInstance(Context context) {
        if(mInstance == null){
            mInstance = new ContactHelper(context);
        }
        return mInstance;
    }

    private ContactHelper(){
    }

    private ContactHelper(Context context) {
        this.context = context;
    }

    private final Cursor getContactDisplayDataCursor() {
        return context.getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts.CONTACT_ID,
                        ContactsContract.RawContacts.VERSION,
                        ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.RawContacts.TIMES_CONTACTED,
                        ContactsContract.RawContacts.LAST_TIME_CONTACTED,
                        ContactsContract.RawContacts.STARRED,
                        ContactsContract.RawContacts.RAW_CONTACT_IS_USER_PROFILE},
                ContactsContract.RawContacts.DELETED + " = 0",
                null,
                ContactsContract.RawContacts.CONTACT_ID + " ASC, " +
                        ContactsContract.RawContacts.VERSION + " ASC"
        );
    }

    public HashMap<Integer, HashSet<Integer>> update() {
        HashMap<Integer, HashSet<Integer>> changedContacts = new HashMap<>();
        HashMap<String, String> contentProviderDisplayNameAndBoostLatestDataLiteral = null;
        StringBuilder sb_sb = new StringBuilder();
        StringBuilder sbOne = new StringBuilder();

        Cursor c = null;
        try {
            c = getContactDisplayDataCursor();
            if (c.moveToFirst()) {
                final int cursorCount = c.getCount();
                contentProviderDisplayNameAndBoostLatestDataLiteral = new HashMap<String, String>(cursorCount);
                do {
                    final String contact_id = c.getString(0);
                    final String displayName = c.getString(2);
                    if (contact_id == null || displayName == null) {
                        continue;
                    }

                    String version = c.getString(1);
                    String timesContacted = c.getString(3);
                    String lastTimeContacted = c.getString(4);
                    String starred = c.getString(5);

                    starred = starred == null ? "0" : starred;
                    timesContacted = timesContacted == null ? "0" : timesContacted;
                    lastTimeContacted = lastTimeContacted == null ? "0" : lastTimeContacted;
                    version = version == null ? "0" : version;

                    sbOne.setLength(0);
                    sbOne.append(starred);
                    sbOne.append(delimiter);
                    sbOne.append(timesContacted);
                    sbOne.append(delimiter);
                    sbOne.append(lastTimeContacted);
                    sbOne.append(delimiter);
                    sbOne.append(version);
                    sbOne.append(delimiter);
                    sbOne.append(displayName);

                    if (!contentProviderDisplayNameAndBoostLatestDataLiteral.containsKey(contact_id)) {
                        contentProviderDisplayNameAndBoostLatestDataLiteral.put(contact_id, sbOne.toString());
                    } else {
                        // TO DO JUST use version number to determine what one to use --test
                        final String nameAndBoostLiteral = contentProviderDisplayNameAndBoostLatestDataLiteral.get(contact_id);
                        final String[] data = nameAndBoostLiteral.split(delimiter, 5);

                        int starredi = Integer.valueOf(starred);
                        long timesContactedi = Long.valueOf(timesContacted);
                        long lastTimeContactedi = Long.valueOf(lastTimeContacted);
                        int versioni = Integer.valueOf(version);

                        final int literal_starred = Integer.valueOf(data[0]);
                        final long literal_timesContacted = Long.valueOf(data[1]);
                        final long literal_lastTimeContacted = Long.valueOf(data[2]);
                        final int literal_version = Integer.valueOf(data[3]);

                        starredi = literal_starred > starredi ? literal_starred : starredi;
                        timesContactedi = literal_timesContacted > timesContactedi ? literal_timesContacted : timesContactedi;
                        lastTimeContactedi = literal_lastTimeContacted > lastTimeContactedi ? literal_lastTimeContacted : lastTimeContactedi;
                        versioni = literal_version > versioni ? literal_version : versioni;

                        sbOne.setLength(0);
                        sbOne.append(String.valueOf(starredi));
                        sbOne.append(delimiter);
                        sbOne.append(String.valueOf(timesContactedi));
                        sbOne.append(delimiter);
                        sbOne.append(String.valueOf(lastTimeContactedi));
                        sbOne.append(delimiter);
                        sbOne.append(String.valueOf(versioni));
                        sbOne.append(delimiter);
                        sbOne.append(displayName);
                        contentProviderDisplayNameAndBoostLatestDataLiteral.put(contact_id, sbOne.toString());
                    }
                } while (c.moveToNext());
            }
        } finally {
            if (c != null)
                c.close();
        }

        contentProviderDisplayNameAndBoostLatestDataLiteral = contentProviderDisplayNameAndBoostLatestDataLiteral == null ? new HashMap<String, String>(0) : contentProviderDisplayNameAndBoostLatestDataLiteral;

        HashMap<String, String> contentProviderDisplayNameAndBoostLatestData = new HashMap<String, String>(contentProviderDisplayNameAndBoostLatestDataLiteral.size());
        for (String idKey : contentProviderDisplayNameAndBoostLatestDataLiteral.keySet()) {
            String[] verdata = contentProviderDisplayNameAndBoostLatestDataLiteral.get(idKey).split(delimiter, 5);
            contentProviderDisplayNameAndBoostLatestData.put(idKey, idKey + delimiter + verdata[3]);
        }

        HashSet<String> currentIncrementalDataSnapShot = new HashSet<String>();

        final Set<String> idKeySet = contentProviderDisplayNameAndBoostLatestData.keySet();
        for (String contactId : idKeySet) {

            sb_sb.setLength(0);
            sb_sb.append(contentProviderDisplayNameAndBoostLatestData.get(contactId));

            currentIncrementalDataSnapShot.add(sb_sb.toString());
        }

        HashSet<String> latestIncrementalDataSnapShot = HashDatabase.getInstance(context).getContactHashData();

        HashSet<String> additions = new HashSet<String>();
        additions.addAll(currentIncrementalDataSnapShot);
        additions.removeAll(latestIncrementalDataSnapShot);

        HashSet<String> deletions = new HashSet<String>();
        deletions.addAll(latestIncrementalDataSnapShot);
        deletions.removeAll(currentIncrementalDataSnapShot);

        final int sizeOfAdditions = additions.size();
        final int sizeOfDeletions = deletions.size();

        List<Integer> addedContacts;
        if (sizeOfAdditions > 0) {
            addedContacts = new ArrayList<>(sizeOfDeletions);
            for (String data : additions) {
                String[] datums = data.split(delimiter, 2);
                int addedEvent = Integer.parseInt(datums[0]);
                addedContacts.add(addedEvent);
            }
        } else {
            addedContacts = new ArrayList<>();
        }

        List<Integer> deletedContacts;
        if (sizeOfDeletions > 0) {
            deletedContacts = new ArrayList<>(sizeOfDeletions);
            for (String data : deletions) {
                String[] datums = data.split(delimiter, 2);
                int deletedEvent = Integer.parseInt(datums[0]);
                deletedContacts.add(deletedEvent);
            }
        } else {
            deletedContacts = new ArrayList<>();
        }

        HashSet<Integer> contactsAdded = new HashSet<>();
        contactsAdded.addAll(addedContacts);
        contactsAdded.removeAll(deletedContacts);
        changedContacts.put(CONTACT_ADDED, contactsAdded);

        HashSet<Integer> contactsDeleted = new HashSet<>();
        contactsDeleted.addAll(deletedContacts);
        contactsDeleted.removeAll(addedContacts);
        changedContacts.put(CONTACT_DELETED, contactsDeleted);

        HashSet<Integer> contactsUpdated = new HashSet<>();
        contactsUpdated.addAll(addedContacts);
        contactsUpdated.retainAll(deletedContacts);
        changedContacts.put(CONTACT_UPDATED, contactsUpdated);

        latestIncrementalDataSnapShot.removeAll(deletions);
        latestIncrementalDataSnapShot.addAll(additions);

        HashDatabase.getInstance(context).setContactHashData(latestIncrementalDataSnapShot);

        return changedContacts;
    }

}
