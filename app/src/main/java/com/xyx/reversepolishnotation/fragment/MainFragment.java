package com.xyx.reversepolishnotation.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xyx.reversepolishnotation.R;
import com.xyx.reversepolishnotation.net.Server;
import com.xyx.reversepolishnotation.net.TaskBean;

import java.util.EmptyStackException;
import java.util.Stack;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainFragment extends Fragment {

    private MainFragmentListener mListener;

    private RadioGroup radioGroup;
    private TextView question;
    private ProgressDialog progressDialog;

    private CompositeDisposable compositeDisposable;

    private TaskBean currentTask;
    private double answer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainFragmentListener) {
            mListener = (MainFragmentListener) context;
        } else {
            throw new RuntimeException(context + " must implement MainFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (currentTask.answers.get(checkedId) == answer) {
                    mListener.onAnswerCorrect();
                } else {
                    progressDialog.setMessage(getString(R.string.answer_wrong));
                    fetchNewTask();
                }
            }
        });
        question = view.findViewById(R.id.question);
        progressDialog = new ProgressDialog(getActivity());

        fetchNewTask();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void fetchNewTask() {
        compositeDisposable.add(
                Server.getInstance()
                        .getTask()
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) {
                                progressDialog.show();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Consumer<TaskBean>() {
                                    @Override
                                    public void accept(TaskBean taskBean) {
                                        question.setText(taskBean.notation);
                                        radioGroup.removeAllViews();
                                        for (int i = 0; i < taskBean.answers.size(); i++) {
                                            double candidate = taskBean.answers.get(i);
                                            RadioButton radioButton = new RadioButton(radioGroup.getContext());
                                            radioButton.setId(i);
                                            radioButton.setText(String.valueOf(candidate));
                                            radioGroup.addView(radioButton);
                                        }
                                        currentTask = taskBean;
                                        answer = calculateRPN(taskBean.notation);

                                        progressDialog.dismiss();
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        )
        );
    }

    private double calculateRPN(String notation) throws EmptyStackException, NumberFormatException, ArithmeticException {
        Stack<Double> stack = new Stack<>();
        String[] expressions = notation.split(" ");
        for (String e : expressions) {
            switch (e) {
                case "+": {
                    double v1 = stack.pop();
                    double v0 = stack.pop();
                    stack.push(v0 + v1);
                }
                break;
                case "-": {
                    double v1 = stack.pop();
                    double v0 = stack.pop();
                    stack.push(v0 - v1);
                }
                break;
                case "*": {
                    double v1 = stack.pop();
                    double v0 = stack.pop();
                    stack.push(v0 * v1);
                }
                break;
                case "/": {
                    double v1 = stack.pop();
                    if (v1 == 0) {
                        throw new ArithmeticException();
                    }
                    double v0 = stack.pop();
                    stack.push(v0 / v1); // could
                }
                break;
                default: // should be double. Other operators are not supported yet
                    double d = Double.parseDouble(e);
                    stack.push(d);
                    break;
            }
        }
        return stack.pop();
    }

    public interface MainFragmentListener {
        void onAnswerCorrect();
    }
}
