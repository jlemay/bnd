package bndtools.launch.ui;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class LaunchStatusHandler implements IStatusHandler {

    public Boolean handleStatus(final IStatus status, Object source) throws CoreException {
        if (status.isOK())
            return true;

        final AtomicBoolean result = new AtomicBoolean();
        Runnable uitask = new Runnable() {
            public void run() {
                LaunchStatusDialog dialog = new LaunchStatusDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), status);
                int response = dialog.open();

                result.set(response == Window.OK && status.getSeverity() < IStatus.ERROR);
            }
        };

        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display.getThread() == Thread.currentThread())
            uitask.run();
        else
            display.syncExec(uitask);

        return result.get();
    }

}
