package rce10.ic.ac.uk.exics.Model;

import java.util.ArrayList;

/**
 * Created by Rich on 02/06/2014.
 */
public class ExICSPresetMessages {

    ArrayList<PresetMessage> presetMessages;
    ArrayList<PresetMessage> presetResponses;

    public ExICSPresetMessages() {
        presetMessages = new ArrayList<PresetMessage>();
        presetMessages.add(new PresetMessage("Toilet Escort", "Can someone come and escort a student to the bathroom?"));
        presetMessages.add(new PresetMessage("School Shooter", "We have a student who can't get laid.  Violence breaking out.  Send the 5-0"));

        presetResponses = new ArrayList<PresetMessage>();
        presetResponses.add(new PresetMessage("Yes", "Yes"));
        presetResponses.add(new PresetMessage("No", "No"));
        presetResponses.add(new PresetMessage("On way", "I'm on my way to assist you"));
        presetResponses.add(new PresetMessage("Custom", ""));
    }

    public ArrayList<PresetMessage> getPresetMessages() {
        return presetMessages;
    }

    public ArrayList<PresetMessage> getPresetResponses() {
        return presetResponses;
    }
}
