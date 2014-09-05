package srv.btp.wml.data;

import srv.btp.wml.BuildConfig;
import srv.btp.wml.view.Form_Main;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.text.TextUtils.StringSplitter;

public class State {
	//Value state
	
	//Yang diperlukan adalah status login, status absen, status laporan.
	public static int status = 0;
	public static final int STATUS_IDLE = 0;
	public static final int STATUS_LOGGED_IN = 1;
	public static final int STATUS_PRESENCED = 2;
	
	public static int AuthID = -1; //UserID yang akan disimpan di dalam preferences
	public static Form_Main main_activity;
	public static Fragment current_fragment;
	public static float longitude;
	public static float latitude;
	public static String SessionID = "";
	public static boolean isGPSConnected;
	public static String UserName;
	public static boolean isGPSWorking;
	public static boolean isRemembered;
	public final static int REQUEST_ENABLE_GPS = 0;
	public static String CategoryList[][];
	private static String inlineCategoryList;
	
	public static void RaiseInitialization(Form_Main Main){
		//Set the shared activity
		main_activity = Main;
		//Lalu load semua restore data;
		SharedPreferences Configuration = PreferenceManager.getDefaultSharedPreferences(
				main_activity.getBaseContext());
		
		status = Configuration.getInt("status", 0);
		AuthID = Configuration.getInt("AuthID", -1);
		SessionID = Configuration.getString("sessionID", "");
		longitude = Configuration.getFloat("long",0f);
		latitude = Configuration.getFloat("lat", 0f);
		UserName = Configuration.getString("username", "");
		isRemembered = Configuration.getBoolean("remember", false);
		BuildCategory(Configuration.getString("kategori",""));
	}
	
	public static boolean SaveData(){
		SharedPreferences Configuration = PreferenceManager.getDefaultSharedPreferences(
				main_activity.getBaseContext());
		CompressCategory();
		Configuration.edit()
		.putInt("status", status)
		.putInt("AuthID", AuthID)
		.putInt("user_id", AuthID)
		.putString("sessionID", SessionID)
		.putFloat("long", longitude)
		.putFloat("lat", latitude)
		.putString("username", UserName)
		.putBoolean("remember", isRemembered)
		.putString("kategori", inlineCategoryList)
		.commit();
		
		_ReloadInitialization();
		return true;
	}
	
	public static void BuildCategory(String category){
		//construction
		// 1|Waypoint/2|Resume/3|Laporan Awal/4|Lokasi
		int counter = 0;
		StringSplitter ss = new TextUtils.SimpleStringSplitter('/');
		ss.setString(category);
		for(String majorLine : ss){
			StringSplitter subSS = new TextUtils.SimpleStringSplitter('|');
			subSS.setString(majorLine);
			int miniLines = 0;
			for(String minorLine : subSS){
				CategoryList[counter][miniLines] = minorLine;
				miniLines ++;
			}
			counter++;
		}
	}
	
	public static void CompressCategory(){
		for(int a = 0;a<CategoryList.length;a++){
			inlineCategoryList += CategoryList[a][0] + "|" + CategoryList[a][1] + "/";
		}
		inlineCategoryList = inlineCategoryList.substring(0, inlineCategoryList.length()-2);
		
	}
	
	private static boolean _ReloadInitialization(){
		if(main_activity == null) return false;
		RaiseInitialization(main_activity);
		return true;
	}
}
