package com.arizatakip.system;

import android.app.Application;

import com.arizatakip.system.data.ArizaLogRepository;
import com.arizatakip.system.data.ArizaRepository;
import com.arizatakip.system.data.UserRepository;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ArizaRepository.init(this);
        UserRepository.init(this);
        ArizaLogRepository.init(this);
    }
}