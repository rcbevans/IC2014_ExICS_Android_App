package rce10.ic.ac.uk.exics.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.ViewModel.ExICSData;

/**
 * Created by Rich on 28/05/2014.
 */
public class RoomListFragmentListAdapter extends ArrayAdapter<Integer> {

    private final Activity context;
    private final Integer[] values;
    private final ExICSData exICSData = ExICSData.getInstance();

    public RoomListFragmentListAdapter(Activity context, int resource, Integer[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = context.getLayoutInflater().inflate(R.layout.room_list_item_layout, parent, false);
        if (position < values.length) {
            int rowRoomNum = values[position];

            TextView roomNumber = (TextView) convertView.findViewById(R.id.tvRoomNumber);
            roomNumber.setText(String.valueOf(rowRoomNum));

            if (!exICSData.inRoom(rowRoomNum)) {
                ImageView homeLogo = (ImageView) convertView.findViewById(R.id.ivHomeIcon);
                homeLogo.setVisibility(View.INVISIBLE);
            }
            ImageView statusIcon = (ImageView) convertView.findViewById(R.id.ivStatusOfRoom);

            TextView numExamsStartedInRoom = (TextView) convertView.findViewById(R.id.tvNumExamsStartedInRoom);
            numExamsStartedInRoom.setText(String.valueOf(exICSData.getNumStartedExamsInRoom(rowRoomNum)));

            TextView numExamsPausedInRoom = (TextView) convertView.findViewById(R.id.tvNumExamsPausedInRoom);
            numExamsPausedInRoom.setText(String.valueOf(exICSData.getNumPausedExamsInRoom(rowRoomNum)));

            TextView numInvigilatorsInRoom = (TextView) convertView.findViewById(R.id.tvNumInvigilatorsInRoom);
            numInvigilatorsInRoom.setText(String.valueOf(exICSData.getNumUsersInRoom(rowRoomNum)));

            switch (exICSData.getLowestRoomStatus(rowRoomNum)) {
                case 0:
                    statusIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.red_light));
                    break;
                case 1:
                    statusIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.yellow_light));
                    break;
                case 2:
                    statusIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.green_light));
                    break;
            }
        }

        return convertView;
    }

    public int getRoomNumAtPosition(int pos) {
        if (pos < values.length)
            return values[pos];
        else
            throw new IllegalArgumentException("Requested room number outside data bounds!");
    }
}
