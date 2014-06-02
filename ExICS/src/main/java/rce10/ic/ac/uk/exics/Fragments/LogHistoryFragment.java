package rce10.ic.ac.uk.exics.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import rce10.ic.ac.uk.exics.Adapters.MessageSendRecipientSpinnerAdapter;
import rce10.ic.ac.uk.exics.Adapters.MessageSendRecipientTypeSpinnerAdapter;
import rce10.ic.ac.uk.exics.Adapters.PresetMessagesSpinnerAdapter;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Child_Fragment_Interface;
import rce10.ic.ac.uk.exics.Interfaces.ExICS_Main_Fragment_Interface;
import rce10.ic.ac.uk.exics.Model.ExICSData;
import rce10.ic.ac.uk.exics.Model.ExICSPresetMessages;
import rce10.ic.ac.uk.exics.Model.PresetMessage;
import rce10.ic.ac.uk.exics.R;
import rce10.ic.ac.uk.exics.Utilities.FragmentOnSwipeTouchListener;
import rce10.ic.ac.uk.exics.Utilities.wsCommunicationManager;

public class LogHistoryFragment extends Fragment implements ExICS_Main_Child_Fragment_Interface {

    ExICSData exICSData = ExICSData.getInstance();

    ScrollView logScrollView = null;
    TextView logText = null;
    Button sendMessage = null;

    AlertDialog messageTypeChoiceDialog, customDialog, presetDialog;

    ExICS_Main_Fragment_Interface mCallbacks;

    private wsCommunicationManager wsCM;

    public LogHistoryFragment() {
        // Required empty public constructor
    }

