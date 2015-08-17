package app.morningsignout.com.morningsignoff;

/**
 * Created by Daniel on 6/20/2015.
 */
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class HeadlineArtProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private HeadlineDBHelper mOpenHelper;

    static final int H_ARTICLES = 1;
    static final int H_ARTICLES_INDEX = 5;

    private static final SQLiteQueryBuilder sH_articlesQueryBuilder;

    static{
        sH_articlesQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sH_articlesQueryBuilder.setTables(
                HeadlineArtContract.H_articleEntry.TABLE_NAME);
    }

//    private Cursor getH_articles() {
//        return sH_articlesQueryBuilder.query(mOpenHelper.getReadableDatabase(),
//                null,
//                null,
//                null,
//                null,
//                null,
//                "_id ASC"
//        );
//    }

    private Cursor getH_articlesIndex(Uri uri, String[] projection, String sortOrder) {
        long index = HeadlineArtContract.H_articleEntry.getIndexFromUri(uri);

        return sH_articlesQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                "_id = ?",
                new String[] {Long.toString(index)},
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.
        uriMatcher.addURI(HeadlineArtContract.CONTENT_AUTHORITY, "hArticles", H_ARTICLES);
        uriMatcher.addURI(HeadlineArtContract.CONTENT_AUTHORITY, "hArticles/#", H_ARTICLES_INDEX);

        // 3) Return the new matcher!
        return uriMatcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new HeadlineDBHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case H_ARTICLES:
                return HeadlineArtContract.H_articleEntry.CONTENT_TYPE;
            case H_ARTICLES_INDEX:
                return HeadlineArtContract.H_articleEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "h_articles"
            case H_ARTICLES:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        HeadlineArtContract.H_articleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "h_articles/#"
            case H_ARTICLES_INDEX: {
                retCursor = getH_articlesIndex(uri, projection, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case H_ARTICLES: {
                long _id = db.insert(HeadlineArtContract.H_articleEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = HeadlineArtContract.H_articleEntry.buildHArticlesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        int retDelRows;

        if (selection == null) selection = "1";
        switch (match) {
            case H_ARTICLES: {
                retDelRows = db.delete(HeadlineArtContract.H_articleEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (retDelRows != 0) getContext().getContentResolver().notifyChange(uri, null);

        // Student: return the actual rows deleted
        return retDelRows;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        // Student: Start by getting a writable database
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.
        final int match = sUriMatcher.match(uri);
        int retUpdRows;

        switch (match) {
            case H_ARTICLES: {
                retUpdRows = db.update(HeadlineArtContract.H_articleEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Student: A null value updates all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsUpdated != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (retUpdRows != 0) getContext().getContentResolver().notifyChange(uri, null);

        // Student: return the actual rows deleted
        return retUpdRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case H_ARTICLES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(
                                HeadlineArtContract.H_articleEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}

