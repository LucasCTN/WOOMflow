package org.modelio.warc.command;

import java.util.ArrayList;

public class WarcData {
	public ArrayList<String> classes;
	public ArrayList<String> responsavel;
	public ArrayList<String[]> colaboradores;
	
	public WarcData(ArrayList<String> classes, 
					ArrayList<String> responsavel, 
					ArrayList<String> listaColaboradores){
		this.classes = classes;
		this.responsavel = responsavel;
		this.colaboradores = new ArrayList<String[]>();
		
		for(String colaboradores : listaColaboradores) {
			this.colaboradores.add(colaboradores.split(","));
		}
	}
}
