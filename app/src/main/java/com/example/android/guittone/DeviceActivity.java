package com.example.android.guittone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import static com.example.android.guittone.MainFragment.devices;

/**
 * Created by ${Giacomo} on ${04/01/2017}
 */

public class DeviceActivity extends AppCompatActivity {


    int position = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        position=getIntent().getIntExtra("extras",0);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextAppearance(this, R.style.MyTitleTextAppearance);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(DeviceActivity.this);
            }
        });

        setSupportActionBar(myToolbar);
        JSONAsyncTask task = new JSONAsyncTask();
        task.execute();


        final TextView deviceName = (TextView) findViewById(R.id.device_name_activity);
        deviceName.setText(devices.get(position).getmName());

        //powerTextView.setText("0.00");

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                alert.setMessage("Are you sure?");
                alert.setTitle("Delete Device");

                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        devices.remove(position);
                        Save();
                        Intent mainIntent = new Intent(DeviceActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });

        Button modifyButton = (Button) findViewById(R.id.modify_button);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the numbers View is clicked on.
            @Override
            public void onClick(View view) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DeviceActivity.this);
                final EditText edittext = new EditText(DeviceActivity.this);
                alert.setMessage("Enter Name");
                alert.setTitle("New Device");
                alert.setView(edittext);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = edittext.getText().toString();
                        devices.get(position).setmName(name);
                        deviceName.setText(devices.get(position).getmName());
                        Save();

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });
                alert.show();
            }
        });

    }


    public void Save(){
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(devices);
        Log.w(""+devices.size(),"array size on save");
        prefsEditor.putString("Devices", json);
        prefsEditor.apply();

    }

    private class JSONAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            // Create URL object
            URL url ;


            // Extract relevant fields from the JSON response and create an {@link Event} object

            String jsonResponse = "";
            try {

                url = createUrl("http://guittone.ddns.net:8081/power");
                if(url != null){jsonResponse = makeHttpRequest(url);}

            } catch (IOException e) {
                // TODO Handle the IOException
            }

            // Return the {@link Event} object as the result fo the {@link TsunamiAsyncTask}
            return jsonResponse;
        }


        @Override
        protected void onPostExecute(String pow) {
            if (pow == null) {
                return;
            }
            Log.e("pow",pow + "");

        }

        private URL createUrl(String stringUrl) {
            URL url;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e("", "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make an HTTP request to the given URL and return a String as the response.
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            //if (url == null){
            //    return jsonResponse;
            //}
            HttpURLConnection urlConnection = null;
            InputStream inputStream;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                //urlConnection.setRequestMethod("GET");
                //urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                if(urlConnection.getResponseCode()==200){
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e("MainActivity","" + urlConnection.getResponseCode());
                    //  Toast toast = Toast.makeText(getApplicationContext(),"Could not connect to server", Toast.LENGTH_SHORT);
                    //  toast.show();
                }


            } catch (IOException e) {
                // TODO: Handle the exception
                Log.e("MainActivity", e.getMessage());
                Toast toast = Toast.makeText(getApplicationContext(),"Could not connect to server", Toast.LENGTH_SHORT);
                toast.show();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                //if (inputStream != null) {
                //function must handle java.io.IOException here
                //  inputStream.close();
                // }
            }
            Log.e("jsonresponde",jsonResponse);
            return jsonResponse;

        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }




    }




}
