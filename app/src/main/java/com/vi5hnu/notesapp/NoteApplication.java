package com.vi5hnu.notesapp;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class NoteApplication extends Application {
    public NoteApplication(){
        super();
    }
}