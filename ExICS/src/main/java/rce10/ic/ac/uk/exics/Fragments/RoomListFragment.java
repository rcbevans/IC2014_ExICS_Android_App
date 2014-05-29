package rce10.ic.ac.uk.exics.Fragments;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import rce10.ic.ac.uk.exics.Adapters.RoomListFragmentListAdapter;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.FragmentOnSwipeTouchListener;

public class RoomListFragment extends Fragment implements ExICS_Main_Child_Fragment_Interface {

    private static final String TAG = RoomListFragment.class.getName();

    private final ExICSData exICSData = ExICSData.getInstance();
    private ExICS_Main_Fragment_Interface mCallbacks;

    public RoomListFragment() {
    }

    public static RoomListFragment newInstance() {
        RoomListFragment fragment = new RoomListFragment();
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
        View view = inflater.inflate(R.layout.fragment_overview_list, container, false);

        setView(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG, "onAttach()");
        super.onAttach(activity);
        try {
            mCallbacks = (ExICS_Main_Fragment_Interface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ExICS Main Fragment Interface.");
        }
    }

    @Override
    public void onDetach() {
        Log.i(TAG, "onDetach()");
        super.onDetach();
        mCallbacks = null;
    }

    public void setView(View view) {
        TextView title = (TextView) view.findViewById(R.id.tvOverviewListTitle);
        title.setText("Room Overview");

        final ListView roomListView = (ListView) view.findViewById(R.id.lvInformationList);
        ArrayList<Integer> roomList = new ArrayList<Integer>();
        roomList.addAll(exICSData.getAllRooms());
        Integer[] rooms = new Integer[roomList.size()];
        rooms = roomList.toArray(rooms);
        RoomListFragmentListAdapter adapter = new RoomListFragmentListAdapter(getActivity(), R.layout.room_list_item_layout, rooms);
        roomListView.setAdapter(adapter);
        roomListView.setOnTouchListener(new FragmentOnSwipeTouchListener(this.getActivity().getApplicationContext(), mCallbacks));
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomListFragmentListAdapter adapter = (RoomListFragmentListAdapter) roomListView.getAdapter();
                try {
                    int roomNum = adapter.getRoomNumAtPosition(position);
                    ExamListFragment newFrag = ExamListFragment.newInstance(roomNum);
                    mCallbacks.updateContentFragment(newFrag);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Failed to get roomNum", e);
                }
            }
        });
    }

    @Override
    public void refreshView() {
        setView(getView());
    }
}
