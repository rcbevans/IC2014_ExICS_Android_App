package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import rce10.ic.ac.uk.exics.Adapters.ExamListFragmentListAdapter;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.FragmentOnSwipeTouchListener;
import rce10.ic.ac.uk.exics.ViewModel.ExICSData;

public class ExamListFragment extends Fragment implements ExICS_Main_Child_Fragment_Interface {

    private static final String TAG_ROOM_NUMBER = "ROOM_NUMBER";
    private static final ExICSData exICSData = ExICSData.getInstance();
    private ExICS_Main_Fragment_Interface mCallbacks;
    private int roomNum;
    private ListView examDetailListView;

    public ExamListFragment() {
        // Required empty public constructor
    }

    public static ExamListFragment newInstance(int roomNum) {
        ExamListFragment fragment = new ExamListFragment();
        Bundle args = new Bundle();
        args.putInt(TAG_ROOM_NUMBER, roomNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomNum = getArguments().getInt(TAG_ROOM_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview_list, container, false);

        setView(view);

        return view;
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
        setView(getView());
    }

    private void setView(View view) {
        TextView title = (TextView) view.findViewById(R.id.tvOverviewListTitle);
        title.setText(String.format("Exams in Room %d", roomNum));

        examDetailListView = (ListView) view.findViewById(R.id.lvInformationList);
        examDetailListView.setOnTouchListener(new FragmentOnSwipeTouchListener(getActivity().getApplicationContext(), mCallbacks));
        ArrayList<Exam> exams = exICSData.getExams(roomNum);
        if (exams == null) {
            Toast.makeText(getActivity().getApplicationContext(), "There are no exams in this room!", Toast.LENGTH_SHORT).show();
            mCallbacks.fragmentViewUnavailable();
        } else {
            Exam[] examArray = new Exam[exams.size()];
            examArray = exams.toArray(examArray);
            examDetailListView.setAdapter(new ExamListFragmentListAdapter(getActivity(), R.layout.exam_list_item_layout, examArray));
            examDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ExamListFragmentListAdapter adapter = (ExamListFragmentListAdapter) examDetailListView.getAdapter();
                    Exam clickedExam = adapter.getExamAtPosition(position);
                    ExamDetailFragment examDetailFragment = ExamDetailFragment.newInstance(clickedExam.getRoom(), clickedExam.getExamSubModule());
                    mCallbacks.updateContentFragment(examDetailFragment);
                }
            });
        }
    }
}
