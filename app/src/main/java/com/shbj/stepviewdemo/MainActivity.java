package com.shbj.stepviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckBox cbTouch = findViewById(R.id.cb_touch);
        CheckBox cbIsDown = findViewById(R.id.cb_is_down);
        final StepView stepView = findViewById(R.id.step_view);
        String[] stepTexts = new String[]{"订单已提交", "商家已接单", "配送中", "已送达"};
        stepView.setStepTexts(stepTexts);
        cbTouch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stepView.setStepIsTouch(isChecked);
            }
        });
        cbIsDown.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stepView.setTextUpLine(!isChecked);
            }
        });
    }
}
