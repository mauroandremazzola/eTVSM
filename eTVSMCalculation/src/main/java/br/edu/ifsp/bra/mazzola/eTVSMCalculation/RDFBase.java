package br.edu.ifsp.bra.mazzola.eTVSMCalculation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

public class RDFBase {

	String owlFile = "C:\\baseRDF\\exemple.owl"; // "C:\\baseRDF\\movie.owl";
	OntModel base = ModelFactory.createOntologyModel();

	public RDFBase(){
		readOwl();
	}

	public RDFBase(String owlPath){
		owlFile = owlPath;
		readOwl();
	}

	private void readOwl() {
		InputStream in = FileManager.get().open(owlFile);
		if (in == null) {
			throw new IllegalArgumentException("\nfile: " + owlFile
					+ " not found!\n");
		}
		base.read(in, "");
	}

	/**
	 * @return List<OntClass>
	 */
	public List<OntClass> getOntClassList() {
		return base.listClasses().toList();
	}

	/**
	 * @return OntClass - First root class
	 */
	public OntClass getFirstRootOntClass() {
		List<OntClass> roots =  OntTools.namedHierarchyRoots(base);
		if(roots != null && roots.size() > 0){
			return OntTools.namedHierarchyRoots(base).get(0);
		}else{
			return null;
		}
	}

	/**
	 * @return count of classes are leaf
	 */
	public int countLeafes() {
		return getLeafes().size();
	}

	/**
	 * @return List<OntClass> all leaf classes
	 */
	public List<OntClass> getLeafes() {
		List<OntClass> leafes = new ArrayList<OntClass>();

		for(OntClass ontClass : getOntClassList()){			
			if(ontClass.listSubClasses(true).toList().size() == 0) leafes.add(ontClass);
		}

		return leafes;
	}

	/**
	 * @param OntClass ontClass
	 * @return true case it's leaf  
	 * @return false case isn't
	 */
	public boolean isLeaf(OntClass ontClass) {
		return ontClass.listSubClasses(true).toList().size() == 0;	
	}

	/**
	 * @param OntClass ontClassA
	 * @param OntClass ontClassB 
	 * @return true - case ontClassB is super class of ontClassA
	 * @return false - case isn't
	 */
	boolean checkSuperClasse(OntClass ontClassA, OntClass ontClassB) {	
		for (ExtendedIterator<OntClass> i = ontClassA.listSuperClasses(); i.hasNext();) {			
			if (i.next().equals(ontClassB)){
				return true;
			}			
		} 
		return false;
	}
}
