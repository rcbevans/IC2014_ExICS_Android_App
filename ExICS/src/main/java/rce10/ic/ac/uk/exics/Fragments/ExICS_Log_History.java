package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.FragmentOnSwipeTouchListener;

public class ExICS_Log_History extends Fragment {

    ExICSData exICSData = ExICSData.getInstance();

    ScrollView logScrollView = null;
    TextView logText = null;

    ExICS_Main_Fragment_Interface mCallbacks;

    public ExICS_Log_History() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ExICS_Log_History newInstance() {
        ExICS_Log_History fragment = new ExICS_Log_History();
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
        View inflatedView = inflater.inflate(R.layout.fragment_ex_ics_log_history, container, false);
        logScrollView = (ScrollView) inflatedView.findViewById(R.id.svChatLog);
        logScrollView.setOnTouchListener(new FragmentOnSwipeTouchListener(this.getActivity().getApplicationContext(), mCallbacks));
        logText = (TextView) inflatedView.findViewById(R.id.tvExICSLog);
        updateLogText();
        return inflatedView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        updateLogText();
        try {
            mCallbacks = (ExICS_Main_Fragment_Interface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ExICS_Main_Fragment_Interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void updateLogText() {
        if (logText != null) {
            logText.setText(exICSData.getChatLog());
            if (logScrollView != null) {
                logScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        logScrollView.scrollTo(0, logText.getBottom());
                    }
                });
            }
        }
    }
}
