package org.gzipper.java.presentation.controller.main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.util.converter.PercentageStringConverter;
import org.gzipper.java.application.ArchiveInfo;
import org.gzipper.java.application.ArchiveOperation;
import org.gzipper.java.application.model.ArchiveType;
import org.gzipper.java.application.observer.Listener;
import org.gzipper.java.application.observer.Notifier;
import org.gzipper.java.application.util.StringUtils;
import org.gzipper.java.exceptions.GZipperException;
import org.gzipper.java.i18n.I18N;
import org.gzipper.java.presentation.ProgressManager;
import org.gzipper.java.util.Log;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

/**
 * Represents the currently active state of the {@link MainViewController}.
 */
abstract class ArchivingState implements Listener<Integer> {

    /**
     * The aggregated controller of type {@link MainViewController}.
     */
    protected final MainViewController controller;

    /**
     * Converts percentage values to string objects. See method
     * {@link #update(org.gzipper.java.application.observer.Notifier, java.lang.Integer)}.
     */
    private final PercentageStringConverter _converter = new PercentageStringConverter();

    /**
     * Holds the current progress or {@code -1d}. The current progress is retrieved by the UI thread to update
     * the progress in the UI. A new task is only submitted to the UI thread if the value is {@code -1d}.
     * This avoids an unresponsive UI since the UI thread will not be flooded with new tasks.
     */
    private ProgressManager _progressManager;

    /**
     * Used to filter files or archive entries when processing archives.
     */
    protected Predicate<String> _filterPredicate = null;

    /**
     * Creates a new instance of {@link ArchivingState}.
     *
     * @param controller the controller to be aggregated.
     */
    protected ArchivingState(MainViewController controller) {
        this.controller = controller;
    }

    /**
     * Set the filter to be used when processing archives.
     *
     * @param filterPredicate the filter or {@code null} to reset it.
     */
    final void setFilterPredicate(Predicate<String> filterPredicate) {
        _filterPredicate = filterPredicate;
    }

    /**
     * Returns the filter to be used when processing archives.
     *
     * @return the filter to be used when processing archives. If no filter
     * is set, this method will return {@code null}.
     */
    final Predicate<String> getFilterPredicate() {
        return _filterPredicate;
    }

    /**
     * Validates the output path specified in user control.
     *
     * @return true if output path is valid, false otherwise.
     */
    abstract boolean checkUpdateOutputPath();

    /**
     * Initializes the archiving operation.
     *
     * @param archiveType the type of the archive, see {@link ArchiveType}.
     * @return list consisting of {@link ArchiveOperation}.
     * @throws GZipperException if the archive type could not have been
     *                          determined.
     */
    abstract List<ArchiveOperation> initOperation(ArchiveType archiveType) throws GZipperException;

    //<editor-fold desc="Private helper methods">

    /**
     * Initializes the archiving job by creating the required {@link Task}. This
     * task will not perform the algorithmic operations for archiving but instead
     * constantly checks for interruption to properly detect the abortion of an
     * operation. For the algorithmic operations a new task will be created and
     * submitted to the task handler. If an operation has been aborted, e.g.
     * through user interaction, the operation will be interrupted.
     *
     * @param operation the {@link ArchiveOperation} that will eventually be
     *                  performed by the task when executed.
     * @return a {@link Task} that can be executed to perform the specified archiving operation.
     */
    @SuppressWarnings("SleepWhileInLoop")
    private Task<Boolean> initArchivingJob(final ArchiveOperation operation) {
        Task<Boolean> task = new Task<>() {
            @SuppressWarnings("BusyWait")
            @Override
            protected Boolean call() throws Exception {
                final Future<Boolean> futureTask = controller.taskHandler.submit(operation);
                while (!futureTask.isDone()) {
                    try {
                        Thread.sleep(10); // continuous check for interruption
                    } catch (InterruptedException ex) {
                        // if exception is caught, task has been interrupted
                        Log.i(I18N.getString("interrupt.text"), true);
                        Log.w(ex.getLocalizedMessage(), false);
                        operation.interrupt();
                        if (futureTask.cancel(true)) {
                            Log.i(I18N.getString("operationCancel.text"), true, operation);
                        }
                    }
                }

                try { // check for cancellation
                    return futureTask.get();
                } catch (CancellationException ex) {
                    return false; // ignore exception
                }
            }
        };

        showSuccessMessageAndFinalizeArchivingJob(operation, task);
        showErrorMessageAndFinalizeArchivingJob(operation, task);

        return task;
    }

