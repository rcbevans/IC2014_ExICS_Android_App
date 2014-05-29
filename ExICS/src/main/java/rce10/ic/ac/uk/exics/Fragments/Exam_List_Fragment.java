package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import rce10.ic.ac.uk.exics.Adapters.ExamListFragmentListAdapter;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.FragmentOnSwipeTouchListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Exam_List_Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Exam_List_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Exam_List_Fragment extends Fragment {

    private static final String TAG_ROOM_NUMBER = "ROOM_NUMBER";
    private static final ExICSData exICSData = ExICSData.getInstance();
    private ExICS_Main_Fragment_Interface mCallbacks;
    private int roomNum;
    private ListView examDetailListView;

    public Exam_List_Fragment() {
        // Required empty public constructor
    }

    public static Exam_List_Fragment newInstance(int roomNum) {
        Exam_List_Fragment fragment = new Exam_List_Fragment();
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

        TextView title = (TextView) view.findViewById(R.id.tvOverviewListTitle);
        title.setText(String.format("Exams in Room %d", roomNum));

        examDetailListView = (ListView) view.findViewById(R.id.lvInformationList);
        examDetailListView.setOnTouchListener(new FragmentOnSwipeTouchListener(getActivity().getApplicationContext(), mCallbacks));
        ArrayList<Exam> exams = exICSData.getExams(roomNum);
        Exam[] examArray = new Exam[exams.size()];
        examArray = exams.toArray(examArray);
        examDetailListView.setAdapter(new ExamListFragmentListAdapter(getActivity(), R.layout.exam_list_item_layout, examArray));
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
}
