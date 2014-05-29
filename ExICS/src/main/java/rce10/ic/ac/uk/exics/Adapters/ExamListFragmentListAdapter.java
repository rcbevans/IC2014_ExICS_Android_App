package rce10.ic.ac.uk.exics.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.R;

/**
 * Created by Rich on 29/05/2014.
 */
public class ExamListFragmentListAdapter extends ArrayAdapter<Exam> {
    private final Activity context;
    private final Exam[] exams;
    private final ExICSData exICSData = ExICSData.getInstance();

    public ExamListFragmentListAdapter(Activity context, int resource, Exam[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.exams = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = context.getLayoutInflater().inflate(R.layout.exam_list_item_layout, parent, false);
        if (position < exams.length) {
            Exam rowExam = exams[position];
            TextView examCode = (TextView) convertView.findViewById(R.id.tvExamCode);
//            TextView numQns = (TextView) convertView.findViewById(R.id.tvNumQuestions);
            TextView startText = (TextView) convertView.findViewById(R.id.tvStartText);
            TextView schedStartTime = (TextView) convertView.findViewById(R.id.tvScheduledStartTime);
            TextView examDuration = (TextView) convertView.findViewById(R.id.tvExamDuration);
            TextView schedFinishTime = (TextView) convertView.findViewById(R.id.tvScheduledEndTime);
            ImageView examStatus = (ImageView) convertView.findViewById(R.id.ivExamStatus);

            examCode.setText(rowExam.getExamSubModule());
//            numQns.setText(String.valueOf(rowExam.getNumQuestions()));
            examDuration.setText(String.valueOf(rowExam.getDuration()));

            schedFinishTime.setText(android.text.format.DateFormat.format("HH:mm", rowExam.getExpectedFinish().getTime()));


            if (rowExam.isRunning()) {
                startText.setText("Started:");
                schedStartTime.setText(android.text.format.DateFormat.format("HH:mm", rowExam.getActualStart().getTime()));

                if (rowExam.isPaused()) {
                    examStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.yellow_light));
                } else {
                    examStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.green_light));
                }
            } else {
                schedStartTime.setText(android.text.format.DateFormat.format("HH:mm", rowExam.getScheduledStart().getTime()));

                examStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.red_light));
            }
        }

        return convertView;
    }

    public Exam getExamAtPosition(int pos) {
        if (pos > exams.length) {
            return null;
        } else {
            return exams[pos];
        }
    }
}
