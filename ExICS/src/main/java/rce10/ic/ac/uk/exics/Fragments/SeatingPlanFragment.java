package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rce10.ic.ac.uk.exics.Adapters.RoomSelectSpinnerAdapter;
import rce10.ic.ac.uk.exics.Adapters.SeatingPlanListAdapter;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.ExICSProtocol;
import rce10.ic.ac.uk.exics.Model.SeatingInformation;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.FragmentOnSwipeTouchListener;
import rce10.ic.ac.uk.exics.Utilities.ISO8601DateParser;
import rce10.ic.ac.uk.exics.ViewModel.ExICSData;

public class SeatingPlanFragment extends Fragment implements ExICS_Main_Child_Fragment_Interface {

    private static final ExICSData exICSData = ExICSData.getInstance();
    private static String TAG = SeatingPlanFragment.class.getName();
    private ExICS_Main_Fragment_Interface mCallbacks;
    private ProgressDialog loadingDataDialog = null;
    private ListView seatingPlanList = null;

    public SeatingPlanFragment() {
        // Required empty public constructor
    }

    public static SeatingPlanFragment newInstance() {
        SeatingPlanFragment fragment = new SeatingPlanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View seatingPlanView = inflater.inflate(R.layout.fragment_seating_plan, container, false);
        setView(seatingPlanView);
        return seatingPlanView;
    }

    private void setView(View seatingPlanView) {
        Spinner roomSelectSpinner = (Spinner) seatingPlanView.findViewById(R.id.spSeatingPlanRoomSpinner);
        seatingPlanList = (ListView) seatingPlanView.findViewById(R.id.lvSeatingPlanList);
        seatingPlanList.setOnTouchListener(new FragmentOnSwipeTouchListener(this.getActivity().getApplicationContext(), mCallbacks));

        Set<Integer> roomSet = exICSData.getAllRooms();
        ArrayList<String> roomsArrayList = new ArrayList<String>();
        for (int num : roomSet)
            roomsArrayList.add(String.valueOf(num));
        String[] rooms = new String[roomsArrayList.size()];
        rooms = roomsArrayList.toArray(rooms);
        RoomSelectSpinnerAdapter rssa = new RoomSelectSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, rooms);
        roomSelectSpinner.setAdapter(rssa);
        roomSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mCallbacks.getActivityContext(), "Downloading data", Toast.LENGTH_SHORT).show();
                downloadSeatingPlanTask downloadTask = new downloadSeatingPlanTask();
                downloadTask.execute(Integer.parseInt((String) parent.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(mCallbacks.getActivityContext(), "Please select a room", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (ExICS_Main_Fragment_Interface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void refreshView() {
        setView(getView());
    }


    private class downloadSeatingPlanTask extends AsyncTask<Integer, Void, ArrayList<SeatingInformation>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDataDialog = new ProgressDialog(mCallbacks.getActivityContext());
            loadingDataDialog.setTitle("Fetching Data");
            loadingDataDialog.setMessage(String.format("Loading seating plan data"));
            loadingDataDialog.show();
        }

        @Override
        protected ArrayList<SeatingInformation> doInBackground(Integer... params) {
            int roomNum = params[0];
            String requestURL = "http://146.169.44.162:8080/seatingPlan?";
            Calendar sessionStart = new GregorianCalendar();
            sessionStart.set(Calendar.MONTH, 4);
            sessionStart.set(Calendar.DATE, 2);
            Calendar sessionEnd = (Calendar) sessionStart.clone();
//            if(sessionStart.get(Calendar.HOUR_OF_DAY) < 1 || sessionStart.get(Calendar.HOUR_OF_DAY) == 1 && sessionStart.get(Calendar.MINUTE) < 30){
            sessionStart.set(Calendar.HOUR_OF_DAY, 0);
            sessionStart.set(Calendar.MINUTE, 0);
            sessionEnd.set(Calendar.HOUR_OF_DAY, 13);
            sessionEnd.set(Calendar.MINUTE, 30);
//            } else {
//                sessionStart.set(Calendar.HOUR_OF_DAY, 13);
//                sessionStart.set(Calendar.MINUTE, 30);
//                sessionEnd.set(Calendar.HOUR_OF_DAY, 23);
//                sessionEnd.set(Calendar.MINUTE, 59);
//            }

            CredentialsProvider credProvider = new BasicCredentialsProvider();
            credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(exICSData.getUsername(), exICSData.getPassword()));

            ISO8601DateParser dp = new ISO8601DateParser();
            List<NameValuePair> p = new LinkedList<NameValuePair>();

            p.add(new BasicNameValuePair("room", String.valueOf(roomNum)));
            p.add(new BasicNameValuePair("sessionStart", dp.toString(sessionStart)));
            p.add(new BasicNameValuePair("sessionEnd", dp.toString(sessionEnd)));

            String paramString = URLEncodedUtils.format(p, "utf-8");

            requestURL += paramString;

            try {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                client.setCredentialsProvider(credProvider);
                request.setURI(new URI(requestURL));
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String responseBody = client.execute(request,
                        responseHandler);

                JSONObject response = new JSONObject(responseBody);
                JSONObject seatingPlans = response.getJSONObject(ExICSProtocol.TAG_SP_SEATING_PLANS);
                Iterator<?> dates = seatingPlans.keys();
                ArrayList<SeatingInformation> results = new ArrayList<SeatingInformation>();
                while (dates.hasNext()) {
                    String date = (String) dates.next();
                    if (seatingPlans.get(date) instanceof JSONObject) {
                        JSONObject dateObj = seatingPlans.getJSONObject(date);
                        Iterator<?> rooms = dateObj.keys();
                        while (rooms.hasNext()) {
                            String room = (String) rooms.next();
                            if (dateObj.get(room) instanceof JSONArray) {
                                JSONArray info = dateObj.getJSONArray(room);
                                for (int i = 0; i < info.length(); i++) {
                                    JSONObject seatInfo = info.getJSONObject(i);
                                    String seatCourse = seatInfo.getString(ExICSProtocol.TAG_SP_COURSE);
                                    String seatClass = seatInfo.getString(ExICSProtocol.TAG_SP_CLASS);
                                    int seatNum = seatInfo.getInt(ExICSProtocol.TAG_SP_SEAT);
                                    String seatCID = seatInfo.getString(ExICSProtocol.TAG_SP_CID);
                                    results.add(new SeatingInformation(roomNum, seatCourse, seatClass, seatNum, seatCID));
                                }
                            }
                        }
                    }
                }
                return results;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<SeatingInformation> seatingInformations) {
            super.onPostExecute(seatingInformations);
            SeatingInformation[] res = new SeatingInformation[seatingInformations.size()];
            res = seatingInformations.toArray(res);
            if (seatingPlanList != null) {
                SeatingPlanListAdapter spla = new SeatingPlanListAdapter(getActivity(), R.layout.seating_plan_list_item, res);
                seatingPlanList.setAdapter(spla);
            }
            loadingDataDialog.dismiss();
            if (seatingInformations.size() == 0) {
                Toast.makeText(mCallbacks.getActivityContext(), "There is no exam seating info available for this exam, please select another", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled(ArrayList<SeatingInformation> seatingInformations) {
            super.onCancelled(seatingInformations);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

}
