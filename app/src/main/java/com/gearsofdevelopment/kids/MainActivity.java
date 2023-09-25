package com.gearsofdevelopment.kids;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.gd_recycler);
        adapter = new Adapter(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (data == null || data.isEmpty()) {
            data = unzip("data.zip", new File("/images/"));
        }
        int span = new Random().nextInt(4);
        if (span < 2) {
            span = 2;
        }
        recyclerView.setBackgroundColor(getRandomColor());
        recyclerView.setLayoutManager(new GridLayoutManager(this, span));
        recyclerView.setAdapter(adapter);
        adapter.updateList(data);
        try {
            Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(Integer.MAX_VALUE / 2);
        } catch (Exception ignored) {

        }
    }

    public List<String> unzip(String zipFile, File directory) {
        List<String> paths = new ArrayList<>();
        try {
            File targetDirectory = new File(getFilesDir().getPath() + directory);
            if (!targetDirectory.exists()) {
                targetDirectory.mkdir();
            }
            AssetManager am = getAssets();
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(am.open(zipFile)));
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[1024];
                while ((ze = zis.getNextEntry()) != null) {
                    String path = targetDirectory + "/" + ze.getName();
                    try (FileOutputStream fout = new FileOutputStream(path)) {
                        paths.add(path);
                        while ((count = zis.read(buffer)) != -1)
                            fout.write(buffer, 0, count);
                    } catch (Exception e) {
                        return paths;
                    }
                }
            } finally {
                zis.close();
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }
        return paths;
    }

    public static int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private List<String> list;
        private final Animation animation;
        private final Random random;
        private final HashMap<Integer, String> indexes;

        public Adapter(Context context) {
            animation = getAnimation(context);
            random = new Random();
            indexes = new HashMap<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_base, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (indexes.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    indexes.put(i, list.get(i));
                }
            }
            Object[] values = indexes.keySet().toArray();
            Integer randomValue = (Integer) values[random.nextInt(values.length)];
            String image = indexes.get(randomValue);
            indexes.remove(randomValue);
            holder.setData(image);
            holder.setAnimation(animation);
        }

        @Override
        public int getItemCount() {
            return list != null && !list.isEmpty() ? Integer.MAX_VALUE : 0;
        }

        public Animation getAnimation(Context context) {
            return AnimationUtils.loadAnimation(context, R.anim.push_in);
        }

        public void updateList(List<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private Animation animation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);
        }

        public void setData(String image) {
            imageView.setOnClickListener(view -> view.startAnimation(animation));
            try {
                Glide.with(itemView.getContext())
                        .load(image)
                        .into(imageView);
            } catch (Exception e) {
            }
        }

        public void setAnimation(Animation animation) {
            this.animation = animation;
        }
    }

}