package com.example.baka2.Tools;

import android.os.Binder;
import android.os.IBinder;

import com.example.baka2.Tools.ClientData;

public class ObjectWrapperForBinder extends Binder {

    private final Object mData;

    public ObjectWrapperForBinder(Object data) {
        mData = data;
    }

    public Object getData() {
        return mData;
    }
}
