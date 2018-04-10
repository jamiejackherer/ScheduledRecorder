package com.iclaude.scheduledrecorder.espresso;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.view.View;
import android.widget.TimePicker;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.didagger2.AppModule;
import com.iclaude.scheduledrecorder.didagger2.DatabaseModule;
import com.iclaude.scheduledrecorder.testutils.DaggerTestComponent;
import com.iclaude.scheduledrecorder.testutils.TestUtils;
import com.iclaude.scheduledrecorder.ui.activities.MainActivity;
import com.iclaude.scheduledrecorder.ui.fragments.scheduledrecordings.ScheduledRecordingsFragment;
import com.iclaude.scheduledrecorder.utils.Utils;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;


/**
 * Tests on ScheduledRecordingFragment.
 */
public class EspressoScheduledRecordingsFragment {

    @Inject
    RecordingsRepository recordingsRepository;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE);

    private Calendar calToday = new GregorianCalendar();
    private Calendar calTomorrow = new GregorianCalendar();
    private Calendar calNextMonth = new GregorianCalendar();
    private String todayMonth, todayDay, tomorrowMonth, tomorrowDay, nextMonthMonth, nextMonthDay;


    @Before
    public void setUp() {
        DaggerTestComponent.builder()
                .appModule(new AppModule(InstrumentationRegistry.getTargetContext()))
                .databaseModule(new DatabaseModule())
                .build()
                .inject(this);

        // Clear database before tests.
        recordingsRepository.deleteAllRecordings();
        recordingsRepository.deleteAllScheduledRecordings();

        // Setup useful dates form testing.
        calToday.setTimeInMillis(System.currentTimeMillis());
        todayMonth = Utils.formatDateMonthName(new Date(calToday.getTimeInMillis()));
        todayDay = Utils.formatDateDayNumber(new Date(calToday.getTimeInMillis()));

        calTomorrow.setTimeInMillis(System.currentTimeMillis());
        calTomorrow.add(Calendar.DATE, 1);
        tomorrowMonth = Utils.formatDateMonthName(new Date(calTomorrow.getTimeInMillis()));
        tomorrowDay = Utils.formatDateDayNumber(new Date(calTomorrow.getTimeInMillis()));

        calNextMonth.setTimeInMillis(System.currentTimeMillis());
        calNextMonth.add(Calendar.MONTH, 1);
        nextMonthMonth = Utils.formatDateMonthName(new Date(calNextMonth.getTimeInMillis()));
        nextMonthDay = Utils.formatDateDayNumber(new Date(calNextMonth.getTimeInMillis()));
    }

    // Swipe che calendar view: the month should be updated.
    @Test
    public void testChangeMonth() {
        openFragment();

        onView(ViewMatchers.withId(R.id.tvMonth)).check(matches(withText(todayMonth)));

        onView(withId(R.id.compactcalendar_view)).perform(swipeLeft());
        onView(withId(R.id.tvMonth)).check(matches(withText(nextMonthMonth)));

        onView(withId(R.id.compactcalendar_view)).perform(swipeRight());
        onView(withId(R.id.tvMonth)).check(matches(withText(todayMonth)));
    }

    // Click on a different day in the calendar: the UI should be updated.
    @Test
    public void testChangeDay() throws Exception {
        openFragment();

        // Click on tomorrow in the calendar view.
        clickOnDay(calTomorrow);

        // Check that the day in the TextView changes.
        onView(withId(R.id.tvDate)).check(matches(withText(tomorrowDay)));

        // Click on today in the calendar view.
        clickOnDay(calToday);

        // Check that the day in the TextView changes.
        onView(withId(R.id.tvDate)).check(matches(withText(todayDay)));
    }

    @Test
    public void testAddScheduledRecording() {
        openFragment();

        // Check initial (empty) state of the UI.
        checkInitialUI();

        // Add a new scheduled recording.
        addScheduledRecording(23, 50, 23, 55);

        // Check that the scheduled recording is added to the list and that UI is updated correctly.
        checkRecordingInList(11, 50, 11, 55);

        // Click on the item and check that the details Activity opens and displays the data correctly.
        checkDetailsActivityForRecording(calToday, 11, 50, 11, 55);

        // Go back and delete the recording.
        deleteRecording(11, 50, 11, 55);

        // Check final (empty) state of the UI.
        checkInitialUI();
    }

    @Test
    public void testAdd2ScheduledRecordingsToday() {
        openFragment();

        // Check initial (empty) state of the UI.
        checkInitialUI();

        // Add 1st scheduled recording.
        addScheduledRecording(23, 50, 23, 55);

        // Check that the scheduled recording is added to the list and that UI is updated correctly.
        checkRecordingInList(11, 50, 11, 55);

        // Add 2nd scheduled recording.
        addScheduledRecording(23, 56, 23, 59);

        // Check that the scheduled recording is added to the list and that UI is updated correctly.
        checkRecordingInList(11, 50, 11, 55);
        checkRecordingInList(11, 56, 11, 59);

        // Click on each recording and check that the details Activity opens and displays the data correctly.
        checkDetailsActivityForRecording(calToday, 11, 50, 11, 55);
        checkDetailsActivityForRecording(calToday, 11, 56, 11, 59);

        // Delete 1st recording and check the UI.
        deleteRecording(11, 50, 11, 55);
        checkRecordingNotInList(11, 50, 11, 55);
        checkRecordingInList(11, 56, 11, 59);

        // Delete 2nd recording.
        deleteRecording(11, 56, 11, 59);

        // Check final (empty) state of the UI.
        checkInitialUI();
    }

    @Test
    public void testAddRecordingsOnDifferentDays() throws Exception {
        openFragment();

        // Add a scheduled recording today.
        addScheduledRecording(23, 50, 23, 55);

        // Check that the recording is in the list.
        checkRecordingInList(11, 50, 11, 55);

        // Click on the recording and check that the details Activity opens and displays the data correctly.
        checkDetailsActivityForRecording(calToday, 11, 50, 11, 55);

        // Click on tomorrow in the calendar view.
        clickOnDay(calTomorrow);

        // Add a scheduled recording.
        addScheduledRecording(10, 30, 10, 45);

        // Check that the recording is in the list for that day and that the other recording is not displayed.
        checkRecordingInList(10, 30, 10, 45);
        checkRecordingNotInList(11, 50, 11, 55);

        // Click on the recording of tomorrow and check that the details Activity opens and displays the data correctly.
        checkDetailsActivityForRecording(calTomorrow, 10, 30, 10, 45);

        // Delete the recording of tomorrow and check that it's not longer in the list.
        deleteRecording(10, 30, 10, 45);
        checkRecordingNotInList(10, 30, 10, 45);

        // Click on today and check that the recording of today is still there.
        clickOnDay(calToday);
        checkRecordingInList(11, 50, 11, 55);

        // Delete the recording and check that it's no longer in the list.
        deleteRecording(11, 50, 11 , 55);
        checkRecordingNotInList(11, 50, 11, 55);

        // Check that the UI is restored to its initial state.
        checkInitialUI();
    }

    @Test
    public void testRestoreSelectedDateAndMonth() throws Exception {
        openFragment();

        // Add a scheduled recording today.
        addScheduledRecording(23, 50, 23, 55);
        // Check that the recording is in the list.
        checkRecordingInList(11, 50, 11, 55);

        // Click on tomorrow on the calendar view.
        clickOnDay(calTomorrow);
        // Add a scheduled recording.
        addScheduledRecording(10, 30, 10, 45);
        // Check that the recording is in the list for tomorrow and that the other recording is not displayed.
        checkRecordingInList(10, 30, 10, 45);
        checkRecordingNotInList(11, 50, 11, 55);

        // Reopen the Fragment (onCreateView is called).
        reopenFragment();

        // Check that the recordings of tomorrow are correctly displayed.
        checkDayAndMonth(calTomorrow, calTomorrow);
        checkRecordingInList(10, 30, 10, 45);
        checkRecordingNotInList(11, 50, 11, 55);

        // Change month and check that the data displayed is correct.
        onView(withId(R.id.tvMonth)).check(matches(withText(todayMonth)));
        onView(withId(R.id.compactcalendar_view)).perform(swipeLeft());
        onView(withId(R.id.tvMonth)).check(matches(withText(nextMonthMonth)));

        // Reopen the Fragment.
        reopenFragment();

        // Check that the month and recordings of tomorrow are correctly displayed.
        checkDayAndMonth(calTomorrow, calTomorrow);
        checkRecordingInList(10, 30, 10, 45);
        checkRecordingNotInList(11, 50, 11, 55);

        // Change the month and click on a date.
        onView(withId(R.id.compactcalendar_view)).perform(swipeLeft());
        clickOnDay(calNextMonth);

        // Check that the UI displays day and month selected with no recordings.
        checkDayAndMonth(calNextMonth, calNextMonth);
        checkNoRecordings();

        // Reopen the Fragment and check that the selection is kept.
        reopenFragment();
        checkDayAndMonth(calNextMonth, calNextMonth);
        checkNoRecordings();
    }

