package com.hydroh.yamibo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hydroh.yamibo.R;
import com.hydroh.yamibo.util.DocumentParser;
import com.hydroh.yamibo.ui.adapter.PostAdapter;
import com.hydroh.yamibo.util.HttpCallbackListener;
import com.hydroh.yamibo.util.HttpUtil;

import java.util.List;

import static android.content.ContentValues.TAG;

public class PostActivity extends AppCompatActivity {

    List<MultiItemEntity> replyList;
    List<String> imgUrlList;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("url")) {
                url = extras.getString("url");
            }
            if (extras.containsKey("title")) {
                setTitle(extras.getString("title"));
            } else {
                setTitle("贴子详情");
            }
        }

        Log.d(TAG, "onCreate: URL: " + url);

        loadPosts(findViewById(R.id.hint_text));
    }

    public void loadPosts(View view) {
        Log.d(TAG, "refreshNetwork: URL: " + url);

        if (view.getId() == R.id.hint_text) {
            TextView hintText = findViewById(R.id.hint_text);
            hintText.setVisibility(View.GONE);
            ProgressBar hintProgressBar = findViewById(R.id.hint_progressbar);
            hintProgressBar.setVisibility(View.VISIBLE);
        }

        HttpUtil.getHtmlDocument(url, false, new HttpCallbackListener() {
            @Override
            public void onFinish(DocumentParser doc) {
                replyList = doc.parsePost();
                imgUrlList = doc.getImgUrlList();
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

                        PostAdapter adapter = new PostAdapter(replyList, imgUrlList);
                        recyclerView.setAdapter(adapter);
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
                        PostAdapter adapter = (PostAdapter) recyclerView.getAdapter();
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

}
