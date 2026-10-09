package ru.mirea.dolgovis.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

import ru.mirea.dolgovis.accelerometer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;

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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            Toast.makeText(this, "Акселерометр не найден на устройстве",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            binding.textViewAzimuth.setText(
                    String.format(Locale.US, "Azimuth (X): %.2f м/с²", x));
            binding.textViewPitch.setText(
                    String.format(Locale.US, "Pitch (Y): %.2f м/с²", y));
            binding.textViewRoll.setText(
                    String.format(Locale.US, "Roll (Z): %.2f м/с²", z));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    Log.d(TAG, "Акселерометр: ненадёжные данные");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    Log.d(TAG, "Акселерометр: высокая точность");
                    break;
                default:
                    Log.d(TAG, "Акселерометр: точность = " + accuracy);
            }
        }
    }
}