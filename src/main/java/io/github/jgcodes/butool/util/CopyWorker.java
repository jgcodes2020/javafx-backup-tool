package io.github.jgcodes.butool.util;

import io.github.jgcodes.butool.Controller;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class CopyWorker extends Task<Void> {

  public final Path dirIn;
  public final Path dirOut;

  public final LocalDate startDate;
  public final LocalDate endDate;

  public final Controller.CopySettings settings;
  private final DirectoryStream.Filter<Path> DATE_FILE_FILTER = path -> {
    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
    LocalDate creationDate = LocalDate.ofInstant(
      attrs.creationTime().toInstant(),
      TimeZone.getDefault().toZoneId()
    );
    return checkDates(creationDate) && attrs.isRegularFile();
  };

  private final List<Path> files = new ArrayList<>(32);
  private final List<Long> fileSizes = new ArrayList<>(32);
  private long maxProgress = -1;

  public CopyWorker(Path dirIn, Path dirOut, LocalDate startDate, LocalDate endDate, Controller.CopySettings settings) {
    if (!(Files.isDirectory(dirIn) && Files.isDirectory(dirOut)))
      throw new IllegalArgumentException("Input and output paths must be directories");

    this.dirIn = dirIn;
    this.dirOut = dirOut;

    this.startDate = startDate;
    this.endDate = endDate;

    this.settings = settings;
  }

  @Override
  protected Void call() throws Exception {
    updateProgress(-1L, maxProgress);
    updateTitle("Counting files...");

    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dirIn, DATE_FILE_FILTER)) {
      maxProgress = 0;
      fileSizes.add(0L);

      for (Path p: ds) {
        updateMessage("Counting " + p);

        if (isCancelled())
          return null;

        files.add(p);
        long size = Files.size(p);
        maxProgress += size;
        fileSizes.add(fileSizes.get(fileSizes.size() - 1) + size);
      }
    }
    updateTitle("Copying files");
    for (int i = 0; i < files.size(); i++) {
      if (isCancelled()) return null;

      Path p = files.get(i);

      Path pTo = resolveOutput(p, dirOut);
      Files.createDirectories(pTo.getParent());
      copy(p, pTo, i);
      updateProgress(fileSizes.get(i), maxProgress);
      // ThreadUtil.sleepIgnoreInterrupt(1000); // used for dialog debug
    }
    updateProgress(maxProgress, maxProgress);
    return null;
  }

  //Impl methods

  private boolean checkDates(LocalDate date) {
    boolean start = startDate == null || !date.isBefore(startDate);
    boolean end = endDate == null || !date.isAfter(endDate);

    return start && end;
  }

  private void copy(Path from, Path to, int filesCopied) throws IOException {
    updateMessage(String.format("Copying %s to %s", from, to));

    //conform to overwrite policy
    if (Files.exists(to)) {
      switch (settings) {
        case ASK -> {
          boolean[] overwrite = {false};
          try {
            PlatformUtil.runAndWait(() -> {
              Alert alert = new Alert(AlertType.CONFIRMATION, String.format("%s already exists. Do you want to overwrite it?", to
                .getFileName()));
              alert.setResizable(true);
              alert.showAndWait().ifPresent(btn -> overwrite[0] = (btn == ButtonType.OK));
            });
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
          if (!overwrite[0]) return;
        }
        case SKIP -> {
          return;
        }
        case OVERWRITE -> {
          //no-op, just copy the file
        }
      }
    }
    else {
      Files.createFile(to);
    }

    try (
      FileChannel in = FileChannel.open(from, StandardOpenOption.READ);
      FileChannel out = FileChannel.open(to, StandardOpenOption.WRITE)
    ) {
      long nCopied = 0;
      final long inSize = in.size();
      while (nCopied < in.size()) {
        nCopied += in.transferTo(nCopied, in.size(), out);
        updateProgress(fileSizes.get(filesCopied) + nCopied, maxProgress);
      }
    }
  }

  private Path resolveOutput(Path fromFile, Path toDir) throws IOException {
    Path name = fromFile.getFileName();

    BasicFileAttributes attrs = Files.readAttributes(fromFile, BasicFileAttributes.class);
    LocalDate creationDate = LocalDate.ofInstant(
      attrs.creationTime().toInstant(),
      TimeZone.getDefault().toZoneId()
    );

    String folderName = String.format("%1$tY-%1$tm", creationDate);
    return toDir.resolve(folderName).resolve(name);
  }

  //Event receivers

  @Override
  protected void succeeded() {
    updateTitle("Copies complete!");
    updateMessage("All files copied");
  }

  @Override
  protected void cancelled() {
    updateTitle("Copying cancelled, please wait...");
    updateMessage("The dialog should close soon. I just didn't want to copy half of a file and ruin it.");
  }

  @Override
  protected void failed() {
    updateTitle("Oh no!");
    updateMessage(
      "If you ran this app from the command line, " +
        "you should see the stack trace there in yellow. " +
        "I will be adding a custom dialog later."
    );

    Throwable e = getException();
    if (e != null) {
      System.out.print("\u001B[93m");
      e.printStackTrace(System.out);
      System.out.print("\u001B[1m");
    }
  }
}
