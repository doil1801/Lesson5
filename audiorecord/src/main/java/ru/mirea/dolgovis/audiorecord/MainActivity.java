package ru.mirea.dolgovis.audiorecord; // ← ваш пакет!

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;

import ru.mirea.dolgovis.audiorecord.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSION = 200;
    private static final String RECORD_TEXT = "Начать запись. №10, БИСО-01-22";

    private ActivityMainBinding binding;
    private boolean isWork;
    private String recordFilePath;

    private MediaRecorder recorder;
    private MediaPlayer player;

    private boolean isStartRecording = true;
    private boolean isStartPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.playButton.setEnabled(false);

        recordFilePath = new File(
                getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "audiorecordtest.3gp").getAbsolutePath();

        int audioPermissionStatus = ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO);
        if (audioPermissionStatus == PackageManager.PERMISSION_GRANTED) {
            isWork = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_PERMISSION);
        }

        binding.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWork) {
                    Toast.makeText(MainActivity.this,
                            "Нет разрешения на микрофон", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isStartRecording) {
                    binding.recordButton.setText("Остановить запись");
                    binding.playButton.setEnabled(false);
                    startRecording();
                } else {
                    binding.recordButton.setText(RECORD_TEXT);
                    binding.playButton.setEnabled(true);
                    stopRecording();
                }
                isStartRecording = !isStartRecording;
            }
        });

        binding.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStartPlaying) {
                    binding.playButton.setText("Остановить воспроизведение");
                    binding.recordButton.setEnabled(false);
                    startPlaying();
                } else {
                    binding.playButton.setText("Воспроизвести");
                    binding.recordButton.setEnabled(true);
                    stopPlaying();
                }
                isStartPlaying = !isStartPlaying;
            }
        });
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFilePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed", e);
        }
        recorder.start();
    }

    private void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                Log.e(TAG, "stop() failed", e);
            }
            recorder.release();
            recorder = null;
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(recordFilePath);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                    binding.playButton.setText("Воспроизвести");
                    binding.recordButton.setEnabled(true);
                    isStartPlaying = true;
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed", e);
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            isWork = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!isWork) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            stopRecording();
            isStartRecording = true;
            binding.recordButton.setText(RECORD_TEXT);
            binding.playButton.setEnabled(true);
        }
        if (player != null) {
            stopPlaying();
            isStartPlaying = true;
            binding.playButton.setText("Воспроизвести");
            binding.recordButton.setEnabled(true);
        }
    }
}