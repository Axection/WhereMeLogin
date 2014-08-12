package srv.btp.wml.view;

import srv.btp.wml.R;
import srv.btp.wml.data.FontEstablishment;
import srv.btp.wml.data.State;
import srv.btp.wml.service.AbsenService;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class Frag_Form_Presence extends Fragment {
	// Declaring Objects
	private ImageButton btnAbsen;
	private TextView lblNotice1;
	private TextView lblNotice2;
	private TextView lblAbsen;
	private Bundle loaded;
	private int internetStatus;
	private int GPSStatus;
	public static int GPSCode = 0;
	private int lastGPSCode = 0;

	private AbsenService presence;
	public static Runnable runUi;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.frag_form_presence, container, false);
		if (savedInstanceState != null) {
			loaded = savedInstanceState;
		}

		// Inflate the layout for this fragment
		return v;
	}

	private void setState() {
		if(lastGPSCode == GPSCode)return;
		Log.e("RunnableThreadUI", "Beginning setState with GPSCode=" + GPSCode);
		if (GPSCode == 1) {
			btnAbsen.setImageResource(R.drawable.absen_ok_rec);
			btnAbsen.setClickable(true);
			btnAbsen.setEnabled(true);
		} else if (GPSCode == 2) {
			btnAbsen.setImageResource(R.drawable.absen_no_ok_rec);
			btnAbsen.setClickable(false);
			btnAbsen.setEnabled(false);
		}
		lastGPSCode = GPSCode;
	}

	private void initializeComponents() {
		lblNotice1 = (TextView) getActivity().findViewById(R.id.lblNotice);
		lblNotice2 = (TextView) getActivity().findViewById(R.id.lblNotice2);
		lblAbsen = (TextView) getActivity().findViewById(R.id.labelAbsensi);

		btnAbsen = (ImageButton) getActivity().findViewById(R.id.btnPress);

		// Styles
		FontEstablishment.setCustomFont(lblNotice1, "myriad.otf",
				lblNotice1.getContext());
		FontEstablishment.setCustomFont(lblNotice2, "myriad.otf",
				lblNotice2.getContext());
		FontEstablishment.setCustomFont(lblAbsen, "myriad-condensed.otf",
				lblAbsen.getContext());

		// Listener stacks
		btnAbsen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				presence = new AbsenService();
				String DATA_PRESS[] = { State.AuthID + "",
						State.longitude + "", State.latitude + "" };
				try {
					presence.execute(DATA_PRESS);
				} catch (Exception e) {
					e.printStackTrace();
					State.main_activity.CallPress();
				}

			}
		});

		runUi = new Runnable() {
			@Override
			public void run() {
				Log.e("RunnableThreadUI", "Run SetState");
				setState();
			}
		};
		
		//State.main_activity.runOnUiThread(runUi);
	}

	@Override
	public void onStart() {
		super.onStart();
		initializeComponents();
		// During startup, check if there are arguments passed to the fragment.
		// onStart is a good place to do this because the layout has already
		// been
		// applied to the fragment at this point so we can safely call the
		// method
		// below that sets the article text.
		Bundle args = loaded;
		if (args != null) {
			internetStatus = args.getInt("network");
			GPSStatus = args.getInt("GPS");

		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		outState.putInt("GPS", GPSStatus);
		outState.putInt("network", internetStatus);
	}
}