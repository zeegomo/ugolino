package com.example.android.guittone;



import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import android.os.Handler;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.entries;

/**
 * A simple {@link Fragment} subclass.
 */
public class PowerFragment extends Fragment {


    public PowerFragment() {
        // Required empty public constructor
    }

    ArrayList<PowerChart> power = new ArrayList<>();
    BarChart chart;
    TextView today_textview;
    TextView last30_textview;
    int totalpower;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_power, container, false);
        super.onCreate(savedInstanceState);

        //Initialize the power chart used to represent Arduino data
        chart = (BarChart) rootView.findViewById(R.id.chart);
        today_textview = (TextView) rootView.findViewById(R.id.today_textview);
        last30_textview = (TextView) rootView.findViewById(R.id.last30_textview);
        //API JSON request to retrieve Arduino data
        //JSONAsyncTask task = new JSONAsyncTask();
        //task.execute();
        callAsynchronousTask();

        return rootView;
    }




    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            power.clear();
                            JSONAsyncTask performBackgroundTask = new JSONAsyncTask();
                            // PerformBackgroundTask this class is the class that extends AsynchTask
                            performBackgroundTask.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 100000); //execute in every 50000 ms
    }





    private class JSONAsyncTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            // Create URL object
            URL url ;

            // Extract relevant fields from the JSON response
            String jsonResponse = "";
            try {

                url = createUrl("http://guittone.ddns.net:8081/power");
                if(url != null){jsonResponse = makeHttpRequest(url);}

            } catch (IOException e) {
                // TODO Handle the IOException
            }


            return jsonResponse;
        }


        @Override
        protected void onPostExecute(String pow) {
            //Checking for data
            if (pow == null) {
                return;
            }

            //Parsing the jsonresponse
            JSONparser(pow);

            //Actually populating the chart with data
            Draw();

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

        //Check data integrity
        if (TextUtils.isEmpty(json)) {
            Log.e("null","ss");
            return;
        }


        JSONObject object;

        try {

            //retrieves a JSONArray from the API json response
            JSONArray baseJsonResponse = new JSONArray(json);
            int size = baseJsonResponse.length();

            //repeats for every item of the JSONArray
            for (int i=0; i<size; i++) {

                //get object n. i from the JSONArray
                object = baseJsonResponse.getJSONObject(i);

                //get the id JSONObject from the current object
                JSONObject _id = object.getJSONObject("_id");

                //create a new PowerChart object with power per day
                power.add(new PowerChart(object.getInt("totalPower"), _id.getInt("day"),_id.getInt("month")));

                Log.d("powerchart", object.getDouble("totalPower") + "ss");
                Log.d("powerchart", power + "");
                Log.d("id", _id+ " id");
                Log.d("id", _id.getInt("day") + "day" + _id.getInt("month") +"month" );

            }
        }catch (JSONException e){Log.e("1","d");}

    }

    public void Draw(){
        totalpower = 0;
        List<BarEntry> entries = new ArrayList<>();
        entries.clear();
        for (int zz = 0; zz< power.size(); zz++){
            //add elements to the chart data
            if(zz<30){
                entries.add(new BarEntry( (float) zz, (float) power.get(zz).getPower()));
                totalpower += power.get(zz).getPower();
            }

        }

        //power textviews assignment
        today_textview.setText(String.valueOf(power.get(0).getPower()));
        last30_textview.setText(String.valueOf(totalpower));


        //just some strange variables
        Description desc = new Description();
        desc.setText("");

        // Data Configuration
        BarDataSet set = new BarDataSet(entries, "kWh");
        BarData data = new BarData(set);
        data.setValueTextSize(0f);
        data.setValueTextColor(Color.WHITE);
        data.setBarWidth(0.9f);

        // Y Axis
        YAxis left = chart.getAxisLeft();
        left.setDrawLabels(true); // no axis labels
        left.setDrawAxisLine(true); // no axis line
        left.setDrawGridLines(false); // no grid lines
        left.setDrawZeroLine(true); // draw a zero line
        chart.getAxisRight().setEnabled(false);

        // X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(0f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        // Chart Setting
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setScaleXEnabled(false);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDescription(desc);
        chart.setData(data);
        chart.setFitBars(true); // make the x-axis fit exactly all bars
        chart.invalidate();
    }




    }


