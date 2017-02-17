package com.example.android.guittone;


import android.content.pm.LabeledIntent;
import android.graphics.Color;
import android.icu.text.AlphabeticIndex;
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
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
import java.util.Date;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.text.SimpleDateFormat;
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


import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.attr.y;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.media.CamcorderProfile.get;
import static com.example.android.guittone.R.id.chart;
import static com.example.android.guittone.R.menu.toolbar;


/**
 * A simple {@link Fragment} subclass.
 */
public class PowerFragment extends Fragment {


    public PowerFragment() {
        // Required empty public constructor
    }

    ArrayList<PowerChart> power = new ArrayList<>();
    BarChart chart;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.activity_power, container, false);
        super.onCreate(savedInstanceState);
        //Toolbar myToolbar1 = (Toolbar) rootView.findViewById(R.id.my_toolbar1);
        //myToolbar1.setTitleTextAppearance(this.getActivity(), R.style.MyTitleTextAppearance);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Home");

        chart = (BarChart) rootView.findViewById(R.id.chart);
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
            Draw();
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
        List<BarEntry> entries = new ArrayList<>();
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
                JSONObject _id = object.getJSONObject("_id");
                power.add(new PowerChart(object.getDouble("totalPower"), _id.getInt("day"),_id.getInt("month")));
                Log.e("powerchart", object.getDouble("totalPower") + "ss");
                Log.e("powerchart", power + "");
                Log.e("id", _id+ " id");
                Log.e("id", _id.getInt("day") + "day" + _id.getInt("month") +"month" );
            }
        }catch (JSONException e){Log.e("1","d");}


        // If there are results in the features array
        //Log.e("powerarray",value + "");
        //Log.e("powerss",pow);
        //return pow ;


        return;


    }

    public void Draw(){
        List<BarEntry> entries = new ArrayList<>();
        int a = 0;
        for (int zz = 0; zz<30; zz++){
            entries.add(new BarEntry( (float) zz, (float) zz*4+1/*power.get(a).getPower()*/));
            Log.e("a", (float) zz +  "  x  " + (float) zz*4 );
            Log.e("entrie", entries.get(zz).getY() + "y" + entries.get(zz).getX() );

        }

        Description desc = new Description();
        desc.setText("");

        // Data Configuration
        BarDataSet set = new BarDataSet(entries, "Power");
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


