package rce10.ic.ac.uk.exics.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Rich on 26/05/2014.
 */
public class RoomSelectSpinnerAdapter extends ArrayAdapter<String> {

    private Activity context;
    private int resource;
    private String[] data;

    public RoomSelectSpinnerAdapter(Activity ctx, int resource, String[] data) {
        super(ctx, resource, data);
        this.context = ctx;
        this.resource = resource;
        this.data = data;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) context.getLayoutInflater().inflate(resource, parent, false);
        }
        String rowValue = data[position];
        if (rowValue != null) {
            row.setText(rowValue);
        }
        return row;
    }
}
