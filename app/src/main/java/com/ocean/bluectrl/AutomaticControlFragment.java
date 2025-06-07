package com.ocean.bluectrl;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AutomaticControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AutomaticControlFragment extends Fragment {
    private FanControlViewModel viewModel;
    public interface OnBTClickListener {
        void clickBTTemperature(int temperature);
        void clickBTHumidity(int humidity);
        void clickBTSimulate();
    }

    private AutomaticControlFragment.OnBTClickListener btClickListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            btClickListener = (AutomaticControlFragment.OnBTClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "必须实现OnBTClickListener");
        }
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AutomaticControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AutomaticControlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AutomaticControlFragment newInstance(String param1, String param2) {
        AutomaticControlFragment fragment = new AutomaticControlFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*// Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_automatic_control, container, false);*/
        View view = inflater.inflate(R.layout.fragment_automatic_control, container, false);
        Button btTemperature = view.findViewById(R.id.temperature_bt);
        EditText etTemperature = view.findViewById(R.id.temperature_et);
        Button btHumidity = view.findViewById(R.id.humidity_bt);
        EditText etHumidity = view.findViewById(R.id.humidity_et);
        Button btSimulate = view.findViewById(R.id.simulate_bt);
        TextView textMode = view.findViewById(R.id.mode_text);
        viewModel = new ViewModelProvider(requireActivity()).get(FanControlViewModel.class);

        // 监听LiveData变化
        viewModel.getText().observe(getViewLifecycleOwner(), newText -> {
            textMode.setText("");
        });

        btTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String setTemperature = etTemperature.getText().toString();
                if (setTemperature.isEmpty()) {
                    Toast.makeText(getActivity(), "设定温度异常", Toast.LENGTH_SHORT).show();
                } else {
                    int setT = Integer.parseInt(setTemperature);
                    if (0 < setT && setT < 50) {
                        textMode.setText("恒定温度" + setTemperature + "℃");
                        btClickListener.clickBTTemperature(setT);
                        etTemperature.setText("");
                    } else {
                        Toast.makeText(getActivity(), "设定温度异常", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btHumidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String setHumidity = etHumidity.getText().toString();
                if (setHumidity.isEmpty()) {
                    Toast.makeText(getActivity(), "设定湿度异常", Toast.LENGTH_SHORT).show();
                } else {
                    int setH = Integer.parseInt(setHumidity);
                    if (0 <= setH && setH <= 100) {
                        textMode.setText("恒定湿度" + setHumidity + "%");
                        btClickListener.clickBTHumidity(setH);
                        etHumidity.setText("");
                    } else {
                        Toast.makeText(getActivity(), "设定湿度异常", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btSimulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textMode.setText("模拟自然风");
                btClickListener.clickBTSimulate();
            }
        });

        return view;
    }
}