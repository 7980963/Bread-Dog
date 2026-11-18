package cc.doger.bread_dog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ButtonGenerator {
    private final Context context;
    private final LinearLayout container;
    private final ImageClickListener listener;
    private final Map<String, Button> buttons = new LinkedHashMap<>();

    public ButtonGenerator(Context context, LinearLayout container, ImageClickListener listener) {
        this.context = context;
        this.container = container;
        this.listener = listener;
    }

    public List<String> generateButtons() throws IOException {
        List<String> names = PackFiles.packs(context.getAssets().list(""));
        container.removeAllViews();
        buttons.clear();
        for (String name : names) {
            Button button = new Button(context);
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            button.setAllCaps(false);
            button.setText(PackFiles.packLabel(name));
            button.setPaddingRelative(dp(16), dp(4), dp(12), dp(4));
            button.setCompoundDrawablePadding(dp(16));
            Drawable icon = getIcon(name);
            if (icon != null) {
                icon.setBounds(0, 0, dp(32), dp(32));
                button.setCompoundDrawablesRelative(icon, null, null, null);
            }
            button.setOnClickListener(view -> listener.onImageClick(name));
            buttons.put(name, button);
            container.addView(button);
        }
        select(null);
        return names;
    }

    public void select(String selectedName) {
        TypedValue textColor = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, textColor, true);
        int normalColor = textColor.resourceId != 0
                ? ContextCompat.getColor(context, textColor.resourceId) : textColor.data;
        for (Map.Entry<String, Button> entry : buttons.entrySet()) {
            Button button = entry.getValue();
            boolean selected = entry.getKey().equals(selectedName);
            button.setSelected(selected);
            button.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            button.setTextColor(selected
                    ? ContextCompat.getColor(context, R.color.button_selected_text_color) : normalColor);
            if (selected) {
                button.setBackground(AppCompatResources.getDrawable(context, R.drawable.ripple_button));
            } else {
                button.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        for (Button button : buttons.values()) {
            button.setEnabled(enabled);
        }
    }

    @SuppressLint("DiscouragedApi")
    private Drawable getIcon(String name) {
        int id = context.getResources().getIdentifier("image" + name.substring(0, 2),
                "mipmap", context.getPackageName());
        return AppCompatResources.getDrawable(context, id == 0 ? R.drawable.ic_launcher : id);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public interface ImageClickListener {
        void onImageClick(String fileName);
    }
}
