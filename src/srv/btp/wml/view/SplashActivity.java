package srv.btp.wml.view;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import srv.btp.wml.R;
import srv.btp.wml.data.FontEstablishment;
import srv.btp.wml.data.State;
import srv.btp.wml.service.CategoryService;
import srv.btp.wml.service.LoginService;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class SplashActivity extends Activity {

	Intent mStartActivity;
	private Timer Timer_Service_Countdown = new Timer(true);
	private TextView txtLoading;
	private CategoryService cs;

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		State.splash_activity = this;
		// Lalu jalankan semua servis countdown...
		Timer_Service_Countdown.schedule(timerService, Calendar.getInstance()
				.getTime(), 1000);
		mStartActivity = new Intent(this, Form_Main.class);
		cs = new CategoryService();
		cs.execute();

	}

	private TimerTask timerService = new TimerTask() {
		@Override
		public void run() {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				State.splash_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						RunTask();

					}
				});

			} else {
				RunTask();
			}
		};
	};

	protected void RunTask() {
		if (CategoryService.isDone) {
			CategoryService.isDone = false;
			State.splash_activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {

					Log.e("Category", "Read status Category isFail = "
							+ CategoryService.isFail);
					if (CategoryService.isFail) {
						CategoryService.isFail = false;
						Log.d("RESPOND", "Failed" + "");
						CallError();
					} else {
						Log.d("RespondContent", "a");
						initDataAndStartMain();
					}
				}
			});
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		initializeComponents();
	}

	private void initializeComponents() {
		txtLoading = (TextView) findViewById(R.id.txtLoading);
		// initialize styles
		FontEstablishment.setCustomFont(txtLoading, "myriad.otf",
				txtLoading.getContext());
	}

	public void initDataAndStartMain() {
		Log.d("Er", State.CategoryList.length + " size");
		State.CompressCategory();
		startActivity(mStartActivity);
		finish();
	}

	@Override
	public void onBackPressed() {

	}

	void CallError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				State.splash_activity);
		builder.setTitle("Error");
		builder.setMessage("Gagal menghubungi server. Pastikan anda terkoneksi ke internet dan silahkan muat ulang.");
		builder.setCancelable(false);
		builder.setNegativeButton("Keluar",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		builder.setPositiveButton("Muat Ulang", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				cs = new CategoryService();
				cs.execute();
				return;
			}
		});

		AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}
}
