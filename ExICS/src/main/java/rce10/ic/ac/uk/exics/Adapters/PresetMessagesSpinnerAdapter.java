package rce10.ic.ac.uk.exics.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import rce10.ic.ac.uk.exics.Model.PresetMessage;

/**
 * Created by Rich on 02/06/2014.
 */
public class PresetMessagesSpinnerAdapter extends ArrayAdapter<PresetMessage> {
    private Activity context;
    private int resource;
    private PresetMessage[] data;

    public PresetMessagesSpinnerAdapter(Activity ctx, int resource, PresetMessage[] data) {
        super(ctx, resource, data);
        this.context = ctx;
        this.resource = resource;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView rowView = (TextView) context.getLayoutInflater().inflate(android.R.layout.simple_spinner_item, parent, false);
        if (position < data.length)
            rowView.setText(data[position].getTitle());
        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView row = (TextView) convertView;
        if (row == null) {
            row = (TextView) context.getLayoutInflater().inflate(resource, parent, false);
        }
        String rowValue = data[position].getTitle();
        if (rowValue != null) {
            row.setText(rowValue);
        }
        return row;
    }
}
