package com.ebanswers.attendance;

import com.ebanswers.attendance.R;
import com.example.util.AbstractActivity;

import android.content.Context;  
import android.content.SharedPreferences;  
import android.content.SharedPreferences.Editor;  
import android.os.Bundle;  
import android.view.View;  
import android.widget.Button;  
import android.widget.EditText;  
import android.widget.Toast;
  
public class config extends AbstractActivity {  
    private EditText nameEditText = null;  
    private EditText ageEditText = null;  
    private Button saveButton = null;  
    private Button resumeButton = null;  
    //private ToggleButton togglebutton; 
    
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState); 
        initUI(savedInstanceState, R.layout.config);
      
    }

	@Override
	public void findView() {
		// TODO Auto-generated method stub
		nameEditText = (EditText) findViewById(R.id.name);  
        ageEditText = (EditText) findViewById(R.id.age);  
        saveButton = (Button) findViewById(R.id.button);  
        resumeButton = (Button) findViewById(R.id.resume);  
	}

	@Override
	public void fillData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setListener() {
		// TODO Auto-generated method stub
		
		saveButton.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_WORLD_READABLE);  
                Editor editor = sharedPreferences.edit();  
                editor.putString("LocalIPAddr", nameEditText.getText().toString());  
                editor.putString("GateIPAddr", nameEditText.getText().toString());
                editor.putString("MaskAddr", nameEditText.getText().toString());
                editor.putInt("Id", new Integer(ageEditText.getText()  
                        .toString()));  
                editor.putInt("Rs485enable", new Integer(ageEditText.getText()  
                        .toString())); 
                editor.putInt("Rs232enable", new Integer(ageEditText.getText()  
                        .toString())); 
                editor.putInt("Rate", new Integer(ageEditText.getText()  
                        .toString())); 
                editor.putInt("PassWord", new Integer(ageEditText.getText()  
                        .toString())); 
                editor.putInt("WeiGenIn", new Integer(ageEditText.getText()  
                        .toString())); 
                editor.putInt("WeiGenOut", new Integer(ageEditText.getText()  
                        .toString())); 
                editor.commit();  
                Toast.makeText(config.this, R.string.success, Toast.LENGTH_LONG)  
                .show();  
            }  
        });  
          
        resumeButton.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                SharedPreferences sharedPreferences = getSharedPreferences("test", Context.MODE_PRIVATE);  
                String name = sharedPreferences.getString("name", "");  
                int age = sharedPreferences.getInt("age", 20);  
                nameEditText.setText(name);  
                ageEditText.setText(String.valueOf(age));  
            }  
        });  
		
	}

	@Override
	public Bundle saveData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadData(Bundle bundle) {
		// TODO Auto-generated method stub
		
	}  
}  
