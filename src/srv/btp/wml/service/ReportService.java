package srv.btp.wml.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import srv.btp.wml.R;
import srv.btp.wml.data.State;
import srv.btp.wml.view.Form_Main;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

public class ReportService extends AsyncTask<String, Integer, Boolean> {

	// Ada kabar gembira untuk kita semua :v
	// Gabungan SubmitData dan Retrieve Data

	public static final String FIELD_TABLE_NAME = "reporting"; // Nama tabel
																// absen
	public static final int MAXIMUM_WAITING_TIME = 60000;
	// public static final String TABLE_ID = "authID";
	public static final String TABLE_LOCATION = "location";
	public static final String TABLE_SESSIONID = "sessionID";
	public static final String TABLE_KATEGORI = "kategori";
	public static final String TABLE_LAPORAN = "laporan";
	public static final String TABLE_SUCCESS = "success";

	// General
	static int respondCode = 0;
	public static boolean isDone = false;
	public static boolean isFail = false;

	private boolean isGetDataFailed;
	String URLService = PreferenceManager.getDefaultSharedPreferences(
			State.main_activity.getApplicationContext()).getString(
			"service_address",
			State.main_activity.getResources().getString(
					R.string.default_service));
	private ProgressDialog progressDialog = new ProgressDialog(
			(Activity) State.main_activity);

	public static String message = "Mengirim laporan... ";
	public static String SessionIDResult;
	protected CountDownTimer ctd = new CountDownTimer(MAXIMUM_WAITING_TIME, 200) {
		@Override
		public void onTick(long arg0) {
			Log.d("AbsenTicker", arg0 + " JSON waiting time.");
			progressDialog.setMessage(message
					+ "\nTekan tombol 'Back' untuk batal.");
		}

		@Override
		public void onFinish() {
			Log.e("ctd", "Forced Exit");
			progressDialog.cancel();
		}
	};

	// Submission Type
	String id_user;
	String latitude;
	String longitude;
	String SessionID;
	String kategori;
	String laporan;
	// Retrieving Type

	String userNameResult = "";
	InputStream inputStream = null;
	String result;
	public static boolean isNeedLogout = false;

	@Override
	protected void onPreExecute() {
		isDone = false;
		// Membersihkan State
		progressDialog.setMessage(message);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_anim;
		progressDialog.show();
		ctd.start();

		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				progressDialog.dismiss();
				if (!isDone) {
					isFail = true;
				}
				isDone = false;
				cancel(true);
				ctd.cancel();

			}
		});
	}

	@Override
	protected Boolean doInBackground(String... values) {
		if (values.length == 5) {
			Log.v("Report Service : status", "Valid data! Working...");
			// 0 : username
			if (values[0] != null) { // sessionID
				SessionID = values[0];
			}
			// 1 : passowrd
			if (values[1] != null) { // Longitude
				longitude = values[1];
			}

			if (values[2] != null) { // Latitude
				latitude = values[2];
			}

			if (values[3] != null) { // kategori
				kategori = values[3];
			}

			if (values[4] != null) { // laporan
				laporan = values[4];
			}

			int status = postData(SessionID, longitude, latitude, kategori,
					laporan);

			Log.v("Report : status", status + "");
			if (!isGetDataFailed) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	};

	@Override
	protected void onPostExecute(Boolean result) {
		ctd.cancel();
		this.progressDialog.dismiss();
		isDone = true;
		if (isGetDataFailed)
			isFail = true;
		else
			isFail = false;
	};

	private int postData(String sessionID, String longitude, String latitude,
			String kategori2, String laporan2) {
		String target_post = URLService
				+ State.main_activity.getResources().getString(
						R.string.extension_string) + FIELD_TABLE_NAME;

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(target_post);
		int code = -1;
		List<NameValuePair> nameValuePairs = null;
		try {
			nameValuePairs = new ArrayList<NameValuePair>(2);

			nameValuePairs.add(new BasicNameValuePair("sessionID", sessionID));
			nameValuePairs.add(new BasicNameValuePair(
					"location[longitude]", longitude));
			nameValuePairs.add(new BasicNameValuePair("location[latitude]",
					latitude));
			nameValuePairs.add(new BasicNameValuePair("kategori", kategori2));
			nameValuePairs.add(new BasicNameValuePair("laporan", laporan2));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			Log.v("Report : target", target_post);
			Log.d("Report : nameValuePairs", nameValuePairs.toString());
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			// waiting for da respond
			HttpEntity entity = response.getEntity();
			// String responseString = EntityUtils.toString(entity,
			// "iso-8859-1");
			getEntityContent(entity);
			code = response.getStatusLine().getStatusCode();
			return code;
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.toString());
			e.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;

		} catch (UnsupportedEncodingException e1) {
			Log.e("UnsupportedEncodingException", e1.toString());
			e1.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
		} catch (IllegalStateException e3) {
			Log.e("IllegalStateException", e3.toString());
			e3.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			return -1024;
		} catch (UnknownHostException e4) {
			Log.e("UnknownHost", e4.toString());
			e4.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			return -1024;
		} catch (IOException e) {
			Log.d("error Absen query io exception", e.getMessage());
			Log.d("error Absen server URL", target_post);
			Log.e("IOException", e.toString());
			e.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			return -8;
		}
		return code;
	}

	private void getEntityContent(HttpEntity httpEntity) {
		try {
			inputStream = httpEntity.getContent();
		} catch (IllegalStateException e) {
			Log.e("IllegalStateException", e.toString());
			e.printStackTrace();
			isFail = true;
			isDone = false;
		} catch (IOException e) {
			Log.e("IOException", e.toString());
			e.printStackTrace();
			isFail = true;
			isDone = false;
		}

		// Convert response to string using String Builder
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream, "iso-8859-1"), 65728);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				Log.d("BufferingData", "lineworks = " + line);
				sb.append(line + " ");
			}
			inputStream.close();
			Log.d("BufferingData", "Data= " + sb.toString());
			result = sb.toString();

		} catch (Exception e) {
			Log.e("StringBuilding & BufferedReader", "Error converting result "
					+ e.toString());
			isGetDataFailed = true;
		}
		// Mengolah result userID
		JSONObject jObject;
		try {
			jObject = (JSONObject) new JSONTokener(result).nextValue();
			Log.d("parse", jObject.toString());
			String success = jObject.getString(TABLE_SUCCESS);

		} catch (JSONException e) {
			isFail = true;
			isDone = false;
			Log.e("JSONException", "Error: " + e.toString());
			isGetDataFailed = true;
			e.printStackTrace();
		} // end: catch (JSONException e)
		catch (ClassCastException castE) {
			isFail = true;
			isDone = false;
			Log.e("ClassCasting", "Error: " + castE.toString());
			isGetDataFailed = true;
			try{
				JSONArray jA = new JSONArray(result);
				String jO = jA.getString(1);
				if(jO.toLowerCase(Locale.ENGLISH).equals("user not sign-in")){
					isNeedLogout  = true;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			castE.printStackTrace();
		}
	}

}
