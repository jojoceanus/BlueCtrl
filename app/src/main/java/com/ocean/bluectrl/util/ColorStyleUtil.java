package com.ocean.bluectrl.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.List;

public class ColorStyleUtil {

    private static final String PREFS_NAME = "color_prefs";
    public static final int VIBRANT_COLOR = 0;
    public static final int VIBRANT_DARK_COLOR = 1;
    public static final int VIBRANT_LIGHT_COLOR = 2;
    public static final int MUTED_COLOR = 3;
    public static final int MUTED_DARK_COLOR = 4;
    public static final int MUTED_LIGHT_COLOR = 5;
    private static final String[] paletteColorNames = new String[] {
            "vibrant_color",
            "vibrant_dark_color",
            "vibrant_light_color",
            "muted_color",
            "muted_dark_color",
            "muted_light_color"
    };

    // 从 ImageView 中获取 Bitmap
    public static Bitmap getBitmapFromImageView(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    // 计算并保存颜色
    public static void calculateAndSaveColors(Context context, ImageView imageView, final OnColorCalculatedListener listener) {
        Log.d("oceanus", "calculateAndSaveColors: used");
        Bitmap bitmap = getBitmapFromImageView(imageView);
        if (bitmap != null) {
            Palette.from(bitmap).generate(palette -> {
                List<Integer> paletteColors = new ArrayList<Integer>();
                int vibrant = palette.getVibrantColor(0xFF808080);
                int vibrantDark = palette.getDarkVibrantColor(0xFF808080);
                int vibrantLight = palette.getLightVibrantColor(0xFF808080);
                int muted = palette.getMutedColor(0xFF808080);
                int mutedDark = palette.getDarkMutedColor(0xFF808080);
                int mutedLight = palette.getLightMutedColor(0xFF808080);
                paletteColors.add(vibrant);
                paletteColors.add(vibrantDark);
                paletteColors.add(vibrantLight);
                paletteColors.add(muted);
                paletteColors.add(mutedDark);
                paletteColors.add(mutedLight);
                if (listener != null)  {
                    for (int i = 0; i < paletteColors.size(); i++) {
                        saveColorToPreferences(context, paletteColorNames[i], paletteColors.get(i));
                    }
                    listener.onColorsCalculated();
                }
            });
        }
    }

    // 定义一个接口来处理颜色计算完成后的回调
    public interface OnColorCalculatedListener {
        void onColorsCalculated();
    }

    // 保存颜色到 SharedPreferences
    private static void saveColorToPreferences(Context context, String colorName, int paletteColor) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(colorName, paletteColor);
        editor.apply();
    }

    // 从文件中读取颜色
    public static int[] readColorsFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int[] colors = new int[paletteColorNames.length];
        for (int i = 0; i < paletteColorNames.length; i++) {
            colors[i] = sharedPreferences.getInt(paletteColorNames[i], 0);
        }
        return colors;
    }

    public static int combineColor(int foregroundColor, int backgroundColor, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(foregroundColor) * ratio) + (Color.red(backgroundColor) * inverseRation);
        float g = (Color.green(foregroundColor) * ratio) + (Color.green(backgroundColor) * inverseRation);
        float b = (Color.blue(foregroundColor) * ratio) + (Color.blue(backgroundColor) * inverseRation);
        //float a = (Color.alpha(primaryColor) * ratio) + (Color.alpha(backgroundColor) * inverseRation);
        float a = Color.alpha(backgroundColor);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    // 获取颜色
    public static int getColor(Activity act, int colorType) {
        int[] colors = readColorsFromPreferences(act);
        return colors[colorType];
    }

    //以下部分可用于设置视图样式

    public static void setStatusBarColor(Activity act, ImageView imageView, int position) {
        // 读取保存的颜色
        int[] colors = readColorsFromPreferences(act);
        int statusBarColor = colors[position];

        // 应用颜色
        if (statusBarColor != 0) {
            Window window = act.getWindow();
            window.setStatusBarColor(statusBarColor);
        } else {
            // 如果没有保存的颜色，重新计算并保存
            calculateAndSaveColors(act, imageView, new OnColorCalculatedListener() {
                @Override
                public void onColorsCalculated() {
                    // 再次读取以确保颜色已保存
                    int[] colors = readColorsFromPreferences(act);
                    Window window = act.getWindow();
                    window.setStatusBarColor(colors[0]);
                }
            });

        }
    }

    public static void setViewColor(Activity act, ImageView imageView, View view, int position, float ratio) {
        ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
        int[] foregroundColors = readColorsFromPreferences(act);
        int selectedColor = foregroundColors[position];
        if (selectedColor !=0) {
            int backgroundColor = colorDrawable.getColor();
            view.setBackgroundColor(combineColor(selectedColor, backgroundColor, 1.0f));
        } else {
            calculateAndSaveColors(act, imageView, new OnColorCalculatedListener() {
                @Override
                public void onColorsCalculated() {
                    int[] foregroundColors = readColorsFromPreferences(act);
                    int selectedColor = foregroundColors[position];
                    int backgroundColor = colorDrawable.getColor();
                    view.setBackgroundColor(combineColor(selectedColor, backgroundColor, 1.0f));
                }
            });
        }
    }

    public static void setViewBackgroundColor(Activity act, ImageView imageView, View view, int position, float ratio) {
        GradientDrawable mGradientDrawable = (GradientDrawable) view.getBackground();
        int[] foregroundColors = readColorsFromPreferences(act);
        int selectedColor = foregroundColors[position];
        if (selectedColor !=0) {
            int backgroundColor = mGradientDrawable.getColor().getDefaultColor();
            mGradientDrawable.setColor(combineColor(selectedColor, backgroundColor, 1.0f));
        } else {
            calculateAndSaveColors(act, imageView, new OnColorCalculatedListener() {
                @Override
                public void onColorsCalculated() {
                    int[] foregroundColors = readColorsFromPreferences(act);
                    int selectedColor = foregroundColors[position];
                    int backgroundColor = mGradientDrawable.getColor().getDefaultColor();
                    mGradientDrawable.setColor(combineColor(selectedColor, backgroundColor, 1.0f));
                }
            });
        }
    }

    public static void setAllViewBackgroundColors(Activity act, ImageView imageView, int position, float ratio) {
        View rootView = act.findViewById(android.R.id.content);
        traverseAndSetBackground(rootView, act, imageView, position, ratio);
    }

    private static void traverseAndSetBackground(View view, Activity act, ImageView imageView, int position, float ratio) {
        if (view.getBackground() instanceof GradientDrawable) {
            setViewBackgroundColor(act, imageView, view, position, ratio);
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                traverseAndSetBackground(viewGroup.getChildAt(i), act, imageView, position, ratio);
            }
        }
    }

}
