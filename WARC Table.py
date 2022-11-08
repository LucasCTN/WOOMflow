from org.eclipse.swt import SWT
from org.eclipse.swt.widgets import (Display, Shell, Text, TabFolder, TabItem, 
                                    Composite, Table, TableColumn, TableItem,
                                    Label, Button, Listener)
from org.eclipse.swt.layout import  (GridLayout, FillLayout, FormLayout, FormData, 
                                    FormAttachment)
from org.eclipse.swt.graphics import Point
from org.eclipse.swt.custom import TableEditor, CCombo
from xml.dom import minidom

from org.eclipse.jface.viewers import TableViewer, ColumnPixelData, TableViewerColumn
from org.eclipse.jface.layout import TableColumnLayout
import os
from javax.swing import JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter 

FileNameExtensionFilter


class openwindow:
    classes = []
    class_items = []

    table_caso_de_uso = None
    table_classes = None

    def __init__(self):
        self.parent = Display.getDefault().getActiveShell()
        
        self.child = Shell(self.parent, SWT.CLOSE | SWT.RESIZE)
        self.child.setText("VALVES WARC")
        self.child.setSize(490, 424)
        self.child.setLayout(GridLayout())

        self.tabFolder = TabFolder(self.child, SWT.NONE)

        scxml = self.ler_scxml()

        self.view_setar_tab_principal()
        self.view_setar_tab_caso_de_uso(scxml)
    def view_setar_tab_principal(self):
        tab_principal = TabItem(self.tabFolder, SWT.NONE)
        tab_principal.setText("Principal")
            
        composite_2 = Composite(self.tabFolder, SWT.NONE)
        tab_principal.setControl(composite_2)
        composite_2.setLayout(FormLayout())
        
        label = Label(composite_2, SWT.SEPARATOR | SWT.HORIZONTAL)
        fd_label = FormData()
        fd_label.right = FormAttachment(100, -10)
        fd_label.left = FormAttachment(0, 10)
        fd_label.bottom = FormAttachment(0, 66)
        label.setLayoutData(fd_label)
        
        lblNewLabel = Label(composite_2, SWT.NONE)
        fd_lblNewLabel = FormData()
        fd_lblNewLabel.bottom = FormAttachment(100, -260)
        fd_lblNewLabel.top = FormAttachment(label, 6)
        fd_lblNewLabel.left = FormAttachment(0, 10)
        lblNewLabel.setLayoutData(fd_lblNewLabel)
        lblNewLabel.setText("Exportar a FSM como SCXML:")
        
        btnNewButton = Button(composite_2, SWT.NONE)
        fd_btnNewButton = FormData()
        fd_btnNewButton.top = FormAttachment(lblNewLabel, -5, SWT.TOP)
        fd_btnNewButton.left = FormAttachment(lblNewLabel, 17)
        btnNewButton.setLayoutData(fd_btnNewButton)
        btnNewButton.setText("Exportar")

        class MyListener(Listener):
            def handleEvent(self, event):
                selected = selectedElements.get(0)
                dh = Modelio.getInstance().getDiagramService().getDiagramHandle(selected)
                diagramNode = dh.getDiagramNode()
                state = InitialPseudoState.MNAME
                scxml_file = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                scxml_file += "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\" initial=\"" + state + "\" name=\"" + diagramNode.getName() + "\">\n"
                for topLevelNode in diagramNode.getNodes():
                    if topLevelNode != None:
                        scxml_file += "    <state id=\"" + topLevelNode.getElement().getName() + "\">\n"
                        # bounds = topLevelNode.getBounds()
                        # geometry = str(bounds.width()) + ";" + str(bounds.height()) + ";" + str(bounds.x()) + ';' + str(bounds.y())
                        # scxml_file += "        <qt:editorinfo initialGeometry=\"" +  geometry + "\">\n"
                        for trans in topLevelNode.getFromLinks():
                            scxml_file += "        <transition guard=\"\" target=\"" + trans.getTo().getName() + "\" />\n"
                        scxml_file += "    </state>\n"
                scxml_file += "</scxml>"
                dh.close()

                save_file = JFileChooser()
                file_filter = FileNameExtensionFilter("State Chart XML", ["scxml"])
                save_file.setFileFilter(file_filter)
                save_file.setDialogTitle("Specify a file to save")
                if save_file.showSaveDialog(None) == JFileChooser.APPROVE_OPTION:
                    file_to_save = save_file.getSelectedFile()
                    location = file_to_save.getAbsolutePath()
                    if not location.endswith(".scxml"):
                        location += ".scxml"
                    try:
                        with open(location, 'w+') as f:
                            f.write(scxml_file)
                            f.close()
                        print("File exported successfully to \"" + location + "\"!")
                    except:
                        print("the file couldn't be saved.")


        listener = MyListener()
        btnNewButton.addListener(SWT.Selection, listener)

        lblNewLabel_1 = Label(composite_2, SWT.NONE)
        fd_label.top = FormAttachment(lblNewLabel_1, 6)
        fd_lblNewLabel_1 = FormData()
        fd_lblNewLabel_1.bottom = FormAttachment(0, 35)
        fd_lblNewLabel_1.top = FormAttachment(0, 10)
        fd_lblNewLabel_1.left = FormAttachment(0, 10)
        lblNewLabel_1.setLayoutData(fd_lblNewLabel_1)
        lblNewLabel_1.setText("Tabela WARC")

    def view_setar_tab_caso_de_uso(self, scxml):
        tab_item = TabItem(self.tabFolder, SWT.NONE)
        tab_item.setText("Caso de Uso")

        tab_item_composite              = Composite(self.tabFolder, SWT.NO_SCROLL)
        tab_item_composite_layout       = FillLayout()
        tab_item_composite_layout.type  = SWT.HORIZONTAL
        tab_item_composite.setLayout(tab_item_composite_layout)

        tab_item.setControl(tab_item_composite)

        table = Table(tab_item_composite, SWT.BORDER)
        table.setLinesVisible(True)

        table_column_one = TableColumn(table, SWT.LEFT)
        table_column_two = TableColumn(table, SWT.LEFT)

        table_column_one.setText("Acao")
        table_column_two.setText("Reacao")

        table_column_one.setWidth(200)  # table_column_one.pack()
        table_column_two.setWidth(200)  # table_column_two.pack()

        self.table_caso_de_uso = table

        # populating the table
        for state_name in scxml["states"]:
            state = scxml["states"][state_name]
            for transition in state["transitions"]:
                target_name = transition["target"]
                target_state = scxml["states"][target_name]
                reactions = [event["reaction"] for event in target_state["events"]]
                item_one = TableItem(table, SWT.NONE)
                item_one.setText([transition["action"], ",".join(reactions)])

        table.setHeaderVisible(True)
        table.pack()

    def ler_scxml(self):
        folder_path = os.path.join(os.path.expanduser("~/Desktop"), "WARC")
        file_name = "TesteStateMachine SHOULDBE.scxml"
        file_path = os.path.join(folder_path, file_name)

        dom = minidom.parse(file_path)
        states = dom.getElementsByTagName('state')

        scxml_obj = {"states": {}}

        for state in states:
            if state.childNodes: # state not empty
                name = state.attributes['id'].value
                events = []
                transitions = []

                # internal transitions (do / entry)
                for internal_transition in state.getElementsByTagName('internaltransition'):
                    internal_transition_obj = {"name": internal_transition.attributes['event'].value, "reaction": internal_transition.attributes['guard'].value}
                    events.append(internal_transition_obj)
                                
                # transitions (action / target)
                for transition in state.getElementsByTagName('transition'):
                    transition_obj = {"action": transition.attributes['id'].value, "target": transition.attributes['target'].value}
                    transitions.append(transition_obj)

            # scxml_obj["states"].append({"name": name, "events": events, "transitions": transitions})
            scxml_obj["states"][name] = {"events": events, "transitions": transitions}

        return scxml_obj

    def criar_tabela_acoes(self):
        pass

openwindow()