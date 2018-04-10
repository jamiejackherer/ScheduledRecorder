/*
 * Year: 2017. This class was added by iClaude.
 */

package com.iclaude.scheduledrecorder.espresso;

import android.Manifest;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.didagger2.AppModule;
import com.iclaude.scheduledrecorder.didagger2.DatabaseModule;
import com.iclaude.scheduledrecorder.testutils.DaggerTestComponent;
import com.iclaude.scheduledrecorder.ui.activities.MainActivity;
import com.iclaude.scheduledrecorder.utils.Utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.internal.util.Checks.checkArgument;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Tests on RecordFragment.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoRecordFragment {

    @Inject
    RecordingsRepository recordingsRepository;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE);

    private String myRecordings;
    private String timeStr;


    @Before
    public void setUp() {
        // Clear database before tests.
        DaggerTestComponent.builder()
                .appModule(new AppModule(InstrumentationRegistry.getTargetContext()))
                .databaseModule(new DatabaseModule())
                .build()
                .inject(this);

        recordingsRepository.deleteAllRecordings();
        recordingsRepository.deleteAllScheduledRecordings();

        myRecordings = mActivityRule.getActivity().getResources().getString(R.string.default_file_name);
        timeStr = Utils.formatDateShort(System.currentTimeMillis());
    }

    /*
        Checks that:
        - when I click on the start/stop record button the UI changes correctly
        - when I stop recording the new recording is added to the file viewer Fragment
     */
    @Test
    public void startAndStopRecording() {
        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        try {
            Thread.sleep(3000); // record for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the UI displays the ongoing recording.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is added to FileViewerFragment list.
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Open the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.file_name_text_view)).check(matches(withText(containsString(myRecordings))));
        pressBack();

        // Delete the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, longClick()));
        onView(withText(R.string.dialog_file_delete)).perform(click());
        onView(withText(R.string.dialog_action_yes)).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(matches(not(isDisplayed())));
        onView(withId(R.id.textView5)).check(matches(isDisplayed()));
    }

    /*
        Checks that:
        - when I stop the Activity while recording the recording continues
        - when I start the Activity again the UI shows the ongoing recording correctly
     */
    @Test
    public void stopActivityWhileRecording() {
        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        try {
            Thread.sleep(3000); // record for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Stop the Activity.
        MainActivity activity = mActivityRule.getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnPause(activity);
                getInstrumentation().callActivityOnStop(activity);
                try {
                    Thread.sleep(2000); // wait 2 seconds before restarting the app
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getInstrumentation().callActivityOnRestart(activity);
                getInstrumentation().callActivityOnStart(activity);
                getInstrumentation().callActivityOnResume(activity);
            }
        });

        try {
            Thread.sleep(200); // the service requires some time to connect
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the UI displays the ongoing recording.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is in the list.
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Delete the recording.
        onView(withText(containsString(myRecordings))).perform(longClick());
        onView(withText(R.string.dialog_file_delete)).perform(click());
        onView(withText(R.string.dialog_action_yes)).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(matches(not(isDisplayed())));
        onView(withId(R.id.textView5)).check(matches(isDisplayed()));
    }

    /*
        Checks that:
        - when I destroy the Activity while recording the recording continues
        - when I start the Activity again the UI shows the ongoing recording correctly
    */
    @Test
    public void destroyActivityWhileRecording() {
        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Destroy the Activity.
        MainActivity activity = mActivityRule.getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.finish();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Restart the app.
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().startActivitySync(intent);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the UI shows the ongoing recording correctly.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop the recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is in the list.
        onView(withId(R.id.pager)).perform(swipeLeft());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Delete the recording.
        onView(withText(containsString(myRecordings))).perform(longClick());
        onView(withText(R.string.dialog_file_delete)).perform(click());
        onView(withText(R.string.dialog_action_yes)).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(matches(not(isDisplayed())));
        onView(withId(R.id.textView5)).check(matches(isDisplayed()));
    }

    /**
     * A custom {@link Matcher} which matches an item in a {@link RecyclerView} by its text,
     * using containsString.
     * <p>
     * View constraints:
     * <ul>
     * <li>View must be a child of a {@link RecyclerView}
     * <ul>
     *
     * @param itemText the text to match
     * @return Matcher that matches text in the given view
     */

    private Matcher<View> withItemContainsText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(
                        isDescendantOfA(isAssignableFrom(RecyclerView.class)),
                        withText(containsString(itemText))).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA LV with text " + itemText);
            }
        };
    }

}
