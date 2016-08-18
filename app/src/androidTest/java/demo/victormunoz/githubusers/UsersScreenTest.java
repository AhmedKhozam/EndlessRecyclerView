package demo.victormunoz.githubusers;

import android.support.test.filters.MediumTest;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static demo.victormunoz.githubusers.UsersScreenTest.Matchers.withItemCount;

import demo.victormunoz.githubusers.macher.RecyclerViewMatcher;
import demo.victormunoz.githubusers.ui.userdetail.UserDetailActivity;
import demo.victormunoz.githubusers.ui.users.UsersActivity;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class UsersScreenTest {
    private final static  String FIRST_USER_LOGINNAME="mojombo";
    private final static String TWENTY_NINTH_USER_LOGINNAME="bmizerany";

    @Rule
    public ActivityTestRule<UsersActivity> mNotesActivityTestRule =
            new ActivityTestRule<>(UsersActivity.class);

    @Before
    public void registerIdlingResource() {
        //Initializes Intents and begins recording intents
        Intents.init();
       
        //set idle
        IdlingResource resource = mNotesActivityTestRule.getActivity();
        Espresso.registerIdlingResources(resource);
       
        //trick to allow scrollToPosition inside CoordinatorLayout, otherwise the scroll will not be
        // performed
        mNotesActivityTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView recyclerView = (RecyclerView) mNotesActivityTestRule.getActivity().findViewById(R.id.recycler_view);
                CoordinatorLayout.LayoutParams params =
                        (CoordinatorLayout.LayoutParams) recyclerView.getLayoutParams();
                params.setBehavior(null);
                recyclerView.requestLayout();
            }
        });
    }

    /**
     * scroll three times to the last element of the recyclerview and the check if exactly 90
     * elements are loaded
     */
    @Test
    public void endlessScrollingTest() {
        //element 0  displayed
        onView(withRecyclerView(R.id.recycler_view).atPosition(0)).check(matches(isDisplayed()));
        //scroll to element 29 (load more)
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(29));
        //scroll to the next row
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(30));
        //element 30 is displayed
        onView(withRecyclerView(R.id.recycler_view).atPosition(30)).check(matches(isDisplayed()));
        //scroll to element 59 (load more)
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(59));
        //scroll to the next row
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(60));
        //element 60 is displayed
        onView(withRecyclerView(R.id.recycler_view).atPosition(60)).check(matches(isDisplayed()));
        //total loaded users
        onView(withId(R.id.recycler_view)).check(matches(withItemCount(90)));
        //scroll to beginning
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(0));


    }

    @Test
    public void clickFirstUser_openDetailActivity() throws Exception {
        onView(withId(R.id.recycler_view)).perform(actionOnItemAtPosition(0, click()));
        intended(hasComponent(UserDetailActivity.class.getName()));
        onView(withId(R.id.user_login)).check(matches(withText(FIRST_USER_LOGINNAME)));
        Espresso.pressBack();

    }

    @Test
    public void clickLastUser_openDetailActivity() throws Exception {
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(29));
        onView(withId(R.id.recycler_view)).perform(actionOnItemAtPosition(29, click()));
        intended(hasComponent(UserDetailActivity.class.getName()));
        onView(withId(R.id.user_login)).check(matches(withText(TWENTY_NINTH_USER_LOGINNAME)));
        Espresso.pressBack();
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(0));


    }

    @After
    public void unregisterIdlingResource() {
        Intents.release();
        IdlingResource resource = mNotesActivityTestRule.getActivity();
        Espresso.unregisterIdlingResources(resource);
    }
    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }
    static class Matchers {
        public static Matcher<View> withItemCount(final int size) {
            return new TypeSafeMatcher<View>() {
                @Override public boolean matchesSafely (final View view) {
                    return ((RecyclerView) view).getAdapter().getItemCount() == size;
                }

                @Override public void describeTo (final Description description) {
                    description.appendText ("recyclerView should have " + size + " items");
                }
            };
        }
    }
}