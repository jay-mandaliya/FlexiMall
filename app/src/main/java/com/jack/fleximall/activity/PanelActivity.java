package com.jack.fleximall.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.jack.fleximall.Global;
import com.jack.fleximall.fragment.MainFragment;
import com.jack.fleximall.fragment.ProfileFragment;
import com.jack.fleximall.R;
import com.jack.fleximall.UserProfile;
import com.jack.fleximall.fragment.WishlistFragment;

public class PanelActivity extends AppCompatActivity {

    private Global global;
    private UserProfile currentUser;
    private DrawerLayout drawerLayout;
    private Class currentFragment;

    @Override
    protected void onStart() {
        super.onStart();

        if (global.getFirebaseUser()==null){
            startMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        global = (Global)getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.panel_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        NavigationView navigationView = (NavigationView) findViewById(R.id.panel_navView);
        drawerLayout = (DrawerLayout)findViewById(R.id.panel_navDrawer);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();

                Fragment fragment;
                Class fragmentClass;

                switch (menuItem.getItemId()){
                    case R.id.panel_signOut:
                        userSignOut();
                        return true;

                    case R.id.panel_user_profile:
                        fragmentClass = ProfileFragment.class;
                        break;

                    case R.id.panel_wishlist:
                        fragmentClass = WishlistFragment.class;
                        break;

                    case R.id.panel_cart:
                        fragmentClass = MainFragment.class;
                        break;

                    default:
                        fragmentClass = MainFragment.class;
                        break;
                }

                if (fragmentClass != currentFragment){
                    try {
                        fragment = (Fragment)fragmentClass.newInstance();
                        currentFragment = fragmentClass;
                    } catch (IllegalAccessException | InstantiationException e) {
                        fragment = new MainFragment();
                        currentFragment = MainFragment.class;
                        e.printStackTrace();
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.panel_fragment,fragment).commit();
                    setTitle(menuItem.getTitle());
                }
                return true;
            }
        });

        setUserInfo();

        getSupportFragmentManager().beginTransaction().replace(R.id.panel_fragment,new MainFragment()).commit();
        currentFragment = MainFragment.class;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checkout_option, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.checkout:
                startActivity(new Intent(this,CheckoutActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUserInfo(){

        global.getProfileRef().child(global.getFirebaseUser().getUid()).addListenerForSingleValueEvent
                (new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUser = dataSnapshot.getValue(UserProfile.class);
                    if (currentUser != null) {

                        if ((TextView) findViewById(R.id.panel_nav_header_txtName) != null && ((TextView)
                                findViewById(R.id.panel_nav_header_txtEmail)) != null) {
                            ((TextView) findViewById(R.id.panel_nav_header_txtName)).setText(currentUser.getName());
                            ((TextView) findViewById(R.id.panel_nav_header_txtEmail)).setText(global.getFirebaseUser().
                                    getEmail());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void userSignOut(){
        global.getDatabaseHelper().dropAll();
        global.getFirebaseAuth().signOut();
        startMainActivity();
    }

}
