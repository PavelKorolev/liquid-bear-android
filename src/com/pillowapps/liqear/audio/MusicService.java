package com.pillowapps.liqear.audio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int getCurrentPosition() {
        return 0;
    }

    public int getCurrentBuffer() {
        return 0;
    }
}
