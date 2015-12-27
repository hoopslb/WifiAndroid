package com.sunkeun.wifiandroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView mStatusText;
    Button button;
    ListView listView;
    WifiManager mWifiManager;
    List<ScanResult> scanResult;
    Boolean mScanning=false;
    private ScanAdapter mAdapter = null;
    ScanReceiver mScanReceiver=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        mStatusText = (TextView) findViewById(R.id.textView);
        listView = (ListView) findViewById(R.id.listView);

        mWifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        mStatusText.append("WiFi State: \n"+mWifiManager.getWifiState()+"\n\n");
        mStatusText.append("Connection Info: \n"+mWifiManager.getConnectionInfo().toString()+"\n\n");
        mStatusText.append("DHCO Info: \n"+mWifiManager.getDhcpInfo().toString()+"\n\n");
        //scanResult = new ScanResult();
        // Permission Problem!
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
        }
        //
        mAdapter = new ScanAdapter();
        listView.setAdapter(mAdapter);
        ScanReceiver mScanReceiver = new ScanReceiver();
        mScanReceiver.register(this);
    }
    public void onButtonClicked(View v){
        if(!mWifiManager.isWifiEnabled()){
            //Toast.makeText(getApplicationContext(),"WiFi Disabled Turing on!",Toast.LENGTH_SHORT).show();
            mWifiManager.setWifiEnabled(true);
        }
        mScanning = true;
        mWifiManager.startScan();
    }
    private class ScanReceiver extends ManagedReceiver{
        private IntentFilter mFilter = null;

        public ScanReceiver(){
            mFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        }

        public IntentFilter getFilter(){
            return mFilter;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onReceive(Context context, Intent intent){
            Toast.makeText(getApplicationContext(), "WiFi Scanned!", Toast.LENGTH_SHORT).show();
            if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                //Toast.makeText(getApplicationContext(),"SCAN_RESULTS_AVAILABLE_ACTION",Toast.LENGTH_SHORT).show();
                if(mScanning){
                    mAdapter.reset();

                    List<ScanResult> results = mWifiManager.getScanResults();

                    for(ScanResult result : results){
                        mAdapter.addResult(result);
                    }

                    mScanning = false;
                    mStatusText.setText("Scan Finished!");
                }

                mAdapter.notifyDataSetChanged();
            }
        }

    }
    public class ScanAdapter extends ArrayAdapter<ScanResult> {
        private ArrayList<ScanResult> mResults = null;

        public ScanAdapter() {
            super(MainActivity.this, R.layout.wifi_scanner_list_item);
            mResults = new ArrayList<ScanResult>();
        }

        public void addResult(ScanResult result) {
            for (ScanResult res : mResults) {
                if (res.BSSID.equals(result.BSSID))
                    return;
            }

            mResults.add(result);

            Collections.sort(mResults, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    if (lhs.level > rhs.level)
                        return -1;

                    else if (rhs.level > lhs.level)
                        return 1;

                    else
                        return 0;
                }
            });
        }
        public void reset(){
            mResults.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount(){
            return mResults.size();
        }

        @Override
        public ScanResult getItem(int position){
            return mResults.get(position);
        }

        public int getWifiIcon(ScanResult wifi){
            int level = Math.abs(wifi.level);

            if(wifi.capabilities.contains("WPA") || wifi.capabilities.contains("WEP")){
                if(level <= 76)
                    return R.drawable.ic_wifi_lock_signal_4;

                else if(level <= 87)
                    return R.drawable.ic_wifi_lock_signal_3;

                else if(level <= 98)
                    return R.drawable.ic_wifi_lock_signal_2;

                else
                    return R.drawable.ic_wifi_lock_signal_1;
            } else{
                if(level <= 76)
                    return R.drawable.ic_wifi_signal_4;

                else if(level <= 87)
                    return R.drawable.ic_wifi_signal_3;

                else if(level <= 98)
                    return R.drawable.ic_wifi_signal_2;

                else
                    return R.drawable.ic_wifi_signal_1;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            //Toast.makeText(getApplicationContext(),"ScanAdapter getView()!",Toast.LENGTH_SHORT).show();
            View row = convertView;
            ResultHolder holder = null;

            if(row == null){
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.wifi_scanner_list_item, parent, false);

                holder = new ResultHolder();

                holder.supported = (ImageView) (row != null ? row.findViewById(R.id.supported) : null);
                holder.powerIcon = (ImageView) (row != null ? row.findViewById(R.id.powerIcon) : null);
                holder.ssid = (TextView) (row != null ? row.findViewById(R.id.ssid) : null);
                holder.bssid = (TextView) (row != null ? row.findViewById(R.id.bssid) : null);

                if(row != null)
                    row.setTag(holder);

            } else{
                holder = (ResultHolder) row.getTag();
            }

            ScanResult result = mResults.get(position);

            holder.powerIcon.setImageResource(getWifiIcon(result));
            holder.ssid.setTypeface(null, Typeface.BOLD);
            holder.ssid.setText(result.SSID);

            String protection = "<b>Open</b>";
            boolean isOpen = true;

            List<String> capabilities = Arrays.asList(result.capabilities.split("[\\-\\[\\]]"));

            if(capabilities.contains("WEP")){
                isOpen = false;
                protection = "<b>WEP</b>";
            } else if(capabilities.contains("WPA2")){
                isOpen = false;
                protection = "<b>WPA2</b>";
            } else if(capabilities.contains("WPA")){
                isOpen = false;
                protection = "<b>WPA</b>";
            }

            if(capabilities.contains("PSK"))
                protection += " PSK";

            if(capabilities.contains("WPS"))
                protection += " ( WPS )";

            holder.bssid.setText(Html.fromHtml(
                            result.BSSID.toUpperCase() + " " + protection + " <small>( " + (Math.round((result.frequency / 1000.0) * 10.0) / 10.0) + " Ghz )</small>")
            );
            /*
            if(mWifiMatcher.getKeygen(result) != null || isOpen)
                holder.supported.setImageResource(R.drawable.ic_possible);

            else
                holder.supported.setImageResource(R.drawable.ic_impossible);
            */
            holder.supported.setImageResource(R.drawable.ic_possible);
            return row;
        }

        class ResultHolder{
            ImageView supported;
            ImageView powerIcon;
            TextView ssid;
            TextView bssid;
        }
    }


}
