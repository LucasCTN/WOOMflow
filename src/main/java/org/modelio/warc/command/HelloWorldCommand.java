package org.modelio.warc.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.api.module.context.configuration.IModuleUserConfiguration;
import org.modelio.api.module.context.log.ILogService;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.vcore.smkernel.mapi.MObject;

/**
 * Implementation of the IModuleContextualCommand interface.
 * <br>The module contextual commands are displayed in the contextual menu and in the specific toolbar of each module property page.
 * <br>The developer may inherit the DefaultModuleContextualCommand class which contains a default standard contextual command implementation.
 *
 */
public class HelloWorldCommand extends DefaultModuleCommandHandler {
	String scxmlPath = null;
	String classesWarcPath = null;
	
	ArrayList<String> classes = new ArrayList<String>();
	ArrayList<String> responsavel = new ArrayList<String>();
	ArrayList<String> colaboradores = new ArrayList<String>();
	
    /**
     * Constructor.
     */
    public HelloWorldCommand() {
        super();
    }

    /**
     * @see org.modelio.api.module.commands.DefaultModuleContextualCommand#accept(java.util.List,
     *      org.modelio.api.module.IModule)
     */
    @Override
    public boolean accept(List<MObject> selectedElements, IModule module) {
        // Check that there is only one selected element
        return selectedElements.size() == 1;
    }

    /**
     * @see org.modelio.api.module.commands.DefaultModuleContextualCommand#actionPerformed(java.util.List,
     *      org.modelio.api.module.IModule)
     */
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        ILogService logService = module.getModuleContext().getLogService();
        logService.info("HelloWorldCommand - actionPerformed(...)");

        //IModelingSession session = module.getModuleContext().getModelingSession();
        //List<MObject> root = session.getModel().getModelRoots();
        //IModuleUserConfiguration configuration = module.getModuleContext().getConfiguration();

        //ModelElement modelelt = (ModelElement)selectedElements.get(0);
        //MessageDialog.openInformation(null, "Hello", modelelt.getName());
        
    	/*
    	Shell shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("WARC Table");
		shell.open();
		*/
    	WTable table = new WTable();
    	table.main(this.scxmlPath, this.classesWarcPath, classes, responsavel, colaboradores, logService);
    	this.scxmlPath = table.scxmlPath;
    	this.classesWarcPath = table.classesWarcPath;
    	this.classes = table.classes;
        logService.info("end");
    	//table.open();
    }
}
