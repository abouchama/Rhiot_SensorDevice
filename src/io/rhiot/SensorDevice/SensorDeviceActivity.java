package io.rhiot.SensorDevice;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SensorDeviceActivity extends Activity {

	protected static final long DELTA = 1000;
	protected long lastUpdateAcc = System.currentTimeMillis();
	protected long lastUpdateLinearAcc = lastUpdateAcc;
	protected long lastUpdateGravity = lastUpdateAcc;

	private SensorManager mSensorManager;

	//private TextView mTextViewAccelerometer_x;
    public TextView mTextViewAccelerometer_x;
    private TextView mTextViewAccelerometer_y;
    private TextView mTextViewAccelerometer_z;
	private TextView mTextViewGravity_x;
    private TextView mTextViewGravity_y;
    private TextView mTextViewGravity_z;
	private TextView mTextViewLinearAcceleration_x;
    private TextView mTextViewLinearAcceleration_y;
    private TextView mTextViewLinearAcceleration_z;
	private TextView mTextViewLight;
	private TextView mTextViewTemperature;
	private TextView mTextViewOrientation_x;
    private TextView mTextViewOrientation_y;
    private TextView mTextViewOrientation_z;
	private TextView mTextViewMagneticField_x;
    private TextView mTextViewMagneticField_y;
    private TextView mTextViewMagneticField_z;
	private TextView mTextViewPressure;
	private TextView mTextViewRotationVector_x;
    private TextView mTextViewRotationVector_y;
    private TextView mTextViewRotationVector_z;
    private TextView mTextViewLatitude;
    private TextView mTextViewLongitude;
    private TextView mTextViewCountry;
    private TextView mTextViewCity;
    private TextView mTextViewPostalCode;
    private TextView mTextViewAddressLine;
    private TextView mTextViewResult;
    private EditText edit_text_RestURL;
    private Button myStartButton;
    private Button myStopButton;

    private SensorEventListener mEventListenerAccelerometer;
	private SensorEventListener mEventListenerGravity;
	private SensorEventListener mEventListenerLinearAcceleration;
	private SensorEventListener mEventListenerLight;
	private SensorEventListener mEventListenerTemperature;
	private SensorEventListener mEventListenerOrientation;
	private SensorEventListener mEventListenerMagneticField;
	private SensorEventListener mEventListenerPressure;
	private SensorEventListener mEventListenerRotationVector;


    //String Accelerometer, Gravity, LinearAcceleration, Light;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTextViewAccelerometer_x = (TextView) findViewById(R.id.text_accelerometer_x);
        mTextViewAccelerometer_y = (TextView) findViewById(R.id.text_accelerometer_y);
        mTextViewAccelerometer_z = (TextView) findViewById(R.id.text_accelerometer_z);
		mTextViewGravity_x = (TextView) findViewById(R.id.text_gravity_x);
        mTextViewGravity_y = (TextView) findViewById(R.id.text_gravity_y);
        mTextViewGravity_z = (TextView) findViewById(R.id.text_gravity_z);
		mTextViewLinearAcceleration_x = (TextView) findViewById(R.id.text_linear_acceleration_x);
        mTextViewLinearAcceleration_y = (TextView) findViewById(R.id.text_linear_acceleration_y);
        mTextViewLinearAcceleration_z = (TextView) findViewById(R.id.text_linear_acceleration_z);
		mTextViewLight = (TextView) findViewById(R.id.text_light);
		mTextViewTemperature = (TextView) findViewById(R.id.text_temperature);
		mTextViewOrientation_x = (TextView) findViewById(R.id.text_orientation_x);
        mTextViewOrientation_y = (TextView) findViewById(R.id.text_orientation_y);
        mTextViewOrientation_z = (TextView) findViewById(R.id.text_orientation_z);
		mTextViewMagneticField_x = (TextView) findViewById(R.id.text_magnetic_field_x);
        mTextViewMagneticField_y = (TextView) findViewById(R.id.text_magnetic_field_y);
        mTextViewMagneticField_z = (TextView) findViewById(R.id.text_magnetic_field_z);
		mTextViewPressure = (TextView) findViewById(R.id.text_pressure);
		mTextViewRotationVector_x = (TextView) findViewById(R.id.text_rotation_vector_x);
        mTextViewRotationVector_y = (TextView) findViewById(R.id.text_rotation_vector_y);
        mTextViewRotationVector_z = (TextView) findViewById(R.id.text_rotation_vector_z);
        mTextViewResult = (TextView) findViewById(R.id.text_result);
        mTextViewLatitude = (TextView)findViewById(R.id.fieldLatitude);
        mTextViewLongitude = (TextView)findViewById(R.id.fieldLongitude);
        mTextViewCountry = (TextView)findViewById(R.id.fieldCountry);
        mTextViewCity = (TextView)findViewById(R.id.fieldCity);
        mTextViewPostalCode = (TextView)findViewById(R.id.fieldPostalCode);
        mTextViewAddressLine = (TextView)findViewById(R.id.fieldAddressLine);

        edit_text_RestURL = (EditText) findViewById(R.id.edit_text_RestURL);
        myStartButton = (Button) findViewById(R.id.button_start);
        myStopButton = (Button) findViewById(R.id.button_stop);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();

        myStartButton.setVisibility(View.VISIBLE);
        myStopButton.setVisibility(View.GONE);


        //Start button action on click
            myStartButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    myStopButton.setVisibility(View.VISIBLE);
                    myStartButton.performClick();
                    myStartButton.setPressed(true);
                    myStartButton.invalidate();
                    myStartButton.post(runnableCode);
                    return true;
                }
            });

        //Stop button action on click
        myStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartButton.removeCallbacks(runnableCode);
                myStartButton.setVisibility(View.VISIBLE);
                myStopButton.setVisibility(View.GONE);
                //finish();
            }
        });
	}

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            //new HttpAsyncTask().execute(edit_text_RestURL.getText().toString());
            String URL = edit_text_RestURL.getText().toString();
            //String URL = "http://192.168.1.50:9080/Sensors/";
            //new HttpAsyncTask().execute("http://192.168.1.50:9080/Sensors/");
            new HttpAsyncTask().execute(URL);
            myStartButton.postDelayed(runnableCode,1000);
        }
    };

    //}
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getBaseContext(), "Sending Sensors Data!", Toast.LENGTH_LONG).show();
            myStartButton.setEnabled(true);
            mTextViewResult.setText(""+ result);
        }
    }

    public String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";


            // 3. build jsonObject

            JSONObject jsonParam = new JSONObject();
            Date dNow = new Date( );
            SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSS");
            ft.setTimeZone(TimeZone.getTimeZone("GMT"));
            System.out.println("Current Date: " + ft.format(dNow));
            jsonParam.put("event_datetime", ft.format(dNow));
            jsonParam.put("Accelerometer_x", mTextViewAccelerometer_x.getText().toString());
            jsonParam.put("Accelerometer_y", mTextViewAccelerometer_y.getText().toString());
            jsonParam.put("Accelerometer_z", mTextViewAccelerometer_z.getText().toString());
            jsonParam.put("Gravity_x", mTextViewGravity_x.getText().toString());
            jsonParam.put("Gravity_y", mTextViewGravity_y.getText().toString());
            jsonParam.put("Gravity_z", mTextViewGravity_z.getText().toString());
            jsonParam.put("LinearAcceleration_x", mTextViewLinearAcceleration_x.getText().toString());
            jsonParam.put("LinearAcceleration_y", mTextViewLinearAcceleration_y.getText().toString());
            jsonParam.put("LinearAcceleration_z", mTextViewLinearAcceleration_z.getText().toString());
            jsonParam.put("Orientation_x", mTextViewOrientation_x.getText().toString());
            jsonParam.put("Orientation_y", mTextViewOrientation_y.getText().toString());
            jsonParam.put("Orientation_z", mTextViewOrientation_z.getText().toString());
            jsonParam.put("Light", mTextViewLight.getText().toString());
            jsonParam.put("Magnetic_x", mTextViewMagneticField_x.getText().toString());
            jsonParam.put("Magnetic_y", mTextViewMagneticField_y.getText().toString());
            jsonParam.put("Magnetic_z", mTextViewMagneticField_z.getText().toString());
            jsonParam.put("Pressure", mTextViewPressure.getText().toString());
            jsonParam.put("RotationVector_x", mTextViewRotationVector_x.getText().toString());
            jsonParam.put("RotationVector_y", mTextViewRotationVector_y.getText().toString());
            jsonParam.put("RotationVector_z", mTextViewRotationVector_z.getText().toString());
            jsonParam.put("Location", mTextViewLatitude.getText().toString()+"," +mTextViewLongitude.getText().toString());
            jsonParam.put("Country", mTextViewCountry.getText().toString());
            jsonParam.put("PostalCode", mTextViewPostalCode.getText().toString());
            jsonParam.put("City", mTextViewCity.getText().toString());
            jsonParam.put("AddressLine", mTextViewAddressLine.getText().toString());

            // 4. convert JSONObject to JSON to String
            json = jsonParam.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
                else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

	private void initListeners() {
        // check if GPS enabled
        GPSTracker gpsTracker1 = new GPSTracker(this);
        String stringLatitude = String.valueOf(gpsTracker1.latitude);
        mTextViewLatitude.setText(stringLatitude);

        String stringLongitude = String.valueOf(gpsTracker1.longitude);
        mTextViewLongitude.setText(stringLongitude);

            String country = gpsTracker1.getCountryName(this);
            mTextViewCountry.setText(country);

            String city = gpsTracker1.getLocality(this);
            mTextViewCity.setText(city);

            String postalCode = gpsTracker1.getPostalCode(this);
            mTextViewPostalCode.setText(postalCode);

            String addressLine = gpsTracker1.getAddressLine(this);
            mTextViewAddressLine.setText(addressLine);

		mEventListenerAccelerometer = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
                mTextViewAccelerometer_x.setText(""+values[0]);
                mTextViewAccelerometer_y.setText(""+values[1]);
                mTextViewAccelerometer_z.setText(""+values[2]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerLinearAcceleration = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
                mTextViewLinearAcceleration_x.setText(""+ values[0]);
                mTextViewLinearAcceleration_y.setText(""+ values[1]);
                mTextViewLinearAcceleration_z.setText(""+ values[2]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerGravity = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
                mTextViewGravity_x.setText(""+ values[0]);
                mTextViewGravity_y.setText(""+ values[1]);
                mTextViewGravity_z.setText(""+ values[2]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerMagneticField = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
                mTextViewMagneticField_x.setText(""+ values[0]);
                mTextViewMagneticField_y.setText(""+ values[1]);
                mTextViewMagneticField_z.setText(""+ values[2]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerOrientation = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
                mTextViewOrientation_x.setText(""+ values[0]);
                mTextViewOrientation_y.setText(""+ values[1]);
                mTextViewOrientation_z.setText(""+ values[2]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerTemperature = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
                mTextViewTemperature.setText(""+ values[0]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerLight = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
				mTextViewLight.setText(""+ values[0]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerPressure = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
				mTextViewPressure.setText("" + values[0]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		mEventListenerRotationVector = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				float[] values = event.values;
				mTextViewRotationVector_x.setText(""+values[0]);
                mTextViewRotationVector_y.setText(""+values[1]);
                mTextViewRotationVector_z.setText(""+values[2]);
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
	}


	@Override
	protected void onResume() {
		super.onResume();
        // check if GPS enabled
        GPSTracker gpsTracker = new GPSTracker(this);
        if (gpsTracker.getIsGPSTrackingEnabled())
        {
            String stringLatitude = String.valueOf(gpsTracker.latitude);
            mTextViewLatitude.setText(stringLatitude);

            String stringLongitude = String.valueOf(gpsTracker.longitude);
            mTextViewLongitude.setText(stringLongitude);

            String country = gpsTracker.getCountryName(this);
            mTextViewCountry.setText(country);

            String city = gpsTracker.getLocality(this);
            mTextViewCity.setText(city);

            String postalCode = gpsTracker.getPostalCode(this);
            mTextViewPostalCode.setText(postalCode);

            String addressLine = gpsTracker.getAddressLine(this);
            mTextViewAddressLine.setText(addressLine);
        }
        else
        {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }
		mSensorManager.registerListener(mEventListenerAccelerometer,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerLinearAcceleration,
				mSensorManager
						.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerGravity,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerMagneticField,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerOrientation,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerTemperature,
				mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerLight,
				mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerPressure,
				mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mEventListenerRotationVector,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(mEventListenerAccelerometer);
		mSensorManager.unregisterListener(mEventListenerLinearAcceleration);
		mSensorManager.unregisterListener(mEventListenerGravity);
		mSensorManager.unregisterListener(mEventListenerMagneticField);
		mSensorManager.unregisterListener(mEventListenerOrientation);
		mSensorManager.unregisterListener(mEventListenerTemperature);
		mSensorManager.unregisterListener(mEventListenerLight);
		mSensorManager.unregisterListener(mEventListenerPressure);
		mSensorManager.unregisterListener(mEventListenerRotationVector);
		super.onStop();
	}
}