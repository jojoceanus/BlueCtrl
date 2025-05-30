package com.ocean.bluectrl;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MaunalControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MaunalControlFragment extends Fragment {
    private FanControlViewModel viewModel;
    // 定义数据传输接口
    public interface OnProgressSendListener {
        void onProgressSent(int progress);
    }

    private OnProgressSendListener sendListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            sendListener = (OnProgressSendListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "必须实现OnProgressSendListener");
        }
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MaunalControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MaunalControlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MaunalControlFragment newInstance(String param1, String param2) {
        MaunalControlFragment fragment = new MaunalControlFragment();
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
        return inflater.inflate(R.layout.fragment_maunal_control, container, false);*/
        View view = inflater.inflate(R.layout.fragment_maunal_control, container, false);
        SeekBar seekBar = view.findViewById(R.id.speedcontrol_seekbar);
        viewModel = new ViewModelProvider(requireActivity()).get(FanControlViewModel.class);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 处理SeekBar进度变化
                if (sendListener != null) {
                    sendListener.onProgressSent(progress); // 触发回调
                    viewModel.setText("");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 处理SeekBar开始滑动
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 处理SeekBar停止滑动
            }
        });
        return view;
    }
}