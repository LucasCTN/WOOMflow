package org.modelio.warc.command;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.modelio.api.module.context.log.ILogService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;

import java.awt.List;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Group;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class WTable {
	ArrayList<String> classes;
	ArrayList<String> listaResponsavel;
	ArrayList<String> listaColaboradores;
	
	ArrayList<CCombo> comboResps = new ArrayList<CCombo>();

	Table table_caso_de_uso = null;
	Table tabelaDeClasses = null;

	protected Shell shell;

	Shell parent;
	Shell child;
	TabFolder tabFolder;

	String scxmlPath = null;
	String classesWarcPath = null;

	FSM scxml = null;
	
	ILogService service;

	/**
	 * @wbp.parser.entryPoint
	 */
	public void main(	String scxmlPath, String classesWarcPath, 
						ArrayList<String> classes, ArrayList<String> responsavel, ArrayList<String> colaboradores, 
						ILogService service) {
		this.service = service;
		serviceInfo("comecando!...");
		this.parent = Display.getDefault().getActiveShell();

		this.child = new Shell(this.parent, SWT.CLOSE | SWT.RESIZE);
		this.child.setText("VALVES WARC");
		this.child.setSize(490, 424);
		this.child.setLayout(new GridLayout());
		
		this.scxmlPath = scxmlPath;

		if (this.scxmlPath == null) {
			this.setScxmlPath();
		}
		
		this.classesWarcPath = classesWarcPath;
		
		this.classes = classes;
		this.listaResponsavel = responsavel;
		this.listaColaboradores = colaboradores;

		try {
			ler_scxml();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		serviceInfo(this.classes.toString());

		this.tabFolder = new TabFolder(this.child, SWT.NONE);
		this.view_setar_tab_principal();
		this.view_setar_tab_classes();

		this.child.open();
	}

	void setScxmlPath() {
		FileDialog fd = new FileDialog(this.child, SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath("C:/");
		String[] filterExt = { "*.scxml" };
		fd.setFilterExtensions(filterExt);
		this.scxmlPath = fd.open();
	}

	void ler_scxml() throws ParserConfigurationException, SAXException, IOException {
		Path file_path = Paths.get(this.scxmlPath);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(new File(file_path.toString()));

		NodeList states = dom.getElementsByTagName("state");

		FSM scxml_obj = new FSM();

		for (int i = 0; i < states.getLength(); i++) {
			Node stateNode = states.item(i);
			Element stateElement = (Element) stateNode;

			if (stateNode.hasChildNodes()) {
				String name = stateElement.getAttribute("id");
				ArrayList<Dictionary<String, String>> events = new ArrayList<Dictionary<String, String>>();
				ArrayList<Dictionary<String, String>> transitions = new ArrayList<Dictionary<String, String>>();

				// internal transitions (do / entry)
				NodeList internalTransitions = stateElement.getElementsByTagName("internaltransition");
				for (int j = 0; j < internalTransitions.getLength(); j++) {
					Node internal_transition = internalTransitions.item(j);
					Element internalTransitionElement = (Element) internal_transition;
					String internalTransitionName = internalTransitionElement.getAttribute("event");
					String reaction = internalTransitionElement.getAttribute("guard");
					Dictionary<String, String> internal_transition_obj = new Hashtable<>();
					internal_transition_obj.put("name", internalTransitionName);
					internal_transition_obj.put("reaction", reaction);
					events.add(internal_transition_obj);
				}

				// transitions (action / target)
				NodeList stateTransitions = stateElement.getElementsByTagName("transition");
				// serviceInfo(Integer.toString(stateTransitions.getLength()));
				for (int k = 0; k < stateTransitions.getLength(); k++) {
					Node stateTransition = stateTransitions.item(k);
					Element stateTransitionElement = (Element) stateTransition;
					String action = stateTransitionElement.getAttribute("id");
					String target = stateTransitionElement.getAttribute("target");
					Dictionary<String, String> stateTransitionsObj = new Hashtable<>();
					stateTransitionsObj.put("action", action);
					stateTransitionsObj.put("target", target);
					transitions.add(stateTransitionsObj);
				}
				scxml_obj.addState(new State(name, events, transitions));
			}
		}
		// serviceInfo(scxml_obj.toString());
		// serviceInfo(scxml_obj.states.toString());
		this.scxml = scxml_obj;
	}

	void view_setar_tab_principal() {
		serviceInfo("principal!!!");
		TabItem tab_principal = new TabItem(this.tabFolder, SWT.NONE);
		tab_principal.setText("Principal");

		Composite composite_2 = new Composite(this.tabFolder, SWT.NONE);
		tab_principal.setControl(composite_2);
		composite_2.setLayout(new FormLayout());

		Label label = new Label(composite_2, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label = new FormData();
		fd_label.right = new FormAttachment(100, -10);
		label.setLayoutData(fd_label);
		
		Label lblNewLabel_1 = new Label(composite_2, SWT.NONE);
		fd_label.top = new FormAttachment(lblNewLabel_1, 6);
		fd_label.left = new FormAttachment(lblNewLabel_1, 0, SWT.LEFT);
		
		FormData fd_lblNewLabel_1 = new FormData();
		fd_lblNewLabel_1.bottom = new FormAttachment(0, 35);
		fd_lblNewLabel_1.top = new FormAttachment(0, 10);
		fd_lblNewLabel_1.left = new FormAttachment(0, 10);
		lblNewLabel_1.setLayoutData(fd_lblNewLabel_1);
		lblNewLabel_1.setText("Tabela WARC");
		
		Group grpClassesEWarc = new Group(composite_2, SWT.NONE);
		fd_label.bottom = new FormAttachment(100, -329);
		grpClassesEWarc.setText("FSM");
		FormData fd_grpClassesEWarc = new FormData();
		fd_grpClassesEWarc.right = new FormAttachment(100, -238);
		fd_grpClassesEWarc.left = new FormAttachment(0, 10);
		fd_grpClassesEWarc.bottom = new FormAttachment(100, -240);
		fd_grpClassesEWarc.top = new FormAttachment(label, 6);
		grpClassesEWarc.setLayoutData(fd_grpClassesEWarc);
		
		Group grpClassesEWarc_1 = new Group(composite_2, SWT.NONE);
		grpClassesEWarc_1.setText("Classes e WARC");
		FormData fd_grpClassesEWarc_1 = new FormData();
		fd_grpClassesEWarc_1.bottom = new FormAttachment(100, -151);
		fd_grpClassesEWarc_1.top = new FormAttachment(grpClassesEWarc, 6);
		fd_grpClassesEWarc_1.left = new FormAttachment(label, 0, SWT.LEFT);
		fd_grpClassesEWarc_1.right = new FormAttachment(100, -238);
		
		Button btnNewButton = new Button(grpClassesEWarc, SWT.NONE);
		btnNewButton.setBounds(76, 33, 58, 25);
		btnNewButton.setText("Importar");
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setScxmlPath();

				try {
					ler_scxml();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		grpClassesEWarc_1.setLayoutData(fd_grpClassesEWarc_1);
		
		Button btnImportar = new Button(grpClassesEWarc_1, SWT.NONE);
		btnImportar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				classesWarcPath = getClassesWarcPath();
				
				if(classesWarcPath != null) {
					try {
						importarClassesWarc();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}						
			}
		});
		btnImportar.setText("Importar");
		btnImportar.setBounds(34, 31, 56, 25);
		
		Button btnExportar = new Button(grpClassesEWarc_1, SWT.NONE);
		btnExportar.setBounds(134, 31, 56, 25);
		btnExportar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String exportClassesWarcPath = getClassesWarcPath();
				WarcData warcData = new WarcData(classes, listaResponsavel, listaColaboradores);
				
				try {
					Gson gson = new Gson();
					Writer writer = new FileWriter(exportClassesWarcPath);
					gson.toJson(warcData, writer);
					writer.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		btnExportar.setText("Exportar");
		
		serviceInfo("terminei principal!!!");
	}

	void view_setar_tab_classes() {
		TabItem tab_item = new TabItem(this.tabFolder, SWT.NONE);
		tab_item.setText("Classes");

		tab_item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				serviceInfo("YEEEEEEE");
			}
		});

		Composite tab_item_composite = new Composite(this.tabFolder, SWT.NO_SCROLL);

		tab_item.setControl(tab_item_composite);
		tab_item_composite.setLayout(new GridLayout(1, false));
		
		this.tabelaDeClasses = new Table(tab_item_composite, SWT.BORDER);
		this.tabelaDeClasses.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		this.tabelaDeClasses.setLinesVisible(true);

		TableColumn table_column_one = new TableColumn(this.tabelaDeClasses, SWT.LEFT);
		table_column_one.setText("Classes");
		table_column_one.setWidth(200); // table_column_one.pack();
		
		iniciarListaItemsTabelaDeClasses();

		TableEditor editor = new TableEditor(this.tabelaDeClasses);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		this.tabelaDeClasses.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.count == 2) {
					Rectangle clientArea = tabelaDeClasses.getClientArea();
					Point pt = new Point(event.x, event.y);
					int index = tabelaDeClasses.getTopIndex();
					while (index < tabelaDeClasses.getItemCount()) {
						boolean visible = false;
						TableItem item = tabelaDeClasses.getItem(index);
						for (int i = 0; i < tabelaDeClasses.getColumnCount(); i++) {
							Rectangle rect = item.getBounds(i);
							if (rect.contains(pt)) {
								int column = i;
								Text text = new Text(tabelaDeClasses, SWT.NONE);

								Listener text_listener = (Event e) -> {
									if (e.type == SWT.FocusOut) {
										item.setText(column, text.getText());
										text.dispose();
									} else if (e.type == SWT.Traverse) {
										if (e.detail == SWT.TRAVERSE_RETURN) {
											item.setText(column, text.getText());
										} else if (e.detail == SWT.TRAVERSE_ESCAPE) {
											text.dispose();
											e.doit = false;
										}
									}
								};

								text.addListener(SWT.FocusOut, text_listener);
								text.addListener(SWT.Traverse, text_listener);
								editor.setEditor(text, item, i);
								text.setText(item.getText(i));
								text.selectAll();
								text.setFocus();
								return;
							}
							if (!visible && rect.intersects(clientArea)) {
								visible = true;
							}
							i += 1;
						}
						if (!visible) {
							return;
						}
						index += 1;
					}
				} else {
					// Only one click
				}
			}

		});

		this.tabelaDeClasses.setHeaderVisible(true);
		this.tabelaDeClasses.pack();

		Composite composite = new Composite(tab_item_composite, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Button new_class_button = new Button(composite, SWT.NONE);
		new_class_button.setText("Criar");

		new_class_button.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				addClasseAosItensDaTabela("Nova_Classe");
				attListaDeClasses();
			}
		});

		Button delete_class_button = new Button(composite, SWT.NONE);
		delete_class_button.setText("Apagar Selecionado");

		delete_class_button.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (tabelaDeClasses.getSelection().length > 0) {
					TableItem item_selected = tabelaDeClasses.getSelection()[0];
					removerClasseDosItensDaTabela(item_selected);
				} else {
					System.out.println("No classes to delete.");
				}
			}
		});
	}

				}

									}

}