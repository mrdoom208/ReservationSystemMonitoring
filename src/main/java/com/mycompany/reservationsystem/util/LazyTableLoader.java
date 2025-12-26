package com.mycompany.reservationsystem.util;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.function.Consumer;
import java.util.function.Function;

public class LazyTableLoader<T> {

    private final TableView<T> tableView;
    private final ObservableList<T> backingList;
    private Function<PageRequest, Page<T>> pageFetcher;
    private Consumer<Void> onPageLoaded;

    private int page = 0;
    private boolean loading = false;
    private boolean hasMore = true;
    private final int pageSize;

    public LazyTableLoader(
            TableView<T> tableView,
            ObservableList<T> backingList,
            int pageSize,
            Function<PageRequest, Page<T>> pageFetcher
    ) {
        this.tableView = tableView;
        this.backingList = backingList;
        this.pageSize = pageSize;
        this.pageFetcher = pageFetcher;

        attachScrollListener();
    }

    // 🔹 Load next page
    public void loadNext() {
        if (loading || !hasMore) return;
        loading = true;

        Task<Page<T>> task = new Task<>() {
            @Override
            protected Page<T> call() {
                return pageFetcher.apply(PageRequest.of(page, pageSize));
            }
        };

        task.setOnSucceeded(e -> {
            Page<T> result = task.getValue();

            if (result.isEmpty()) {
                hasMore = false;
            } else {
                backingList.addAll(result.getContent());
                page++;
            }

            if (onPageLoaded != null) {
                onPageLoaded.accept(null);
            }

            loading = false;
        });

        task.setOnFailed(e -> loading = false);

        new Thread(task, "LazyTableLoader").start();
    }

    // 🔹 Reset + reload
    public void reset(Function<PageRequest, Page<T>> newFetcher) {
        this.pageFetcher = newFetcher;
        page = 0;
        hasMore = true;
        backingList.clear();
        loadNext();
    }

    public void onPageLoaded(Consumer<Void> callback) {
        this.onPageLoaded = callback;
    }

    private void attachScrollListener() {
        Platform.runLater(() -> {
            ScrollBar vBar =
                    (ScrollBar) tableView.lookup(".scroll-bar:vertical");

            if (vBar != null) {
                vBar.valueProperty().addListener((obs, ov, nv) -> {
                    if (nv.doubleValue() >= vBar.getMax()) {
                        loadNext();
                    }
                });
            }
        });
    }
}