    private void showErrorMessageAndFinalizeArchivingJob(ArchiveOperation operation, Task<Boolean> task) {
        task.setOnFailed(e -> {
            Log.i(I18N.getString("operationFail.text"), true, operation);
            final Throwable thrown = e.getSource().getException();
            if (thrown != null) Log.e(thrown.getLocalizedMessage(), thrown);
            finishArchivingJob(operation, task);
        });
    }

    private void showSuccessMessageAndFinalizeArchivingJob(ArchiveOperation operation, Task<Boolean> task) {
        task.setOnSucceeded(e -> {
            final boolean success = (boolean) e.getSource().getValue();
            if (success) {
                Log.i(I18N.getString("operationSuccess.text"), true, operation);
            } else {
                Log.w(I18N.getString("operationNoSuccess.text"), true, operation);
            }
            finishArchivingJob(operation, task);
        });
    }

    /**
     * Calculates the total duration in seconds of the specified {@link ArchiveOperation}
     * and logs it to the text area. Also toggles the Start and Abort button.
     *
     * @param operation {@link ArchiveOperation} that holds elapsed time.
     * @param task      the task to be removed from the list of active tasks.
     */
    private void finishArchivingJob(ArchiveOperation operation, Task<?> task) {
        Log.i(I18N.getString("elapsedTime.text"), true, operation.calculateElapsedTime());
        controller.activeTasks.remove(task.hashCode());
        if (controller.activeTasks.isEmpty()) {
            controller.enableUIControls();
            controller.resetProgressBar();
            controller.setTextInProgressBar(StringUtils.EMPTY);
        }
    }

    //</editor-fold>

    /**
     * Applies the required extension filters to the specified file chooser.
     *
     * @param chooser the {@link FileChooser} to which the extension filters
     *                will be applied to.
     */
    void applyExtensionFilters(FileChooser chooser) {
        if (chooser != null) {
            final ArchiveType selectedType = controller.getSelectedArchiveType();
            for (ArchiveType type : ArchiveType.values()) {
                if (type.equals(selectedType)) {
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                            type.getDisplayName(), type.getExtensionNames(true));
                    chooser.getExtensionFilters().add(extFilter);
                }
            }
        }
    }

    /**
     * Performs the specified array of {@link ArchiveOperation} instances.
     *
     * @param operations the {@link ArchiveOperation} instances to be performed.
     */
    void performOperations(ArchiveOperation... operations) {
        if (operations == null || operations.length == 0) return;

        _progressManager = new ProgressManager(operations.length);

        for (var operation : operations) {
            if (operation != null) {
                Task<Boolean> task = initArchivingJob(operation);
                final ArchiveInfo info = operation.getArchiveInfo();

                Log.i(I18N.getString("operationStarted.text"), true, operation,
                        info.getArchiveType().getDisplayName());
                Log.i(I18N.getString("outputPath.text", info.getOutputPath()), true);

                controller.disableUIControlsAsLongAsAnyTaskIsActive();

                var future = controller.taskHandler.submit(task);
                controller.activeTasks.put(task.hashCode(), future);
            }
        }
    }

    @Override
    public final void update(Notifier<Integer> notifier, Integer value) {
        if (value >= 100) {
            notifier.detach(this);
        } else {
            double progress = _progressManager.updateProgress(notifier.getId(), value);
            if (_progressManager.getAndSetProgress(progress) == ProgressManager.SENTINEL) {
                Platform.runLater(() -> {
                    double totalProgress = _progressManager.getAndSetProgress(ProgressManager.SENTINEL);
                    if (totalProgress > controller.getProgressOfProgressBar()) {
                        controller.setProgressInProgressBar(totalProgress);
                        controller.setTextInProgressBar(_converter.toString(totalProgress));
                    }
                });
            }
        }
    }
}