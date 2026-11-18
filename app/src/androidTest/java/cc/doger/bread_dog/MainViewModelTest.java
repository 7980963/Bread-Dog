package cc.doger.bread_dog;

import android.app.Application;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.lifecycle.SavedStateHandle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class MainViewModelTest {
    @Test public void restoredPendingExportDoesNotNeedAnImageList() throws Exception {
        Application app = ApplicationProvider.getApplicationContext();
        File directory = new File(app.getCacheDir(), "img/export-test");
        PackFiles.ensureDirectory(directory);
        File source = new File(directory, "original.png");
        File destination = new File(directory, "saved.png");
        byte[] data = {1, 2, 3, 4};
        Files.write(source.toPath(), data);
        Uri uri = FileProvider.getUriForFile(app, app.getPackageName() + ".FileProvider", destination);
        Map<String, Object> restored = new HashMap<>();
        restored.put("pendingExport", source.getAbsolutePath());
        restored.put("selectedPack", "02_another.zip");
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Integer> message = new AtomicReference<>();
        AtomicReference<MainViewModel> model = new AtomicReference<>();
        try {
            onMain(() -> {
                model.set(new MainViewModel(app, new SavedStateHandle(restored)));
                assertEquals("02_another.zip", model.get().getSelectedPack());
                assertNull(model.get().getLoadState().getValue());
                model.get().getSaveMessage().observeForever(value -> {
                    message.set(value);
                    finished.countDown();
                });
                model.get().finishExport(uri);
            });
            assertTrue(finished.await(10, TimeUnit.SECONDS));
            assertEquals(Integer.valueOf(R.string.save_success), message.get());
            assertArrayEquals(data, Files.readAllBytes(destination.toPath()));
        } finally {
            onMain(() -> model.get().onCleared());
            PackFiles.deleteTree(directory);
        }
    }

    @Test public void failedPackKeepsThePreviousImages() throws Exception {
        Application app = ApplicationProvider.getApplicationContext();
        AtomicReference<MainViewModel> model = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        CountDownLatch failed = new CountDownLatch(1);
        AtomicReference<MainViewModel.LoadState> success = new AtomicReference<>();
        AtomicReference<MainViewModel.LoadState> failure = new AtomicReference<>();
        try {
            onMain(() -> {
                model.set(new MainViewModel(app, new SavedStateHandle()));
                model.get().getLoadState().observeForever(state -> {
                    if (!state.loading && !state.failed) {
                        success.set(state);
                        loaded.countDown();
                    } else if (state.failed) {
                        failure.set(state);
                        failed.countDown();
                    }
                });
                model.get().loadPack("00_Sample.zip");
            });
            assertTrue(loaded.await(10, TimeUnit.SECONDS));
            onMain(() -> model.get().loadPack("99_missing.zip"));
            assertTrue(failed.await(10, TimeUnit.SECONDS));
            assertFalse(failure.get().loading);
            assertEquals(success.get().files, failure.get().files);
            assertEquals("00_Sample.zip", failure.get().selectedPack);
            assertTrue(failure.get().files.get(0).isFile());
        } finally {
            onMain(() -> model.get().onCleared());
        }
    }

    @Test public void activityRecreationRetainsLoadAndPendingExport() throws Exception {
        AtomicReference<MainViewModel> retained = new AtomicReference<>();
        CountDownLatch loaded = new CountDownLatch(1);
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                MainViewModel model = new androidx.lifecycle.ViewModelProvider(activity).get(MainViewModel.class);
                retained.set(model);
                model.getLoadState().observe(activity, state -> {
                    if (!state.loading && !state.files.isEmpty()) {
                        loaded.countDown();
                    }
                });
            });
            assertTrue(loaded.await(10, TimeUnit.SECONDS));
            scenario.onActivity(activity -> retained.get().beginExport(
                    retained.get().getLoadState().getValue().files.get(0)));
            scenario.recreate();
            scenario.onActivity(activity -> {
                MainViewModel model = new androidx.lifecycle.ViewModelProvider(activity).get(MainViewModel.class);
                assertSame(retained.get(), model);
                assertFalse(model.getLoadState().getValue().loading);
                model.finishExport(null);
            });
        }
    }

    private static void onMain(Runnable action) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action);
    }
}
