/*
 * Year: 2017. This class was added by iClaude.
 */

package com.iclaude.scheduledrecorder.espresso;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.didagger2.AppModule;
import com.iclaude.scheduledrecorder.didagger2.DatabaseModule;
import com.iclaude.scheduledrecorder.testutils.DaggerTestComponent;
import com.iclaude.scheduledrecorder.testutils.TestUtils;
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
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.internal.util.Checks.checkArgument;
import static com.iclaude.scheduledrecorder.ui.fragments.fileviewer.RecyclerViewSwipeCallback.BUTTON_LEFT_WIDTH_DP;
import static com.iclaude.scheduledrecorder.ui.fragments.fileviewer.RecyclerViewSwipeCallback.BUTTON_RIGHT_WIDTH_DP;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Tests on RecordFragment.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoRecordFragment {

    private static final String TAG = "SCHEDULED_RECORDER_TAG";

    private enum ICONS {EDIT, SHARE, DELETE};

    @Inject
    RecordingsRepository recordingsRepository;

    /*@Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);*/

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE);

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(MainActivity.class);


    private String myRecordings, tabRecordTitle, tabFilesTitle, tabScheduledTitle;
    private String timeStr;
    private float buttonRightWidth, buttonLeftWidth, cardViewMargin;


    @Before
    public void setUp() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Clear database before tests.
        DaggerTestComponent.builder()
                .appModule(new AppModule(InstrumentationRegistry.getTargetContext()))
                .databaseModule(new DatabaseModule())
                .build()
                .inject(this);

        recordingsRepository.deleteAllRecordings();
        recordingsRepository.deleteAllScheduledRecordings();

        Resources resources = mActivityRule.getActivity().getResources();
        myRecordings = resources.getString(R.string.default_file_name);
        tabRecordTitle = resources.getString(R.string.tab_title_record);
        tabFilesTitle = resources.getString(R.string.tab_title_saved_recordings);
        tabScheduledTitle = resources.getString(R.string.tab_title_scheduled_recordings);

        timeStr = Utils.formatDateShort(System.currentTimeMillis());
        buttonRightWidth = Utils.convertDpToPixel(InstrumentationRegistry.getTargetContext(), BUTTON_RIGHT_WIDTH_DP);
        buttonLeftWidth = Utils.convertDpToPixel(InstrumentationRegistry.getTargetContext(), BUTTON_LEFT_WIDTH_DP);
        cardViewMargin = resources.getDimension(R.dimen.cardview_margin);
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
        sleep(3000); // record for 3 seconds

        // Check that the UI displays the ongoing recording.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is added to FileViewerFragment list.
        onView(allOf(withText(tabFilesTitle),
                isDescendantOfA(withId(R.id.tabs)))).perform(click());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Open the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.file_name_text_view)).check(matches(withText(containsString(myRecordings))));
        pressBack();

        // Delete the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, swipeLeft()));
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, clickOnIcon(ICONS.DELETE)));
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
        sleep(3000); // record for 3 seconds

        // Stop the Activity.
        MainActivity activity = mActivityRule.getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnPause(activity);
                getInstrumentation().callActivityOnStop(activity);
                sleep(2000); // wait 2 seconds before restarting the app
                getInstrumentation().callActivityOnRestart(activity);
                getInstrumentation().callActivityOnStart(activity);
                getInstrumentation().callActivityOnResume(activity);
            }
        });

        sleep(200); // the service requires some time to connect

        // Check that the UI displays the ongoing recording.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is in the list.
        onView(allOf(withText(tabFilesTitle),
                isDescendantOfA(withId(R.id.tabs)))).perform(click());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Delete the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, swipeLeft()));
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, clickOnIcon(ICONS.DELETE)));
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
        sleep(3000);

        // Destroy the Activity.
        MainActivity activity = mActivityRule.getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                activity.finish();
                sleep(2000);
            }
        });

        // Restart the app.
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getInstrumentation().startActivitySync(intent);
        sleep(2000);

        // Check that the UI shows the ongoing recording correctly.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop the recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is in the list.
        onView(allOf(withText(tabFilesTitle),
                isDescendantOfA(withId(R.id.tabs)))).perform(click());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Delete the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, swipeLeft()));
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, clickOnIcon(ICONS.DELETE)));
        onView(withText(R.string.dialog_action_yes)).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(matches(not(isDisplayed())));
        onView(withId(R.id.textView5)).check(matches(isDisplayed()));
    }

    /*
        Checks that:
        - when I change the screen orientation the recording continues and the UI is
          updated correctly
    */
    @Test
    public void rotateScreenWhileRecording() {
        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        sleep(2000); // record 2 secs

        // Rotate screen.
        TestUtils.rotateOrientation(mActivityRule.getActivity());
        sleep(2000); // record 2 secs

        // Check that the UI shows the ongoing recording correctly.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Rotate screen.
        TestUtils.rotateOrientation(mActivityRule.getActivity());
        sleep(2000); // record 2 secs

        // Check that the UI shows the ongoing recording correctly.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop the recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is in the list.
        onView(allOf(withText(tabFilesTitle),
                isDescendantOfA(withId(R.id.tabs)))).perform(click());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Delete the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, swipeLeft()));
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, clickOnIcon(ICONS.DELETE)));
        onView(withText(R.string.dialog_action_yes)).perform(click());

        // Check that the recording is no longer in the list.
        onView(withText(containsString(myRecordings))).check(matches(not(isDisplayed())));
        onView(withId(R.id.textView5)).check(matches(isDisplayed()));
    }

    /*
        Checks that:
        - renaming of a recording file works correctly
     */
    @Test
    public void editRecording() {
        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        sleep(3000); // record for 3 seconds

        // Check that the UI displays the ongoing recording.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is added to FileViewerFragment list.
        onView(allOf(withText(tabFilesTitle),
                isDescendantOfA(withId(R.id.tabs)))).perform(click());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Rename the recording.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, swipeRight()));
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, clickOnIcon(ICONS.EDIT)));
        onView(withId(R.id.new_name)).perform(typeText("test"));
        onView(withText(R.string.dialog_action_ok)).perform(click());

        // Check that the file was renamed.
        onView(allOf(withItemContainsText("test"), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));
    }

    /*
        Checks that:
        - recording's sharing works
     */
    @Test
    public void shareRecording() {
        // Start recording.
        onView(withId(R.id.btnRecord)).perform(click());
        sleep(3000); // record for 3 seconds

        // Check that the UI displays the ongoing recording.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_in_progress)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(not(containsString("00:00")))));

        // Stop recording.
        onView(withId(R.id.btnRecord)).perform(click());

        // Check that the UI is restored to its initial state.
        onView(withId(R.id.recording_status_text)).check(matches(withText(R.string.record_prompt)));
        onView(withId(R.id.tvChronometer)).check(matches(withText(containsString("00:00"))));

        // Check that the recording is added to FileViewerFragment list.
        onView(allOf(withText(tabFilesTitle),
                isDescendantOfA(withId(R.id.tabs)))).perform(click());
        onView(allOf(withItemContainsText(myRecordings), hasSibling(withText(containsString(timeStr))))).check(matches(isDisplayed()));

        // Click on the share icon.
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, swipeRight()));
        onView(withId(R.id.recyclerView)).perform(actionOnItemAtPosition(0, clickOnIcon(ICONS.SHARE)));

        // Check that the correct Intent is displayed.
        intended(allOf(hasAction(Intent.ACTION_CHOOSER),
                hasExtra(is(Intent.EXTRA_INTENT),
                        allOf( hasAction(Intent.ACTION_SEND),
                                hasAction(Intent.ACTION_SEND),
                                hasType("audio/mp4")))));
    }


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private ViewAction clickOnIcon(ICONS icon) {
        return new GeneralClickAction(
                Tap.SINGLE,
                view -> {
                    final int[] screenPos = new int[2];
                    view.getLocationOnScreen(screenPos);

                    float screenX = 0;
                    float screenY = 0;
                    switch (icon) {
                        case DELETE:
                            screenX = view.getWidth() - buttonRightWidth/2;
                            screenY = screenPos[1] + view.getHeight()/2;
                            break;
                        case EDIT:
                            screenX = cardViewMargin + (buttonLeftWidth - 2*cardViewMargin) * 0.25f;
                            screenY = screenPos[1] + view.getHeight()/2;
                            break;
                        case SHARE:
                            screenX = cardViewMargin + (buttonLeftWidth - 2*cardViewMargin) * 0.75f;
                            screenY = screenPos[1] + view.getHeight()/2;
                            break;
                        default:
                            Log.e(TAG, "clickOnIcon: icon identifier unknown");
                    }


                    float[] coordinates = {screenX, screenY};

                    return coordinates;
                },
                Press.FINGER,
                InputDevice.SOURCE_MOUSE,
                MotionEvent.BUTTON_PRIMARY);
    }


}
