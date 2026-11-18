package cc.doger.bread_dog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.gyf.immersionbar.ImmersionBar;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ButtonGenerator.ImageClickListener {
    private MainViewModel model;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private View statusView;
    private View retryButton;
    private TextView statusText;
    private ButtonGenerator buttons;
    private Adapter adapter;
    private Snackbar errorSnackbar;

    private final ActivityResultLauncher<Intent> saveDocument = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                Uri destination = result.getResultCode() == RESULT_OK && data != null ? data.getData() : null;
                model.finishExport(destination);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainViewModel.class);
        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.header_toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        statusView = findViewById(R.id.load_view);
        statusText = findViewById(R.id.status_text);
        retryButton = findViewById(R.id.retry_button);

        ImmersionBar.with(this).transparentStatusBar().statusBarColor(R.color.colorPrimary)
                .navigationBarColor(R.color.white).init();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        findViewById(R.id.footer_item_github).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/7980963/Bread-Dog")));
            } catch (RuntimeException e) {
                Toast.makeText(this, R.string.browser_unavailable, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.footer_item_setting).setOnClickListener(view ->
                Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show());
        TextView version = findViewById(R.id.iv_version);
        version.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME,
                getString(BuildConfig.DEBUG ? R.string.app_version_debug : R.string.app_version_release)));

        adapter = new Adapter(this::saveImage);
        GridLayoutManager layout = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layout);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnLayoutChangeListener((view, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> {
            int targetWidth = Math.round(88 * getResources().getDisplayMetrics().density);
            int columns = Math.max(1, (right - left - view.getPaddingLeft() - view.getPaddingRight()) / targetWidth);
            if (layout.getSpanCount() != columns) {
                layout.setSpanCount(columns);
            }
        });
        buttons = new ButtonGenerator(this, findViewById(R.id.buttonContainer), this);
        model.getLoadState().observe(this, this::render);
        model.getSaveMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                model.consumeSaveMessage();
            }
        });
        loadCatalog();
    }

    private void loadCatalog() {
        try {
            List<String> packs = buttons.generateButtons();
            MainViewModel.LoadState current = model.getLoadState().getValue();
            if (current != null) {
                render(current);
            } else if (packs.isEmpty()) {
                showStatus(R.string.no_packs, false);
            } else {
                String selected = model.getSelectedPack();
                model.loadPack(packs.contains(selected) ? selected : packs.get(0));
            }
        } catch (IOException e) {
            showStatus(R.string.load_failed, true);
            retryButton.setOnClickListener(view -> loadCatalog());
        }
    }

    @Override
    public void onImageClick(String fileName) {
        drawer.closeDrawers();
        model.loadPack(fileName);
    }

    private void render(MainViewModel.LoadState state) {
        adapter.updateList(state.files);
        buttons.select(state.selectedPack);
        buttons.setEnabled(!state.loading);
        toolbar.setTitle(state.selectedPack == null ? getString(R.string.app_name)
                : PackFiles.packLabel(state.selectedPack));
        findViewById(R.id.loading_indicator).setVisibility(state.loading ? View.VISIBLE : View.INVISIBLE);
        recyclerView.setVisibility(state.files.isEmpty() ? View.GONE : View.VISIBLE);
        statusView.setVisibility(state.files.isEmpty() ? View.VISIBLE : View.GONE);
        statusText.setText(state.loading ? R.string.loading_text : R.string.load_failed);
        retryButton.setVisibility(state.failed ? View.VISIBLE : View.GONE);
        retryButton.setOnClickListener(view -> model.loadPack(state.requestedPack));
        if (errorSnackbar != null) {
            errorSnackbar.dismiss();
            errorSnackbar = null;
        }
        if (state.failed && !state.files.isEmpty()) {
            errorSnackbar = Snackbar.make(recyclerView, R.string.load_failed, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, view -> model.loadPack(state.requestedPack));
            errorSnackbar.show();
        }
    }

    private void showStatus(int message, boolean retry) {
        statusView.setVisibility(View.VISIBLE);
        statusText.setText(message);
        retryButton.setVisibility(retry ? View.VISIBLE : View.GONE);
        findViewById(R.id.loading_indicator).setVisibility(View.INVISIBLE);
    }

    private void saveImage(File file) {
        try {
            Intent intent = Utils.createDocumentIntent(this, file);
            model.beginExport(file);
            saveDocument.launch(intent);
        } catch (RuntimeException e) {
            model.finishExport(null);
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
        }
    }
}
