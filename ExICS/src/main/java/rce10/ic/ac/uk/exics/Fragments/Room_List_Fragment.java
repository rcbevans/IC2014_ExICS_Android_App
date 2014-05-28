package rce10.ic.ac.uk.exics.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import rce10.ic.ac.uk.exics.Adapters.RoomListFragmentListAdapter;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.R;

public class Room_List_Fragment extends Fragment {

    private final ExICSData exICSData = ExICSData.getInstance();

    public Room_List_Fragment() {
    }

    public static Room_List_Fragment newInstance() {
        Room_List_Fragment fragment = new Room_List_Fragment();
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
        View fragmentView = inflater.inflate(R.layout.fragment_room_list, container, false);
        ListView roomListView = (ListView) fragmentView.findViewById(R.id.lvRoomList);
        ArrayList<Integer> roomList = new ArrayList<Integer>();
        roomList.addAll(exICSData.getAllRooms());
        Integer[] rooms = new Integer[roomList.size()];
        rooms = roomList.toArray(rooms);
        RoomListFragmentListAdapter adapter = new RoomListFragmentListAdapter(getActivity(), R.layout.room_list_item_layout, rooms);
        roomListView.setAdapter(adapter);
        return fragmentView;
    }
}
