package cc.doger.bread_dog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private List<File> files = Collections.emptyList();
    private final SaveListener saveListener;

    public Adapter(SaveListener saveListener) {
        this.saveListener = saveListener;
        setStateRestorationPolicy(StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView label;

        ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.item_img);
            label = view.findViewById(R.id.item_text);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(clicked -> {
            int position = holder.getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                showImage(clicked.getContext(), files.get(position));
            }
        });
        return holder;
    }

    private void showImage(Context context, File file) {
        View root = LayoutInflater.from(context).inflate(R.layout.navigation_dialog, null);
        TextView title = root.findViewById(R.id.dialog_text);
        ImageView image = root.findViewById(R.id.dialog_img);
        AlertDialog dialog = new AlertDialog.Builder(context).setView(root).create();
        int[] ids = {R.id.dialog_share, R.id.dialog_qq, R.id.dialog_wechat, R.id.dialog_tim};
        for (int type = 0; type < ids.length; type++) {
            final int shareType = type;
            root.findViewById(ids[type]).setOnClickListener(view -> {
                dialog.dismiss();
                Utils.share(context, file, shareType);
            });
        }
        root.findViewById(R.id.dialog_save).setOnClickListener(view -> {
            dialog.dismiss();
            saveListener.onSave(file);
        });
        title.setText(PackFiles.imageLabel(file));
        Glide.with(image).load(file).error(R.drawable.ic_launcher).into(image);
        dialog.setOnDismissListener(ignored -> Glide.with(context.getApplicationContext()).clear(image));
        dialog.show();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = files.get(position);
        String label = PackFiles.imageLabel(file);
        holder.label.setText(label);
        holder.image.setContentDescription(label);
        Glide.with(holder.image).load(file).fitCenter()
                .placeholder(R.drawable.ic_launcher).error(R.drawable.ic_launcher).into(holder.image);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Glide.with(holder.image.getContext().getApplicationContext()).clear(holder.image);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void updateList(List<File> newFiles) {
        if (!files.equals(newFiles)) {
            files = new ArrayList<>(newFiles);
            notifyDataSetChanged();
        }
    }

    public interface SaveListener {
        void onSave(File file);
    }
}
