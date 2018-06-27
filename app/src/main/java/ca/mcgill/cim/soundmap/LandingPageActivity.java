package ca.mcgill.cim.soundmap;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class LandingPageActivity extends AppCompatActivity {

    private static final String TAG = "LandingPage";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private String mUser;

    private TextView mUserText;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Button mStartButton;

    private FileTransferService mFileTransferService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        // -----------------------------------------------------------------------------------------

        try {
            String sampleFile = getExternalCacheDir().getAbsolutePath();
            sampleFile += "/test.txt";

            File file = new File(sampleFile);
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("Test data");
                writer.close();

                LatLng loc = new LatLng(45,73);

                mFileTransferService = new FileTransferService(sampleFile, "test", loc);

            } catch (Exception e) {
                Log.e(TAG, "onCreate: Error - " + e.getMessage());
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreate: Error - " + e.getMessage());
            return;
        }

        // -----------------------------------------------------------------------------------------

        mUserText = (TextView) findViewById(R.id.signed_in_header);
        mStartButton = (Button) findViewById(R.id.start_mapping_button);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(mViewPager);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        // Check to ensure Google Play Services is active and up-to-date
        if (isServicesAvailable()) {
            Log.d(TAG, "onCreate: Google Play Services are available");
        } else {
            Log.w(TAG, "onCreate: Google Play Services are not available");
            mStartButton.setEnabled(false);
        }

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFileTransferService.execute();
            }
        });

        if (mUser == null || mUser.trim().equals("")) {
            attemptSignIn();
        }
    }

    private void attemptSignIn() {
        Intent signInScreenIntent = new Intent(this, LoginActivity.class);
        final int result = 1;
        startActivityForResult(signInScreenIntent, result);
    }

    private void startMap() {
        Intent mapIntent = new Intent(this, MappingActivity.class);
        mapIntent.putExtra("email", mUser);
        startActivity(mapIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            Bundle signInResult = data.getExtras();
            if (signInResult != null) {
                mUser = signInResult.getString("email");
                mUserText.append("\n" + mUser);
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new InstructionsFragment(), "Instructions");
        adapter.addFragment(new TermsFragment(), "Terms");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // Check that the Google Play Services is available and compatible
    private boolean isServicesAvailable() {
        Log.d(TAG, "isServicesAvailable: Verifying version and connectivity");
        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(LandingPageActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesAvailable: Google Play Services successfully connected.");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesAvailable: An error occurred, but can be resolved by the user");
            Dialog dialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(LandingPageActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        } else {
            Log.d(TAG, "isServicesAvailable: An unresolvable error occurred");
            Toast.makeText(this, "An unresolvable error occurred with Google Play Services",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing...
    }
}
