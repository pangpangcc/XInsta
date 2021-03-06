package com.ihelp101.instagram;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Module implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit, Listen {

    Boolean hookCheck;
    CharSequence[] mMenuOptions = null;
    CharSequence[] mDirectShareMenuOptions = null;
    CharSequence[] mProfileOptions = null;
    public static Context mContext;
    public static Context nContext;
    Object mCurrentMediaOptionButton;
    Object mCurrentDirectShareMediaOptionButton;
    Object mUserName;
    Object model;

    String directShareCheck = "Nope";
    String fileType;
    String firstHook;
    String Hooks = null;
    String[] HooksArray;
    String HookCheck = "No";
    String HooksSave;
    String linkToDownload;
    String fileName;
    String notificationTitle;
    String oldCheck = "No";
    String userName;
    String version = "123";

    String ACCOUNT_HOOK_CLASS;
    String COMMENT_HOOK;
    String COMMENT_HOOK_CLASS;
    String COMMENT_HOOK_CLASS2;
    String DS_DIALOG_CLASS;
    String DS_MEDIA_OPTIONS_BUTTON_CLASS;
    String DS_PERM_MORE_OPTIONS_DIALOG_CLASS;
    String FEED_CLASS_NAME;
    String FOLLOW_HOOK;
    String FOLLOW_HOOK_CLASS;
    String FULLNAME__HOOK;
    String IMAGE_HOOK_CLASS;
    String ITEMID_HOOK;
    String LIKE_HOOK_CLASS;
    String MEDIA_CLASS_NAME;
    String MEDIA_OPTIONS_BUTTON_CLASS;
    String MEDIA_PHOTO_HOOK;
    String MEDIA_VIDEO_HOOK;
    String NOTIFICATION_CLASS;
    String PERM__HOOK;
    String PROFILE_HOOK_3;
    String PROFILE_HOOK_4;
    String PROFILE_HOOK_CLASS;
    String PROFILE_HOOK_CLASS2;
    String SAVE = "Instagram";
    String SUGGESTION_HOOK_CLASS;
    String TIME_HOOK;
    String USER_CLASS_NAME;
    String USERNAME_HOOK;
    String VIDEO_CLASS;

    Bitmap icon;
    int count = 0;
    int id = 1;
    int versionCheck;
    LoadPackageParam loadPackageParam;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;
    TextView followerCount;
    XModuleResources hookedIcon;

    private static String MODULE_PATH = null;

    boolean appInstalledOrNot(String uri) {
        PackageManager pm = nContext.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public boolean canAutoUpdate(String s, int i) {
        boolean answer = true;
        if (s.equals("com.instagram.android")) {
            String version = Integer.toString(i);
            version = version.substring(0, version.length() - 2);
            if (!hookCheck(version)) {
                answer = false;
            }
        }
        return answer;
    }

    boolean hookCheck(final String version) {
        hookCheck = true;
        Thread getHooks= new Thread() {
            public void run() {
                String versions;
                try {
                    String url;

                    if (Helper.getSetting("Source").equals("GitHub")) {
                        url = "https://raw.githubusercontent.com/iHelp101/XInsta/master/Hooks.txt";
                    } else if (Helper.getSetting("Source").equals("Pastebin")) {
                        url = "http://pastebin.com/raw.php?i=sTXbUFcx";
                    } else if (Helper.getSetting("Source").equals("Alternate Source")) {
                        url = "http://www.snapprefs.com/xinsta/Hooks.txt";
                    } else {
                        url = "https://raw.githubusercontent.com/iHelp101/XInsta/master/Hooks.txt";
                    }

                    URL u = new URL(url);
                    URLConnection c = u.openConnection();
                    c.connect();

                    InputStream inputStream = c.getInputStream();

                    versions = Helper.convertStreamToString(inputStream);
                } catch (Exception e) {
                    setError("Failed to fetch hooks - " +e);
                    versions = "Nope";
                }

                if (versions.contains(version)) {
                    hookCheck = true;
                } else {
                    hookCheck = false;
                }
            }
        };
        getHooks.start();

        try {
            getHooks.join();
        }   catch (Exception e) {

        }

        return hookCheck;
    }

    @Override
    public boolean shouldUserUpdate(String s, int i, String s1) {
        boolean answer = true;
        if (s.equals("com.instagram.android")) {
            String version = Integer.toString(i);
            version = version.substring(0, version.length() - 2);
            if (!hookCheck(version)) {
                answer = false;
            }
        }
        return answer;
    }

    class Download extends AsyncTask<String, String, String> {

        int downloadFailed = 1;

        @Override
        protected String doInBackground(String... uri) {
            String responseString = "Nope";

            try {
                downloadFailed = 1;
                Random r = new Random();
                id = r.nextInt(9999999 - 65) + 65;

                String iconString = "iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAYAAABV7bNHAAAABHNCSVQICAgIfAhkiAAAAyVJREFU\n" +
                        "eJzt2U9IVFEUBvDvzGgKbRRCDdpE0LKFbiKCcBMEbQRp4UxjWiQaWAqFlTO81EVtahG0yo1BuqgM\n" +
                        "oXYm0UaocXT6R6gJYSkWZIKT4bx32sngjHNnxnvnjXJ+y+v1vMOnPu89AwghhBBCCCGyR2489Ejv\n" +
                        "wMEi0PtM9xNwJxw822uyp60UufHQEttLttfZm+l+Byg22U86HrcevFNIQAoSkIIEpCABKUhAChKQ\n" +
                        "ggSkIAEpSEAKEpCCBKQgASlIQAoSkIIEpCABKWgfudb0DRxnRkX6h1IlAw8yrUnAEwYPKvcVl46G\n" +
                        "u878ybRuJrSPXJmxB6AhpBmTcrY1gXqA6hXbRsJrn55nWVrJq7vgwtjwXFVtXRERndBdO41v7F0/\n" +
                        "vRjsWNVd2Mg7yFP5tw/AaxO1U4gzcyByo/mnieJGAgq3tKzHbfIDMNJ0IgJ6IqGAsR+Gsf9iUcs/\n" +
                        "z8AFALapZ4AwiorYbWP1YeAdlGhxbPjL/tq6chAdNVB+ySbnVKTz/LKB2huMn4NWypa7ALzVXNb2\n" +
                        "gJumuhu/a66bxHhAM+3t/xx4fAD0nU8Y994FAy+11UsjLyfpyaBvmpjakP0RKJXxlbLf3RrqZMTo\n" +
                        "OyjRwqunH6reRA8QUL2NMstx8MnP1y7+0taYQv7uYkTsiccuE+hjjhUcZm6NBgNzWvtSyOtlNWy1\n" +
                        "xNhjNwDI/sRLeBgJBYb0d5Ve3m/zEzcbowB1ZPlt0XgJdxppSMGVcceEPdMPINPfhlWb7Ybo1YD2\n" +
                        "e1Ym3JkHWZbj2PFWALOKnUyMK1Ohc7m+t7bNtYHZpNW0DCI/gLWt9jB4MBz09+exrSSuThQnuv3j\n" +
                        "RAhu8eVpT3FpG4h0nJ1y5vrINRyfvQvGi03Law6TT/d0MBeuBwTLcuI2NxMwv7FGdH0y5Nd9f8uJ\n" +
                        "+wEBiN4KLDG4EcA6gJFDh/fcd7unglTdO3Cpxnq8z+0+hBBC7BBJHz1X9zx6xoRyN5pxH/+IBAO+\n" +
                        "xJWkj56ZcIyAyvw1VUCIvm5eKoiDYsFglGxekoAScIq/KAlIIVViMc5lZrwLeICY2z0IIYQQQgix\n" +
                        "O/wH4P/cZvq+E/gAAAAASUVORK5CYII=";

                byte[] decodedByte = Base64.decode(iconString, 0);
                icon = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

                if (!Helper.getSettings("Notification")) {
                    mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(mContext);
                    mBuilder.setContentTitle(notificationTitle)
                            .setContentText(Helper.getString(mContext, R.string.DownloadDots))
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setLargeIcon(icon);
                    mNotifyManager.notify(id, mBuilder.build());
                }

                URL url = new URL (linkToDownload);
                URLConnection connection = url.openConnection();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output;

                output = new FileOutputStream(SAVE);

                byte data[] = new byte[1024];

                while ((count = input.read(data)) >= 0) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();


                if (!Helper.getSettings("Notification")) {
                    mBuilder.setContentText(Helper.getString(mContext, R.string.Download_Completed)).setTicker(Helper.getString(mContext, R.string.Download_Completed));

                    mBuilder.setContentTitle(mBuilder.mContentTitle)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setLargeIcon(icon)
                            .setAutoCancel(true);

                    Intent notificationIntent = new Intent();
                    notificationIntent.setAction(Intent.ACTION_VIEW);

                    File file = new File(SAVE);
                    if (SAVE.contains("jpg")) {
                        notificationIntent.setDataAndType(Uri.fromFile(file), "image/*");
                    } else {
                        notificationIntent.setDataAndType(Uri.fromFile(file), "video/*");
                    }
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    mBuilder.setContentIntent(contentIntent);
                    mNotifyManager.notify(id, mBuilder.build());
                }
            } catch (Throwable t) {
                downloadFailed = 2;
                setError("Download Error: " + t);
                if (!Helper.getSettings("Notification")) {
                    mBuilder.setContentText(Helper.getString(mContext, R.string.Download_Failed))
                            .setTicker(Helper.getString(mContext, R.string.Download_Failed))
                            .setContentTitle(notificationTitle)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setLargeIcon(icon)
                            .setAutoCancel(true);
                    mNotifyManager.notify(id, mBuilder.build());
                }
            }


            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (downloadFailed == 1) {
                Toast(Helper.getString(mContext, R.string.Download_Completed));
            } else {
                Toast(Helper.getString(mContext, R.string.Download_Failed));
            }
        }
    }

    class RequestTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... uri) {
            String responseString = "Nope";

            try {
                URL u = new URL("https://www.instagram.com/" + uri[0] + "/media/");
                URLConnection c = u.openConnection();
                c.connect();

                InputStream inputStream = c.getInputStream();
                String JSONInfo = Helper.convertStreamToString(inputStream);

                JSONObject jsonObject = new JSONObject(JSONInfo);

                String descriptionType = jsonObject.getJSONArray("items").getJSONObject(0).getString("type");
                userName = uri[0];

                if (descriptionType.equals("video")) {
                    descriptionType = Helper.getString(mContext, R.string.video);
                    String fileExtension = ".mp4";
                    fileName = userName + "_" + jsonObject.getJSONArray("items").getJSONObject(0).getString("id") + fileExtension;
                    fileType = "Video";

                    linkToDownload = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("videos").getJSONObject("standard_resolution").getString("url");
                    linkToDownload = linkToDownload.replace("750x750", "");
                    linkToDownload = linkToDownload.replace("640x640", "");
                    linkToDownload = linkToDownload.replace("480x480", "");
                    linkToDownload = linkToDownload.replace("320x320", "");

                    notificationTitle = Helper.getString(mContext, R.string.username_thing, userName, descriptionType);
                    notificationTitle = notificationTitle.substring(0,1).toUpperCase() + notificationTitle.substring(1);

                    SAVE = Helper.getSaveLocation(fileType);

                    downloadOrPass();
                }
            } catch (Exception e) {
                setError("Video Fetch Failed: " + e);
            }

            return responseString;
        }
    }

    Object getFieldByType(Object object, Class<?> type) {
        Field f = XposedHelpers.findFirstFieldByExactType(object.getClass(), type);
        try {
            return f.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    String checkSave() {
        String saveLocation = SAVE;

        try {
            if (SAVE.equals("Instagram")) {
                saveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Instagram/";
                if (Helper.getSettings("Folder")) {
                    saveLocation = saveLocation + userName + "/";
                }
                File directory = new File(URI.create(saveLocation).getPath());
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            } else {
                if (Helper.getSettings("Folder")) {
                    saveLocation = saveLocation + userName + "/";
                }
                File directory = new File(URI.create(saveLocation).getPath());
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            }
        } catch (Exception e) {
            setError("Save Location Check Failed: " + e);
            saveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Instagram/";
        }

        return (saveLocation + fileName).replace("%20", " ");
    }

    String checkSaveProfile() {
        String saveLocation = SAVE;

        try {
            if (SAVE.equals("Instagram")) {
                saveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Instagram/";
                if (Helper.getSettings("Folder")) {
                    saveLocation = saveLocation + userName + "/";
                }
                File directory = new File(URI.create(saveLocation).getPath());
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            } else {
                if (Helper.getSettings("Folder")) {
                    saveLocation = saveLocation + userName + "/";
                }
                File directory = new File(URI.create(saveLocation).getPath());
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            }
        }  catch (Exception e) {
            setError("Profile Save Location Check Failed: " +e);
            saveLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Instagram/";
        }
        return (saveLocation + fileName).replace("%20", " ");
    }

    String dateFormat(Long epochTime) {
        String dateFormat = Helper.getSetting("Date");

        Date date = new Date(epochTime * 1000L);
        TimeZone timeZone = TimeZone.getDefault();

        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Month) + ";", "/MM");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Day) + ";", "/dd");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Year) + ";", "/yyyy");
        if (Helper.getSettings("Hour")) {
            dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Hour) + ";", "kk:");
        } else {
            dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Hour) + ";", "hh:");
        }
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Minute) + ";", "mm:");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Second) + ";", "ss:");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.AM) + ";", "");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Space) + ";", " ");
        dateFormat = dateFormat.substring(1);

        if (dateFormat.substring(dateFormat.length() - 1).equals(":")) {
            dateFormat = dateFormat.substring(0, dateFormat.length() - 1);
        }

        if (Helper.getSetting("Date").contains(Helper.getString(nContext, R.string.AM))) {
            dateFormat = dateFormat + " a";
        }

        if (!Helper.getSetting("Seperator").equals("Instagram")) {
            dateFormat = dateFormat.replace("/", Helper.getSetting("Seperator"));
        }

        DateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(timeZone);

        return format.format(date);
    }

    String getDate (Long epochTime) {
        String dateFormat = Helper.getSetting("File");

        Date date = new Date(epochTime * 1000L);
        TimeZone timeZone = TimeZone.getDefault();

        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Month), "MM");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Day), "dd");
        dateFormat = dateFormat.replace(Helper.getString(nContext, R.string.Year), "yyyy");
        dateFormat = dateFormat.replaceAll("/", "");

        DateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(timeZone);

        return format.format(date);
    }

    void checkMarshmallowPermission() {
        try {
            File notification = new File(Environment.getExternalStorageDirectory().toString() + "/.Instagram/Hooks.txt");

            BufferedReader br = new BufferedReader(new FileReader(notification));

            br.readLine();
            br.close();
        } catch (Throwable t) {
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setContentTitle("Storage Permission Denied").setContentText("Click to open App Settings").setSmallIcon(android.R.drawable.ic_dialog_info).setAutoCancel(true);

                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:com.instagram.android"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                mBuilder.setContentIntent(contentIntent);
                mNotifyManager.notify(id, mBuilder.build());
            }
        }
    }

    void downloadOrPass() {
        SAVE = Helper.getSaveLocation(fileType);

        checkMarshmallowPermission();

        if (!SAVE.toLowerCase().contains("com.android.externalstorage.documents")) {
            SAVE = Helper.getSaveLocation(fileType);
            if (fileType.equals("Profile")) {
                SAVE = checkSaveProfile();
            } else {
                SAVE = checkSave();
            }

            new Download().execute();
        } else {
            Intent downloadIntent = new Intent();
            downloadIntent.setPackage("com.ihelp101.instagram");
            downloadIntent.setAction("com.ihelp101.instagram.PASS");
            downloadIntent.putExtra("URL", linkToDownload);
            downloadIntent.putExtra("SAVE", SAVE);
            downloadIntent.putExtra("Notification", notificationTitle);
            downloadIntent.putExtra("Filename", fileName);
            downloadIntent.putExtra("Filetype", fileType);
            downloadIntent.putExtra("User", userName);
            mContext.startService(downloadIntent);
        }
    }

    @SuppressLint("NewApi")
	void downloadMedia(Object mMedia, String where) throws IllegalAccessException, IllegalArgumentException {
        String filenameExtension;
        String descriptionType;
        int descriptionTypeId;

        try {
            linkToDownload = (String) getObjectField(mMedia, MEDIA_VIDEO_HOOK);
            filenameExtension = "mp4";
            fileType = "Video";
            descriptionType = "video";
            descriptionTypeId = R.string.video;

            if (linkToDownload.equals("None")) {
                filenameExtension = "";
            }
        } catch (Throwable throwable) {
            try {
                setError("Falling Back To New Video Method");

                List videoList = (List) getObjectField(mMedia, MEDIA_VIDEO_HOOK);

                for (int i=0;i < videoList.size();i++) {
                    String videoUrl = (String) XposedHelpers.getObjectField(videoList.get(i), XposedHelpers.findFirstFieldByExactType(videoList.get(i).getClass(), String.class).getName());
                    if (videoList.contains("_n.mp4")) {
                        i = 999;
                        linkToDownload = videoUrl;
                    } else {
                        linkToDownload = (String) XposedHelpers.getObjectField(videoList.get(0), XposedHelpers.findFirstFieldByExactType(videoList.get(0).getClass(), String.class).getName());
                    }
                }

                filenameExtension = "mp4";
                fileType = "Video";
                descriptionType = "video";
                descriptionTypeId = R.string.video;

                if (linkToDownload.equals("None")) {
                    filenameExtension = "";
                }
            } catch (Throwable t) {
                setError("Switch Link - Different Media Type");
                if (oldCheck.equals("No")) {
                    try {
                        Class<?> Image = findClass(IMAGE_HOOK_CLASS, loadPackageParam.classLoader);
                        Object photo = getFieldByType(mMedia, Image);

                        linkToDownload = (String) getObjectField(photo, XposedHelpers.findFirstFieldByExactType(Image, String.class).getName());
                    } catch (Throwable t2) {
                        Toast("Please contact iHelp101 on XDA.");
                        setError("Photo Hook Invalid - " + t2);
                        return;
                    }
                } else {
                    try {
                        linkToDownload = (String) getObjectField(mMedia, MEDIA_PHOTO_HOOK);
                    } catch (Throwable t2) {
                        Toast("Please contact iHelp101 on XDA.");
                        setError("Link To Download Hook Invalid (Photo) - " + t2);
                        return;
                    }
                }
                filenameExtension = "jpg";
                fileType = "Image";
                descriptionType = "photo";
                descriptionTypeId = R.string.photo;
            }
        }

        if (descriptionType.equals("photo")) {
            SAVE = Helper.getString(mContext, R.string.Image);
        } else {
            SAVE = Helper.getString(mContext, R.string.Video);
        }

		// Construct filename
		// username_imageId.jpg
		descriptionType = Helper.getString(mContext, descriptionTypeId);
		String toastMessage = Helper.getString(mContext, R.string.Downloading, descriptionType);
        Toast(toastMessage);

		Object mUser;
        try {
            mUser = getFieldByType(mMedia, findClass(USER_CLASS_NAME, loadPackageParam.classLoader));
        } catch (Throwable t) {
            Toast("Please contact iHelp101 on XDA.");
            setError("mUser Hook Invalid - " + USER_CLASS_NAME);
            return;
        }

        if (where.equals("Direct") && directShareCheck.equals("Yes")) {
            mUser = mUserName;
        }

		String userFullName;

		try {
            userName = (String) getObjectField(mUser, USERNAME_HOOK);
            userFullName = (String) getObjectField(mUser, FULLNAME__HOOK);
		} catch (Throwable t) {
            setError("Failed to get User from Media, using placeholders");
            userName = "username_placeholder";
            userFullName = "Unknown name";
		}

        String itemId;

        if (where.equals("Direct") && directShareCheck.equals("Yes")) {
            try {
                itemId = getObjectField(model, XposedHelpers.findFirstFieldByExactType(model.getClass(), Long.class).getName()).toString();
            } catch (Throwable t) {
                setError("ItemID Directshare Hook Failed");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
                itemId = sdf.format(new Date());
            }
       } else {
            try {
                itemId = (String) getObjectField(mMedia, ITEMID_HOOK);
            } catch (Throwable t) {
                setError("ItemID Hook Invalid - " + t);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
                itemId = sdf.format(new Date());
            }
        }



        if (!Helper.getSetting("File").equals("Instagram") && !where.equals("Direct")) {
            try {
                Long itemID = (Long) getObjectField(mMedia, XposedHelpers.findFirstFieldByExactType(mMedia.getClass(), long.class).getName());

                String itemToString = getDate(itemID);

                itemId =  itemId.replace(itemId.split("_")[1], "") + itemToString;
            } catch (Throwable t) {
                try {
                    itemId = (String) getObjectField(mMedia, ITEMID_HOOK);
                } catch (Throwable t2) {
                    setError("ItemID Hook Invalid - " + t2);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
                    itemId = sdf.format(new Date());
                }
            }
        }

        fileName = userName + "_" + itemId + "." + filenameExtension;

		if (TextUtils.isEmpty(userFullName)) {
			userFullName = userName;
		}

        notificationTitle = Helper.getString(mContext, R.string.username_thing, userFullName, descriptionType);
        notificationTitle = notificationTitle.substring(0,1).toUpperCase() + notificationTitle.substring(1);

        linkToDownload = linkToDownload.replace("750x750", "");
        linkToDownload = linkToDownload.replace("640x640", "");
        linkToDownload = linkToDownload.replace("480x480", "");
        linkToDownload = linkToDownload.replace("320x320", "");

        SAVE = Helper.getSaveLocation(fileType);

        try {
            if (fileType.equals("Video") && Helper.getSettings("OneTap") && appInstalledOrNot("com.phantom.onetapvideodownload")) {
                setError("One Tap - " +Helper.getSettings("OneTap"));
                Intent intent = new Intent("com.phantom.onetapvideodownload.action.saveurl");
                intent.setClassName("com.phantom.onetapvideodownload", "com.phantom.onetapvideodownload.IpcService");
                intent.putExtra("com.phantom.onetapvideodownload.extra.url", linkToDownload);
                intent.putExtra("com.phantom.onetapvideodownload.extra.title", fileName);
                intent.putExtra("com.phantom.onetapvideodownload.extra.package_name", loadPackageParam.packageName);
                mContext.startService(intent);
            } else {
                if (fileType.equals("Profile")) {
                    SAVE = checkSaveProfile();
                } else {
                    SAVE = checkSave();
                }

                downloadOrPass();
            }
        } catch (Exception e) {
            setError("Failed To Send Download Broadcast - " +e);
        }
	}

    @Override
    public void handleInitPackageResources(final XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.instagram.android"))
            return;

        hookedIcon = XModuleResources.createInstance(MODULE_PATH, resparam.res);

        resparam.res.hookLayout("com.instagram.android", "layout", "row_profile_scoreboard_header", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
                followerCount = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("row_profile_header_textview_following_count", "id", "com.instagram.android"));

            }
        });
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.instagram.android")) {
            loadPackageParam = lpparam;

            // Thank you to KeepChat For the Following Code Snippet
            // http://git.io/JJZPaw
            Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            nContext = (Context) callMethod(activityThread, "getSystemContext");

            versionCheck = nContext.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionCode;
            //End Snippet

            setError("XInsta Initialized");
            setError("Instagram Version Code: " + versionCheck);
            setError("Device Codename: " + Build.MODEL);
            setError("Android Version: " + Build.VERSION.RELEASE);
            try {
                PackageInfo pinfo = nContext.getPackageManager().getPackageInfo("com.ihelp101.instagram", 0);
                setError("XInsta Version " + pinfo.versionName);
            } catch (Exception e) {
            }
            startHooks();
        }
    }

    void hookAccountOptions() {
        try {
            Class<?> Account = findClass(ACCOUNT_HOOK_CLASS, loadPackageParam.classLoader);
            Method[] methods = XposedHelpers.findMethodsByExactParameters(Account, boolean.class);

            XposedHelpers.findAndHookMethod(Account, methods[0].getName(), new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.setResult(true);
                }
            });
        } catch (Throwable t) {
            setError("Account Options Failed - " + t.toString());
        }
    }

    void hookComments() {
        try {
            final Class<?> Comments = XposedHelpers.findClass(COMMENT_HOOK_CLASS, loadPackageParam.classLoader);

            Method[] methods = XposedHelpers.findMethodsByExactParameters(Comments, boolean.class, findClass(COMMENT_HOOK_CLASS2, loadPackageParam.classLoader));


            findAndHookMethod(findClass(COMMENT_HOOK_CLASS, loadPackageParam.classLoader), methods[0].getName(), findClass(COMMENT_HOOK_CLASS2, loadPackageParam.classLoader), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Object pj = param.args[0];
                    mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                    if (Helper.getSettings("Comment")) {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) AndroidAppHelper.currentApplication().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Clip", (String) getObjectField(pj, "d"));
                        clipboard.setPrimaryClip(clip);
                        Toast(Helper.getString(AndroidAppHelper.currentApplication().getApplicationContext(), R.string.Copied));
                    }
                }
            });
        } catch (Throwable t) {
            try {
                final Class<?> Comments = XposedHelpers.findClass(COMMENT_HOOK_CLASS, loadPackageParam.classLoader);

                Method[] methods = XposedHelpers.findMethodsByExactParameters(Comments, void.class, findClass(COMMENT_HOOK_CLASS2, loadPackageParam.classLoader));

                XposedHelpers.findAndHookMethod(Comments, methods[0].getName(), COMMENT_HOOK_CLASS2, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Object pj = param.args[0];
                        mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                        if (Helper.getSettings("Comment")) {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) AndroidAppHelper.currentApplication().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("Clip", (String) getObjectField(pj, "d"));
                            clipboard.setPrimaryClip(clip);
                            Toast(Helper.getString(AndroidAppHelper.currentApplication().getApplicationContext(), R.string.Copied));
                        }
                    }
                });
            } catch (Throwable t2) {
                setError("Comment Check Failed - " + t2.toString());
            }
        }
    }

    void hookDate() {
        try {
            if (TIME_HOOK.equals("Nope") && !Helper.getSetting("Date").equals("Instagram")) {
                final Class<?> Time = XposedHelpers.findClass(MEDIA_CLASS_NAME, loadPackageParam.classLoader);
                Method[] methods = XposedHelpers.findMethodsByExactParameters(Time, CharSequence.class, Context.class);

                XposedHelpers.findAndHookMethod(Time, methods[0].getName(), Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult(dateFormat((Long) XposedHelpers.getObjectField(param.thisObject, (XposedHelpers.findFirstFieldByExactType(Time, long.class)).getName())));
                    }
                });
            } else if (!Helper.getSetting("Date").equals("Instagram")) {
                final Class<?> Time = XposedHelpers.findClass(TIME_HOOK, loadPackageParam.classLoader);
                Method[] methods = XposedHelpers.findMethodsByExactParameters(Time, String.class, Context.class, long.class);

                XposedHelpers.findAndHookMethod(Time, methods[1].getName(), Context.class, long.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult(dateFormat((Long) param.args[1]));
                    }
                });
            }
        } catch(Throwable t){
            setError("Time Hooks Failed: " + t);
            setError("Time Hooks Class: " + MEDIA_CLASS_NAME);
        }
    }

    void hookDirectShare() {
        try {
            if (directShareCheck.equals("Nope")) {
                findAndHookMethod(findClass(DS_MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader), PERM__HOOK, injectDownloadIntoCharSequenceHook);
                findAndHookMethod(findClass(DS_MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader), PERM__HOOK, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mCurrentDirectShareMediaOptionButton = param.thisObject;
                    }
                });

                Method[] methods = XposedHelpers.findMethodsByExactParameters(findClass(DS_PERM_MORE_OPTIONS_DIALOG_CLASS, loadPackageParam.classLoader), void.class, DialogInterface.class, int.class);

                findAndHookMethod(findClass(DS_PERM_MORE_OPTIONS_DIALOG_CLASS, loadPackageParam.classLoader), methods[0].getName(), DialogInterface.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        CharSequence localCharSequence = mDirectShareMenuOptions[(Integer) param.args[1]];
                        if (mContext == null) {
                            mContext = ((Dialog) param.args[0]).getContext();
                        }
                        if (Helper.getString(nContext, R.string.the_not_so_big_but_big_button).equals(localCharSequence)) {
                            Object mMedia = null;

                            Field[] mCurrentMediaOptionButtonFields =
                                    mCurrentDirectShareMediaOptionButton.getClass().getDeclaredFields();

                            for (Field iField : mCurrentMediaOptionButtonFields) {
                                if (iField.getType().getName().equals(MEDIA_CLASS_NAME)) {
                                    iField.setAccessible(true);
                                    mMedia = iField.get(mCurrentDirectShareMediaOptionButton);
                                    if (mMedia != null) {
                                        break;
                                    }
                                }
                            }

                            if (mMedia == null) {
                                setError("Unable To Determine Media - Directshare");
                                return;
                            }

                            try {
                                downloadMedia(mMedia, "Other");
                            } catch (Throwable t) {
                                setError("Download Media Failed - " +t.toString());
                            }

                            param.setResult(null);
                        }
                    }
                });
            } else {
                Method[] methods = XposedHelpers.findMethodsByExactParameters(findClass(DS_PERM_MORE_OPTIONS_DIALOG_CLASS, loadPackageParam.classLoader), void.class, DialogInterface.class, int.class);

                findAndHookMethod(findClass(DS_PERM_MORE_OPTIONS_DIALOG_CLASS, loadPackageParam.classLoader), methods[0].getName(), DialogInterface.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Field menuOptionsField = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), ArrayList.class);
                        ArrayList arrayList = (ArrayList) menuOptionsField.get(param.thisObject);
                        arrayList.add("Download");
                        menuOptionsField.set(param.thisObject, arrayList);
                        ArrayList mProfileList = (ArrayList) menuOptionsField.get(param.thisObject);

                        String localCharSequence = mProfileList.get((Integer) param.args[1]).toString();

                        if (mContext == null) {
                            mContext = ((Dialog) param.args[0]).getContext();
                        }

                        if (Helper.getString(nContext, R.string.the_not_so_big_but_big_button).equals(localCharSequence)) {
                            Object mMedia = null;

                            try {
                                model = getObjectField(param.thisObject, "b");
                            } catch (Throwable t) {
                                setError("Directshare Model Hook Invalid - " +t.toString());
                                return;
                            }

                            try {
                                mUserName = getFieldByType(model, findClass(USER_CLASS_NAME, loadPackageParam.classLoader));

                                Field[] mCurrentMediaOptionButtonFields = model.getClass().getDeclaredFields();

                                for (Field iField : mCurrentMediaOptionButtonFields) {
                                    if (iField.getType().getName().equals(MEDIA_CLASS_NAME)) {
                                        iField.setAccessible(true);
                                        mMedia = iField.get(model);
                                        if (mMedia != null) {
                                            break;
                                        }
                                    }
                                }

                                if (mMedia == null) {
                                    setError("Unable To Determine Media - DS");
                                    return;
                                }

                                try {
                                    downloadMedia(mMedia, "Direct");
                                } catch (Throwable t) {
                                    Toast("Please contact iHelp101 on XDA.");
                                }
                            } catch (Throwable t) {
                                setError("Directshare Image/Video Minimized: " +t);
                                Toast(Helper.getString(nContext, R.string.Picture_Not));
                            }

                            param.setResult(null);
                        }
                    }
                });
            }

        } catch (Throwable t) {
            setError("Directshare Hooks Invalid - " +t.toString());
        }

        try {
            Method[] methods = XposedHelpers.findMethodsByExactParameters(findClass(DS_DIALOG_CLASS, loadPackageParam.classLoader), findClass(DS_DIALOG_CLASS, loadPackageParam.classLoader), CharSequence[].class, android.content.DialogInterface.OnClickListener.class);

            findAndHookMethod(findClass(DS_DIALOG_CLASS, loadPackageParam.classLoader), methods[0].getName(), CharSequence[].class, android.content.DialogInterface.OnClickListener.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    CharSequence[] string = (CharSequence[]) param.args[0];
                    android.content.DialogInterface.OnClickListener onClickListener = (android.content.DialogInterface.OnClickListener) param.args[1];
                    ArrayList<String> array = new ArrayList<String>();
                    for (CharSequence sq : string)
                        array.add(sq.toString());

                    if (onClickListener.getClass().getName().equals(DS_PERM_MORE_OPTIONS_DIALOG_CLASS) && !array.contains(Helper.getString(nContext, R.string.the_not_so_big_but_big_button))) {
                        array.add(Helper.getString(nContext, R.string.the_not_so_big_but_big_button));
                        CharSequence[] newResult = new CharSequence[array.size()];
                        array.toArray(newResult);
                        param.args[0] = newResult;
                    }
                }
            });
        } catch (Throwable t) {
            setError("DirectShare Check Failed - " +t.toString());
        }
    }

    void hookFeed() {
        try {
            Class<?> Media = findClass(MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader);
            Method[] methods = XposedHelpers.findMethodsByExactParameters(Media, CharSequence[].class);

            findAndHookMethod(findClass(MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader), methods[0].getName(), injectDownloadIntoCharSequenceHook);
            findAndHookMethod(findClass(MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader), methods[0].getName(), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mCurrentMediaOptionButton = param.thisObject;

                    mContext = AndroidAppHelper.currentApplication().getApplicationContext();
                }
            });
        } catch (Throwable t) {
            setError("Media Options Button Hook Failed - " +t.toString());
        }

        try {
            final Class<?> MenuClickListener = findClass(FEED_CLASS_NAME, loadPackageParam.classLoader);
            final Method[] methods = XposedHelpers.findMethodsByExactParameters(MenuClickListener, void.class, DialogInterface.class, int.class);

            findAndHookMethod(MenuClickListener, methods[0].getName(), DialogInterface.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    CharSequence localCharSequence = mMenuOptions[(Integer) param.args[1]];
                    if (Helper.getString(nContext, R.string.the_not_so_big_but_big_button).equals(localCharSequence)) {
                        Object mMedia = null;

                        mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                        try {
                            mMedia = getObjectField(mCurrentMediaOptionButton, XposedHelpers.findFirstFieldByExactType(findClass(MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader), findClass(MEDIA_CLASS_NAME, loadPackageParam.classLoader)).getName());
                        } catch (Throwable t) {
                            setError("Menu Click Hook Failed: " + t);
                        }

                        if (mMedia == null) {
                            setError("Unable To Determine Media - Feed");
                            return;
                        }

                        try {
                            downloadMedia(mMedia, "Other");
                        } catch (Throwable t) {
                            setError(t.toString());
                            Toast("Please contact iHelp101 on XDA.");
                        }

                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable t) {
            setError("Menu Click Listener Failed - " + t.toString());
        }
    }

    void hookFollow() {
        try {
            XposedHelpers.findAndHookMethod(FOLLOW_HOOK_CLASS, loadPackageParam.classLoader, FOLLOW_HOOK, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        byte[] bytes = (byte[]) getFieldByType(param.thisObject, byte[].class);

                        JSONObject jObject = new JSONObject(new String(bytes, "UTF-8"));

                        if (jObject.getString("followed_by").equals("true")) {
                            String color = Helper.getSetting("Color");
                            if (color.equals("Instagram")) {
                                color = "#2E978C";
                            }
                            followerCount.setTextColor(Color.parseColor(color));
                        }
                    } catch (Throwable t) {
                    }
                }
            });
        } catch (Throwable t) {
            setError("Follow Feature Failed - " + t.toString());
        }
    }

    void hookInstagram() {
        try {
            hookAccountOptions();
        } catch (Throwable t) {
        }

        try {
            hookComments();
        } catch (Throwable t) {
        }

        try {
            hookDate();
        } catch (Throwable t) {
        }

        try {
            hookDirectShare();
        } catch (Throwable t) {
        }

        try {
            hookFeed();
        } catch (Throwable t) {
        }

        try {
            hookFollow();
        } catch (Throwable t) {
        }

        try {
            hookLike();
        } catch (Throwable t) {
        }

        try {
            hookNotification();
        } catch (Throwable t) {
        }

        try {
            hookProfileIcon();
        } catch (Throwable t) {
        }

        try {
            hookSuggestion();
        } catch (Throwable t) {
        }

        try {
            hookVideos();
        } catch (Throwable t) {
        }
    }

    void hookLike() {
        try {
            Class<?> Like = findClass(LIKE_HOOK_CLASS, loadPackageParam.classLoader);
            Method[] methods = XposedHelpers.findMethodsByExactParameters(Like, boolean.class, MotionEvent.class);

            XposedHelpers.findAndHookMethod(Like, methods[0].getName(), MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    if (Helper.getSettings("Like")) {
                        param.setResult(false);
                    }
                }
            });
        } catch (Throwable t) {
            setError("Like Hooks Failed - " + t.toString());
            setError("Like Hook Class: " +LIKE_HOOK_CLASS);
        }
    }

    void hookNotification() {
        Class<?> Notification = findClass(NOTIFICATION_CLASS, loadPackageParam.classLoader);
        final Method[] methods = XposedHelpers.findMethodsByExactParameters(Notification, void.class, Intent.class, String.class);

        XposedHelpers.findAndHookMethod(Notification, methods[0].getName(), Intent.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (Helper.getSettings("Push")) {
                    try {
                        mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                        Intent intent = (Intent) param.args[0];
                        JSONObject jsonObject = new JSONObject(intent.getStringExtra("data"));

                        String userHolder;

                        if (jsonObject.getString("m").contains("]: ")) {
                            userHolder = jsonObject.getString("m").split("]: ")[1];
                        } else {
                            userHolder = jsonObject.getString("m");
                        }

                        userName = userHolder.split(" ")[0];
                        String photoName = Helper.getString(mContext, "photo", loadPackageParam.packageName).toLowerCase();
                        String photoCheck = userHolder.replace(userName, "").toLowerCase();
                        String videoName = Helper.getString(mContext, "video", loadPackageParam.packageName).toLowerCase();
                        if (jsonObject.getString("collapse_key").equals("post") && photoCheck.contains(photoName)) {
                            String fileExtension = ".jpg";
                            String fileDescription = Helper.getString(mContext, R.string.photo);
                            fileName = userName + "_" + jsonObject.getString("ig").replace("media?id=", "") + fileExtension;

                            fileType = "Image";
                            linkToDownload = jsonObject.getString("i");
                            notificationTitle = Helper.getString(mContext, R.string.username_thing, userName, fileDescription);
                            notificationTitle = notificationTitle.substring(0, 1).toUpperCase() + notificationTitle.substring(1);

                            SAVE = Helper.getSaveLocation(fileType);

                            downloadOrPass();
                        } else if (jsonObject.getString("collapse_key").equals("post") && photoCheck.contains(videoName)) {
                            new RequestTask().execute(userName);
                        }
                    } catch (Throwable t) {
                        setError("Notification Error: " + t.toString());
                    }
                }
            }
        });
    }

    void hookProfileIcon() {
        try {
            Class<?> Profile = findClass(PROFILE_HOOK_CLASS, loadPackageParam.classLoader);
            Method[] methods = XposedHelpers.findMethodsByExactParameters(Profile, CharSequence[].class);

            findAndHookMethod(Profile, methods[0].getName(), new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                    CharSequence[] result = (CharSequence[]) param.getResult();

                    ArrayList<String> array = new ArrayList<String>();
                    for (CharSequence sq : result)
                        array.add(sq.toString());

                    if (!array.contains(Helper.getString(nContext, R.string.the_not_so_big_but_big_button)))
                        array.add(Helper.getString(nContext, R.string.the_not_so_big_but_big_button));

                    CharSequence[] newResult = new CharSequence[array.size()];

                    mProfileOptions = newResult;

                    array.toArray(newResult);
                    param.setResult(newResult);
                }
            });
        } catch (Throwable t) {
            setError("Profile Icon Failed - " +t.toString());
            setError("Profile Icon Class: " +PROFILE_HOOK_CLASS);
        }


        try {
            Class<?> Profile2 = findClass(PROFILE_HOOK_CLASS2, loadPackageParam.classLoader);
            Method[] methods = XposedHelpers.findMethodsByExactParameters(Profile2, void.class, DialogInterface.class, int.class);

            findAndHookMethod(Profile2, methods[0].getName(), DialogInterface.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String localCharSequence = mProfileOptions[(int) param.args[1]].toString();

                    if (Helper.getString(nContext, R.string.the_not_so_big_but_big_button).equals(localCharSequence)) {

                        Class<?> Profile = findClass(PROFILE_HOOK_CLASS2, loadPackageParam.classLoader);
                        Class<?> Profile2 = findClass(PROFILE_HOOK_CLASS, loadPackageParam.classLoader);
                        Class<?> ProfileUser = findClass(USER_CLASS_NAME, loadPackageParam.classLoader);

                        String firstField;

                        try {
                            firstField = XposedHelpers.findFirstFieldByExactType(Profile, Profile2).getName();
                        } catch (Throwable e) {
                            setError("Profile First Field Failed: " + e);
                            return;
                        }

                        String secondField;

                        Field[] fields = Profile2.getDeclaredFields();

                        try {
                            secondField = XposedHelpers.findFirstFieldByExactType(Profile2, ProfileUser).getName();
                        } catch (Throwable e) {
                            setError("Profile Second Field Failed: " + e);
                            return;
                        }

                        for (Field field : fields) {
                            if (field.getType().equals(ProfileUser)) {
                                count++;
                                if (count == 2) {
                                    secondField = field.getName();
                                }
                            }
                        }

                        Object objectStart = XposedHelpers.getObjectField(param.thisObject, firstField);
                        Object object = XposedHelpers.getObjectField(objectStart, secondField);

                        linkToDownload = (String) XposedHelpers.getObjectField(object, PROFILE_HOOK_3);
                        linkToDownload = linkToDownload.replace("s150x150/", "");

                        try {
                            userName = (String) XposedHelpers.getObjectField(object, USERNAME_HOOK);
                        } catch (Throwable t) {
                            setError("Profile Icon Username Hooks Failed:  " + t);
                            return;
                        }

                        notificationTitle = Helper.getString(mContext, R.string.username_thing, userName, "Icon");
                        notificationTitle = notificationTitle.substring(0, 1).toUpperCase() + notificationTitle.substring(1);


                        fileName = userName + "-Profile.jpg";

                        SAVE = Helper.getString(mContext, R.string.Profile);

                        fileType = "Profile";

                        downloadOrPass();

                        param.setResult(null);
                    }
                }
            });
        } catch (Throwable t) {
            setError("Profile Icon Click Listener Failed - " + t.toString());
            setError("Profile Icon Click Listener Class: " +PROFILE_HOOK_CLASS2);
        }
    }

    void hookSuggestion() {
        try {
            Class<?> Suggest = XposedHelpers.findClass(SUGGESTION_HOOK_CLASS, loadPackageParam.classLoader);

            Method[] methods = XposedHelpers.findMethodsByExactParameters(Suggest, View.class, Context.class);

            XposedHelpers.findAndHookMethod(Suggest, methods[0].getName(), Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                    if (Helper.getSettings("Suggestion")) {
                        View view = (View) param.getResult();
                        Class<?> listClass = findClass(view.getTag().getClass().getName(), loadPackageParam.classLoader);
                        List list = (List) XposedHelpers.getObjectField(view.getTag(), XposedHelpers.findFirstFieldByExactType(listClass, List.class).getName());
                        list.clear();
                    }
                }
            });
        } catch (Throwable t) {
            try {
                Class<?> Suggest = XposedHelpers.findClass(SUGGESTION_HOOK_CLASS, loadPackageParam.classLoader);

                Method[] methods = Suggest.getDeclaredMethods();

                String SuggestMethod = "";
                Class SuggestClass = null;
                Class SuggestClass2 = null;

                for (Method method:methods) {
                    if (method.getParameterTypes()[0].equals(Context.class)) {
                        SuggestMethod = method.getName();
                        SuggestClass = method.getParameterTypes()[0];
                        SuggestClass2 = method.getParameterTypes()[1];
                    }
                }

                XposedHelpers.findAndHookMethod(Suggest, SuggestMethod, SuggestClass, SuggestClass2, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        mContext = AndroidAppHelper.currentApplication().getApplicationContext();

                        checkMarshmallowPermission();

                        if (Helper.getSettings("Suggestion")) {
                            View view = (View) param.getResult();
                            Class<?> listClass = findClass(view.getTag().getClass().getName(), loadPackageParam.classLoader);
                            List list = (List) XposedHelpers.getObjectField(view.getTag(), XposedHelpers.findFirstFieldByExactType(listClass, List.class).getName());
                            list.clear();
                        }
                    }
                });
            } catch (Throwable t2) {
                setError("Suggestion Hooks Failed - " + t2.toString());
                setError("Suggestion Hook Class: " + SUGGESTION_HOOK_CLASS);
            }
        }
    }

    void hookVideos() {
        Class<?> Video = XposedHelpers.findClass(VIDEO_CLASS, loadPackageParam.classLoader);
        Method[] methods = XposedHelpers.findMethodsByExactParameters(Video, void.class, Object.class);
        final Method[] methods2 = XposedHelpers.findMethodsByExactParameters(Video, void.class, int.class);

        XposedHelpers.findAndHookMethod(Video, methods[0].getName(), Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (Helper.getSettings("Sound")) {
                    callMethod(param.thisObject, methods2[0].getName(), 0);
                }
            }
        });
    }

    @Override
    public void init() {
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    void setError(String status) {
        if (!Helper.getSettings("Log")) {
            XposedBridge.log("XInsta - " + status);
        }

        Helper.setError(status);
    }

    void startHooks() {
        final List<PackageInfo> packs = nContext.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if (p.packageName.equals("com.instagram.android")) {
                version = Integer.toString(p.versionCode);
                version = version.substring(0, version.length() - 2);
            }
        }

        final String[] split = Helper.getSetting("Hooks").split(";");

        try {
            FEED_CLASS_NAME = split[1];
            firstHook = split[1];
            MEDIA_CLASS_NAME = split[2];
            USER_CLASS_NAME = split[4];
            MEDIA_OPTIONS_BUTTON_CLASS = split[5];
            DS_MEDIA_OPTIONS_BUTTON_CLASS = split[6];
            DS_PERM_MORE_OPTIONS_DIALOG_CLASS = split[7];
            PERM__HOOK = split[10];
            MEDIA_VIDEO_HOOK = split[14];
            MEDIA_PHOTO_HOOK = split[15];
            USERNAME_HOOK = split[16];
            FULLNAME__HOOK = split[17];
        } catch (ArrayIndexOutOfBoundsException e) {
            HookCheck = "Yes";
        }

        try {
            IMAGE_HOOK_CLASS = split[18];
        } catch (ArrayIndexOutOfBoundsException e) {
            oldCheck = "Yes";
        }

        try {
            ITEMID_HOOK = split[20];
            COMMENT_HOOK_CLASS = split[21];
            COMMENT_HOOK = split[22];
            COMMENT_HOOK_CLASS2 = split[23];
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        try {
            DS_DIALOG_CLASS = split[24];
            directShareCheck = "Yes";
        } catch (ArrayIndexOutOfBoundsException e) {
            directShareCheck = "Nope";
        }

        try {
            PROFILE_HOOK_CLASS = split[29];
            PROFILE_HOOK_CLASS2 = split[30];
            PROFILE_HOOK_3 = split[33];
            PROFILE_HOOK_4 = split[34];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            LIKE_HOOK_CLASS = split[35];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            ACCOUNT_HOOK_CLASS = split[37];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            SUGGESTION_HOOK_CLASS = split[39];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            FOLLOW_HOOK_CLASS = split[41];
            FOLLOW_HOOK = split[42];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            TIME_HOOK = split[43];
        } catch (ArrayIndexOutOfBoundsException e) {
            TIME_HOOK = "Nope";
        }

        try {
            NOTIFICATION_CLASS = split[44];
        } catch (ArrayIndexOutOfBoundsException e) {
            NOTIFICATION_CLASS = "Nope";
        }

        try {
            VIDEO_CLASS = split[44];
        } catch (ArrayIndexOutOfBoundsException e) {
            VIDEO_CLASS = "Nope";
        }

        if (HookCheck.equals("Yes") || !version.equalsIgnoreCase(split[0])) {
            updateHooks();
        }

        if (HookCheck.equals("Yes")) {
            setError("Please update your hooks via the module.");
        } else {
            try {
                hookInstagram();
            } catch (Throwable t) {
                setError("Hooks Check Failed - " +t.toString());
            }
        }
    }

    void Toast(String message) {
        Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if (v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }

    void updateHooks() {
        Thread getHooks= new Thread() {
                public void run() {
                    try {
                        String url;

                        if (Helper.getSetting("Source").equals("GitHub")) {
                            url = "https://raw.githubusercontent.com/iHelp101/XInsta/master/Hooks.txt";
                        } else if (Helper.getSetting("Source").equals("Pastebin")) {
                            url = "http://pastebin.com/raw.php?i=sTXbUFcx";
                        } else if (Helper.getSetting("Source").equals("Alternate Source")) {
                            url = "http://www.snapprefs.com/xinsta/Hooks.txt";
                        } else {
                            url = "https://raw.githubusercontent.com/iHelp101/XInsta/master/Hooks.txt";
                        }

                        URL u = new URL(url);
                        URLConnection c = u.openConnection();
                        c.connect();

                        InputStream inputStream = c.getInputStream();

                        Hooks = Helper.convertStreamToString(inputStream);
                    } catch (Exception e) {
                        setError("Failed to fetch hooks.");
                        Hooks = "Nope";
                    }
                }
            };
            getHooks.start();
            try {
                getHooks.join();
            } catch (InterruptedException e) {
            }

            int max = 0;
            int count = 0;

            String[] html = Hooks.split("<p>");

            String matched = "No";

            for (String data : html) {
                max++;
            }

            for (String data : html) {
                count++;

                String finalCheck = "123";

                if (!data.isEmpty()) {
                    String[] PasteVersion = data.split(";");
                    finalCheck = PasteVersion[0];
                }

                if (version.equalsIgnoreCase(finalCheck) && !data.isEmpty()) {
                    HooksSave = data.replace("<p>", "");
                    HooksSave = HooksSave.replace("</p>", "");
                    matched = "Yes";
                } else if (count == max && matched == "No") {
                    String fallback = html[1];
                    HooksSave = fallback.replace("<p>", "");
                    HooksSave = HooksSave.replace("</p>", "");
                } else {
                    int typeCheck = Integer.parseInt(version) - Integer.parseInt(finalCheck);
                    if (typeCheck <= 2 && typeCheck >= -2) {
                        HooksSave = data.replace("<p>", "");
                        HooksSave = HooksSave.replace("</p>", "");
                        matched = "Yes";
                    }
                }
            }

            HookCheck = "No";
            oldCheck = "No";
            directShareCheck = "Yes";
            HooksArray = HooksSave.split(";");

            try {
                FEED_CLASS_NAME = HooksArray[1];
                firstHook = HooksArray[1];
                MEDIA_CLASS_NAME = HooksArray[2];
                USER_CLASS_NAME = HooksArray[4];
                MEDIA_OPTIONS_BUTTON_CLASS = HooksArray[5];
                DS_MEDIA_OPTIONS_BUTTON_CLASS = HooksArray[6];
                DS_PERM_MORE_OPTIONS_DIALOG_CLASS = HooksArray[7];
                PERM__HOOK = HooksArray[10];
                MEDIA_VIDEO_HOOK = HooksArray[14];
                MEDIA_PHOTO_HOOK = HooksArray[15];
                USERNAME_HOOK = HooksArray[16];
                FULLNAME__HOOK = HooksArray[17];
                setError("Hooks Fetched!");
            } catch (ArrayIndexOutOfBoundsException e) {
                HookCheck = "Yes";
            }

            try {
                IMAGE_HOOK_CLASS = HooksArray[18];
            } catch (ArrayIndexOutOfBoundsException e) {
                oldCheck = "Yes";
            }

            try {
                ITEMID_HOOK = HooksArray[20];
                COMMENT_HOOK_CLASS = HooksArray[21];
                COMMENT_HOOK = HooksArray[22];
                COMMENT_HOOK_CLASS2 = HooksArray[23];
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            try {
                DS_DIALOG_CLASS = HooksArray[24];
            } catch (ArrayIndexOutOfBoundsException e) {
                directShareCheck = "Nope";
            }

            try {
                PROFILE_HOOK_CLASS = HooksArray[29];
                PROFILE_HOOK_CLASS2 = HooksArray[30];
                PROFILE_HOOK_3 = HooksArray[33];
                PROFILE_HOOK_4 = HooksArray[34];
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            try {
                LIKE_HOOK_CLASS = HooksArray[35];
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            try {
                ACCOUNT_HOOK_CLASS = HooksArray[37];
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            try {
                SUGGESTION_HOOK_CLASS = HooksArray[39];
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            try {
                FOLLOW_HOOK_CLASS = HooksArray[41];
                FOLLOW_HOOK = HooksArray[42];
            } catch (ArrayIndexOutOfBoundsException e) {
            }

            try {
                TIME_HOOK = HooksArray[43];
            } catch (ArrayIndexOutOfBoundsException e) {
                TIME_HOOK = "Nope";
            }

            try {
                NOTIFICATION_CLASS = HooksArray[44];
            } catch (ArrayIndexOutOfBoundsException e) {
                NOTIFICATION_CLASS = "Nope";
            }

            try {
                VIDEO_CLASS = HooksArray[45];
            } catch (ArrayIndexOutOfBoundsException e) {
                VIDEO_CLASS = "Nope";
            }

        Helper.setSetting("Hooks", HooksSave);
    }

    XC_MethodHook injectDownloadIntoCharSequenceHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                CharSequence[] result = (CharSequence[]) param.getResult();

                ArrayList<String> array = new ArrayList<String>();
                for (CharSequence sq : result)
                    array.add(sq.toString());

                if (!array.contains(Helper.getString(nContext, R.string.the_not_so_big_but_big_button))) {
                    array.add(Helper.getString(nContext, R.string.the_not_so_big_but_big_button));
                }
                CharSequence[] newResult = new CharSequence[array.size()];
                array.toArray(newResult);
                Field menuOptionsField;
                if (param.thisObject.getClass().getName().contains("directshare")) {
                    menuOptionsField = XposedHelpers.findFirstFieldByExactType(param.thisObject.getClass(), CharSequence[].class);
                } else {
                    menuOptionsField = XposedHelpers.findFirstFieldByExactType(findClass(MEDIA_OPTIONS_BUTTON_CLASS, loadPackageParam.classLoader), CharSequence[].class);
                }
                menuOptionsField.set(param.thisObject, newResult);
                if (param.thisObject.getClass().getName().contains("directshare")) {
                    mDirectShareMenuOptions = (CharSequence[]) menuOptionsField.get(param.thisObject);
                } else {
                    mMenuOptions = (CharSequence[]) menuOptionsField.get(param.thisObject);
                }
                param.setResult(newResult);
            } catch (Throwable t) {
                setError("Download Button Inject Failed - " +t.toString());
            }
        }
    };
}


