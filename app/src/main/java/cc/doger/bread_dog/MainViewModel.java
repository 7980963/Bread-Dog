package cc.doger.bread_dog;

import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {
    private static final String SELECTED_PACK = "selectedPack";
    private static final String PENDING_EXPORT = "pendingExport";
    private final SavedStateHandle savedState;
    private final EmoticonRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThread = new Handler(Looper.getMainLooper());
    private final MutableLiveData<LoadState> loadState = new MutableLiveData<>();
    private final MutableLiveData<Integer> saveMessage = new MutableLiveData<>();
    private volatile boolean cleared;

    public MainViewModel(@NonNull Application application, SavedStateHandle savedState) {
        super(application);
        this.savedState = savedState;
        repository = new EmoticonRepository(application);
    }

    LiveData<LoadState> getLoadState() {
        return loadState;
    }

    LiveData<Integer> getSaveMessage() {
        return saveMessage;
    }

    void consumeSaveMessage() {
        saveMessage.setValue(null);
    }

    String getSelectedPack() {
        return savedState.get(SELECTED_PACK);
    }

    void loadPack(String pack) {
        LoadState previous = loadState.getValue();
        if (previous != null && previous.loading) {
            return;
        }
        List<File> previousFiles = previous == null ? Collections.emptyList() : previous.files;
        String previousPack = previous == null ? null : previous.selectedPack;
        loadState.setValue(new LoadState(previousFiles, previousPack, pack, true, false));
        executor.execute(() -> {
            try {
                List<File> files = Collections.unmodifiableList(repository.load(pack));
                mainThread.post(() -> {
                    if (!cleared) {
                        savedState.set(SELECTED_PACK, pack);
                        loadState.setValue(new LoadState(files, pack, pack, false, false));
                    }
                });
            } catch (IOException | RuntimeException e) {
                Log.e("BreadDog", "Cannot load pack: " + pack, e);
                mainThread.post(() -> {
                    if (!cleared) {
                        loadState.setValue(new LoadState(previousFiles, previousPack, pack, false, true));
                    }
                });
            }
        });
    }

    void beginExport(File source) {
        // Save a stable path, not an adapter position. Pack switching never deletes this file.
        savedState.set(PENDING_EXPORT, source.getAbsolutePath());
    }

    void finishExport(Uri destination) {
        String path = savedState.get(PENDING_EXPORT);
        savedState.remove(PENDING_EXPORT);
        if (destination == null) {
            return;
        }
        if (path == null) {
            saveMessage.setValue(R.string.save_failed);
            return;
        }
        executor.execute(() -> {
            int message;
            try {
                Utils.saveToUri(getApplication(), new File(path), destination);
                message = R.string.save_success;
            } catch (IOException | RuntimeException e) {
                Log.e("BreadDog", "Cannot export image", e);
                message = R.string.save_failed;
            }
            if (!cleared) {
                saveMessage.postValue(message);
            }
        });
    }

    @Override
    protected void onCleared() {
        cleared = true;
        executor.shutdownNow();
        mainThread.removeCallbacksAndMessages(null);
        super.onCleared();
    }

    static final class LoadState {
        final List<File> files;
        final String selectedPack;
        final String requestedPack;
        final boolean loading;
        final boolean failed;

        LoadState(List<File> files, String selectedPack, String requestedPack, boolean loading, boolean failed) {
            this.files = files;
            this.selectedPack = selectedPack;
            this.requestedPack = requestedPack;
            this.loading = loading;
            this.failed = failed;
        }
    }
}
