package com.example.weteams;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.weteams.fragments.chat.ChatFragment;
import com.example.weteams.fragments.dashboard.DashboardFragment;
import com.example.weteams.fragments.files.FilesFragment;
import com.example.weteams.fragments.projects.ProjectsFragment;
import com.example.weteams.fragments.schedule.ScheduleFragment;
import com.example.weteams.fragments.settings.SettingsFragment;
import com.example.weteams.logic.Project;
import com.example.weteams.logic.User;
import com.example.weteams.viewmodel.MainViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public static final String CURRENT_FRAGMENT = "currentFragment";
    public static final String SHARED_PREFS_KEY = "com.example.weteams";

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navView;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startSignInActivity();
            return;
        }
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startSignInActivity();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawerLayout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navView = findViewById(R.id.navView);
        updateNavHeader(user.getDisplayName(), user.getEmail());
        updateNavMenu(null);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switchToFragment(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        ViewModelProvider.Factory factory = new ViewModelProvider.AndroidViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        viewModel.getCurrentUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                updateNavHeader(user.getDisplayName(), user.getEmail());
            }
        });
        viewModel.getCurrentProject().observe(this, new Observer<Project>() {
            @Override
            public void onChanged(Project project) {
                updateNavMenu(project);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(CURRENT_FRAGMENT) == null) {
            switchToFragment(R.id.navItemProjects);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        drawerLayout.openDrawer(GravityCompat.START);
        return true;
    }

    public void updateNavHeader(String displayName, String email) {
        View headerView = navView.getHeaderView(0);
        TextView navHeaderTitle = headerView.findViewById(R.id.navHeaderTitle);
        TextView navHeaderSubtitle = headerView.findViewById(R.id.navHeaderSubtitle);
        navHeaderTitle.setText(displayName);
        navHeaderSubtitle.setText(email);
    }

    public void updateNavMenu(Project project) {
        Menu menu = navView.getMenu();
        MenuItem currentProjectItem = menu.findItem(R.id.navItemCurrentProject);
        currentProjectItem.setTitle(project != null ? project.getName() : "No Project");
        for (int i = 0; i < currentProjectItem.getSubMenu().size(); i++) {
            currentProjectItem.getSubMenu().getItem(i).setEnabled(project != null);
        }
    }

    public void switchToFragment(int itemId) {
        Fragment fragment = null;
        switch (itemId) {
            case R.id.navItemProjects:
                fragment = new ProjectsFragment();
                break;
            case R.id.navItemDashboard:
                fragment = new DashboardFragment();
                break;
            case R.id.navItemSchedule:
                fragment = new ScheduleFragment();
                break;
            case R.id.navItemFiles:
                fragment = new FilesFragment();
                break;
            case R.id.navItemChat:
                fragment = new ChatFragment();
                break;
            case R.id.navItemSettings:
                fragment = new SettingsFragment();
                break;
        }
        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.mainContent, fragment, CURRENT_FRAGMENT).commit();
        }
    }

    public void startSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }


}
