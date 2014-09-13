package srv.btp.wml.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import srv.btp.wml.R;
import srv.btp.wml.data.State;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

public class CategoryService extends AsyncTask<String, Integer, Boolean> {

	// Ada kabar gembira untuk kita semua :v
	// Gabungan SubmitData dan Retrieve Data

	public static final String FIELD_TABLE_NAME = "kategori"; // Nama tabel
																// absen
	public static final int MAXIMUM_WAITING_TIME = 30000;
	// public static final String TABLE_ID = "authID";
	public static final String TABLE_ID = "location";
	public static final String TABLE_CATEGORY = "sessionID";

	public static final String STATUS_SUCCESS = "success";

	// General
	static int respondCode = 0;
	public static boolean isDone = false;
	public static boolean isFail = false;

	private boolean isGetDataFailed;
	String URLService = PreferenceManager.getDefaultSharedPreferences(
			State.splash_activity.getApplicationContext()).getString(
			"service_address",
			State.splash_activity.getResources().getString(
					R.string.default_service));

	public static String message = "";
	public static String SessionIDResult;
	protected CountDownTimer ctd = new CountDownTimer(MAXIMUM_WAITING_TIME, 200) {
		@Override
		public void onTick(long arg0) {
			Log.d("AbsenTicker", arg0 + " JSON waiting time.");

		}

		@Override
		public void onFinish() {
			Log.e("ctd", "Forced Exit");

			isFail = true;
			isDone = true;
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
	public static boolean isReportingBackground = false;

	@Override
	protected void onPreExecute() {
		isDone = false;
		// Membersihkan State
		ctd.start();
	}

	@Override
	protected Boolean doInBackground(String... values) {
		int status = postData(/* SessionID, longitude, latitude, kategori, laporan */);

		Log.v("Report : status", status + "");
		if (!isGetDataFailed) {
			return true;
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

		isDone = true;
		if (isGetDataFailed)
			isFail = true;
		else
			isFail = false;
	};

	private int postData() {
		String target_post = URLService
				+ State.splash_activity.getResources().getString(
						R.string.extension_string) + FIELD_TABLE_NAME;

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httppost = new HttpGet(target_post);
		int code = -1;
		try {
			HttpResponse response = httpclient.execute(httppost);
			// waiting for da respond
			HttpEntity entity = response.getEntity();
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
		//
		//
		//
		// Mengolah result userID
		//
		//
		//
		//
		try {
			JSONArray jA = new JSONArray(result);
			State.CategoryList = new String[jA.length()][];
			for (int i = 0; i < jA.length(); i++) {
				State.CategoryList[i] = new String[2];
				JSONArray arrayInside = jA.getJSONArray(i);
				Log.d("parse", jA.getJSONArray(i).toString());
				State.CategoryList[i][0] = arrayInside.getString(0);
				State.CategoryList[i][1] = arrayInside.getString(1);
				
			}
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
			try {
				JSONArray jA = new JSONArray(result);
				String jO = jA.getString(1);
				if (jO.toLowerCase(Locale.ENGLISH).equals("user not sign-in")) {
					isNeedLogout = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			castE.printStackTrace();
		}
	}

}
