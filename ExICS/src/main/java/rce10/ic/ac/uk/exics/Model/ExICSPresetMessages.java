package rce10.ic.ac.uk.exics.Model;

import java.util.ArrayList;

/**
 * Created by Rich on 02/06/2014.
 */
public class ExICSPresetMessages {

    ArrayList<PresetMessage> presetMessages;

    public ExICSPresetMessages() {
        presetMessages = new ArrayList<PresetMessage>();
        presetMessages.add(new PresetMessage("Toilet Escort", "Can someone come and escort a student to the bathroom?"));
        presetMessages.add(new PresetMessage("School Shooter", "We have a student who can't get laid.  Violence breaking out.  Send the 5-0"));
    }

    public ArrayList<PresetMessage> getPresetMessages() {
        return presetMessages;
    }
}
