/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package srv.btp.wml.view;

import srv.btp.wml.R;
import srv.btp.wml.data.FontEstablishment;
import srv.btp.wml.data.State;
import srv.btp.wml.service.ReportService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Frag_Form_Report extends Fragment {
	// Declaring objects
	private Spinner comboKategori;
	private EditText txtReport;
	private Button btnReport;
	private Bundle loaded;
	private TextView title;
	protected ReportService report;

	static class CustomArrayAdapter<T> extends ArrayAdapter<T> {
		
		private T[] itemList;
		private Context ctx;
		
		public CustomArrayAdapter(Context ctx, T[] objects) {
			super(ctx, android.R.layout.simple_spinner_item, objects);
			this.ctx = ctx;
			this.itemList = objects;
		}

		// other constructors

		@Override
		public TextView getView(int position, View convertView, ViewGroup parent) {
			TextView v = (TextView)super.getView(position, convertView, parent);
			v.setText(itemList[position].toString());
			v.setTextColor(Color.BLACK);// choose your color :)
			v.setGravity(Gravity.LEFT + Gravity.CENTER);
			FontEstablishment.setCustomFont(v, "myriad.otf",
					v.getContext());
			return v;
		}
		
		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			View view = super.getView(position, convertView, parent);

			// we know that simple_spinner_item has android.R.id.text1 TextView:

			/* if(isDroidX) { */
			TextView text = (TextView) view.findViewById(android.R.id.text1);
			text.setTextColor(Color.BLACK);// choose your color :)
			text.setHeight((int)(45*getContext().getResources().getDisplayMetrics().density));
			//text.setBackgroundColor(Color.WHITE);
			//text.set
			text.setGravity(Gravity.LEFT + Gravity.CENTER);
			FontEstablishment.setCustomFont(text, "myriad.otf",
					text.getContext());
			/* } */

			return view;

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_form_report, container, false);

		if (savedInstanceState != null) {
			loaded = savedInstanceState;
		} else {
			// initializeComponents();
		}
		return v;
	}

	private void initializeComponents() {
		comboKategori = (Spinner) getActivity()
				.findViewById(R.id.comboKategori);
		txtReport = (EditText) getActivity().findViewById(R.id.txtReport);
		btnReport = (Button) getActivity().findViewById(R.id.btnReport);
		title = (TextView) getActivity().findViewById(R.id.labelReport);

		// Styles
		FontEstablishment.setCustomFont(txtReport, "myriad.otf",
				txtReport.getContext());
		FontEstablishment.setCustomFont(btnReport, "myriad-semibold.otf",
				btnReport.getContext());
		FontEstablishment.setCustomFont(title, "myriad-condensed.otf",
				title.getContext());

		// Listener stacks
		btnReport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CallSendData();

			}
		});
		String[] StringArray = new String[State.CategoryList.length];
		for (int a = 0; a < State.CategoryList.length; a++) {
			StringArray[a] = State.CategoryList[a][1];
		}
		// Initialize entries via State.CategoryList
		CustomArrayAdapter<String> spinnerArrayAdapter = new CustomArrayAdapter<String>(
				State.main_activity.getApplicationContext(), StringArray);

		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		comboKategori.setAdapter(spinnerArrayAdapter);
		comboKategori.setSelection(1);
	}

	private void CallSendData() {
		String msg = "";
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(State.main_activity);
		msg = "Apakah anda yakin? Laporan yang telah terkirim tidak akan dapat diubah kembali.";
		builder.setTitle("Mengirim");
		builder.setMessage(msg);
		builder.setCancelable(false);
		builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				report = new ReportService();
				String DATA_PRESS[] = {
						State.SessionID + "",
						State.longitude + "",
						State.latitude + "",
						State.To2From(State.CategoryList, comboKategori
								.getSelectedItem().toString(), 1, 0),
						txtReport.getText() + "" };
				try {
					report.execute(DATA_PRESS);
				} catch (Exception e) {
					e.printStackTrace();
					State.main_activity.CallPress();
				}
			}
		});
		builder.setNegativeButton("Tidak",
				new DialogInterface.OnClickListener() {
					// No
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		alert.setCanceledOnTouchOutside(false);
		alert.show();

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
			txtReport.setText(args.getString("report"));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the
		// fragment
		// outState.putInt(ARG_POSITION, mCurrentPosition);
		outState.putString("report", txtReport.getText() + "");
	}
}
