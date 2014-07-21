package srv.btp.wml.view;

import srv.btp.wml.R;
import srv.btp.wml.data.FontEstablishment;
import srv.btp.wml.data.State;
import srv.btp.wml.service.LoginService;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class Frag_Form_Login extends Fragment {
    //Declaring objects
    EditText txtUsername;
    EditText txtPassword;
    Button btnLogin;
    Bundle loaded;
    TextView labelWML;
    CheckBox ckRemember;
    
	protected srv.btp.wml.service.LoginService logins;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.frag_form_login, container, false);
        
    	if (savedInstanceState != null) {
        	loaded = savedInstanceState;
        }else{
        	//initializeComponents();
        }
        return v;
    }

    private void initializeComponents() {
    	//initialize components
    	txtUsername = (EditText)getActivity().findViewById(R.id.txtUsername);
    	txtPassword = (EditText)getActivity().findViewById(R.id.txtPassword);
    	btnLogin = (Button)getActivity().findViewById(R.id.btnLogin);
    	labelWML = (TextView)getActivity().findViewById(R.id.labelWML);
    	ckRemember = (CheckBox)getActivity().findViewById(R.id.ckRemember);
    	
    	//initialize styles
    	FontEstablishment.setCustomFont(txtUsername, "myriad.otf", txtUsername.getContext());
    	FontEstablishment.setCustomFont(txtPassword, "myriad.otf", txtPassword.getContext());
    	FontEstablishment.setCustomFont(ckRemember, "myriad.otf", ckRemember.getContext());
    	FontEstablishment.setCustomFont(btnLogin, "myriad-semibold.otf", btnLogin.getContext());
    	FontEstablishment.setCustomFont(labelWML, "myriad-condensed.otf", labelWML.getContext());
    	
    	//Listener stacks
    	btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Login
				logins = new LoginService();
				//get the username password data
				String DATA_LOGIN[] = {txtUsername.getText() + "", txtPassword.getText()+ ""};
				Log.d("LoginService","initializing Login with data : " + txtUsername.getText() + " & " + txtPassword.getText());

				try {
					logins.execute(DATA_LOGIN);
				} catch (Exception e) {
					State.main_activity.CallPassword(-8);
				}
				
				
			}
		});
		
	}

    

	@Override
    public void onStart() {
        super.onStart();
        initializeComponents();
        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = loaded;
        if (args != null) {
        	txtUsername.setText(args.getString("username"));
        	txtPassword.setText(args.getString("password"));

        } 
    }

    

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current article selection in case we need to recreate the fragment
        outState.putString("username", txtUsername.getText()+"");
        outState.putString("password", txtPassword.getText()+"");
    }
}