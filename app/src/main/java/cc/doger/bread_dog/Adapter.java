package cc.doger.bread_dog;

import static cc.doger.bread_dog.Utils.save;
import static cc.doger.bread_dog.Utils.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;


public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private Context context;
    private final List<File> list;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout linearLayout;
        final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            linearLayout = (LinearLayout) view;
            imageView = view.findViewById(R.id.item_img);

            // 设置图片固定大小为240x240像素
            int imageSize = 240; // 设置图片大小（宽度和高度）
            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageView.setLayoutParams(params);
        }
    }

    public Adapter(List<File> list) {
        this.list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.navigation_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.linearLayout.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            File file = list.get(position);
            View root = LayoutInflater.from(context).inflate(R.layout.navigation_dialog, null);
            TextView textView = root.findViewById(R.id.dialog_text);
            ImageView img = root.findViewById(R.id.dialog_img);
            LinearLayout btnQQ = root.findViewById(R.id.dialog_qq);
            LinearLayout btnWechat = root.findViewById(R.id.dialog_wechat);
            LinearLayout btnSave = root.findViewById(R.id.dialog_save);
            LinearLayout btnShare = root.findViewById(R.id.dialog_share);
            LinearLayout btnTim = root.findViewById(R.id.dialog_tim);
            AlertDialog dialog = new AlertDialog.Builder(context).setView(root).create();
            btnShare.setOnClickListener(view1 -> {
                dialog.dismiss();
                share(context, file, 0);
            });
            btnQQ.setOnClickListener(view12 -> {
                dialog.dismiss();
                share(context, file, 1);
            });
            btnWechat.setOnClickListener(view12 -> {
                dialog.dismiss();
                share(context, file, 2);
            });
            btnTim.setOnClickListener(view12 -> {
                dialog.dismiss();
                share(context, file, 3);
            });
            btnSave.setOnClickListener(view12 -> {
                dialog.dismiss();
                save((Activity) context, file, position);
            });
            textView.setText(file.getName());
            Glide.with(context).load(file).into(img);
            dialog.show();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = list.get(position);
        holder.imageView.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        String fileName = file.getName().substring(0, file.getName().lastIndexOf(".")); // 获取不带扩展名的文件名
        TextView textView = holder.linearLayout.findViewById(R.id.item_text); // 获取TextView
        textView.setText(fileName); // 设置TextView的文本
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public void updateList(List<File> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }
}