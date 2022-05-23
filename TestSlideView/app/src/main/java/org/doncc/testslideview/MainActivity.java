package org.doncc.testslideview;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.doncc.testslideview.databinding.ActivityMainBinding;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final DecimalFormat decimalFormatShowText = new DecimalFormat("0.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SlideView slideView = binding.slide;

        Button button = binding.random;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float value = Float.parseFloat(decimalFormatShowText.format(Math.random() * 7.0f + 1.0f));
                System.out.println("随机数：" + value);
                slideView.setValue(value);
            }
        });

    }


}