/*    @Test
    public void testPressBackClosesActivity() {
        openFragment();

        try {
            pressBack();
            fail("Should kill the app and throw an exception");
        } catch (NoActivityResumedException e) {
            // Test OK
        }

    }*/


    // Actions common to multiple tests.

    // Go from RecordFragment to ScheduledRecordingsFragment.
    private void openFragment() {
        onView(withText(R.string.tab_title_saved_recordings)).perform(click());
        onView(withText(R.string.tab_title_scheduled_recordings)).perform(click());
    }

    // Go from ScheduledRecordingsFragment to RecordFragment and back.
    private void reopenFragment() {
        onView(withText(R.string.tab_title_saved_recordings)).perform(click());
        onView(withText(R.string.tab_title_record)).perform(click());
        openFragment();
    }

    // Default day and month with no data.
    private void checkInitialUI() {
        onView(withId(R.id.tvMonth)).check(matches(withText(todayMonth)));
        onView(withId(R.id.tvDate)).check(matches(withText(todayDay)));
        onView(withId(R.id.textView6)).check(matches(isDisplayed()));
        onView(withId(R.id.textView6)).check(matches(withText(R.string.no_data)));
        onView(withText(R.string.frag_sched_scheduled_recording)).check(doesNotExist());
    }

    // Check that a recording with the supplied data is displayed.
    private void checkRecordingInList(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        onView(withId(R.id.textView6)).check(matches(not(isDisplayed())));
        onView(allOf(withText(R.string.frag_sched_scheduled_recording), hasSibling(withText(containsString("" + hourStart + ":" + minuteStart))), hasSibling(withText(containsString("" + hourEnd + ":" + minuteEnd))))).check(matches(isDisplayed()));
    }

    // Check that a recording with the supplied data does not exist.
    private void checkRecordingNotInList(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        onView(allOf(withText(R.string.frag_sched_scheduled_recording), hasSibling(withText(containsString("" + hourStart + ":" + minuteStart))), hasSibling(withText(containsString("" + hourEnd + ":" + minuteEnd))))).check(doesNotExist());
    }

    // Check that no scheduled recordings are displayed.
    private void checkNoRecordings() {
        onView(withText(R.string.frag_sched_scheduled_recording)).check(doesNotExist());
    }

    private void checkDetailsActivityForRecording(Calendar calDay, int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        onView(allOf(withText(R.string.frag_sched_scheduled_recording), hasSibling(withText(containsString("" + hourStart + ":" + minuteStart))), hasSibling(withText(containsString("" + hourEnd + ":" + minuteEnd))))).perform(click());
        onView(withId(R.id.tvTimeStart)).check(matches(withText(containsString("" + hourStart + ":" + minuteStart))));
        onView(withId(R.id.tvTimeEnd)).check(matches(withText(containsString("" + hourEnd + ":" + minuteEnd))));
        onView(withId(R.id.tvDateStart)).check(matches(withText(Utils.formatDateMedium(calDay.getTimeInMillis()))));
        onView(withId(R.id.tvDateEnd)).check(matches(withText(Utils.formatDateMedium(calDay.getTimeInMillis()))));
        onView(withContentDescription(getToolbarNavigationContentDescription())).perform(click());
    }

    private void checkDayAndMonth(Calendar calDay, Calendar calMonth) {
        onView(withId(R.id.tvDate)).check(matches(withText(Utils.formatDateDayNumber(new Date(calDay.getTimeInMillis())))));
        onView(withId(R.id.tvMonth)).check(matches(withText(Utils.formatDateMonthName(new Date(calMonth.getTimeInMillis())))));
    }

    private void addScheduledRecording(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        onView(withId(R.id.fab_add)).perform(click());

        onView(withId(R.id.tvTimeStart)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hourStart, minuteStart));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        onView(withId(R.id.tvTimeEnd)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hourEnd, minuteEnd));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());

        onView(withId(R.id.action_save)).perform(click());
    }

    private void deleteRecording(int hourStart, int minuteStart, int hourEnd, int minuteEnd) {
        onView(allOf(withText(R.string.frag_sched_scheduled_recording), hasSibling(withText(containsString("" + hourStart + ":" + minuteStart))), hasSibling(withText(containsString("" + hourEnd + ":" + minuteEnd))))).perform(longClick());
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());
    }

    // Click on a specific day in the calendar view.
    private void clickOnDay(Calendar cal) {
        final String tag = "android:switcher:" + R.id.pager + ":" + 2;
        ScheduledRecordingsFragment fragment = (ScheduledRecordingsFragment) mActivityRule.getActivity().getSupportFragmentManager().findFragmentByTag(tag);
        fragment.clickOnDay(new Date(cal.getTimeInMillis()));
        try {
            Thread.sleep(200); // the change requires some time...
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
        Class used to interact with the TimePicker with Espresso.
     */
    private static ViewAction setTime(final int hour, final int minute) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                TimePicker tp = (TimePicker) view;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tp.setHour(hour);
                    tp.setMinute(minute);
                } else {
                    tp.setCurrentHour(hour);
                    tp.setCurrentMinute(minute);
                }
            }

            @Override
            public String getDescription() {
                return "Set the passed time into the TimePicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(TimePicker.class);
            }
        };
    }

    private String getToolbarNavigationContentDescription() {
        return TestUtils.getToolbarNavigationContentDescription(
                getActivityInstance(), R.id.my_toolbar);
    }

    // Get the current Activity.
    private Activity getActivityInstance() {
        final Activity[] activity = new Activity[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Activity currentActivity;
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                currentActivity = (Activity) resumedActivities.iterator().next();
                activity[0] = currentActivity;
            }
        });
        return activity[0];
    }
}
