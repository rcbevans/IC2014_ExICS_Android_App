package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.R;

public class InvigilationPlanFragment extends Fragment implements ExICS_Main_Child_Fragment_Interface {

    public static final String TAG_IP_SETTINGS = "Invigilation Plan Settings";
    private static final String TAG_CB_PAPERS = "cbIPpaper";
    private static final String TAG_CB_ANSWERS = "cbIPanswers";
    private static final String TAG_CB_CHEATING = "cbIPcheating";
    private static final String TAG_CB_SHUFFLE = "cbIPshuffle";
    private static final String TAG_CB_COLLECT = "cbIPcollect";
    private static final String TAG_CB_REPORT = "cbIPreport";
    private static final String TAG_CB_COLLEAGUE = "cbIPcolleagues";

    private ExICS_Main_Fragment_Interface mCallbacks = null;

    public InvigilationPlanFragment() {
        // Required empty public constructor
    }

    public static void clearSavedPreferences(Context appContext) {
        SharedPreferences sp = appContext.getSharedPreferences(TAG_IP_SETTINGS, Context.MODE_PRIVATE);
        sp.edit().remove(TAG_CB_ANSWERS)
                .remove(TAG_CB_CHEATING)
                .remove(TAG_CB_COLLEAGUE)
                .remove(TAG_CB_COLLECT)
                .remove(TAG_CB_PAPERS)
                .remove(TAG_CB_REPORT)
                .remove(TAG_CB_SHUFFLE)
                .commit();
    }

    public static InvigilationPlanFragment newInstance() {
        InvigilationPlanFragment fragment = new InvigilationPlanFragment();
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
        View view = inflater.inflate(R.layout.fragment_invigilation_plan, container, false);
        setUpView(view);
        return view;
    }

    private void setUpView(View view) {
        final SharedPreferences sp = mCallbacks.getActivityContext().getSharedPreferences(TAG_IP_SETTINGS, Context.MODE_PRIVATE);
        final CheckBox papers = (CheckBox) view.findViewById(R.id.cbIPpaper);
        papers.setChecked(sp.getBoolean(TAG_CB_PAPERS, false));
        papers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_PAPERS, papers.isChecked()).commit();
            }
        });
        final CheckBox answers = (CheckBox) view.findViewById(R.id.cbIPanswers);
        answers.setChecked(sp.getBoolean(TAG_CB_ANSWERS, false));
        answers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_ANSWERS, answers.isChecked()).commit();
            }
        });
        final CheckBox cheating = (CheckBox) view.findViewById(R.id.cbIPcheating);
        cheating.setChecked(sp.getBoolean(TAG_CB_CHEATING, false));
        cheating.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_CHEATING, cheating.isChecked()).commit();
            }
        });
        final CheckBox shuffle = (CheckBox) view.findViewById(R.id.cbIPshuffle);
        shuffle.setChecked(sp.getBoolean(TAG_CB_SHUFFLE, false));
        shuffle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_SHUFFLE, shuffle.isChecked()).commit();
            }
        });
        final CheckBox collect = (CheckBox) view.findViewById(R.id.cbIPcollect);
        collect.setChecked(sp.getBoolean(TAG_CB_COLLECT, false));
        collect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_COLLECT, collect.isChecked()).commit();
            }
        });
        final CheckBox report = (CheckBox) view.findViewById(R.id.cbIPreport);
        report.setChecked(sp.getBoolean(TAG_CB_REPORT, false));
        report.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_REPORT, report.isChecked()).commit();
            }
        });
        final CheckBox colleagues = (CheckBox) view.findViewById(R.id.cbIPcolleagues);
        colleagues.setChecked(sp.getBoolean(TAG_CB_COLLEAGUE, false));
        colleagues.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean(TAG_CB_COLLEAGUE, colleagues.isChecked()).commit();
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
                    + " must implement ExICS_Main_Fragment_Interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void refreshView() {
        return;
    }
}
