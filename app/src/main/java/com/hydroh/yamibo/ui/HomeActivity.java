package com.hydroh.yamibo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hydroh.yamibo.R;
import com.hydroh.yamibo.model.Section;
import com.hydroh.yamibo.ui.adaptor.SectionAdapter;
import com.hydroh.yamibo.util.HttpCallbackListener;
import com.hydroh.yamibo.util.HttpUtil;
import com.hydroh.yamibo.util.ObjectUtil;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    List<Section> sectionList;
    String url;
    public static final String DEFAULT_URL = "http://bbs.yamibo.com/forum.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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

        loadSectionList(findViewById(R.id.hint_text));

    }

    public void loadSectionList(View view) {
        Log.d(TAG, "refreshNetwork: URL: " + url);

        TextView hintText = findViewById(R.id.hint_text);
        hintText.setVisibility(View.INVISIBLE);
        ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
        hintProgressBar.setVisibility(View.VISIBLE);

        HttpUtil.getHtmlDocument(url, new HttpCallbackListener() {
            @Override
            public void onFinish(Document doc) {
                sectionList = ObjectUtil.documentToSections(doc);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
                        hintProgressBar.setVisibility(View.INVISIBLE);
                        RecyclerView recyclerView = findViewById(R.id.section_list);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
                        recyclerView.setLayoutManager(layoutManager);
                        SectionAdapter adapter = new SectionAdapter(sectionList);
                        recyclerView.setAdapter(adapter);
                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                                recyclerView.getContext(),
                                layoutManager.getOrientation()
                        );
                        recyclerView.addItemDecoration(dividerItemDecoration);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                sectionList = new ArrayList<Section>();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView hintText = findViewById(R.id.hint_text);
                        hintText.setVisibility(View.VISIBLE);
                        ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
                        hintProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
