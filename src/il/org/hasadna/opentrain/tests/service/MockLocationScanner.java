package il.org.hasadna.opentrain.tests.service;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import il.org.hasadna.opentrain.service.LocationScanner;

/**
 * Created by Noam.m on 6/2/2014.
 */
public class MockLocationScanner extends LocationScanner {

    public MockLocationScanner(Context context) {
        super(context);
    }

    @Override
    public void onConnected() {
        startMockLocation();
    }

    private void startMockLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 7; i++) {
                    if (mLocationClient.isConnected()) {
                        mLocationClient.setMockMode(true);
                        Location mockLocation = LocationTestUtils.getMockLocation();
                        mLocationClient.setMockLocation(mockLocation);
                        reportNewLocationReceived(mockLocation);
                        try {
                            Thread.sleep(1000 * 15);
                        } catch (Exception e) {

                        }
                    }
                }
                LocationTestUtils.reportDone(mContext);
            }
        }).start();
    }
}