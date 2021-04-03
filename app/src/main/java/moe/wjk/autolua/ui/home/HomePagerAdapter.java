package moe.wjk.autolua.ui.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import moe.wjk.autolua.R;
import moe.wjk.autolua.ui.home.code.CodeFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class HomePagerAdapter extends FragmentStateAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.navigation_code, R.string.navigation_control};

    public HomePagerAdapter(Fragment f) {
        super(f);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return CodeFragment.newInstance();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return new ControlFragment();
            default:
                return null;
        }
    }

    @Nullable
    public static CharSequence getPageTitle(Context context, int position) {
        return context.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getItemCount() {
        // Show 2 total pages.
        return 2;
    }
}