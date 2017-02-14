package com.example.android.guittone;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.example.android.guittone.R.menu.toolbar;


/**
 * A simple {@link Fragment} subclass.
 */
public class PowerFragment extends Fragment {


    public PowerFragment() {
        // Required empty public constructor
    }

    ArrayList<PowerChart> power = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_power, container, false);
        super.onCreate(savedInstanceState);
        //Toolbar myToolbar1 = (Toolbar) rootView.findViewById(R.id.my_toolbar1);
        //myToolbar1.setTitleTextAppearance(this.getActivity(), R.style.MyTitleTextAppearance);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Home");

        BarChart chart = (BarChart) rootView.findViewById(R.id.chart);
        JSONAsyncTask task = new JSONAsyncTask();
        task.execute();
        return rootView;
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
                if(url == null){}else{jsonResponse = makeHttpRequest(url);}
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
            JSONparser(pow);
            //Log.e("power",power + "");
            //powerTextView.setText(power);
            //Log.e("power",JSONparser(pow));
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
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
            InputStream inputStream = null;
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
                    //Toast toast = Toast.makeText(getActivity(),"Could not connect to server", Toast.LENGTH_SHORT);
                    //toast.show();
                }


            } catch (IOException e) {
                // TODO: Handle the exception
                Log.e("MainActivity", e.getMessage());
                //Toast toast = Toast.makeText(getApplicationContext(),"Could not connect to server", Toast.LENGTH_SHORT);
                //toast.show();
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
    public void JSONparser(String json){

        if (TextUtils.isEmpty(json)) {
            Log.e("null","ss");
            return;
        }


        JSONObject object;
        Log.e("powerarray", "");
        try {
            JSONArray baseJsonResponse = new JSONArray(json);
            int size = baseJsonResponse.length();
            for (int i=0; i<size; i++) {
                object = baseJsonResponse.getJSONObject(i);
                power.add(new PowerChart(Double.parseDouble(object.getJSONArray("value").getString(0)), Long.parseLong(object.getString("_id"))));
                Log.e("powerchart", object.getJSONArray("value").getString(0) + object.getString("_id") + "ss");
                Log.e("powerchart", power + "");
            }
        }catch (JSONException e){Log.e("1","d");}


        // If there are results in the features array
        //Log.e("powerarray",value + "");
        //Log.e("powerss",pow);
        //return pow ;
        return;


    }

}
