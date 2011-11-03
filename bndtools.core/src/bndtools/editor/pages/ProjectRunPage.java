package bndtools.editor.pages;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import bndtools.Plugin;
import bndtools.editor.model.BndEditModel;
import bndtools.editor.project.RepositorySelectionPart;
import bndtools.editor.project.RunBundlesPart;
import bndtools.editor.project.RunFrameworkPart;
import bndtools.editor.project.RunPropertiesPart;
import bndtools.editor.project.RunRequirementsPart;
import bndtools.editor.project.RunVMArgsPart;
import bndtools.utils.MessageHyperlinkAdapter;

public class ProjectRunPage extends FormPage {

    private final BndEditModel model;

    public static final IPageFactory FACTORY = new IPageFactory() {
        public IFormPage createPage(FormEditor editor, BndEditModel model, String id) throws IllegalArgumentException {
            return new ProjectRunPage(editor, model, id, "Run");
        }
    };

    public ProjectRunPage(FormEditor editor, BndEditModel model, String id, String title) {
        super(editor, id, title);
        this.model = model;
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        managedForm.setInput(model);

        FormToolkit tk = managedForm.getToolkit();
        final ScrolledForm form = managedForm.getForm();
        form.setText("Run");
        tk.decorateFormHeading(form.getForm());
        form.getForm().addMessageHyperlinkListener(new MessageHyperlinkAdapter(getEditor()));

        RunAction runAction = new RunAction(this, "run");
        runAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.PLUGIN_ID, "icons/run.gif"));
        runAction.setText("Run OSGi");
        ActionContributionItem runContrib = new ActionContributionItem(runAction);
        runContrib.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        form.getToolBarManager().add(runContrib);

        RunAction debugAction = new RunAction(this, "debug");
        debugAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Plugin.PLUGIN_ID, "icons/debug.gif"));
        debugAction.setText("Debug OSGi");
        ActionContributionItem debugContrib = new ActionContributionItem(debugAction);
        debugContrib.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        form.getToolBarManager().add(debugContrib);

        form.getToolBarManager().update(true);

        GridLayout gl;
        GridData gd;

        // Create Controls
        final Composite body = form.getBody();
//        TableWrapLayout twl = new TableWrapLayout();
//        twl.numColumns = 1;
//        twl.makeColumnsEqualWidth = true;
//        body.setLayout(twl);
        gl = new GridLayout(2, true);
        body.setLayout(gl);

        final Composite left = tk.createComposite(body);
//        TableWrapData twd;
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
//        left.setLayoutData(twd);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        left.setLayoutData(gd);
//        twl = new TableWrapLayout();
//        twl.numColumns = 1;
//        left.setLayout(twl);
        gl = new GridLayout(1, true);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        left.setLayout(gl);

        final Composite right = tk.createComposite(body);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
//        right.setLayoutData(twd);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        right.setLayoutData(gd);
//        twl = new TableWrapLayout();
//        twl.numColumns = 1;
//        right.setLayout(twl);
        gl = new GridLayout(1, true);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        right.setLayout(gl);



        // First column
        RepositorySelectionPart reposPart = new RepositorySelectionPart(left, tk, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
        managedForm.addPart(reposPart);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL);
//        twd.maxWidth = 200;
//        twd.heightHint = 200;
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 50;
        gd.heightHint = 50;
        reposPart.getSection().setLayoutData(gd);
//
        RunFrameworkPart runFwkPart = new RunFrameworkPart(left, tk, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
        managedForm.addPart(runFwkPart);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL);
//        twd.maxWidth = 200;
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        runFwkPart.getSection().setLayoutData(gd);
//
//
        RunPropertiesPart runPropertiesPart = new RunPropertiesPart(left, tk, Section.TITLE_BAR | Section.TWISTIE | Section.DESCRIPTION);
        managedForm.addPart(runPropertiesPart);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL);
//        twd.maxWidth = 200;
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        runPropertiesPart.getSection().setLayoutData(gd);
//
//
        RunVMArgsPart vmArgsPart = new RunVMArgsPart(left, tk, Section.TITLE_BAR | Section.TWISTIE);
        managedForm.addPart(vmArgsPart);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL);
//        twd.maxWidth = 200;
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        vmArgsPart.getSection().setLayoutData(gd);

        // Second column
        RunRequirementsPart requirementsPart = new RunRequirementsPart(right, tk, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION);
        managedForm.addPart(requirementsPart);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
//        requirementsPart.getSection().setLayoutData(twd);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 50;
        gd.heightHint = 50;
        requirementsPart.getSection().setLayoutData(gd);

        RunBundlesPart runBundlesPart = new RunBundlesPart(right, tk, Section.TITLE_BAR | Section.TWISTIE);
        managedForm.addPart(runBundlesPart);
//        twd = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
//        runBundlesPart.getSection().setLayoutData(twd);
        gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        runBundlesPart.getSection().setLayoutData(gd);

    };
}