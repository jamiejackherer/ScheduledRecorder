package com.iclaude.scheduledrecorder.espresso;

import android.Manifest;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.iclaude.scheduledrecorder.R;
import com.iclaude.scheduledrecorder.database.RecordingsRepository;
import com.iclaude.scheduledrecorder.didagger2.AppModule;
import com.iclaude.scheduledrecorder.didagger2.DatabaseModule;
import com.iclaude.scheduledrecorder.testutils.DaggerTestComponent;
import com.iclaude.scheduledrecorder.ui.activities.scheduled_recording.ScheduledRecordingDetailsActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Tests on ScheduledRecordingDetailsActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EspressoScheduledRecordingDetailsActivity {

    @Inject
    RecordingsRepository recordingsRepository;

    @Rule
    public ActivityTestRule<ScheduledRecordingDetailsActivity> mActivityRule = new ActivityTestRule<>(ScheduledRecordingDetailsActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE);

    private Calendar cal;


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

        cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
    }

    // Start hour after end hour.
    @Test
    public void testStartTimeAfterEnd() {
        // Data.
        cal.add(Calendar.DATE, 5);
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        // Set dates on TimePicker and check that the TextView is updated.
        onView(ViewMatchers.withId(R.id.tvDateStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());
        onView(withId(R.id.tvDateStart)).check(matches(withText(containsString("" + day))));

        onView(withId(R.id.tvDateEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());
        onView(withId(R.id.tvDateEnd)).check(matches(withText(containsString("" + day))));

        // Set times on TimePicker and check that the TextView is updated.
        onView(withId(R.id.tvTimeStart)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(23, 45));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());
        onView(withId(R.id.tvTimeStart)).check(matches(withText(containsString("11:45"))));

        onView(withId(R.id.tvTimeEnd)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(23, 45));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());
        onView(withId(R.id.tvTimeStart)).check(matches(withText(containsString("11:45"))));

        // Check error message and try to save the scheduled recording.
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(withText(R.string.toast_scheduledrecording_timeerror_start_after_end)));
        onView(withId(R.id.action_save)).perform(click());
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(withText(R.string.toast_scheduledrecording_timeerror_start_after_end)));
    }

    // Start date after end date.
    @Test
    public void testStartDateAfterEnd() {
        // Data.
        cal.add(Calendar.DATE, 5);
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int dayEnd = day - 1;

        // Set dates on DatePicker and check that the TextView is updated.
        onView(withId(R.id.tvDateStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());

        onView(withId(R.id.tvDateEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, dayEnd));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        // Set times on DatePicker and check that the TextView is updated.
        onView(withId(R.id.tvTimeStart)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(23, 0));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());

        onView(withId(R.id.tvTimeEnd)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(23, 30));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        // Check error message and try to save the scheduled recording.
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(withText(R.string.toast_scheduledrecording_timeerror_start_after_end)));
        onView(withId(R.id.action_save)).perform(click());
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(withText(R.string.toast_scheduledrecording_timeerror_start_after_end)));
    }

    // Recording scheduled in the past.
    @Test
    public void testScheduleInThePast() {
        // Data.
        cal.add(Calendar.DATE, -5);
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        // Set dates.
        onView(withId(R.id.tvDateStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());

        onView(withId(R.id.tvDateEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        // Set times.
        onView(withId(R.id.tvTimeStart)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(23, 0));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());

        onView(withId(R.id.tvTimeEnd)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(23, 30));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        // Check error message and try to save the scheduled recording.
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(withText(R.string.toast_scheduledrecording_timeerror_past)));
        onView(withId(R.id.action_save)).perform(click());
        onView(withId(R.id.textView4)).check(matches(isDisplayed()));
        onView(withId(R.id.textView4)).check(matches(withText(R.string.toast_scheduledrecording_timeerror_past)));
    }

    // Data correct.
    @Test
    public void testDatesAndTimesCorrect() {
        // Data.
        cal.add(Calendar.DATE, 5);
        int day = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        // Set dates (times are correct by default).
        onView(withId(R.id.tvDateStart)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        onView(withId(R.id.tvDateEnd)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(setDate(year, month, day));
        onView(anyOf(withText("OK"), withText("Done"))).perform(click());


        // Check error message.
        onView(withId(R.id.textView4)).check(matches(not(isDisplayed())));
    }

    /*
        Class used to interact with the TimePicker with Espresso.
     */
    public static ViewAction setTime(final int hour, final int minute) {
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

    /*
    Class used to interact with the DatePicker with Espresso.
 */
    public static ViewAction setDate(final int year, final int month, final int day) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                DatePicker dp = (DatePicker) view;
                dp.updateDate(year, month, day);
            }

            @Override
            public String getDescription() {
                return "Set the passed time into the DatePicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(DatePicker.class);
            }
        };
    }
}
