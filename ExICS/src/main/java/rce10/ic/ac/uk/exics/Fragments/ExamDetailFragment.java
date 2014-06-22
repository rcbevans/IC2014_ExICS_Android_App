package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.Exam;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.wsCommunicationManager;
import rce10.ic.ac.uk.exics.ViewModel.ExICSData;

public class ExamDetailFragment extends Fragment implements ExICS_Main_Child_Fragment_Interface {

    private static final String TAG = ExamDetailFragment.class.getName();
    private static final String TAG_ROOM_NUM = "ROOM_NUM";
    private static final String TAG_COURSE_CODE = "COURSE_CODE";
    private static final int START_BUTTON = 0;
    private static final int PAUSE_BUTTON = 1;
    private static final int RESUME_BUTTON = 2;
    private static final int STOP_BUTTON = 3;
    private static final int XTIME_BUTTON = 4;
    private static final ExICSData exICSData = ExICSData.getInstance();
    private ExICS_Main_Fragment_Interface mCallbacks;
    private int roomNum;
    private String courseCode;
    private wsCommunicationManager wsCM;

    private AlertDialog confirmStopExamDialog;

    private countdownTimer timer = null;

    private Exam exam;

    public ExamDetailFragment() {
        // Required empty public constructor
    }

    public static ExamDetailFragment newInstance(int room, String code) {
        ExamDetailFragment fragment = new ExamDetailFragment();
        Bundle args = new Bundle();
        args.putInt(TAG_ROOM_NUM, room);
        args.putString(TAG_COURSE_CODE, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            roomNum = getArguments().getInt(TAG_ROOM_NUM);
            courseCode = getArguments().getString(TAG_COURSE_CODE);

            AlertDialog.Builder confirmStopBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
            confirmStopBuilder.setTitle(String.format("Stop Exam %s?", courseCode));
            confirmStopBuilder.setMessage("Are you sure you want to stop this exam?\nThis action can't be undone...");
            confirmStopBuilder.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            confirmStopBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    wsCM.stopExam(exam.getRoom(), exam.getExamSubModule());
                }
            });
            confirmStopExamDialog = confirmStopBuilder.create();

            exam = exICSData.getExam(roomNum, courseCode);
            wsCM = wsCommunicationManager.getInstance(getActivity().getApplicationContext());
            if (exam == null) {
                throw new NullPointerException("exam is null");
            }
        } else {
            mCallbacks.fragmentViewUnavailable();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exam_detail, container, false);

        setView(view);

        return view;
    }

    private View getActionButton(int type, View parentView) {
        Log.i(TAG, "getActionButton");
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams((int) (parentView.getHeight() * 7.0 / 10.0), (int) (parentView.getHeight() * 7.0 / 10.0));
        View actionButton = getActivity().getLayoutInflater().inflate(R.layout.exam_detail_action_button_layout, null, false);
        actionButton.setLayoutParams(lparams);
        ImageView icon = (ImageView) actionButton.findViewById(R.id.ivButtonIcon);
        TextView text = (TextView) actionButton.findViewById(R.id.tvButtonTitle);
        switch (type) {
            case START_BUTTON:
                icon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                text.setText("Start Exam");
                break;
            case PAUSE_BUTTON:
                icon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                text.setText("Pause Exam");
                break;
            case RESUME_BUTTON:
                icon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                text.setText("Resume Exam");
                break;
            case STOP_BUTTON:
                icon.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_offline));
                text.setText("Stop Exam");
                break;
            case XTIME_BUTTON:
                icon.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
                text.setText("Add Extra Time");
        }

        return actionButton;
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
        timer.cancel();
    }

    public void setView(View view) {
        TextView examRoom = (TextView) view.findViewById(R.id.tvExamRoom);
        final TextView examCourseCode = (TextView) view.findViewById(R.id.tvExamCourseCode);
        TextView examTitle = (TextView) view.findViewById(R.id.tvExamDetailTitle);

        TextView countdown = (TextView) view.findViewById(R.id.tvCountDownTime);

        ImageView examStatus = (ImageView) view.findViewById(R.id.ivExamDetailStatus);

        LinearLayout numQsRow = (LinearLayout) view.findViewById(R.id.llNumQs);
        TextView numQs = (TextView) view.findViewById(R.id.tvExamDetailNumQuestions);

        LinearLayout scheduledStartRow = (LinearLayout) view.findViewById(R.id.llScheduledStart);
        TextView scheduledStart = (TextView) view.findViewById(R.id.tvScheduledStartTime);

        LinearLayout actualStartRow = (LinearLayout) view.findViewById(R.id.llActualStart);
        TextView actualStart = (TextView) view.findViewById(R.id.tvExamDetailActualStart);

        LinearLayout durationRow = (LinearLayout) view.findViewById(R.id.llDuration);
        TextView duration = (TextView) view.findViewById(R.id.tvExamDetailDuration);

        LinearLayout extraTimeRow = (LinearLayout) view.findViewById(R.id.llExtraTime);
        TextView extraTime = (TextView) view.findViewById(R.id.tvExamDetailExtraTime);

        LinearLayout expectedFinishRow = (LinearLayout) view.findViewById(R.id.llExpectedFinish);
        TextView expectedFinish = (TextView) view.findViewById(R.id.tvExamDetailExpectedFinish);

        LinearLayout actionButtionPanel = (LinearLayout) view.findViewById(R.id.llExamDetailInteractionBar);
        actionButtionPanel.removeAllViewsInLayout();

        if (actionButtionPanel.getHeight() == 0) {
            ViewTreeObserver actionButtonPanelObserver = actionButtionPanel.getViewTreeObserver();
            actionButtonPanelObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = getView().findViewById(R.id.llExamDetailInteractionBar).getViewTreeObserver();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        obs.removeOnGlobalLayoutListener(this);
                    } else {
                        obs.removeGlobalOnLayoutListener(this);
                    }
                    addActionButtons();
                }
            });
        } else {
            addActionButtons();
        }

        examRoom.setText(String.valueOf(exam.getRoom()));
        examCourseCode.setText(exam.getExamSubModule());
        examTitle.setText(exam.getTitle());

        numQs.setText(String.valueOf(exam.getNumQuestions()));
        duration.setText(String.valueOf(exam.getDuration()));
        extraTime.setText(String.valueOf(exam.getExtraTime()));

        scheduledStart.setText(android.text.format.DateFormat.format("HH:mm", exam.getScheduledStart().getTime()));
        expectedFinish.setText(android.text.format.DateFormat.format("HH:mm", exam.getExpectedFinish()));

        if (exam.isRunning()) {
            actualStart.setText(android.text.format.DateFormat.format("HH:mm", exam.getActualStart().getTime()));
            actualStartRow.setVisibility(View.VISIBLE);

            if (exam.isPaused()) {
                countdown.setVisibility(View.GONE);
                if (timer != null)
                    timer.cancel();
                examStatus.setImageDrawable(getResources().getDrawable(R.drawable.yellow_light));
            } else {
                countdown.setVisibility(View.VISIBLE);
                if (timer != null)
                    timer.cancel();
                timer = new countdownTimer(exam.getExpectedFinish().getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 1000, countdown);
                timer.start();
                examStatus.setImageDrawable(getResources().getDrawable(R.drawable.green_light));
            }
        } else {
            actualStartRow.setVisibility(View.GONE);
            countdown.setVisibility(View.GONE);

            examStatus.setImageDrawable(getResources().getDrawable(R.drawable.red_light));
        }
    }

    private void addActionButtons() {
        LinearLayout actionButtionPanel = (LinearLayout) getView().findViewById(R.id.llExamDetailInteractionBar);

        if (exam.isRunning()) {
            if (exam.isPaused()) {
                View resumeActionButton = getActionButton(RESUME_BUTTON, actionButtionPanel);
                resumeActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (exam.getRoom() == roomNum) {
                            wsCM.pauseExam(exam.getRoom(), exam.getExamSubModule());
                            timer.cancel();
                        } else {
                            AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
                            confirmationBuilder.setTitle("Confirm").setMessage("This exam is not taking place in the room you are set to invigilate, are you sure you want to do this?");
                            confirmationBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    wsCM.pauseExam(exam.getRoom(), exam.getExamSubModule());
                                    timer.cancel();
                                }
                            }).create().show();
                        }
                    }
                });
                actionButtionPanel.addView(resumeActionButton);
            } else {
                View pauseActionButton = getActionButton(PAUSE_BUTTON, actionButtionPanel);
                pauseActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (exam.getRoom() == roomNum) {
                            wsCM.pauseExam(exam.getRoom(), exam.getExamSubModule());
                            timer.cancel();
                        } else {
                            AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
                            confirmationBuilder.setTitle("Confirm").setMessage("This exam is not taking place in the room you are set to invigilate, are you sure you want to do this?");
                            confirmationBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    wsCM.pauseExam(exam.getRoom(), exam.getExamSubModule());
                                    timer.cancel();
                                }
                            }).create().show();
                        }
                    }
                });
                actionButtionPanel.addView(pauseActionButton);
            }
            final View xTimeActionButton = getActionButton(XTIME_BUTTON, actionButtionPanel);
            xTimeActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder xTimeDialog = new AlertDialog.Builder(mCallbacks.getActivityContext());
                    xTimeDialog.setTitle("Add Extra Time");
                    View xTimeView = getActivity().getLayoutInflater().inflate(R.layout.extra_time_dialog, null, false);
                    final NumberPicker time = (NumberPicker) xTimeView.findViewById(R.id.npXTimeDialogTime);
                    time.setMinValue(-120);
                    time.setMaxValue(120);
                    xTimeDialog.setView(xTimeView);
                    xTimeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton("Add Time", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (exam.getRoom() == roomNum) {
                                int timeAdding = time.getValue();
                                if ((exam.getExtraTime() + timeAdding) >= 0)
                                    wsCM.addExtraTime(exam.getRoom(), exam.getExamSubModule(), timeAdding);
                                else
                                    Toast.makeText(mCallbacks.getActivityContext(), "Unable to adjust extra time to less than 0!", Toast.LENGTH_LONG);
                            } else {
                                AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
                                confirmationBuilder.setTitle("Confirm").setMessage("This exam is not taking place in the room you are set to invigilate, are you sure you want to do this?");
                                confirmationBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int timeAdding = time.getValue();
                                        if ((exam.getExtraTime() + timeAdding) >= 0)
                                            wsCM.addExtraTime(exam.getRoom(), exam.getExamSubModule(), timeAdding);
                                        else
                                            Toast.makeText(mCallbacks.getActivityContext(), "Unable to adjust extra time to less than 0!", Toast.LENGTH_LONG);
                                    }
                                }).create().show();
                            }
                        }
                    }).setCancelable(true).create().show();
                }
            });
            actionButtionPanel.addView(xTimeActionButton);
            View stopActionButton = getActionButton(STOP_BUTTON, actionButtionPanel);
            stopActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exam.getRoom() == roomNum) {
                        if (confirmStopExamDialog != null && !confirmStopExamDialog.isShowing())
                            confirmStopExamDialog.show();
                    } else {
                        AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
                        confirmationBuilder.setTitle("Confirm").setMessage("This exam is not taking place in the room you are set to invigilate, are you sure you want to do this?");
                        confirmationBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (confirmStopExamDialog != null && !confirmStopExamDialog.isShowing())
                                    confirmStopExamDialog.show();
                            }
                        }).create().show();
                    }
                }
            });
            actionButtionPanel.addView(stopActionButton);
        } else {
            View startActionButton = getActionButton(START_BUTTON, actionButtionPanel);
            startActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (exam.getRoom() == roomNum) {
                        wsCM.startExam(exam.getRoom(), exam.getExamSubModule());
                    } else {
                        AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
                        confirmationBuilder.setTitle("Confirm").setMessage("This exam is not taking place in the room you are set to invigilate, are you sure you want to do this?");
                        confirmationBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                wsCM.startExam(exam.getRoom(), exam.getExamSubModule());
                            }
                        }).create().show();
                    }
                }
            });
            actionButtionPanel.addView(startActionButton);
        }
    }

    @Override
    public void refreshView() {
        Log.i(TAG, "RefreshView");
        exam = exICSData.getExam(roomNum, courseCode);
        if (exam != null) {
            setView(getView());
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "This exam is no longer available!", Toast.LENGTH_SHORT).show();
            mCallbacks.fragmentViewUnavailable();
        }
    }

    public class countdownTimer extends CountDownTimer {

        private TextView display;

        public countdownTimer(long millisInFuture, long countDownInterval, TextView display) {
            super(millisInFuture, countDownInterval);
            this.display = display;
        }

        @Override
        public void onFinish() {
            display.setText("done!");
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (millisUntilFinished < 900500 && millisUntilFinished >= 899500) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mCallbacks.getActivityContext());
                builder.setCancelable(true).setTitle("Exam Nearly Over").setMessage("This exam will finish in 15 minutes!").setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
            long hours = millisUntilFinished / 3600000;
            long mins = millisUntilFinished / 60000 % 60;
            long seconds = millisUntilFinished / 1000 % 60;
            display.setText(String.format("%02d:%02d:%02d", hours, mins, seconds));
        }
    }
}
