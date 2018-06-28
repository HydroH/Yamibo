package com.hydroh.yamibo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.util.DocumentParser;
import com.hydroh.yamibo.ui.adapter.HomeAdapter;
import com.hydroh.yamibo.util.CookieUtil;
import com.hydroh.yamibo.util.HttpCallbackListener;
import com.hydroh.yamibo.util.HttpUtil;

import java.util.List;

import static android.content.ContentValues.TAG;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String DEFAULT_URL = "forum.php";
    private List<MultiItemEntity> homeItemList;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        final SwipeRefreshLayout sectionRefresh = findViewById(R.id.refresh_common);
        sectionRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadHome(findViewById(R.id.refresh_common));
            }
        });

        Toolbar toolbar = findViewById(R.id.nav_toolbar);
        setSupportActionBar(toolbar);
        CookieUtil.getInstance().getCookiePreference(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        url = DEFAULT_URL;
        if (extras != null) {
            if (extras.containsKey("url")) {
                url = extras.getString("url");
            }
            if (extras.containsKey("title")) {
                setTitle(extras.getString("title"));
            }
        }

        Log.d(TAG, "onCreate: URL: " + url);

        loadHome(findViewById(R.id.hint_text));
    }

    public void loadHome(View view) {
        Log.d(TAG, "refreshNetwork: URL: " + url);

        if (view.getId() == R.id.hint_text) {
            TextView hintText = findViewById(R.id.hint_text);
            hintText.setVisibility(View.GONE);
            ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
            hintProgressBar.setVisibility(View.VISIBLE);
        }

        HttpUtil.getHtmlDocument(url, false, new HttpCallbackListener() {
            @Override
            public void onFinish(DocumentParser docParser) {
                homeItemList = docParser.parseHome();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
                        hintProgressBar.setVisibility(View.GONE);
                        RecyclerView recyclerView = findViewById(R.id.list_common);
                        SwipeRefreshLayout sectionRefresh = findViewById(R.id.refresh_common);
                        sectionRefresh.setRefreshing(false);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
                        recyclerView.setLayoutManager(layoutManager);

                        HomeAdapter adapter = new HomeAdapter(homeItemList);
                        recyclerView.setAdapter(adapter);
                        adapter.expandAll();
                        adapter.collapseSticky();

                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                                recyclerView.getContext(),
                                layoutManager.getOrientation()
                        );
                        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.divider_horizontal_thin));
                        if (recyclerView.getItemDecorationCount() == 0) {
                            recyclerView.addItemDecoration(dividerItemDecoration);
                        }

                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView recyclerView = findViewById(R.id.list_common);
                        HomeAdapter adapter = (HomeAdapter) recyclerView.getAdapter();
                        if (adapter != null) {
                            adapter.clear();
                        }
                        TextView hintText = findViewById(R.id.hint_text);
                        hintText.setVisibility(View.VISIBLE);
                        ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
                        hintProgressBar.setVisibility(View.GONE);
                        SwipeRefreshLayout sectionRefresh = findViewById(R.id.refresh_common);
                        sectionRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void startLoginActivity(View view) {
        Intent intent = new Intent(view.getContext(), LoginActivity.class);
        view.getContext().startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
