package srv.btp.wml.view;

import srv.btp.wml.R;
import srv.btp.wml.data.FontEstablishment;
import srv.btp.wml.data.State;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class SplashActivity extends Activity {
	
	Intent mStartActivity;
	protected CountDownTimer ctd = new CountDownTimer(2500, 200) {
		@Override
		public void onTick(long arg0) {
			
		}

		@Override
		public void onFinish() {
			//TODO: Load Main Activity
			StartMain();
		}
	};
	private TextView txtLoading;
	
	@Override 
	public void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		ctd.start();
		mStartActivity = new Intent(
				this, Form_Main.class);
		
	}
	
	@Override
    public void onStart() {
        super.onStart();
        initializeComponents();
	}
	
	private void initializeComponents() {
		txtLoading = (TextView)findViewById(R.id.txtLoading);
		//initialize styles
    	FontEstablishment.setCustomFont(txtLoading, "myriad.otf", txtLoading.getContext());
    	
		
	}

	public void StartMain(){

		startActivity(mStartActivity);
		finish();
	}
	
	@Override
	public void onBackPressed(){
		
	}
}
