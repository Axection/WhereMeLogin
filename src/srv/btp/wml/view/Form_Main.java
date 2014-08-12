package srv.btp.wml.view;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import srv.btp.wml.R;
import srv.btp.wml.data.State;
import srv.btp.wml.service.AbsenService;
import srv.btp.wml.service.GPSLocationService;
import srv.btp.wml.service.LoginService;
import srv.btp.wml.service.LogoutService;
import srv.btp.wml.service.ReportService;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class Form_Main extends FragmentActivity {

	// Object List
	private Timer Timer_Service_Countdown = new Timer(true);
	private static MenuItem logout;
	private static MenuItem signout;
	public static TextView txtGPS;
	public static TextView txtNetwork;
	private ImageView gps_indicator;
	private GPSLocationService gls;
	protected LogoutService logouts;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.form_main);
		
		gps_indicator = (ImageView) findViewById(R.id.indicator_gps);
		txtGPS = (TextView) findViewById(R.id.txt_gps);
		txtNetwork = (TextView) findViewById(R.id.txtNetwork);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Masukkan fragment yang seharusnya muncul pertama kali
			// 
			State.RaiseInitialization(this);
			setFragmentView(State.status);

			// Lalu jalankan semua servis countdown...
			Timer_Service_Countdown.schedule(timerService, Calendar.getInstance().getTime(),
					1000);
			gls = new GPSLocationService(gps_indicator);
			//gls.ActivateGPS();
		}
	}
	
	public static void MsgBox(CharSequence title, CharSequence text){
			AlertDialog.Builder builder = new AlertDialog.Builder(State.main_activity);
			builder.setTitle(title);
			builder.setMessage(text);
			builder.setCancelable(false);
			builder.setPositiveButton("OK", null);
			AlertDialog alert = builder.create();
			alert.setCanceledOnTouchOutside(false);
			alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
			alert.show();
	}
	
	
	void CallPassword(int ErrorCode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(State.main_activity);
		builder.setTitle("Login");
		switch (ErrorCode){
		case 0:
			builder.setMessage("Silahkan login terlebih dahulu untuk mengatur konfigurasi sistem.");
			break;
		case -1:
			builder.setMessage("Login gagal, username & password tidak ditemukan atau salah.");
			break;
		case -1024:
			builder.setMessage("Login gagal, periksa kembali koneksi.");
			break;
		default:
			builder.setMessage("Login gagal, terjadi galat. Cobalah beberapa saat lagi.");
			break;
		}
	
		// View theContent = inflater.inflate(R.layout.sign, null);
		// builder.setView(theContent);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				return;
			}
		});
		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(false);
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.show();
	}

	public static void setFragmentView(int status) {
		FragmentActivity fg = (FragmentActivity) State.main_activity;
		FragmentTransaction ft = fg.getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.flyfadein, R.anim.flyfadeout);
		
		if (State.current_fragment != null)
			ft.remove(State.current_fragment);
		try {
			switch (status) {
			case State.STATUS_IDLE:
				Frag_Form_Login form_login = new Frag_Form_Login();
				form_login.setArguments(fg.getIntent().getExtras());
				ft.add(R.id.container, form_login);
				State.current_fragment = form_login;
				State.main_activity.invalidateOptionsMenu();
				break;
			case State.STATUS_LOGGED_IN:
				Frag_Form_Presence form_presence = new Frag_Form_Presence();
				form_presence.setArguments(fg.getIntent().getExtras());
				ft.add(R.id.container, form_presence);
				State.current_fragment = form_presence;
				State.main_activity.invalidateOptionsMenu();
				break;
			case State.STATUS_PRESENCED:
				Frag_Form_Report form_report = new Frag_Form_Report();
				form_report.setArguments(fg.getIntent().getExtras());
				ft.add(R.id.container, form_report);
				State.current_fragment = form_report;
				State.main_activity.invalidateOptionsMenu();
				break;

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			ft.commit();
		}
		State.SaveData();
	}

	// Untuk bikin menu logout
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.form__main, menu);
		logout = menu.findItem(R.id.action_logout);
		signout = menu.findItem(R.id.action_signout);
		switch (State.status) {
		case State.STATUS_IDLE:
			logout.setVisible(false);
			signout.setVisible(false);
			break;
		case State.STATUS_LOGGED_IN:
			logout.setVisible(true);
			signout.setVisible(false);
			break;
		case State.STATUS_PRESENCED:
			logout.setVisible(false);
			signout.setVisible(true);

			break;

		}
		return true;
	}
	
	private void ForceLogout(){
		State.status = State.STATUS_LOGGED_IN;
		Form_Main.setFragmentView(State.status);
		State.SessionID = "";
		State.SaveData();
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        msg = "Terdeteksi waktu kerja anda telah berakhir. Silahkan lakukan presensi ulang.";
        builder.setTitle("Peringatan");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
        alert.show();
		
	}
	private void CallLogout(){
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        msg = "Apakah anda yakin ingin menutup sesi kerja anda hari ini?";
        builder.setTitle("Logout");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				//Sistem logout

				logouts = new LogoutService();
				//get the username password data
				String DATA_LOGIN[] = {
						State.AuthID + "", //1 - auth
						State.longitude + "", //2 - longitude
						State.latitude + "", //3 - latitude
						State.SessionID + "" //4 - sessionID
				};
				Log.d("LogoutService","initializing Logout...");

				try {
					logouts.execute(DATA_LOGIN);
				} catch (Exception e) {
					State.main_activity.CallLogoutError();
				}
				
				

			}
		});
        builder.setNegativeButton("Tidak",
            new DialogInterface.OnClickListener() {
              	//No
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
            });
        AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
        alert.show();
	}
	
	private void CallDeauthorize(){
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        msg = "Meng-deauthorize device ini menyebabkan anda perlu login kembali agar dapat melakukan presensi. Apakah anda yakin ingin melanjutkan?";
        builder.setTitle("Deauthorize");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				//Sistem Deauthorize
				State.AuthID = -1;
				State.SessionID = "";
				//State.UserName = "";
				State.SaveData();
				State.status = State.STATUS_IDLE;
				Form_Main.setFragmentView(State.status);
				Frag_Form_Presence.GPSCode = -1;
				
			}
		});
        builder.setNegativeButton("Tidak",
            new DialogInterface.OnClickListener() {
              	//No
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
            });
        AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
        alert.show();
	}
	
	private void CallExit(){
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        msg = "Apakah anda yakin ingin keluar aplikasi?";
        builder.setTitle("Exit");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				
				timerService.cancel();
				gls.StopGPS();
				Frag_Form_Presence.GPSCode = -1;
				finish();
			}
		});
        builder.setNegativeButton("Tidak",
            new DialogInterface.OnClickListener() {
              	//No
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
            });
        AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
        alert.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_logout) {
			//Deauthorize
			CallDeauthorize();
			return true;
		}
		if (id == R.id.action_signout) {
			//LOGOUUTTTTT
			CallLogout();
			return true;
		}
		if (id == R.id.action_exit) {
			CallExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed(){
		//super.onBackPressed();
		CallExit();
		//System.exit(0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("Form_Main", "onActivityResult " + resultCode);
		switch (requestCode) {

		case State.REQUEST_ENABLE_GPS:
			// Restart~
			Intent mStartActivity = new Intent(
					State.main_activity.getBaseContext(), Form_Main.class);
			int mPendingIntentId = 45556;
			PendingIntent mPendingIntent = PendingIntent.getActivity(
					State.main_activity.getBaseContext(), mPendingIntentId,
					mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager) State.main_activity
					.getBaseContext().getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400,
					mPendingIntent);
			onBackPressed();

		}
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		State.isGPSWorking = false;
		gls.ActivateGPS();
		Log.d("MainForm","onPostCreate");
		
		
		
	}

	// CallPassword Extension
	private TimerTask timerService = new TimerTask() {
		@Override
		public void run() {
			//LoginService Division
			if (LoginService.isDone) {
				// LoginUpdate.cancel();
				LoginService.isDone = false;
				State.main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("LoginUpdate",
								"Read status LoginService isFail = "
										+ LoginService.isFail);
						if (LoginService.isFail) {
							LoginService.isFail = false;
							Log.d("RESPOND",LoginService.respondCode + "");
							CallPassword(LoginService.respondCode);
						} else {
							// Simpan Auth ID dan lainnya
							State.status = State.STATUS_LOGGED_IN;
							Form_Main.setFragmentView(State.status);

							
						}
					}
				});
			}
			//AbsenService
			//Log.d("AbsenServiceTimer", AbsenService.isDone + "");
			//AbsenService
			if (AbsenService.isDone) {
				AbsenService.isDone = false;
				State.main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("AbsenService",
								"Read status Absen isFail = "
										+ AbsenService.isFail);
						if (AbsenService.isFail) {
							AbsenService.isFail = false;
							Log.d("RESPOND",AbsenService.SessionIDResult + "");
							CallPress();
						} else {
							Log.d("AbsenService RESULT","Wah, accessed");
							// Simpan Auth ID dan lainnya
							State.status = State.STATUS_PRESENCED;
							Form_Main.setFragmentView(State.status);

							
						}
					}
				});
			}
			
			//LogoutService
			if (LogoutService.isDone) {
				LogoutService.isDone = false;
				State.main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("LogoutService",
								"Read status Logout isFail = "
										+ LogoutService.isFail);
						if (LogoutService.isFail) {
							LogoutService.isFail = false;
							Log.d("LogoutRespond",LogoutService.SessionIDResult + "");
							if(LogoutService.isNeedLogout){
								LogoutService.isNeedLogout = false;
								ForceLogout();
								
							}else
								CallLogoutError();
						} else {
							Log.d("LogoutService RESULT","Wah, accessed");
							// Simpan Auth ID dan lainnya
							State.status = State.STATUS_LOGGED_IN;
							Form_Main.setFragmentView(State.status);
						}
					}
				});
			}
			
			//ReportService
			if (ReportService.isDone) {
				ReportService.isDone = false;
				State.main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("ReportService",
								"Read status Report isFail = "
										+ ReportService.isFail);
						if (ReportService.isFail) {
							ReportService.isFail = false;
							Log.d("ReportService",ReportService.SessionIDResult + "");
							if(ReportService.isNeedLogout){
								ReportService.isNeedLogout = false;
								ForceLogout();
							}else
							MsgBox("Laporan","Laporan gagal dikirim.");
							
						} else {
							Log.d("ReportService RESULT","Wah, accessed");
							MsgBox("Laporan","Laporan terkirim.");
							//Terkirim
							
						}
					}
				});
			}
			
			//GPSLocation Service
			if(!GPSLocationService.displayGpsStatus() && !GPSLocationService.isMocked){
				State.main_activity.runOnUiThread(Frag_Form_Presence.runUi);
				State.main_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
							gls.ActivateGPS();
						}
				});
			}else{
				Log.d("isGPSWorking",State.isGPSWorking + "");
				if(State.isGPSWorking){
					Frag_Form_Presence.GPSCode = 1;
				}else{
					Frag_Form_Presence.GPSCode = 2;
				}
				State.main_activity.runOnUiThread(Frag_Form_Presence.runUi);
				
			}
			
			//Rolling~
			Log.d("Timer Service", "Rolling Scanning.");	
		}
	};
	// End CallPassword

	void CallPress() {
		AlertDialog.Builder builder = new AlertDialog.Builder(State.main_activity);
		builder.setTitle("Absen");
		builder.setMessage("Pengajuan presensi gagal. Mohon periksa kembali apakah internet anda aktif atau sudah pernah melakukan presensi sebelumnya di device lain.");
		// View theContent = inflater.inflate(R.layout.sign, null);
		// builder.setView(theContent);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				return;
			}
		});
		AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}
	
	void CallLogoutError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(State.main_activity);
		builder.setTitle("Logout");
		builder.setMessage("Gagal mengakhiri sesi kerja. Pastikan sebelumnya internet anda aktif.");
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				return;
			}
		});
		AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}

}
