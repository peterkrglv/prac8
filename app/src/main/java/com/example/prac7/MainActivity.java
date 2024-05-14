package com.example.prac7;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.prac7.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainTag";
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button1.setOnClickListener(v -> {
            startSequentialTasks();
        });

        binding.button2.setOnClickListener(v -> {
            startParallelTasks();
        });

        binding.button3.setOnClickListener(v -> {
            loadImage();
        });
    }

    private void loadImage() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://random.dog/woof.json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String imageUrl = response.getString("url");
                            Picasso.get().load(imageUrl).into(binding.imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        queue.add(jsonObjectRequest);
    }




    private void startParallelTasks() {
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        OneTimeWorkRequest firstWork = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        OneTimeWorkRequest secondWork = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        workManager.enqueue(firstWork);
        workManager.enqueue(secondWork);
        workManager.getWorkInfoByIdLiveData(firstWork.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            Log.d(TAG, "Параллельная задача 1 выполнена");
                        }
                    }
                });
        workManager.getWorkInfoByIdLiveData(secondWork.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            Log.d(TAG, "Последовательная задача 2 выполнена");
                        }
                    }
                });
    }

    private void startSequentialTasks() {
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        OneTimeWorkRequest firstWork = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        OneTimeWorkRequest secondWork = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        OneTimeWorkRequest thirdWork = new OneTimeWorkRequest.Builder(MyWorker.class).build();
        workManager.enqueue(firstWork);
        workManager.getWorkInfoByIdLiveData(firstWork.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            Log.d(TAG, "Последовательная задача 1 выполнена");
                            workManager.enqueue(secondWork);
                        }
                    }
                });
        workManager.getWorkInfoByIdLiveData(secondWork.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            Log.d(TAG, "Последовательная задача 2 выполнена");
                            workManager.enqueue(thirdWork);
                        }
                    }
                });
        workManager.getWorkInfoByIdLiveData(thirdWork.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState().isFinished()) {
                            Log.d(TAG, "Последовательная задача 3 выполнена");
                        }
                    }
                });
    }
}