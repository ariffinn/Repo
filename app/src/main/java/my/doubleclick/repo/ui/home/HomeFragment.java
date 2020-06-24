package my.doubleclick.repo.ui.home;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import my.doubleclick.repo.Adapter;
import my.doubleclick.repo.Item;
import my.doubleclick.repo.Pagination;
import my.doubleclick.repo.R;

import static my.doubleclick.repo.Pagination.PAGE_SIZE;
import static my.doubleclick.repo.Pagination.PAGE_START;

public class HomeFragment extends Fragment
    implements SwipeRefreshLayout.OnRefreshListener {
    public static final String LANGUAGE = "java";
    public static final String TOPIC    = "book";

    private Adapter            mAdapter;
    private boolean            mIsLastPage  = false;
    private boolean            mIsLoading   = false;
    private int                mCurrentPage = PAGE_START;
    private int                mItemCount   = 0;
    private SwipeRefreshLayout mSwipe;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mSwipe = v.findViewById(R.id.swipeRefresh);
        mSwipe.setOnRefreshListener(this);
        RecyclerView rv = v.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
        mAdapter = new Adapter(new ArrayList<Item>());
        rv.setAdapter(mAdapter);
        rv.addOnScrollListener(new Pagination(llm) {
            @Override
            protected void loadMoreItems() {
                mIsLoading = true;
                mCurrentPage++;
                searchGithubRepo();
            }

            @Override
            public boolean isLastPage() {
                return mIsLastPage;
            }

            @Override
            public boolean isLoading() {
                return mIsLoading;
            }
        });
        searchGithubRepo();
        return v;
    }

    @Override
    public void onRefresh() {
        mItemCount = 0;
        mCurrentPage = PAGE_START;
        mIsLastPage = false;
        mAdapter.clear();
        searchGithubRepo();
    }

    /**
     * Search Github repository by topic and language with pagination.
     */
    private void searchGithubRepo() {
        new SearchGithub().execute(
            "https://api.github.com/search/repositories?q=" + TOPIC +
            "+language:" + LANGUAGE + "&sort=stars&order=desc&page=" +
            mCurrentPage + "&per_page=" + PAGE_SIZE);
    }

    /**
     * Http Get using Android HttpURLConnection.
     */
    private class SearchGithub extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
                conn.setRequestProperty("Authorization",
                                        "0335672b3c6232852a1280e54a4a539c6dd5450d");
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept",
                                        "application/vnd.github.mercy-preview" +
                                        "+json");
                InputStream is = conn.getInputStream();
                BufferedReader
                    br =
                    new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();
                conn.disconnect();
                is.close();
                sb.delete(0, sb.length());
                return line;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(String s) {
            final ArrayList<Item> items = new ArrayList<>();
            if (s != null) {
                try {
                    JSONObject json = new JSONObject(s);
                    JSONArray jArray = json.getJSONArray("items");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        String fullName = json_data.getString("full_name");
                        String url = json_data.getString("html_url");
                        String updated_at = json_data.getString("updated_at");
                        mItemCount++;
                        Item item = new Item();
                        //populate data from Github
                        item.setFullName(fullName + mItemCount);
                        item.setUpdatedDate(updated_at);
                        item.setUrl(url);
                        items.add(item);
                    }
                    //Save to SharedPreferences. Ideally should use SQLite
                    SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(TOPIC, jArray.toString());
                    editor.apply();
                    if (mCurrentPage != PAGE_START) mAdapter.removeLoading();
                    mAdapter.addItems(items);
                    mSwipe.setRefreshing(false);
                    if (mCurrentPage < PAGE_SIZE) {
                        mAdapter.addLoading();
                    }
                    else {
                        mIsLastPage = true;
                    }
                    mIsLoading = false;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
