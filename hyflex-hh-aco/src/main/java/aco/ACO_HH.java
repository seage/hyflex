package aco;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public class ACO_HH extends HyperHeuristic {

	/* 
	 * Genera caminos de hormiga de largo n confomados por
	 * secuencias de heuristicas que reutilizan la salida de
	 * la ejecuci-n anterior dentro del camino.
	 * Primera soluci-n del camino es una aleatoria en la primera iteraci-n 
	 * y la mejor encontrada en las siguientes iteraciones.
	 * 
	 * Por Jos- Luis N--ez.
	*/
	
	
	/* 
	 * ****************************************************************
	 * 
	 * Parametros y variables principales
	 * 
	 * ****************************************************************
	*/
	
	// Numero de hormigas (contador j)
	int m=1;
	
	// Numero de nodos por camino de hormiga (contador k)
	int n=1;
	
	// Numero de heur-sticas disponibles
	int H;
	
	// Numero de parametros a utilizar (son fijos)
	int P=5;
	
	// Caminos de hormigas [m x n x 2]. 
	// (j,k,0): Indice h de la heur-stica aplicada
	// (j,k,1): Parametro p de la heuristica empleado
	int[][][] caminos;
	
	//Indica el mejor camino encontrado hasta el momento [n x 2]
	// (k,0): heuristica en posicion k
	// (k,1): parametro en posicion k
	int[][] mejorCamino;
	
	//Mejora obtenida por el mejor camino
	double mejoraMejorCamino;
	
	// FEROMOMONA Mejoras de heuristicas en caminos [H x P x N x 2]. Heuristica (h,p) en la posicion n de caminos
	// (h,p,n,0): suma de mejoras
	// (h,p,n,1): llamados de hp en el nivel n
	double mejorasCaminos[][][][];
	
	// Contador de iteraciones del algoritmo
	public int i = 0; 

	//Problema a resolver
	ProblemDomain problema;
	
	// Par-metros de la funci-n de probabilidad
	double alfa=1;
	double beta=1;
		
	/* 
	 * ****************************************************************
	 * 
	 * Parametros y variables secundarios
	 * 
	 * ****************************************************************
	*/
	
	//Variables para la informacion heuristica nu. 
	//[H][P][n] Mejoras encontradas en cada ejecucion de la heuristica (h,p) en el nivel k
	//[h][p][k][0]: suma de mejoras
	//[h][p][k][1]: llamados
	double[][][][] mejoras;
	
	/* Cantidad de soluciones reservadas.
	 * 0: mejor solucion global
	 * 1: mejor solucion iteracion
	 * 2: solucion entrada 
	 * */
	int solReserva = 3;
	
	//[H][P][n] Arreglo con probabilidades de seleccion de la heuristica (h,p) en la pos k
	double[][][] probabilidades;
	
	//FITNESS de cada camino en cada posicion. [m x n]
	double fitness[][];
	
	/* 
	 * ****************************************************************
	 * 
	 * Metodos principales
	 * 
	 * ****************************************************************
	*/
	
	// Metdodo constructor
	public ACO_HH(long seed) {
		super(seed);
	}
	
	// Algoritmo
	public void solve(ProblemDomain problemon) {
		
		// INICIALIZAR
		problema = problemon;
		setParam(5,30,45,0); 
		inicializar();
		
		i=0;// Inicia iteraciones
		
		double newFitness = Double.NEGATIVE_INFINITY;//nueva solucion
		double prevFitness =Double.NEGATIVE_INFINITY;//solucion anterior en el camino
		double iniFitness = Double.NEGATIVE_INFINITY;//solucion inicial del camino
		double minFitness = Double.NEGATIVE_INFINITY;//mejor solucion de la iteracion
		
		//Iteracion del algoritmo
		while(!hasTimeExpired()){ //Verifica fin del tiempo.
			i++;
			
			iniFitness = problema.getFunctionValue(1);//Guarda el fitness de la sol inicial
			minFitness  = Double.POSITIVE_INFINITY; //Inicializa el fitness del minimo de la iteracion
			problema.copySolution(1,2);//Inicializa entrada como el minimo de la iteracion anterior
			
			
			// Construccion de los m caminos
			for(int j=0;j<m;j++){
				
				// Iteracion de la hormiga. Crea un camino de largo n
				for(int k=0;k<n;k++){

					this.selecHeuristica(j,k);//Elige la siguiente heuristica con los valores de probabilidad
					
					// Aplica la heuristica, almacena el resultado 
		   			// y obtiene el valor de la funcion objetivo 
					// con la solucion encontrada
					fitness[j][k] = this.usaHeuristica(j,k);		
					newFitness = fitness[j][k]; 
					
					if(k==0){
						prevFitness = problema.getFunctionValue(solReserva-1);
					}
					else{
						prevFitness = fitness[j][k-1];
					}
					 
					
					actualizaNu(j,k,prevFitness-newFitness);// Actualiza informacion heuristica
					
					//Evalua si la nueva solucion es mejor que la mejor de la iteracion y actualiza
					// Es la mejor distinta de la mejor de la iteracion, ie la segunda mejor o la nueva global
					//if(newFitness < minFitness && newFitness!=iniFitness){
					if(newFitness < minFitness && !problema.compareSolutions(this.getSolucionPos(j, k),2)){
						problema.copySolution(this.getSolucionPos(j, k),1);
						minFitness = newFitness;
					}
					
					// Evalua si la nueva solucion es mejor que la mejor global actual y actualiza
					if(newFitness < problema.getFunctionValue(0)){
						problema.copySolution(this.getSolucionPos(j, k),0);
						hasTimeExpired();
					}
				}

				//Evalua si el camino es mejor que el actual y actualiza
				if(iniFitness-newFitness > mejoraMejorCamino){
					for(int k =0;k<n;k++){
						mejorCamino[k][0]=caminos[j][k][0];
						mejorCamino[k][1]=caminos[j][k][1];
						mejoraMejorCamino = iniFitness-newFitness;
					}
				}
				
			}
			actualizaFeromona(iniFitness); // Actualiza feromonas en los componentes de soluciones usados
			
			actualizaProbabilidades();// Actualiza probabilidades despues de actualizar feromona y nu
		}
	}

	/* Selecciona la proxima heuristica a emplear en el camino segun la funcion de probabilidad.
	 * Almacena el resultado en 
	 * caminos[j][k][0]: h 
	 * caminos[j][k][1]: p
	*/
	void selecHeuristica(int j,int k){
		double selector = Math.random();
		
		for(int h=0;h<H;h++){
			for(int p=0;p<P;p++){
				if(selector< probabilidades[h][p][k]/probabilidades[H-1][P-1][k]){
					caminos[j][k][0]=h;
					caminos[j][k][1]=p;
					return;
				}
			}
		}
	}
	/*
	 * Actualiza probabilidades en cada posicion
	 * La division se hace al seleccionar la heuristica
	 * */
	void actualizaProbabilidades(){
		double factorFeromona = 0;
		double factorNu = 0;
		for(int k =0;k<n;k++){
			//Calcula probabilidades
			factorFeromona = Math.pow(feromona(0,0,k),alfa);
			factorNu = Math.pow(nu(0,0,k),beta);
			probabilidades[0][0][k] = factorFeromona * factorNu;
			
			for(int p=1;p<P;p++){
				factorFeromona = Math.pow(feromona(0,p,k),alfa);
				factorNu = Math.pow(nu(0,p,k),beta);
				probabilidades[0][p][k] =probabilidades[0][p-1][k] + factorFeromona * factorNu;
			}
			
			for(int h=1;h<H;h++){
				factorFeromona = Math.pow(feromona(h,0,k),alfa);
				factorNu = Math.pow(nu(h,0,k),beta);
				probabilidades[h][0][k] =probabilidades[h-1][P-1][k] + factorFeromona * factorNu;
				
				for(int p=1;p<P;p++){
					factorFeromona = Math.pow(feromona(h,p,k),alfa);
					factorNu = Math.pow(nu(h,p,k),beta);
					probabilidades[h][p][k] = probabilidades[h][p-1][k] + factorFeromona * factorNu;
				}
			}
		}
	}
	/*Calcula la feromona*/
	private double feromona(int h, int p, int k) {
		double minMejora = Double.POSITIVE_INFINITY;
		double aux;
		
		for(int hh=0;hh<H;hh++){
			for(int pp=0;pp<P;pp++){
				if(mejorasCaminos[hh][pp][k][1]!=0){
					aux = mejorasCaminos[hh][pp][k][0]/mejorasCaminos[hh][pp][k][1];
				}
				else{
					aux = 0;
				}
				if(aux<minMejora){
					minMejora=aux;
				}
			}
		}
		//Calcula diferencia para llegar a 1 y luego escalar
		aux = 1 - minMejora;
		
		if(mejorasCaminos[h][p][k][1]!=0){
			return aux + mejorasCaminos[h][p][k][0]/mejorasCaminos[h][p][k][1];
		}
		else{
			return aux;
		}
		
	}

	/*
	 * Entrega la informacion heuristica.
	 * */
	private double nu(int h, int p,int k){
		double minMejora = Double.POSITIVE_INFINITY;
		double aux;
		
		for(int hh=0;hh<H;hh++){
			for(int pp=0;pp<P;pp++){
				if(mejoras[hh][pp][k][1]!=0){
					aux = mejoras[hh][pp][k][0]/mejoras[hh][pp][k][1];
				}
				else{
					aux = 0;
				}
				if(aux<minMejora){
					minMejora=aux;
				}
			}
		}
		//Calcula diferencia para llegar a 1 y luego escalar
		aux = 1 - minMejora;
		
		if(mejoras[h][p][k][1]!=0){
			return aux + mejoras[h][p][k][0]/mejoras[h][p][k][1];
		}
		else{
			return aux;
		}
		
	}

	/* Aplica la heuristica seleccionada con la solucion anterior, guarda la solucion 
	 * y devuelve su valor en la funcion objetivo
	 * j: camino donde se usa el componente
	 * k: posicion en el camino donde se usa
	 * h: heuristica seleccionada
	 * p: parametro seleccionado
	 * */
	double usaHeuristica(int j,int k){
		
		//Define los parametros indistintamente para la heuristica
		problema.setDepthOfSearch(this.getParametro(caminos[j][k][1]));
		problema.setIntensityOfMutation(caminos[j][k][1]);
		
		int solucEntrada;
		if(k==0){
			solucEntrada = solReserva-1;
		}
		else{
			solucEntrada = j+solReserva;
		}
				
		return problema.applyHeuristic(caminos[j][k][0],solucEntrada, j+solReserva);
	}

	/* Actualiza el valor de las feromonas en los caminos generados
	 * */
	void actualizaFeromona(double iniFitness) {
		double newFitness;
		double deltaFitness;
		
		for(int j=0;j<m;j++){//Para todos los caminos
			
			newFitness = problema.getFunctionValue(this.getSolucionPos(j, n-1));//Toma la ultima solucion del camino
			deltaFitness = iniFitness - newFitness;
			for(int k=0;k<n;k++){//Para todas las posiciones
				mejorasCaminos[caminos[j][k][0]][caminos[j][k][1]][k][0] += deltaFitness;//Suma mejora 
				mejorasCaminos[caminos[j][k][0]][caminos[j][k][1]][k][1] ++;// Suma contador
			}
		}
		
		for(int k=0;k<n;k++){//Para todas las posiciones en mejor solucion
			mejorasCaminos[mejorCamino[k][0]][mejorCamino[k][1]][k][0] += mejoraMejorCamino;//Suma mejora 
			mejorasCaminos[mejorCamino[k][0]][mejorCamino[k][1]][k][1] ++;// Suma contador
		}

		
	}
	
	/* Actualiza la informacion heuristica del componente j,k utilizado
	 * */
	void actualizaNu(int j, int k,double mejora) {
		int h = caminos[j][k][0];
		int p=  caminos[j][k][1];	
		mejoras[h][p][k][0] += mejora;
		mejoras[h][p][k][1]++;
	}
	
	//  Crea e inicializa variables
	//public void inicializar(int mm, int nn,double alfalfa,double betasta){
	public void inicializar(){
		
		fitness = new double[m][n];
		// Carga numero de heuristicas
		H = problema.getNumberOfHeuristics();
		// Inicia numero de soluciones
		problema.setMemorySize(m+solReserva);
		// Inicializa variable que contiene los caminos
		iniCaminos();
		//Inicializa mejor camino
		mejorCamino = new int[n][2];
		//Inicializa mejora del mejor camino
		mejoraMejorCamino = Double.NEGATIVE_INFINITY;
		// Inicializa valores de feromonas de cada componente (heuristica)
		iniFeromonas();
		// Crea la primera solucion a evaluar en los caminos
		problema.initialiseSolution(0);
		for(int s=1;s<this.solReserva;s++){
			problema.copySolution(0,s);
		}
		// Inicializa parametros de la funcion de probabilidad
		//alfa = alfalfa;
		//beta = betasta;
		//Inicializa variables usados para calcular informacion heuristica
		iniNu();
		//Inicializa arreglo con probabilidades de seleccion
		probabilidades = new double[H][P][n];
		actualizaProbabilidades();// Inicializa probabilidades
	}
	
	// Inicializa valores de feromonas
	// La probabilidad de elegir debe ser uniforme inicialmente
	void iniFeromonas(){
		mejorasCaminos = new double[H][P][n][2];
		for(int h=0;h<H;h++){
			for(int p=0;p<P;p++){
				for(int k=0;k<n;k++){
					mejorasCaminos[h][p][k][0] = 0;//No hay mejoras positivas ni negativas
					mejorasCaminos[h][p][k][1] = 0;//No hay ejecuciones
				}
			}
		}
	}
	
	/* Inicializa valores empleados en la funcion heuristica
	 * La probabilidad de elegir debe ser uniforme inicialmente
	 * */
	void iniNu(){
		mejoras = new double[H][P][n][2];
		
		for(int h=0;h<H;h++){
			for(int p=0;p<P;p++){
				for(int k=0;k<n;k++){
					mejoras[h][p][k][0]=0;//No hay mejoras positivas ni negativas
					mejoras[h][p][k][1]=0;//No hay ejecuciones
				}
			}
		}
	}
	/* 
	 * ****************************************************************
	 * 
	 * Metodos secundarios
	 * 
	 * ****************************************************************
	*/
	
	//	Inicializa variable para los caminos (limpia)
	void iniCaminos(){
		caminos = new int[m][n][2];
		for(int j=0;j<m;j++){
			for(int k=0;k<n;k++){
				caminos[j][k][0]=-1;// No hay heuristica
				caminos[j][k][1]=-1;// No hay parametro
			}
		}
	}
	
	// Posicion de la solucion parcial dentro del camino.
	int getSolucionPos(int j,int k){return j+solReserva;}
	
	/* Entrega el parametro en pos. 
	 * Son 5 valores entre [0.2 , 1]
	 * */
	double getParametro(int pos){
		if(pos<=0)return 0.2;
		else if(pos == 1)return 0.4;
		else if(pos == 2)return 0.6;
		else if(pos == 3)return 0.8;
		else return 1;
	}
	
	// Entrega string representativo.
	public String toString() {
		return "";
	}
	
	public void setParam(int n,int m, double alfa, double beta){
		this.alfa = alfa;
		this.beta = beta;
		this.m = m;
		this.n = n;
	}
}
