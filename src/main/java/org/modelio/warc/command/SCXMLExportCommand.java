package org.modelio.warc.command;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.modelio.api.modelio.Modelio;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.diagram.dg.IDiagramDG;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.api.module.context.configuration.IModuleUserConfiguration;
import org.modelio.api.module.context.log.ILogService;
import org.modelio.metamodel.diagrams.AbstractDiagram;
import org.modelio.metamodel.uml.behavior.stateMachineModel.InitialPseudoState;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.vcore.smkernel.mapi.MObject;
import org.modelio.metamodel.diagrams.StateMachineDiagram;
import org.modelio.api.modelio.diagram.IDiagramLink;

/**
 * Implementation of the IModuleContextualCommand interface.
 * <br>The module contextual commands are displayed in the contextual menu and in the specific toolbar of each module property page.
 * <br>The developer may inherit the DefaultModuleContextualCommand class which contains a default standard contextual command implementation.
 *
 */
public class SCXMLExportCommand extends DefaultModuleCommandHandler {
    /**
     * Constructor.
     */
    public SCXMLExportCommand() {
        super();
    }

    /**
     * @see org.modelio.api.module.commands.DefaultModuleContextualCommand#accept(java.util.List,
     *      org.modelio.api.module.IModule)
     */
    @Override
    public boolean accept(List<MObject> selectedElements, IModule module) {
        // Check that there is only one selected element
    	if (selectedElements.size() == 1)
    	{
    		ModelElement modelelt = (ModelElement)selectedElements.get(0);
    		
    		// Check if the selected element class name is UseCaseDiagramImpl
            return modelelt.getClass().getName() == "org.modelio.metamodel.impl.diagrams.StateMachineDiagramImpl";
    	}
    	
    	return false;
    }

    /**
     * @see org.modelio.api.module.commands.DefaultModuleContextualCommand#actionPerformed(java.util.List,
     *      org.modelio.api.module.IModule)
     */
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        ILogService logService = module.getModuleContext().getLogService();
        logService.info("HelloWorldCommand - actionPerformed(...)");
        
        IModelingSession session = module.getModuleContext().getModelingSession();
        List<MObject> root = session.getModel().getModelRoots();
        IModuleUserConfiguration configuration = module.getModuleContext().getConfiguration();

        ModelElement modelelt = (ModelElement)selectedElements.get(0);
        
        List<String> diagram_scxml = dumpDiagram((AbstractDiagram)modelelt);
        
        writeToFile(diagram_scxml);
        		
        // MessageDialog.openInformation(null, "Hello", diagram_scxml);
    }
    
    public List<String> dumpDiagram(AbstractDiagram diagram)
    {
    	IDiagramHandle dh = Modelio.getInstance().getDiagramService().getDiagramHandle(diagram);
		IDiagramDG diagramNode = dh.getDiagramNode();
		// change for StateMachine interface?

		// for now constant InitialPseudoState - will change later
		String state = InitialPseudoState.MNAME;
		// I want to take actual name of initial "pseudo" state ( something like getName() )
		
		// header for each scxml file - only initial property need to be added
		List<String> scxml_file = new ArrayList<String>();

		
		scxml_file.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		scxml_file.add("<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\" initial=\"" + state + "\" name=\"" + diagramNode.getName() + "\">");
		// for each over all nodes in diagram (I want to iterate over states - something like - for state in StateMachine.getStates():
		
		for (IDiagramNode topLevelNode : diagramNode.getNodes())
		{
			dumpNode(topLevelNode, scxml_file);
		}
		
		//just add last line of the file
		scxml_file.add("</scxml>");
		dh.close();
		
		return scxml_file;
    }
    
    public void dumpNode(IDiagramNode node, List<String> scxml_file) {
    	if (node != null) {
    		// something like State.getName() but I couldn't find this in javadoc
    		scxml_file.add("    <state id=\"" + node.getElement().getName() + "\">");
    		
    		Rectangle bounds = node.getBounds();
	        String geometry = String.valueOf(bounds.width()) + ";" + String.valueOf(bounds.height()) + ";" + String.valueOf(bounds.x()) + ';' + String.valueOf(bounds.y());
	        scxml_file.add("        <qt:editorinfo initialGeometry=\"" +  geometry + "\">");
    		
    		for (IDiagramLink trans : node.getFromLinks())
    			dumpTransition(trans, scxml_file);
    		
    		scxml_file.add("    </state>");
    	}
    }
    
    public void dumpTransition(IDiagramLink trans, List<String> scxml_file) {
    	// I want to get guard, target or/and event from transitions
    	scxml_file.add("        <transition guard=\"\" target=\"" + trans.getTo().getName() + "\" />");
    	// I know something about Transition.getTarget() and Transition.getGuard() but I think I need pass state through arguments
    }
    
    public void writeToFile(List<String> diagram_scxml)
    {
    	PrintWriter writer;
    	    	
	    	JFileChooser saveFile = new JFileChooser();
	    	FileNameExtensionFilter filter = new FileNameExtensionFilter("State Chart XML", "scxml");
	    	saveFile.setFileFilter(filter);
    	
        int userSelection = saveFile.showSaveDialog(null);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = saveFile.getSelectedFile();
            
            try {
            	String location = fileToSave.getAbsolutePath();
            	
            	if (!location.endsWith(".scxml"))
            		location += ".scxml";
            	
        		writer = new PrintWriter(location, "UTF-8");
            	writer.print("");
            	
            	for(String line : diagram_scxml)
            		writer.println(line);
            	
            	writer.close();
            	
            	MessageDialog.openInformation(null, "Success", "File exported successfully to \"" + location + "\"!");
        	} catch (IndexOutOfBoundsException e) {
        	    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
        		MessageDialog.openInformation(null, "Error", "IndexOutOfBoundsException: " + e.getMessage());
        	} catch (IOException e) {
        	    System.err.println("Caught IOException: " + e.getMessage());
        		MessageDialog.openInformation(null, "Error", "Caught IOException: " + e.getMessage());
        	}
        }
    }
}
