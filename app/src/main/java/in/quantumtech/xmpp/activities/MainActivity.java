package in.quantumtech.xmpp.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.bitmapcache.AvatarImageFetcher;
import in.quantumtech.xmpp.fragments.ContactListFragment;
import in.quantumtech.xmpp.fragments.ConversationFragment;
import in.quantumtech.xmpp.service.MessageService;

import java.util.ArrayList;

/**
 * Created by dilli on 12/24/2015.
 */
public class MainActivity extends AppCompatActivity {
    private AvatarImageFetcher imageFetcher;
    public String forwardText;
    private ViewPager pager;
    public ArrayList<String> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forwardText = getIntent().getStringExtra("action");
        data = getIntent().getStringArrayListExtra("data");
        getIntent().removeExtra("data");
        getIntent().removeExtra("action");
        setContentView(R.layout.activity_main);

        // start service to login
        startService(new Intent(MessageService.ACTION_CONNECT, null, this, MessageService.class));

        imageFetcher = AvatarImageFetcher.getAvatarImageFetcher(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        pager = (ViewPager)findViewById(R.id.pager);
        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(this);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(0);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }
    //goto chat list after creating group chat.
    public void gotoChat(){
        pager.setCurrentItem(0);
    }
    public AvatarImageFetcher getImageFetcher() {
        return imageFetcher;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(this, SearchUserActivity.class));
                return true;

            case R.id.action_set_status:
                startActivity(new Intent(this, SetStatusActivity.class));
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        imageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        imageFetcher.closeCache();
    }

    static class MainFragmentPagerAdapter extends FragmentPagerAdapter {
        private static final int COUNT = 2;
        private Activity activity;

        public MainFragmentPagerAdapter(Activity activity) {
            super(activity.getFragmentManager());
            this.activity = activity;
        }

        public Fragment getItem(int position) {
            if (position == 0) {
                return new ConversationFragment();
            }

            if (position == 1) {
                return new ContactListFragment();
            }

            throw new IllegalArgumentException("invalid position");
        }

        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return activity.getString(R.string.chats);
            }

            if (position == 1) {
                return activity.getString(R.string.contacts);
            }

            throw new IllegalArgumentException("invalid position");
        }
    }
}