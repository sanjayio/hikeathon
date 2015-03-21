/*
I have used Kitkat for testing purposes.
It works fine on kitkat.
Please wait for the gps signal in the location tab. it takes some time.
apk is found in the root directory of the zip file.
name od apk is app-debug.apk
i have used facebook api
for location and phone movement i have used hardware api's
thats it.
thanks for the hackathon!
-sanjay kumar
 */

package in.sanjaykumara.hikeathon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import android.content.Context;
import android.content.pm.Signature;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;

public class MainActivity extends Activity implements SensorEventListener, LocationListener{
    private TextView latitude, longitude;
    private String TAG = "MainActivity";
    private TextView lblEmail, name, birthday, loc, result;
    private LocationManager locationManager;
    private String provider;
    Double[] old_values;
    Sensor accelerometer;
    SensorManager sm;
    TextView acceleration, user_link, id, gender, locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "in.sanjaykumara.hikeathon",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        old_values = new Double[3];


        TabHost tb = (TabHost) findViewById(R.id.tabHost);
        tb.setup();

        TabHost.TabSpec ts1 = tb.newTabSpec("social");
        ts1.setContent(R.id.social);
        ts1.setIndicator("Social");
        tb.addTab(ts1);

        TabHost.TabSpec ts2 = tb.newTabSpec("phone");
        ts2.setContent(R.id.phone);
        ts2.setIndicator("Phone");
        tb.addTab(ts2);

        TabHost.TabSpec ts3 = tb.newTabSpec("phone_location");
        ts3.setContent(R.id.phone_location);
        ts3.setIndicator("Location");
        tb.addTab(ts3);


        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latitude.setText("Lat: Location not available");
            longitude.setText("Long: Location not available");
        }



        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        acceleration = (TextView)findViewById(R.id.acceleration);
        result = (TextView)findViewById(R.id.result);



        lblEmail = (TextView) findViewById(R.id.lblEmail);
        name = (TextView) findViewById(R.id.user_name);
        birthday = (TextView) findViewById(R.id.birthday);
        loc = (TextView) findViewById(R.id.fb_location);
        user_link = (TextView) findViewById(R.id.user_link);
        id = (TextView) findViewById(R.id.id);
        gender = (TextView) findViewById(R.id.gender);
        locale = (TextView) findViewById(R.id.locale);

        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setOnErrorListener(new OnErrorListener() {

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "Error " + error.getMessage());
            }
        });
        // set permission list
        authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes"));
        // session state call back event
        authButton.setSessionStatusCallback(new Session.StatusCallback() {

            @Override
            public void call(Session session, SessionState state, Exception exception) {

                if (session.isOpened()) {
                    Log.i("Facebook", "Logged In");
                    Log.i(TAG, "Access Token" + session.getAccessToken());
                    Request.executeMeRequestAsync(session,
                            new Request.GraphUserCallback() {
                                @Override
                                public void onCompleted(GraphUser user, Response response) {
                                    if (user != null) {
                                        try {
                                            Log.i(TAG, "User ID " + user.getId());
                                            id.setText("ID: " + user.getId());
                                        } catch(NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            Log.i(TAG, "Email " + user.asMap().get("email"));
                                            lblEmail.setText("Email: " + user.asMap().get("email").toString());
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            name.setText("Name: " + user.getName());
                                            Log.i(TAG, "Name " + user.getName());
                                        } catch(NullPointerException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            birthday.setText("Birthday: " + user.getBirthday());
                                            Log.i(TAG, "Birthday " + user.getBirthday());
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            loc.setText("Location: " + user.getLocation().getProperty("name").toString());
                                            Log.i(TAG, "Location " + user.getLocation().getProperty("name").toString());
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            user_link.setText("Link: " + user.getLink());
                                            Log.i(TAG, "Link " + user.getLink());
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            gender.setText("Gender: " + user.getProperty("gender").toString());
                                            Log.i(TAG, "Gender " + user.getProperty("gender").toString());
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            locale.setText("Locale: " + user.getProperty("locale").toString());
                                            Log.i(TAG, "Locale " + user.getProperty("locale").toString());
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });
                } else if(state.isClosed()) {
                    Log.i("Facebook", "Logged Out");
                }

            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        final Double alpha = 0.8;

        Double[] gravity = new Double[3];
        Double[] linear_acceleration = new Double[3];
        for(int i = 0; i < 3; i++) {
            gravity[i] = 0.0;
            linear_acceleration[i] = 0.0;
            old_values[i] = 0.0;
        }

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        acceleration.setText(String.format("X: %s\nY: %s\nZ: %s", linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]));

        if((Math.abs(old_values[0] - linear_acceleration[0]) < 0.5) || (Math.abs(old_values[1] - linear_acceleration[1]) < 0.5) || (Math.abs(old_values[2] - linear_acceleration[2]) < 0.5)) {
            result.setText("The phone is at rest");
        }
        else {
            result.setText("The phone is moving");
        }


        old_values[0] = linear_acceleration[0];
        old_values[1] = linear_acceleration[1];
        old_values[2] = linear_acceleration[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        latitude.setText("Lat: " + String.valueOf(lat));
        longitude.setText("Long: " + String.valueOf(lng));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
}