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
	public void main(String scxmlPath, String classesWarcPath,
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
		serviceInfo("terminei classes!!!");
		this.view_setar_tab_caso_de_uso();
		serviceInfo("terminei caso de uso!!!");
		this.view_setar_tab_warc();
		serviceInfo("terminei setar warc!!!");

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

				if (classesWarcPath != null) {
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

	void view_setar_tab_caso_de_uso() {
		TabItem tab_item = new TabItem(this.tabFolder, SWT.NONE);
		tab_item.setText("Caso de Uso");
		serviceInfo("setar casos de uso");

		Composite tab_item_composite = new Composite(this.tabFolder, SWT.NO_SCROLL);
		FillLayout tab_item_composite_layout = new FillLayout();
		tab_item_composite_layout.type = SWT.HORIZONTAL;
		tab_item_composite.setLayout(tab_item_composite_layout);

		tab_item.setControl(tab_item_composite);

		Table table = new Table(tab_item_composite, SWT.BORDER);
		table.setLinesVisible(true);

		TableColumn table_column_one = new TableColumn(table, SWT.LEFT);
		TableColumn table_column_two = new TableColumn(table, SWT.LEFT);

		table_column_one.setText("Acao");
		table_column_two.setText("Reacao");

		table_column_one.setWidth(200); // table_column_one.pack()
		table_column_two.setWidth(200); // table_column_two.pack()

		this.table_caso_de_uso = table;

		serviceInfo("antes verificar estados");

		for (State state : this.scxml.states) {
			String name = state.name;
			serviceInfo("dentro 1");
			for (Dictionary<String, String> transition : state.transitions) {
				serviceInfo("dentro 2");
				String target_name = transition.get("target");
				State target_state = this.scxml.getStateByName(target_name);
				serviceInfo("dentro 3");
				serviceInfo("dentro 3.8");
				serviceInfo(Integer.toString(target_state.events.size()));
				ArrayList<String> reactions = new ArrayList<String>();
				for (Dictionary<String, String> event : target_state.events) {
					reactions.add(event.get("reaction"));
				}
				serviceInfo("dentro 4");
				TableItem item_one = new TableItem(table, SWT.NONE);
				String joinedReactions = "";
				for (String reaction : reactions) {
					if (joinedReactions == "") {
						joinedReactions = reaction;
					} else {
						joinedReactions = String.join(", ", joinedReactions, reaction);
					}
				}
				serviceInfo("dentro 5");
				item_one.setText(new String[] { transition.get("action"), joinedReactions });
				serviceInfo("dentro 6");
			}
			serviceInfo("dentro 7");
		}

		serviceInfo("depois verificar estados");

		table.setHeaderVisible(true);
		table.pack();
	}

	void view_setar_tab_warc() {
		TabItem tab_item = new TabItem(this.tabFolder, SWT.NONE);
		tab_item.setText("WARC");

		Composite composite = new Composite(this.tabFolder, SWT.NONE);
		tab_item.setControl(composite);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn, new ColumnPixelData(150, true, true));
		tblclmnNewColumn.setText("Caso de Uso");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_1 = tableViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn_1, new ColumnPixelData(150, true, true));
		tblclmnNewColumn_1.setText("Responsavel");

		TableViewerColumn tableViewerColumn_2 = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmnNewColumn_2 = tableViewerColumn_2.getColumn();
		tcl_composite.setColumnData(tblclmnNewColumn_2, new ColumnPixelData(150, true, true));
		tblclmnNewColumn_2.setText("Colaboradores");

		ArrayList<String> casos_de_uso = new ArrayList<String>();
		for (TableItem i : this.table_caso_de_uso.getItems()) {
			casos_de_uso.addAll(Arrays.asList(i.getText(0).split(",")));
			casos_de_uso.addAll(Arrays.asList(i.getText(1).split(",")));
		}

		// removendo itens nulos e vazios da lista
		casos_de_uso.removeAll(Arrays.asList(null, ""));

		serviceInfo("primeiro caso de uso: " + casos_de_uso.get(0));
		serviceInfo("casos de uso: " + casos_de_uso.size());

		for (int i = 0; i < casos_de_uso.size(); i++) {
			if (listaResponsavel.size() < casos_de_uso.size())
				this.listaResponsavel.add("");

			if (listaColaboradores.size() < casos_de_uso.size())
				this.listaColaboradores.add("");
		}

		serviceInfo("responsavel: " + Integer.toString(this.listaResponsavel.size()));
		serviceInfo("colaboradores: " + Integer.toString(this.listaColaboradores.size()));

		for (String caso : casos_de_uso) {
			TableItem tableItem = new TableItem(table, SWT.NONE);

			tableItem.setText(new String[] { caso, "", listaColaboradores.get(casos_de_uso.indexOf(caso)) });

			CCombo combo_resp = new CCombo(table, SWT.NONE);
			combo_resp.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int combo_resp_index = comboResps.indexOf(combo_resp);
					listaResponsavel.set(combo_resp_index, combo_resp.getText());
				}
			});

			this.comboResps.add(combo_resp);
			combo_resp.setText("-");

			setCComboResponsaveis();

			TableEditor editor = new TableEditor(table);
			editor.grabHorizontal = true;
			editor.setEditor(combo_resp, tableItem, 1);

			listaColaboradores.add(tableItem.getText(1));
		}

		serviceInfo("combo resp set");

		Listener table_listener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				Event t_event = event;
				if (event.count == 2) {
					// Create window
					Shell win = new Shell(WTable.this.parent, SWT.CLOSE | SWT.RESIZE);
					win.setText("VALVES WARC - Colaboradores");
					win.setSize(490, 424);
					win.setLayout(new GridLayout());

					Table tableNestedCollaborators = new Table(win, SWT.BORDER);
					tableNestedCollaborators.setLinesVisible(true);

					TableColumn table_column_one = new TableColumn(tableNestedCollaborators, SWT.LEFT);
					table_column_one.setText("Acao");
					table_column_one.setWidth(200); // table_column_one.pack()

					// Treat click on original table
					Rectangle clientArea = table.getClientArea();
					Point pt = new Point(t_event.x, t_event.y);
					int index = table.getTopIndex();

					TableEditor editor = new TableEditor(tableNestedCollaborators);
					editor.horizontalAlignment = SWT.LEFT;
					editor.grabHorizontal = true;

					tableNestedCollaborators.addListener(SWT.MouseDown, new Listener() {
						@Override
						public void handleEvent(Event event) {
							if (event.count == 2) {
								Rectangle clientArea = tableNestedCollaborators.getClientArea();
								Point pt = new Point(event.x, event.y);
								int index = tableNestedCollaborators.getTopIndex();
								while (index < tableNestedCollaborators.getItemCount()) {
									boolean visible = false;
									TableItem item = tableNestedCollaborators.getItem(index);
									for (int i = 0; i < tableNestedCollaborators.getColumnCount(); i++) {
										Rectangle rect = item.getBounds(i);
										if (rect.contains(pt)) {
											int column = i;
											Text text = new Text(tableNestedCollaborators, SWT.NONE);

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

					ArrayList<String> listaNestedCollaborators = new ArrayList<String>();

					while (index < table.getItemCount()) {
						boolean visible = false;
						TableItem item = table.getItem(index);

						for (int i = 0; i < table.getColumnCount(); i++) {
							Rectangle rect = item.getBounds(i);
							if (rect.contains(pt)) {
								int column = i;
								listaNestedCollaborators.addAll(Arrays.asList(item.getText(column).split(",")));
								listaNestedCollaborators.removeAll(Arrays.asList(null, ""));

								break;
							}
							if (!visible && rect.intersects(clientArea)) {
								visible = true;
							}
							i += 1;
						}
						if (!visible) {
							break;
						}
						index += 1;
					}

					for (String nestedCollaborator : listaNestedCollaborators) {
						TableItem item = new TableItem(tableNestedCollaborators, SWT.NONE);
						item.setText(nestedCollaborator);
					}

					Button new_class_button = new Button(win, SWT.NONE);
					new_class_button.setText("Criar");

					Button delete_class_button = new Button(win, SWT.NONE);
					delete_class_button.setText("Apagar Selecionado");

					Button save_collabs_button = new Button(win, SWT.NONE);
					save_collabs_button.setText("Salvar");

					Listener new_class_button_listener = new Listener() {
						@Override
						public void handleEvent(Event event) {
							TableItem item = new TableItem(tableNestedCollaborators, SWT.NONE);
							item.setText("Reacao_Nova");
						}
					};

					Listener delete_class_button_listener = new Listener() {
						@Override
						public void handleEvent(Event event) {
							if (tableNestedCollaborators.getSelection().length >= 0) {
								TableItem item_selected = tableNestedCollaborators.getSelection()[0];
								item_selected.dispose();
							} else {
								System.out.println("No classes to delete.");
							}
						}
					};

					Listener save_collabs_button_listener = new Listener() {
						@Override
						public void handleEvent(Event event) {
							ArrayList<String> collabs = new ArrayList<String>();

							for (TableItem item : tableNestedCollaborators.getItems()) {
								if (item.getText() != "") {
									collabs.add(item.getText());
								}
							}

							Rectangle clientArea = table.getClientArea();
							Point pt = new Point(t_event.x, t_event.y);
							int index = table.getTopIndex();

							while (index < table.getItemCount()) {
								boolean visible = false;
								TableItem item = table.getItem(index);

								for (int i = 0; i < table.getColumnCount(); i++) {
									Rectangle rect = item.getBounds(i);

									if (rect.contains(pt)) {
										int column = i;
										String collaboratorsResult = String.join(",", collabs);
										item.setText(column, collaboratorsResult);
										serviceInfo("listaColaboradores size: "
												+ Integer.toString(listaColaboradores.size()));
										listaColaboradores.set(index, collaboratorsResult);
										serviceInfo("listaColaboradores index: " + Integer.toString(index));
										win.close();
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
						}
					};

					new_class_button.addListener(SWT.MouseDown, new_class_button_listener);
					delete_class_button.addListener(SWT.MouseDown, delete_class_button_listener);
					save_collabs_button.addListener(SWT.MouseDown, save_collabs_button_listener);
					win.open();
				}
			}
		};

		serviceInfo("finished listeners");

		table.addListener(SWT.MouseDown, table_listener);
	}

	String getClassesWarcPath() {
		FileDialog fd = new FileDialog(this.child, SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath("C:/");
		String[] filterExt = { "*.json" };
		fd.setFilterExtensions(filterExt);
		return fd.open();
	}

	void importarClassesWarc() throws ParserConfigurationException, SAXException, IOException {
		serviceInfo("lendo classes warc");

		// esvaziando lista de Classes
		this.classes.clear();

		try {
			Gson gson = new Gson();
			Reader reader = Files.newBufferedReader(Paths.get(this.classesWarcPath));

			// convertendo arquivo JSON para Map
			Map<?, ?> map = gson.fromJson(reader, Map.class);

			for (Map.Entry<?, ?> entry : map.entrySet()) {
				// iniciando a lista de classes
				if (entry.getKey().equals("classes")) {
					for (Object item : (ArrayList<?>) entry.getValue()) {
						this.classes.add((String) item);
					}
					// iniciando a lista que dita o responsavel por cada caso
				} else if (entry.getKey().equals("responsavel")) {
					int i = 0;
					for (Object item : (ArrayList<?>) entry.getValue()) {
						this.listaResponsavel.set(i, (String) item);
						i++;
					}
				} else if (entry.getKey().equals("colaboradores")) {
					int i = 0;
					for (Object colaboradores : (ArrayList<?>) entry.getValue()) {
						ArrayList<String> a = (ArrayList<String>) colaboradores;
						String stringColaboradores = String.join(", ", (ArrayList<String>) colaboradores);
						this.listaColaboradores.set(i, stringColaboradores);
						i++;
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		iniciarListaItemsTabelaDeClasses();

		setCComboResponsaveis();
	}

	void addClasseAosItensDaTabela(String nomeClasse) {
		TableItem item = new TableItem(this.tabelaDeClasses, SWT.NONE);
		item.setText(nomeClasse);
	}

	void removerClasseDosItensDaTabela(TableItem itemDaTabela) {
		itemDaTabela.dispose();

		attListaDeClasses();
	}

	void iniciarListaItemsTabelaDeClasses() {
		// apagando os itens da tabela de classes existente
		for (TableItem item : this.tabelaDeClasses.getItems()) {
			item.dispose();
		}

		// preenchendo os itens da tabela de classes
		for (String nomeClasse : this.classes) {
			addClasseAosItensDaTabela(nomeClasse);
		}
	}

	void attListaDeClasses() {
		this.classes.clear();

		// atualizando lista de classes
		for (TableItem item : this.tabelaDeClasses.getItems()) {
			this.classes.add(item.getText());
		}

		setCComboResponsaveis();
	}

	void setCComboResponsaveis() {
		serviceInfo("setCComboResponsaveis");

		// removendo items existentes
		for (int i = 0; i < this.comboResps.size(); i++) {
			CCombo comboResp = this.comboResps.get(i);
			comboResp.removeAll();

			for (TableItem item : this.tabelaDeClasses.getItems()) {
				comboResp.add(item.getText());
			}

			int responsavelIndex = this.classes.indexOf(this.listaResponsavel.get(i));

			comboResp.select(responsavelIndex);
		}

		// serviceInfo(Integer.toString(this.comboResps.size()));
	}

	void serviceInfo(String text) {
		this.service.info(text);
	}
}