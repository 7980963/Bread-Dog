package com.emoji.bread_dog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ButtonGenerator {
    private final Context context;
    private final LinearLayout buttonContainer;
    private final List<Button> buttons;
    private Button selectedButton;
    private final Toolbar toolbar;
    private ImageClickListener imageClickListener;

    public ButtonGenerator(Context context, LinearLayout buttonContainer, Toolbar toolbar, ImageClickListener imageClickListener) {
        this.context = context;
        this.buttonContainer = buttonContainer;
        this.toolbar = toolbar;
        this.imageClickListener = imageClickListener;
        this.buttons = new ArrayList<>();
        this.selectedButton = null;
    }

    public void generateButtons() {
        String[] fileNames = getFileNamesFromAssets();
        Arrays.sort(fileNames, new FileNameComparator());

        for (String fileName : fileNames) {
            String buttonLabel = getButtonLabel(fileName);
            Drawable buttonIcon = getButtonIcon(fileName);

            if (buttonLabel != null && buttonIcon != null) {
                Button button = createButton(buttonLabel, buttonIcon);
                addButtonClickListener(button, fileName);

                buttons.add(button);
                buttonContainer.addView(button);
            }
        }
    }

    private String[] getFileNamesFromAssets() {
        try {
            return context.getAssets().list("");
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    private String getButtonLabel(String fileName) {
        String[] parts = fileName.split("_");
        if (parts.length >= 2) {
            String labelWithExtension = parts[1];
            // 返回原始的文件名部分，不进行大写转换
            return labelWithExtension.substring(0, labelWithExtension.lastIndexOf("."));
        }
        return null;
    }

    private Drawable getButtonIcon(String fileName) {
        String[] parts = fileName.split("_");
        if (parts.length >= 1) {
            String sorting = parts[0];
            try {
                @SuppressLint("DiscouragedApi") int resourceId = context.getResources().getIdentifier("image" + sorting, "mipmap", context.getPackageName());
                if (resourceId != 0) {
                    return ResourcesCompat.getDrawable(context.getResources(), resourceId, null);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Button createButton(String label, Drawable icon) {
        Button button = new Button(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 10);
        button.setLayoutParams(layoutParams);
        button.setPadding(5, 0, 0, 0);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        button.setTextColor(context.getColor(android.R.color.black));
        button.setAllCaps(false);

        // 设置图标大小为 60x60，并居中显示
        icon.setBounds(0, 0, 60, 60);
        button.setCompoundDrawablesRelative(icon, null, null, null);
        // 设置图标的右边距为 50
        button.setCompoundDrawablePadding(50);

        // 设置图标的左边距为 50
        button.setPaddingRelative(50, 0, 0, 0);

        button.setText(label);
        button.setId(View.generateViewId());
        button.setOnClickListener(v -> {
            String fileName = getFileNameFromButton(button);
            if (fileName != null) {
                imageClickListener.onImageClick(fileName);
            }
        });
        //设置按钮的背景
        button.setBackgroundColor(Color.TRANSPARENT);

        return button;
    }

    private void addButtonClickListener(Button button, final String fileName) {
        button.setOnClickListener(v -> {
            String label = getButtonLabel(fileName);
            toolbar.setTitle(label);
            onButtonClick(button);
            if (imageClickListener != null) {
                imageClickListener.onImageClick(fileName);
            }
        });
    }


    private void onButtonClick(Button button) {
        if (selectedButton != null) {
            //设置未选中状态背景
            selectedButton.setBackgroundColor(Color.TRANSPARENT);
            //设置未选中状态文字颜色
            selectedButton.setTextColor(context.getColor(R.color.black));
            //设置未选中状态文字普通粗细
            Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            selectedButton.setTypeface(typeface);
        }
        selectedButton = button;
        //设置选中状态背景
        //selectedButton.setBackgroundColor(context.getColor(R.color.button_selected_background_color));
        // 创建水波纹背景，并设置为按钮的背景
        selectedButton.setBackground(AppCompatResources.getDrawable(context, R.drawable.ripple_button));

        //设置选中状态文字颜色
        selectedButton.setTextColor(context.getColor(R.color.button_selected_text_color));
        //设置选中状态文字加粗
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        selectedButton.setTypeface(typeface);
    }

    private String getFileNameFromButton(Button button) {
        for (Button btn : buttons) {
            if (btn == button) {
                String label = btn.getText().toString();
                String fileName = getFileNameForLabel(label);
                if (fileName != null) {
                    return fileName;
                }
            }
        }
        return null;
    }

    private String getFileNameForLabel(String label) {
        String[] fileNames = getFileNamesFromAssets();
        for (String fileName : fileNames) {
            String buttonLabel = getButtonLabel(fileName);
            if (buttonLabel != null && buttonLabel.equals(label)) {
                return fileName;
            }
        }
        return null;
    }

    public interface ImageClickListener {
        void onImageClick(String fileName);
    }

    private static class FileNameComparator implements Comparator<String> {
        @Override
        public int compare(String fileName1, String fileName2) {
            String[] parts1 = fileName1.split("_");
            String[] parts2 = fileName2.split("_");
            if (parts1.length >= 1 && parts2.length >= 1) {
                try {
                    int sorting1 = Integer.parseInt(parts1[0]);
                    int sorting2 = Integer.parseInt(parts2[0]);
                    return Integer.compare(sorting1, sorting2);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return fileName1.compareTo(fileName2);
        }
    }

    public void setImageClickListener(ImageClickListener imageClickListener) {
        this.imageClickListener = imageClickListener;
    }
}
