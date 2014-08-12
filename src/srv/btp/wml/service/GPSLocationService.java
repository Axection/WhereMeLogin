package srv.btp.wml.service;

import java.util.Timer;
import java.util.TimerTask;

import srv.btp.wml.R;
import srv.btp.wml.data.State;
import srv.btp.wml.view.Form_Main;
import srv.btp.wml.view.Frag_Form_Presence;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class GPSLocationService {
	/***
	 * GPSLocationService.java
	 * 
	 * Kelas ini dibuat untuk mendapatkan lokasi tablet via GPS/a-GPS serta
	 * translasi lokasi GPS menjadi nama kota (LBS) Fungsi yang dibutuhkan : -
	 * Fungsi dapatin lokasi device - Fungsi menyocokkan lokasi dengan daftar
	 * kota
	 * 
	 * 
	 * Info menyusul
	 */
	private boolean location_flag = false;
	private static MyLocationListener location_listener;
	private LocationManager location_manager;
	@SuppressWarnings("unused")
	private LocationResult locationResult;
	
	private static final int DISTANCE_LOCK = 0;
	private static Context baseContext = State.main_activity.getBaseContext();
	

	// Fast Data Move
	private double current_longitude = 0;
	private double current_latitude = 0;
	
	private ImageView GPSIndicator;
	

	
	private static final String GPS_MOCK_PROVIDER = "GpsMockProvider";
	public static boolean isMocked = false;
	// public CountDownTimer cd;
	private static boolean isChecking;

	public static Boolean displayGpsStatus() {
		ContentResolver contentResolver = baseContext.getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(
				contentResolver, LocationManager.GPS_PROVIDER);
		if (gpsStatus) {
			return true;

		} else {
			return false;
		}
	}

	public GPSLocationService(ImageView indicator) {
		super();
		GPSIndicator = indicator;
		location_manager = (LocationManager) baseContext
				.getSystemService(Context.LOCATION_SERVICE);
		RecreateTimer();

	}

	private void RecreateTimer() {
		// ctd = new Timer();
		// ctd.schedule(new GetLastLocation(), SCAN_TIME);

	}

	public void StopGPS() {
		location_manager.removeUpdates(location_listener);
	}

	@Suppress()
	public boolean ActivateGPS() {
		if (isChecking)
			return false;
		isChecking = true;
		Log.d("GPS", "Activation");
		location_flag = displayGpsStatus();
		location_listener = new MyLocationListener();
		if (location_flag) {
			Toast.makeText(baseContext, "GPS Menyala. Lakukan scanning...",
					Toast.LENGTH_SHORT).show();

			location_manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, DISTANCE_LOCK, /***
					 * telah
					 * diganti dari SCAN_TIME
					 */
					location_listener);
			// GPS nyala, siapkan indikator :D
			State.isGPSConnected = true;
			RecreateTimer();

			GPSIndicator.setImageResource(R.drawable.indicator_gps_warn);
			Form_Main.txtGPS.setText("GPS Status : Scanning");
			isChecking = false;
			return true;
		} else {
			// GPS mati. siapkan indikator mati :(
			GPSIndicator.setImageResource(R.drawable.indicator_gps_off);
			Form_Main.txtGPS.setText("GPS Status : OFF");

			// mengetes mock
			if (!location_manager
					.isProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER)) {
				// Membuat test mock provider
				try {
					location_manager.addTestProvider(
							GPSLocationService.GPS_MOCK_PROVIDER, false, false,
							false, false, true, false, false, 0, 5);
					location_manager.setTestProviderEnabled(
							GPSLocationService.GPS_MOCK_PROVIDER, true);

					Log.e("ERROR GGENERATION", "HAX");

				} catch (SecurityException e) {
					AlertDialog.Builder builder;
					builder = new AlertDialog.Builder(State.main_activity);
					String msg = "Terdeteksi GPS anda mati. Harap segera nyalakan GPS device anda.";
					builder.setTitle("PERINGATAN KERAS");
					builder.setMessage(msg);
					builder.setCancelable(false);
					builder.setNegativeButton("Lanjutkan Saja",
							new DialogInterface.OnClickListener() {
								// DO NOTHING
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									isChecking = false;
								}
							});
					builder.setPositiveButton("Menyalakan GPS",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Jalankan GPS Setting
									Intent enableGPSIntent = new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									State.main_activity.startActivityForResult(
											enableGPSIntent,
											State.REQUEST_ENABLE_GPS);
								}
							});

					AlertDialog alert = builder.create();
					alert.show();

				}
			}

			// summon mock
			if (location_manager
					.isProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER)) {
				GPSIndicator.setImageResource(R.drawable.indicator_gps_mocked);
				Form_Main.txtGPS.setText("GPS Status : Mocked!");
				location_manager.requestLocationUpdates(
						GPSLocationService.GPS_MOCK_PROVIDER, 0, 0,
						location_listener);
				isMocked = true;
				State.isGPSConnected = true; // khusus mocking
				isMocked = true;

				// dan beritahu bahwa program sedang dalam mode testing
				String msg = "";
				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(State.main_activity);
				msg = "Program terdeteksi sedang berjalan dalam mode testing dan SANGAT DILARANG digunakan dalam operasional. Apabila anda tidak mengetahui apa yang sedang terjadi, harap segera nyalakan GPS device dan restart aplikasi untuk keluar dari mode testing.";
				builder.setTitle("PERINGATAN KERAS");
				builder.setMessage(msg);
				builder.setCancelable(false);
				builder.setNegativeButton("Lanjutkan Aplikasi",
						new DialogInterface.OnClickListener() {
							// DO NOTHING
							@Override
							public void onClick(DialogInterface dialog, int id) {
								isChecking = false;
							}
						});
				builder.setPositiveButton("Restart",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// Jalankan GPS Setting
								Intent enableGPSIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								State.main_activity.startActivityForResult(
										enableGPSIntent,
										State.REQUEST_ENABLE_GPS);
							}
						});

				AlertDialog alert = builder.create();
				try {
					alert.show();
				} catch (Exception e) {

				}
				// Tambahin langsung locationmovement
				Location location = new Location(
						GPSLocationService.GPS_MOCK_PROVIDER);
				location.setLatitude(-6.92);
				location.setLongitude(107.66666);
				location.setTime(System.currentTimeMillis());
				location.setAccuracy(0);
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					location.setElapsedRealtimeNanos(System.currentTimeMillis());
				} else {
					location.setSpeed(0);
				}

				// show debug message in log

				// provide the new location
				LocationManager locationManager = (LocationManager) State.main_activity
						.getSystemService(Context.LOCATION_SERVICE);
				locationManager.setTestProviderLocation(
						GPSLocationService.GPS_MOCK_PROVIDER, location);
				// end alert
			}
			return false;
		}
	}
	
	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			/**
			 * Setiap posisinya berpindah, listener ini akan terus terupdate.
			 * Mendapatkan lokasi baru dan dicocokkan dengan daftar kota.
			 */
			// ctd.cancel(); //OBSOLETE
			State.isGPSWorking = true;
			if (!isMocked) {
				GPSIndicator.setImageResource(R.drawable.indicator_gps_on);
				Form_Main.txtGPS.setText("GPS Status : Locked!");
			}
			// Sekarang ini hanya untuk debugging
			Log.d("GPSLocationDebug", loc.getTime() + " timelock.");
			String txt = "Terdeteksi lokasi berpindah :\n Lat: "
					+ loc.getLatitude() + " Lng: " + loc.getLongitude();
			Log.d("GPSLocation", txt);
			current_longitude = loc.getLongitude();
			current_latitude = loc.getLatitude();
			PreferenceManager
					.getDefaultSharedPreferences(
							State.main_activity.getBaseContext()).edit()
					.putFloat("long", (float) current_longitude)
					.putFloat("lat", (float) current_latitude).commit();

			//Masukkan perihal yang diinginkan setelah lat & long
			// ditemukan

			// cd.start();
			// Animasi selesai
			// lastCity = current_city;
			State.latitude = (float) current_latitude;
			State.longitude = (float) current_longitude;
			RecreateTimer();
		}

		// Unused Callbacks
		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status == LocationProvider.AVAILABLE) {
				Frag_Form_Presence.GPSCode = 1;
				State.isGPSWorking = true;
				GPSIndicator.setImageResource(R.drawable.indicator_gps_on);
				Form_Main.txtGPS.setText("GPS Status : Locked!");
				
			} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				Frag_Form_Presence.GPSCode = 2;
				State.isGPSWorking = false;
				GPSIndicator.setImageResource(R.drawable.indicator_gps_warn);
				Form_Main.txtGPS.setText("GPS Status : Re-Scanning");
			}
			try{
				State.main_activity.runOnUiThread(Frag_Form_Presence.runUi);
			}
			catch(NullPointerException e){
				e.printStackTrace();
				//Handle some errors when location locked before logged in.
			}
		}

	}

	

	private static abstract class LocationResult {
		public abstract void gotLocation(Location location);
	}
}