    public static LogHistoryFragment newInstance() {
        LogHistoryFragment fragment = new LogHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wsCM = wsCommunicationManager.getInstance(getActivity().getApplicationContext());

        if (getArguments() != null) {
        }
        AlertDialog.Builder choiceBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
        choiceBuilder.setTitle("Send Message").setMessage("Which type of message would you like to send?");
        choiceBuilder.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        choiceBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setNeutralButton("Preset Message", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presetDialog = createPresetMessageDialog();
                dialog.dismiss();
                presetDialog.show();
            }
        }).setPositiveButton("Custom Message", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                customDialog = createCustomMessageDialog();
                dialog.dismiss();
                customDialog.show();
            }
        });
        messageTypeChoiceDialog = choiceBuilder.create();
    }

    private AlertDialog createPresetMessageDialog() {
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
        View customDialogView = getActivity().getLayoutInflater().inflate(R.layout.preset_message_dialog, null, false);
        final LinearLayout recipientSelect = (LinearLayout) customDialogView.findViewById(R.id.llRecipientSelect);
        final Spinner recipientTypeSpinner = (Spinner) customDialogView.findViewById(R.id.spPresetMessageRecipientType);
        final Spinner recipientSpinner = (Spinner) customDialogView.findViewById(R.id.spPresetMessageRecipient);
        final Spinner messageSpinner = (Spinner) customDialogView.findViewById(R.id.spPresetMessageSelect);
        final TextView messagePreview = (TextView) customDialogView.findViewById(R.id.tvPresetMessagePreview);

        MessageSendRecipientTypeSpinnerAdapter msrsta = new MessageSendRecipientTypeSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{"All", "Room", "Individual"});
        recipientTypeSpinner.setAdapter(msrsta);
        recipientTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MessageSendRecipientTypeSpinnerAdapter ad = (MessageSendRecipientTypeSpinnerAdapter) recipientTypeSpinner.getAdapter();
                String selected = ad.getSelectedType(position);
                if (selected.contentEquals("All")) {
                    recipientSelect.setVisibility(View.GONE);
                    recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{""}));
                } else if (selected.contentEquals("Room")) {
                    Set<Integer> roomsSet = exICSData.getAllRooms();
                    ArrayList<String> roomsArrayList = new ArrayList<String>();
                    for (Integer room : roomsSet) {
                        roomsArrayList.add(String.valueOf(room));
                    }
                    roomsArrayList.add("Delocalised");
                    String[] roomsArray = new String[roomsArrayList.size()];
                    roomsArray = roomsArrayList.toArray(roomsArray);
                    recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, roomsArray));
                    recipientSelect.setVisibility(View.VISIBLE);
                } else {
                    Set<String> allUsersSet = exICSData.getAllUsers();
                    ArrayList<String> allUsersArrayList = new ArrayList<String>();
                    for (String user : allUsersSet) {
                        if (!(user.contentEquals(exICSData.getUsername())))
                            allUsersArrayList.add(user);
                    }
                    String[] allUsersArray = new String[allUsersArrayList.size()];
                    allUsersArray = allUsersArrayList.toArray(allUsersArray);
                    recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, allUsersArray));
                    recipientSelect.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                recipientSelect.setVisibility(View.VISIBLE);
                recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{""}));
            }
        });
        recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{""}));

        ExICSPresetMessages pMessages = new ExICSPresetMessages();
        ArrayList<PresetMessage> pMessagesArrayList = pMessages.getPresetMessages();
        PresetMessage[] pMessagesArray = new PresetMessage[pMessagesArrayList.size()];
        pMessagesArray = pMessagesArrayList.toArray(pMessagesArray);
        messageSpinner.setAdapter(new PresetMessagesSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, pMessagesArray));
        messageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PresetMessage selected = (PresetMessage) parent.getSelectedItem();
                messagePreview.setText(selected.getMessage());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                messagePreview.setText("Please select a message above");
            }
        });

        customBuilder.setTitle("Custom Message");
        customBuilder.setView(customDialogView);
        customBuilder.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        customBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String recipientType = (String) recipientTypeSpinner.getSelectedItem();
                String message = ((PresetMessage) messageSpinner.getSelectedItem()).getMessage();

                if (recipientType.contentEquals("All")) {
                    wsCM.sendMessageToAll(message);
                } else if (recipientType.contentEquals("Room")) {
                    String room = (String) recipientSpinner.getSelectedItem();
                    if (room.contentEquals("Delocalised")) {
                        wsCM.sendMessageToAllInRoom(-3, message);
                    } else {
                        wsCM.sendMessageToAllInRoom(Integer.valueOf(room), message);
                    }
                } else {
                    String user = (String) recipientSpinner.getSelectedItem();
                    wsCM.sendMessageToUser(user, message);
                }
            }
        });
        return customBuilder.create();
    }

    private AlertDialog createCustomMessageDialog() {
        AlertDialog.Builder customBuilder = new AlertDialog.Builder(mCallbacks.getActivityContext());
        View customDialogView = getActivity().getLayoutInflater().inflate(R.layout.custom_message_dialog, null, false);
        final LinearLayout recipientSelect = (LinearLayout) customDialogView.findViewById(R.id.llRecipientSelect);
        final Spinner recipientTypeSpinner = (Spinner) customDialogView.findViewById(R.id.spCustomDialogRecipientType);
        final Spinner recipientSpinner = (Spinner) customDialogView.findViewById(R.id.spCustomDialogRecipient);
        final EditText enteredMessageBox = (EditText) customDialogView.findViewById(R.id.etCustomDialogMessage);
        MessageSendRecipientTypeSpinnerAdapter msrsta = new MessageSendRecipientTypeSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{"All", "Room", "Individual"});
        recipientTypeSpinner.setAdapter(msrsta);
        recipientTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MessageSendRecipientTypeSpinnerAdapter ad = (MessageSendRecipientTypeSpinnerAdapter) recipientTypeSpinner.getAdapter();
                String selected = ad.getSelectedType(position);
                if (selected.contentEquals("All")) {
                    recipientSelect.setVisibility(View.GONE);
                    recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{""}));
                } else if (selected.contentEquals("Room")) {
                    Set<Integer> roomsSet = exICSData.getAllRooms();
                    ArrayList<String> roomsArrayList = new ArrayList<String>();
                    for (Integer room : roomsSet) {
                        roomsArrayList.add(String.valueOf(room));
                    }
                    roomsArrayList.add("Delocalised");
                    String[] roomsArray = new String[roomsArrayList.size()];
                    roomsArray = roomsArrayList.toArray(roomsArray);
                    recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, roomsArray));
                    recipientSelect.setVisibility(View.VISIBLE);
                } else {
                    Set<String> allUsersSet = exICSData.getAllUsers();
                    ArrayList<String> allUsersArrayList = new ArrayList<String>();
                    for (String user : allUsersSet) {
                        if (!(user.contentEquals(exICSData.getUsername())))
                            allUsersArrayList.add(user);
                    }
                    String[] allUsersArray = new String[allUsersArrayList.size()];
                    allUsersArray = allUsersArrayList.toArray(allUsersArray);
                    recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, allUsersArray));
                    recipientSelect.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                recipientSelect.setVisibility(View.VISIBLE);
                recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{""}));
            }
        });
        recipientSpinner.setAdapter(new MessageSendRecipientSpinnerAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{""}));
        customBuilder.setTitle("Custom Message");
        customBuilder.setView(customDialogView);
        customBuilder.setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        customBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String recipientType = (String) recipientTypeSpinner.getSelectedItem();
                String message = enteredMessageBox.getText().toString();

                if (recipientType.contentEquals("All")) {
                    wsCM.sendMessageToAll(message);
                } else if (recipientType.contentEquals("Room")) {
                    String room = (String) recipientSpinner.getSelectedItem();
                    if (room.contentEquals("Delocalised")) {
                        wsCM.sendMessageToAllInRoom(-3, message);
                    } else {
                        wsCM.sendMessageToAllInRoom(Integer.valueOf(room), message);
                    }
                } else {
                    String user = (String) recipientSpinner.getSelectedItem();
                    wsCM.sendMessageToUser(user, message);
                }
                Toast.makeText(getActivity().getApplicationContext(), "Sending message", Toast.LENGTH_SHORT).show();
            }
        });
        return customBuilder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_ex_ics_log_history, container, false);
        logScrollView = (ScrollView) inflatedView.findViewById(R.id.svChatLog);
        logScrollView.setOnTouchListener(new FragmentOnSwipeTouchListener(this.getActivity().getApplicationContext(), mCallbacks));
        logText = (TextView) inflatedView.findViewById(R.id.tvExICSLog);
        sendMessage = (Button) inflatedView.findViewById(R.id.bChatSendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageTypeChoiceDialog.show();
            }
        });
        updateLogText();
        return inflatedView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        updateLogText();
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

    private void updateLogText() {
        if (logText != null) {
            logText.setText(exICSData.getChatLog());
            if (logScrollView != null) {
                logScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        logScrollView.scrollTo(0, logText.getBottom());
                    }
                });
            }
        }
    }

    @Override
    public void refreshView() {
        updateLogText();
    }
}
