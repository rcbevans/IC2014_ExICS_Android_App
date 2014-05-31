package rce10.ic.ac.uk.exics.Interfaces;

import android.app.Fragment;
import android.content.Context;

/**
 * Created by Rich on 28/05/2014.
 */
public interface ExICS_Main_Fragment_Interface {
    public void updateContentFragment(Fragment frag);

    public void onFragmentSwipedLeft();

    public void onFragmentSwipedRight();

    public void fragmentViewUnavailable();

    public Context getActivityContext();
}
