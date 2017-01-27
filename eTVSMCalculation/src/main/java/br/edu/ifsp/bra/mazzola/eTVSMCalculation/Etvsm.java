package br.edu.ifsp.bra.mazzola.eTVSMCalculation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.util.iterator.ExtendedIterator;

public class Etvsm extends RDFBase {

	private List<OntClass> concepts;
	float normalizadoTVSM[][];
	float simTVSM[][];

	public Etvsm() {
		concepts = getOntClassList();
		
		normalizadoTVSM = leafesNormalize(getLeafes(), getFirstRootOntClass());
		int size = concepts.size();

		System.out.printf("\n\n---VETORES NORMALIZADOS TVSM---");
		for(int i=0; i<size; i++){
			System.out.printf("\nC%02d = {",(i+1));
			for(int j=0; j<size; j++){
				System.out.printf("%.3f, ", normalizadoTVSM[i][j]);				
			}
			System.out.print("}");
		}

		simTVSM = simTVSM(normalizadoTVSM);
		System.out.printf("\n\n---SIMILARIDADE - eTVSM---\n");
		for(int i=0; i<size; i++){
			System.out.println("\n"+concepts.get(i).getURI() +" :");
			for(int j=0; j<size; j++){
				System.out.printf("\t"+concepts.get(j).getURI()+" %.4f \n", simTVSM[i][j]);				
			}
		}
	}

	/**
	 * @param List<OntClass> normalized 
	 * @return List<OntClass> next 
	 */
	public List<OntClass> nextToNormalized (List<OntClass> normalized) {	
		List<OntClass> next = new ArrayList<OntClass>();

		for(OntClass classe : normalized){
			next.addAll(classe.listSuperClasses(true).toList());
		}	

		if(next.size() > 0) return next.stream().distinct().collect(Collectors.toList());

		return null;
	}

	/**
	 *  Metodo para normalizar os conceitos para eTVSM
	 *  Implementado por Mauro Andr� 11/2013
	 *  Recebe matriz para normalizar
	 *  Recebe vetor com conceitos com os proximos a serem normalizados
	 *  Retorna um matriz com os conceitos normalizados conforme eTVSM 
	 */
	public float[][] normalizarTVSM(List<OntClass>proximos,float[][]normalizado){

		int i =0;

		for(OntClass con : concepts){
			System.out.println(con.toString());
			//Check if the concept is in the next			
			if(isLeaf(con)){
				//set 0 in all columns to normalize
				for(int j = 0; j<concepts.size(); j++)
					normalizado[i][j]=0;

				OntClass cla = base.getOntClass(con.toString());
				for (ExtendedIterator<OntClass> c = cla.listSubClasses(true) ; c.hasNext();) {			
					OntClass norm = c.next();
					int j =0;
					for(OntClass col : concepts){
						if(col.equals(norm)){
							for(int l = 0; l<concepts.size(); l++){
								normalizado[i][l]=(normalizado[i][l]+normalizado[j][l]);
							}
						}

						j++;
					}
				}

			}
			i++;
		}
		
		int size = normalizado.length;
		//Normalizando vetor
		for(i=0; i<size; i++){
			double tamL = rowSize(normalizado[i]);
			System.out.printf("\n Tamanho da linha %d = %.3f ",i,tamL);
			for(int j=0; j<size; j++){
				normalizado[i][j]=normalizado[i][j]/(float)tamL;
			}
		}
		System.out.println();
		return normalizado;

	}

	public double rowSize(float[]vet){
		double tam = 0;
		for(int i=0; i<vet.length; i++){
			tam = tam + (float)Math.pow(vet[i],2);
		}
		return Math.sqrt(tam);
	}

	/**
	 *  Metodo para normalizar os conceitos folhas para eTVSM
	 *  Implementado por Mauro Andr� 11/2013
	 *  Recebe vetor com todos os conceitos
	 *  Recebe vetor com conceitos folhas
	 *  Retorna um matriz com os conceitos folhas normalizados conforme eTVSM 
	 */
	public float[][] leafesNormalize(List<OntClass>leafes, OntClass root){
		int size = concepts.size();
		float normalizado[][]= new float [size][size];
		List<OntClass> proximos;

		// first step to leafs
		for(OntClass leaf : leafes){
			int i = concepts.indexOf(leaf);
			for(int j =0; j<size;j++){	
				if(leaf.equals(concepts.get(j)) || checkSuperClasse(leaf,concepts.get(j)))
					normalizado[i][j] = 1;
				else
					normalizado[i][j] = 0;
			}
		}

		System.out.println("first leafs step");
		for(int a=0; a<size; a++){
			System.out.printf("\nC%02d = {",(a+1));
			for(int j=0; j<size; j++){
				System.out.printf("%.0f, ", normalizado[a][j]);				
			}
			System.out.print("}");
		}
		System.out.println();
		
		for(int i=0; i<size; i++){
			double tamL = rowSize(normalizado[i]);
			for(int j=0; j<size; j++){
				normalizado[i][j]=normalizado[i][j]/(float)tamL;
			}
		}

		//Primeiros conceitos n�o folhas a normalizar s�o os pais diretos dos folhas
		proximos = nextToNormalized(leafes);

		while(proximos != null){
			normalizado = normalizarTVSM(proximos,normalizado);
			proximos = nextToNormalized(proximos);
		}				

		return normalizado;
	}

	/**
	 * Metodo eTVSM
	 * Implementado por Mauro Andr� 11/2013
	 * @param Matriz normalizada para eTVSM 
	 * @return Matriz com as similaridades
	 */
	public float[][] simTVSM(float [][] normalizado){

		int size = normalizado.length;
		float[][] sim = new float[size][size];

		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				sim[i][j] = similaridadeCos(normalizado[i], normalizado[j]);
				// sim[i][j] = (somaColuna(normalizado,j)+somaLinha(normalizado,i))/(size*size);											
			}
		}


		return sim;
	}

	/**
	 * Metodo para calcular a similaridade entre dois vetores pelo cosseno entre os mesmos
	 * @param u
	 * @param v
	 * @return
	 */
	private float similaridadeCos(float[] u, float[] v){
		float prod = 0;
		float modU = 0;
		float modV = 0;
		for(int i=0; i<u.length; i++){
			prod += u[i]*v[i];
			modU += Math.pow(u[i], 2);
			modV += Math.pow(v[i], 2);
		}
		modU = (float) Math.sqrt(modU);
		modV = (float) Math.sqrt(modV);
		return prod/(modU*modV);
	}

	public float [][] getSim (){
		return simTVSM;
	}

	public List<OntClass> getConceitos() {		
		return concepts;		
	}

}
