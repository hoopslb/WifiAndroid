package com.sunkeun.wifiandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by sunke on 12/26/2015.
 */
public abstract class ManagedReceiver extends BroadcastReceiver {
    private boolean mRegistered = false;
    private Context mContext = null;

    @Override
    public void onReceive(Context context, Intent intent){
    }

    public void unregister(){
        if(mRegistered && mContext != null){
            mContext.unregisterReceiver(this);
            mRegistered = false;
            mContext = null;
        }
    }

    public void register(Context context){
        if(mRegistered)
            unregister();

        context.registerReceiver(this, getFilter());
        mRegistered = true;
        mContext = context;
    }

    public abstract IntentFilter getFilter();

    protected void finalize() throws Throwable{
        unregister();
        super.finalize();
    }
}