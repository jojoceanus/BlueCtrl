package com.ocean.bluectrl.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ocean.bluectrl.R;


public class GlobalBackground extends ConstraintLayout {

    private final ImageView backgroudImage;
    private final View backgroundView;

    public GlobalBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.background_global, this);
        backgroudImage = findViewById(R.id.backgroundImageView);
        backgroundView = findViewById(R.id.background_view);
    }

    // 提供获取 ImageView 的方法
    public ImageView getImageView() {
        return backgroudImage;
    }

    public View getBackgroundView() {
        return backgroundView;
    }

}
