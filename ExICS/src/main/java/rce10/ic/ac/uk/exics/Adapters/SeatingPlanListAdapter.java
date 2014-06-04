package rce10.ic.ac.uk.exics.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.SeatingInformation;
import rce10.ic.ac.uk.exics.R;

/**
 * Created by Rich on 04/06/2014.
 */
public class SeatingPlanListAdapter extends ArrayAdapter<SeatingInformation> {

    private final Activity context;
    private final SeatingInformation[] values;
    private final ExICSData exICSData = ExICSData.getInstance();

    public SeatingPlanListAdapter(Activity context, int resource, SeatingInformation[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = context.getLayoutInflater().inflate(R.layout.seating_plan_list_item, parent, false);
        if (position < values.length) {
            TextView seat = (TextView) convertView.findViewById(R.id.tvSeatingPlanSeatNum);
            TextView cid = (TextView) convertView.findViewById(R.id.tvSeatingPlanCID);
            TextView exam = (TextView) convertView.findViewById(R.id.tvSeatingPlanExam);
            SeatingInformation student = values[position];
            seat.setText(String.valueOf(student.getSeat()));
            cid.setText(student.getCID());
            exam.setText(student.getCourse());
        }

        return convertView;
    }
}
