package com.xyx.reversepolishnotation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.xyx.reversepolishnotation.fragment.MainFragment;
import com.xyx.reversepolishnotation.fragment.SuccessFragment;

public class MainActivity extends AppCompatActivity implements MainFragment.MainFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new MainFragment())
                .commit();
    }

    @Override
    public void onAnswerCorrect() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SuccessFragment())
                .commit();
    }
}
