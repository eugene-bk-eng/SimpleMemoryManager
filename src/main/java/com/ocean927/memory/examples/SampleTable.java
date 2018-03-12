package com.ocean927.memory.examples;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class SampleTable extends Application {

    private static final Random RAND = new Random();
    private static final int COLUMN_COUNT = 100;
    private static final int ROW_COUNT = 100000;
    private static final int BATCH_COUNT = 10;
    private static final int UPDATE_THREAD_COUNT = 1;
    private static final ExecutorService executor = Executors.newFixedThreadPool(12);
    private static final AtomicLong counter = new AtomicLong();

    private final ObservableList<Row> dataRows = FXCollections.observableArrayList();

    private final Runnable changeValues = () -> {
        while (!Thread.currentThread().isInterrupted()) {
            Platform.runLater(() -> {
                IntStream.range(0, BATCH_COUNT).forEach(i -> {
                    Row row = dataRows.get(RAND.nextInt(dataRows.size()));
                    IntStream.range(0, RAND.nextInt(COLUMN_COUNT)).forEach(z -> {
                        int col = RAND.nextInt(COLUMN_COUNT);
                        row.setData(col, row.getData(col).get() + 1);
                        counter.incrementAndGet();
                    });
                });
            });
            try { Thread.sleep(1);  } catch (InterruptedException ignored) { }
        }
    };

    @Override
    public void start(Stage primaryStage) {
        TableView<Row> table = new TableView<>();
        table.setEditable(true);
        TableColumn<Row, String> colName = new TableColumn<>("Name");
        colName.setPrefWidth(200);
        colName.setCellValueFactory(cell -> cell.getValue().getSymbol());
        table.getColumns().add(colName);
        IntStream.range(0, COLUMN_COUNT).forEach(i -> {
            TableColumn<Row, Number> col = new TableColumn<>("Col#" + i);
            col.setPrefWidth(150);
            col.setCellValueFactory(cell -> cell.getValue().getData(i));
            table.getColumns().add(col);
        });
        IntStream.range(0, ROW_COUNT).mapToObj(i -> new Row(String.format("ROW#%5d", i))).forEach(dataRows::add);
        table.setItems(dataRows);
        IntStream.range(0, UPDATE_THREAD_COUNT).forEach(i -> executor.submit(changeValues));
        Scene scene = new Scene(table, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        startTitleUpdater(primaryStage);
    }

    private void startTitleUpdater(Stage primaryStage) {
        new Thread(() -> {
            long time = System.currentTimeMillis();
            long count = counter.get();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long now = System.currentTimeMillis();
                long deltaTime = now - time;
                time = now;
                long countNew = counter.get();
                long deltaCount = countNew - count;
                count = countNew;
                double rate = ((double) deltaCount / (double) deltaTime) * 1000d;

                Platform.runLater(() -> primaryStage.setTitle(String.format("Count: rows: %d columns: %d <~~> Updates: count %10d mm, individual cells updated per second: %10.4f", ROW_COUNT, COLUMN_COUNT, counter.get() / 1000000, rate)));
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    static class Row {

        private final StringProperty symbol = new SimpleStringProperty();
        private final IntegerProperty[] dataCols = IntStream.range(0, COLUMN_COUNT).mapToObj(i -> new SimpleIntegerProperty()).toArray(IntegerProperty[]::new);

        Row(String symbol) {
            this.symbol.setValue(symbol);
        }

        IntegerProperty getData(int col) {
            return dataCols[col];
        }

        StringProperty getSymbol() {
            return symbol;
        }

        void setData(int col, double value) {
            dataCols[col].setValue(value);
        }
    }

}
