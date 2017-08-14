package com.hpr.hus.popularmovies;

/**
 * Created by hk640d on 8/1/2017.
 */

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private RecyclerView rvList;

    //private final String LOG_TAG = MainActivity.class.getSimpleName();





    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("hhhh", "MainActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rvList = (RecyclerView) findViewById(R.id.recyclerview_movies_list);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
       rvList.setLayoutManager(layoutManager);
        rvList.setHasFixedSize(true);

        rvList.setVisibility(View.VISIBLE);

        //Log.v("hhhh-savedInstanceState", savedInstanceState.toString());
        if (savedInstanceState == null) {

            getMoviesFromTMDb(getSortMethod());
        } else {

            Parcelable[] parcelable = savedInstanceState.
                    getParcelableArray(getString(R.string.parcel_movie));

            if (parcelable != null) {
                int numMovieObjects = parcelable.length;
                MovieSelected[] movies = new MovieSelected[numMovieObjects];
                for (int i = 0; i < numMovieObjects; i++) {
                    movies[i] = (MovieSelected) parcelable[i];
                }

                rvList.setAdapter(new MovieAdapter( this,movies));
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, mMenu);


        mMenu = menu;


        mMenu.add(Menu.NONE,
                R.string.pref_sort_pop_desc_key,
                Menu.NONE,
                null)
                .setVisible(false)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        mMenu.add(Menu.NONE, R.string.pref_sort_vote_avg_desc_key, Menu.NONE, null)
                .setVisible(false)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        updateMenu();

        return true;
    }

/*    @Override
    protected void onSaveInstanceState(Bundle outState) {



        int numMovieObjects = 10;
        if (numMovieObjects > 0) {

            MovieSelected[] movies = new MovieSelected[numMovieObjects];
            for (int i = 0; i < numMovieObjects; i++) {

               // movies[i] = (MovieSelected) rvList.getItemAtPosition(i);
            }


            outState.putParcelableArray(getString(R.string.parcel_movie), movies);
        }

        super.onSaveInstanceState(outState);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.string.pref_sort_pop_desc_key:
                updateSharedPrefs(getString(R.string.tmdb_sort_pop_desc));
                updateMenu();
                getMoviesFromTMDb(getSortMethod());
                Log.v("kkkkkk","pref_sort_pop_desc_key");
                return true;
            case R.string.pref_sort_vote_avg_desc_key:
                updateSharedPrefs(getString(R.string.tmdb_sort_vote_avg_desc));
                updateMenu();
                getMoviesFromTMDb(getSortMethod());
                Log.v("kkkkkk","pref_sort_vote_avg_desc_key");
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }


    private final GridView.OnItemClickListener moviePosterClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MovieSelected movie = (MovieSelected) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
            intent.putExtra(getResources().getString(R.string.parcel_movie), movie);

            startActivity(intent);
        }
    };



    private void getMoviesFromTMDb(String sortMethod) {
        if (isNetworkAvailable()) {
            Log.v("gggg","NetworkAvailable");
            String apiKey = getString(R.string.key_themoviedb);

            TaskInterfaceCompleted taskCompleted = new TaskInterfaceCompleted() {
                @Override
                public void onFetchMoviesTaskCompleted(MovieSelected[] movies) {
                    Log.v("gggg2",movies.toString());
                    rvList.setAdapter(new MovieAdapter(movies,getApplicationContext()));
                }
            };


            AsyncTaskFetchPopularMovies movieTask = new AsyncTaskFetchPopularMovies(taskCompleted, apiKey);
            movieTask.execute(sortMethod);
        } else {
            Log.v("gggg","NOT-----------NetworkAvailable");
            Toast.makeText(this, getString(R.string.error_need_internet), Toast.LENGTH_LONG).show();
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void updateMenu() {
        String sortMethod = getSortMethod();

        if (sortMethod.equals(getString(R.string.tmdb_sort_pop_desc))) {
            mMenu.findItem(R.string.pref_sort_pop_desc_key).setVisible(false);
            mMenu.findItem(R.string.pref_sort_vote_avg_desc_key).setVisible(true);
        } else {
            mMenu.findItem(R.string.pref_sort_vote_avg_desc_key).setVisible(false);
            mMenu.findItem(R.string.pref_sort_pop_desc_key).setVisible(true);
        }
    }


    private String getSortMethod() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String result = prefs.getString(getString(R.string.pref_sort_method_key),
                getString(R.string.tmdb_sort_pop_desc));


        Log.e("getSortMethod", result);
        return result;
    }


    private void updateSharedPrefs(String sortMethod) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_sort_method_key), sortMethod);
        editor.apply();
    }
    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        // COMPLETED (1) Pass the weather to the DetailActivity
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);
    }

}
