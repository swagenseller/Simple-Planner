package com.kevinvelcich.hwplanner.Activity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import com.kevinvelcich.hwplanner.AlaramReceiver;
import com.kevinvelcich.hwplanner.Classes.Assignment;
import com.kevinvelcich.hwplanner.Database.DatabaseAssignment;
import com.kevinvelcich.hwplanner.Database.DatabaseClass;
import com.kevinvelcich.hwplanner.Fragments.Dialog.ColorDialogFragment;
import com.kevinvelcich.hwplanner.Fragments.Dialog.CreateAssignmentDialogFragment;
import com.kevinvelcich.hwplanner.Fragments.Dialog.CreateClassDialogFragment;
import com.kevinvelcich.hwplanner.Fragments.Dialog.EditAssignmentDialogFragment;
import com.kevinvelcich.hwplanner.Fragments.Dialog.EditClassDialogFragment;
import com.kevinvelcich.hwplanner.Fragments.Dialog.EditColorDialogFragment;
import com.kevinvelcich.hwplanner.Fragments.classFragment;
import com.kevinvelcich.hwplanner.Fragments.assignmentsFragment;
import com.kevinvelcich.hwplanner.R;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements ColorDialogFragment.onCreateInteractionListener,
        CreateAssignmentDialogFragment.onCreateInteractionListener,
        classFragment.assignmentCall,
        EditClassDialogFragment.onEditListener,
        EditAssignmentDialogFragment.onEditListener,
        EditColorDialogFragment.onCreateInteractionListener {

    private String currentClass;
    /*  Function returns the current parent class of activities being displayed. To only be called
     *  when a assignment fragment is being displayed.*/
    public String getParentClass() {
        return currentClass;
    }

    /* Definition variables, indicating a certain fragment. */
    private static final int CLASS_LIST_VIEW = 0;
    private static final int ASSIGNMENT_LIST_VIEW = 1;
    private static final int ASSIGNMENT_SINGLE_VIEW = 2;

    /* Variable, that is changed depending on which fragment is being displayed. */
    private int currentFragment;
    /*  A function which receives no arguments. Returns an integer value, indicating which fragment
     *  is being displayed within the activity. */
    public int getCurrentFragment() {
        return currentFragment;
    }



    /*  A function that is called when a new fragment is being displayed, which sets the title of
     *  the activity, as well as sets the currentFragment variable to its proper value. */
    public void setCurrentFragment(int fragment) {
        switch (fragment) {
            case CLASS_LIST_VIEW:
                currentFragment = fragment;
                setTitle("Simple Planner");
                break;
            case ASSIGNMENT_LIST_VIEW:
                currentFragment = fragment;
                /*  Setting activity title to the parent class of the assignments being displayed. */
                setTitle(getParentClass());
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            // <---- run your one time code here();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 19);
            calendar.set(Calendar.MINUTE, 30);
            calendar.set(Calendar.SECOND, 0);
            Intent intent1 = new Intent(MainActivity.this, AlaramReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    1000 * 60 * 60 * 24, pendingIntent);

            // mark first time has runned.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fAB);

        /*Fixing elevation for lollipop users, to comply with lollipop guidelines, while allowing
         *availability with previous versions. */
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            floatingActionButton.setElevation(12);
        }

        /* Creating listener for Floating Action Button. */
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Action for when Floating Action Button is clicked. */
                switch (getCurrentFragment()) {
                    case CLASS_LIST_VIEW: {
                        DialogFragment dialogFragment = new CreateClassDialogFragment();
                        dialogFragment.show(getFragmentManager(), "dialog");
                        break;
                    }
                    case ASSIGNMENT_LIST_VIEW: {
                        DialogFragment dialogFragment = new CreateAssignmentDialogFragment();
                        Bundle args = new Bundle();
                        args.putString("parent_class", getParentClass());
                        dialogFragment.setArguments(args);
                        dialogFragment.show(getFragmentManager(), "dialog");
                        break;
                    }
                    default:
                        break;
                }
            }
        });

        /* Creating initial fragment, containing the list view for all of the classes registered. */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment, classFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
        setCurrentFragment(CLASS_LIST_VIEW);
    }

    @Override
    public void assignmentCallMethod(String pClass) {
        /* A class was clicked. Changing the fragment to the assignment fragment. */
        currentClass = pClass;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit);
        transaction.replace(R.id.fragment, assignmentsFragment.newInstance(pClass));
        transaction.addToBackStack(null);
        transaction.commit();
        setCurrentFragment(ASSIGNMENT_LIST_VIEW);
    }

    /*@Override
    public void assignmentClicked(String name, String description) {

        /*  An assignment was clicked, entering a single assignment fragment with the assignment
         *  name and description passed through as arguments.
        DialogFragment dialogFragment = new AssignmentDialogFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("description", description);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "dialog");
    } */

    /* Function that is called from the create class fragment. */
    @Override
    public void createClicked() {
        /* A new class was created. Recreating the class view fragment. */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        transaction.replace(R.id.fragment, classFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void editClassMethod() {
        /* A new class was edited. Recreating the class view fragment. */
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        transaction.replace(R.id.fragment, classFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void editAssignmentMethod(int c) {
        switch (c) {
            case 0:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit);
                transaction.replace(R.id.fragment, assignmentsFragment.newInstance(getParentClass()));
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case 1:
        }
    }

    /* Function for creating the DatePickerDialog, when creating an assignment. */
    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar c = Calendar.getInstance();
        int cyear = c.get(Calendar.YEAR);
        int cmonth = c.get(Calendar.MONTH);
        int cday = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,  mDateSetListener,  cyear, cmonth, cday);
        datePickerDialog.setTitle("Due Date: ");
        return datePickerDialog;
    }
    public Assignment newAssignment;

    @Override
    public void createAssignmentClicked(Assignment assignment) {
        newAssignment = assignment;
        showDialog(0);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            newAssignment.setDate(year, month + 1, day);
            DatabaseAssignment databaseAssignment = new DatabaseAssignment(getApplicationContext(), null, null, 1);
            databaseAssignment.addAssignment(newAssignment);

            /* Adding the count for number of assignemnts to the class. */
            DatabaseClass dbHandler = new DatabaseClass(getApplicationContext(), null, null, 1);
            dbHandler.addQuantity(getParentClass());

            /* A new assignment was created. Recreating the assignment view fragment. */
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit);
            transaction.replace(R.id.fragment, assignmentsFragment.newInstance(getParentClass()));
            transaction.addToBackStack(null);
            transaction.commit();
        }
    };

    @Override
    public void onBackPressed() {
        FragmentTransaction transaction;
        switch (getCurrentFragment()) {
            /* If viewing the class view, close the app. */
            case CLASS_LIST_VIEW:
                finish();
                return;
            /* If viewing an assignment, return to the class view. */
            case ASSIGNMENT_LIST_VIEW:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit);
                transaction.replace(R.id.fragment, classFragment.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
                setCurrentFragment(CLASS_LIST_VIEW);
                break;
        }
    }
}
