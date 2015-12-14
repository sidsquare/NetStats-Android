package com.jacknova.networkstatistics;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ShutdownService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startid) {
        /*SQLiteDatabase db = openOrCreateDatabase("database", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS app('package' VARCHAR NOT NULL UNIQUE,'down' integer,'up' integer);");

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int uid;
        String name;
        long uid_rx, uid_tx;
        for (ApplicationInfo packageInfo : packages) {
            try {
                uid = packageInfo.uid;
                name = packageInfo.packageName;
                uid_rx = TrafficStats.getUidRxBytes(uid) / (1024);
                uid_tx = TrafficStats.getUidRxBytes(uid) / (1024);
                Cursor c = db.rawQuery("select * from app where package=\"" + name + "\";", null);
                if (c.getCount() == 0)
                    db.execSQL("insert into app values(\"" + name + "\"," + uid_rx + "," + uid_tx + ");");
                else
                    db.execSQL("update app set down=down+" + uid_rx + " , up=up+" + uid_tx + " where package = '" + name + "';");
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        db.close();*/
    }
}