package fr.istic.prg1.list;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import fr.istic.prg1.list_util.Comparison;
import fr.istic.prg1.list_util.Iterator;
import fr.istic.prg1.list_util.List;
import fr.istic.prg1.list_util.SmallSet;

/**
 * @author Jérôme Trébern <jerome.trebern@etudiant.univ-rennes1.fr>
 * @version 1.0
 * @since 2018-10-02
 */

public class MySet extends List<SubSet> {

	/**
	 * Borne supérieure pour les rangs des sous-ensembles.
	 */
	private static final int MAX_RANG = 128;
	/**
	 * Sous-ensemble de rang maximal à mettre dans le drapeau de la liste.
	 */
	private static final SubSet FLAG_VALUE = new SubSet(MAX_RANG,
			new SmallSet());
	/**
	 * Entrée standard.
	 */
	private static final Scanner standardInput = new Scanner(System.in);

	public MySet() {
		super();
		setFlag(FLAG_VALUE);
	}

	/**
	 * Fermer tout (actuellement juste l'entrée standard).
	 */
	public static void closeAll() {
		standardInput.close();
	}

	@SuppressWarnings("unused")
	private static Comparison compare(int a, int b) {
		if (a < b) {
			return Comparison.INF;
		} else if (a == b) {
			return Comparison.EGAL;
		} else {
			return Comparison.SUP;
		}
	}

	/**
	 * Afficher à l'écran les entiers appartenant à this, dix entiers par ligne
	 * d'écran.
	 */
	public void print() {
		//System.out.println(" [version corrigee de contenu]");
		this.print(System.out);
	}

	// //////////////////////////////////////////////////////////////////////////////
	// //////////// Appartenance, Ajout, Suppression, Cardinal
	// ////////////////////
	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * Ajouter à this toutes les valeurs saisies par l'utilisateur et afficher
	 * le nouveau contenu (arrêt par lecture de -1).
	 */
	public void add() {
		System.out.println(" valeurs a ajouter (-1 pour finir) : ");
		this.add(System.in);
		System.out.println(" nouveau contenu :");
		this.printNewState();
	}

	/**
	 * Ajouter à this toutes les valeurs prises dans is.
	 * C'est une fonction auxiliaire pour add() et restore().
	 * 
	 * @param is
	 *            flux d'entrée.
	 */
	public void add(InputStream is) {
		
		Scanner sc = new Scanner(is);
		int nb = sc.nextInt();
		while (nb != -1) {
			this.addNumber(nb);
			nb = sc.nextInt();
		}
		sc.close();
	}

	/**
	 * Ajouter value à this.
	 * 
	 * @param value
	 *            valuer à ajouter.
	 */
	public void addNumber(int value) {
		
		int rang = value/256;
		int val = value%256;
		boolean bool = false;
		if (value > 32767) bool = true;
		Iterator<SubSet> it = iterator();
		
		while(!bool && !it.isOnFlag()) {
			if(it.getValue().rank == rang) {
				it.getValue().set.add(val);
				bool = true;
			}else if(it.getValue().rank < rang) {
				it.goForward();
				if(it.getValue().rank == rang) {
					it.getValue().set.add(val);
					bool = true;
				}else if(it.getValue().rank > rang) {
					it.goBackward();
				}
			}else { //if(it.getValue().rank > rang)
				SmallSet s = new SmallSet();
				s.add(val);
				it.addLeft(new SubSet(rang, s));
				bool = true;
			}
			it.goForward();
		}
		if (!bool) {
			//it.goBackward();
			SmallSet s = new SmallSet();
			s.add(val);
			it.addLeft(new SubSet(rang, s));
		}
	}

	/**
	 * Supprimer de this toutes les valeurs saisies par l'utilisateur et
	 * afficher le nouveau contenu (arrêt par lecture de -1).
	 */
	public void remove() {
		System.out.println("  valeurs a supprimer (-1 pour finir) : ");
		this.remove(System.in);
		System.out.println(" nouveau contenu :");
		this.printNewState();
	}

	/**
	 * Supprimer de this toutes les valeurs prises dans is.
	 * 
	 * @param is
	 *            flux d'entrée
	 */
	public void remove(InputStream is) {
		
		Scanner sc = new Scanner(is);
		int nb = sc.nextInt();
		while (nb != -1) {
			this.removeNumber(nb);
			nb = sc.nextInt();
		}
		sc.close();
	}

	/**
	 * Supprimer value de this.
	 * 
	 * @param value
	 *            valeur à supprimer
	 */
	public void removeNumber(int value) {
		
		int rang = value/256;
		int val = value%256;
		boolean bool = false;
		Iterator<SubSet> it = iterator();
		
		if (value > 32767) bool = true;
		
		while(!bool && !it.isOnFlag()) {
			if(it.getValue().rank == rang) {
				it.getValue().set.remove(val);
				if(it.getValue().set.size() == 0) it.remove();
				bool = true;
			}
			it.goForward();
		}
	}

	/**
	 * @return taille de l'ensemble this
	 */
	public int size() {
		
		Iterator<SubSet> it = iterator();
    	int i = 0;
    	while(!it.isOnFlag()) {
        	i += it.getValue().set.size();
        	it.goForward();
    	}
    	return i;
	}


	/**
	 * @return true si le nombre saisi par l'utilisateur appartient à this,
	 *         false sinon
	 */
	public boolean contains() {
		System.out.println(" valeur cherchee : ");
		int value = readValue(standardInput, 0);
		return this.contains(value);
	}

	/**
	 * @param value
	 *            valeur à tester
	 * @return true si valeur appartient à l'ensemble, false sinon
	 */

	public boolean contains(int value) {
		
		int rang = value/256;
		int val = value%256;
		Iterator<SubSet> it = iterator();
		
		while(it.getValue().rank <= rang && !it.isOnFlag()) {
			if(it.getValue().rank == rang) return it.getValue().set.contains(val);
			it.goForward();
		}
		
		return false;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// /////// Difference, DifferenceSymetrique, Intersection, Union ///////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * This devient la différence de this et set2.
	 * 
	 * @param set2
	 *            deuxième ensemble
	 */
	public void difference(MySet set2) { // TODO
   	 	
    	Iterator<SubSet> ita = this.iterator();
    	Iterator<SubSet> itb = set2.iterator();	
    	if(this==set2) {
    		while(!ita.isOnFlag()) {
    			itb.goForward();
    			ita.remove();	
    	}}
    	
    	while(!ita.isOnFlag() || !itb.isOnFlag()){
    		System.out.println("\nA | rang : "+ita.getValue().rank);
    		System.out.println("B | rang : "+itb.getValue().rank);
            if (ita.getValue().rank == itb.getValue().rank){ // SI rankA = rankB  
            	ita.getValue().set.difference(itb.getValue().set); //Alors difference()
            	if(ita.getValue().set.isEmpty()) { // SI A est devenu vide alors on le supprime
            		
           				ita.remove();
            	}else {
           			ita.goForward();
           			itb.goForward();
           		}
            }
            else if (ita.getValue().rank < itb.getValue().rank) ita.goForward();
    		else if (ita.getValue().rank > itb.getValue().rank) itb.goForward();
           
    	}
	}

	/**
	 * This devient la différence symétrique de this et set2.
	 * 
	 * @param set2
	 *            deuxième ensemble
	 */
	public void symmetricDifference(MySet set2) {//TODO
		Iterator<SubSet> ita = iterator();
		Iterator<SubSet> itb = set2.iterator();
//		
//		while(!ita.isOnFlag() && !itb.isOnFlag()){
//            if (ita.getValue().rank == itb.getValue().rank){ // SI rankA = rankB  
//            	ita.getValue().set.symmetricDifference(itb.getValue().set); //Alors difference()
//            	if(ita.getValue().set.isEmpty()) { // SI A est devenu vide alors on le supprime
//           				ita.remove();
//            	}else {
//           			ita.goForward();
//           			itb.goForward();
//           		}
//            }
//            else if (ita.getValue().rank < itb.getValue().rank) ita.goForward();
//    		else if (ita.getValue().rank > itb.getValue().rank) {
//    			ita.addLeft(itb.getValue().clone());
//    			itb.goForward();
//    		}
//           
//    	}
		MySet inter=new MySet();
		Iterator<SubSet> iti = inter.iterator();
		while(!ita.isOnFlag()) {
			iti.addRight(ita.getValue().clone());
			ita.goForward();
		}
		MySet this1 = this;
		MySet this2 = set2;
		inter.intersection(this2);
		this1.difference(inter);
		this2.difference(inter);
		this1.union(this2);
		
	}	

	/**
	 * This devient l'intersection de this et set2.
	 * 
	 * @param set2
	 *            deuxième ensemble
	 */
public void intersection(MySet set2) {
		
		Iterator<SubSet> ita = iterator();
		Iterator<SubSet> itb = set2.iterator();
		
		while(!ita.isOnFlag()) {
			if(ita.getValue().rank == itb.getValue().rank) {
				ita.getValue().set.intersection(itb.getValue().set);
				//System.out.println(ita.getValue().set.isEmpty());
				if (ita.getValue().set.isEmpty()) ita.remove();
				else ita.goForward();
				
				itb.goForward();
			}
			else if (ita.getValue().rank < itb.getValue().rank) ita.remove();
			else if (ita.getValue().rank > itb.getValue().rank) itb.goForward();
			if(itb.isOnFlag() && !ita.isOnFlag()) ita.remove();
		}	
	}


	/**
	 * This devient l'union de this et set2.
	 * 
	 * @param set2
	 *            deuxième ensemble
	 */
	public void union(MySet set2) {
		
    	Iterator<SubSet> ita = iterator();
    	Iterator<SubSet> itb = set2.iterator();
   	 
    	while( !itb.isOnFlag()) {
        	if(ita.getValue().rank > itb.getValue().rank) {
            	ita.addLeft(itb.getValue().clone());
            	ita.goForward();
            	itb.goForward();
        	}else if(ita.getValue().rank == itb.getValue().rank) {
            	ita.getValue().set.union(itb.getValue().set);
            	ita.goForward();
            	itb.goForward();
        	}else if(ita.getValue().rank < itb.getValue().rank) {
            	ita.goForward();
        	}
        }
	}


	// /////////////////////////////////////////////////////////////////////////////
	// /////////////////// Egalitï¿½, Inclusion ////////////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * @param o
	 *            deuxième ensemble
	 * 
	 * @return true si les ensembles this et o sont égaux, false sinon
	 */
	@Override
	public boolean equals(Object o) {
		boolean b = true;
		if (this == o) {
			b = true;
		} else if (o == null) {
			b = false;
		} else if (!(o instanceof MySet)) {
			b = false;
		} else {
			Iterator<SubSet> ita = iterator();
			Iterator<SubSet> itb = ((List<SubSet>) o).iterator();
			boolean bool = true;
			while (!ita.isOnFlag() && bool) {
				if (ita.getValue().rank == itb.getValue().rank) {
					if (ita.getValue().set.size() == ita.getValue().set.size())
						bool = (ita.getValue().set.toString().equals(itb.getValue().set.toString()));
				}else bool = false;
				ita.goForward();
				itb.goForward();
			}
			b = bool && ita.isOnFlag() && itb.isOnFlag();
		}
		return b;
	}

	/**
	 * @param set2
	 *            deuxième ensemble
	 * @return true si this est inclus dans set2, false sinon
	 */
	public boolean isIncludedIn(MySet set2) { //TODO
		Iterator<SubSet> ita = iterator();
		Iterator<SubSet> itb = set2.iterator();
		boolean bool = false;
		if(this.size()>set2.size()) return false;
		while(!ita.isOnFlag() && !itb.isOnFlag()) {
			while(!itb.isOnFlag()) {
				if (ita.getValue().rank == itb.getValue().rank) 
					bool = (bool || ita.getValue().set.isIncludedIn(itb.getValue().set));
				if (bool == false) return bool;
			itb.goForward();
			}
			itb.restart();
			ita.goForward();
		}
		
		return bool;
	}

	// /////////////////////////////////////////////////////////////////////////////
	// //////// Rangs, Restauration, Sauvegarde, Affichage //////////////
	// /////////////////////////////////////////////////////////////////////////////

	/**
	 * Afficher les rangs présents dans this.
	 */
	public void printRanks() {
		System.out.println(" [version corrigee de rangs]");
		this.printRanksAux();
	}

	private void printRanksAux() {
		int count = 0;
		System.out.println(" Rangs presents :");
		Iterator<SubSet> it = this.iterator();
		while (!it.isOnFlag()) {
			System.out.print("" + it.getValue().rank + "  ");
			count = count + 1;
			if (count == 10) {
				System.out.println();
				count = 0;
			}
			it.goForward();
		}
		if (count > 0) {
			System.out.println();
		}
	}

	/**
	 * Créer this à partir d'un fichier choisi par l'utilisateur contenant une
	 * séquence d'entiers positifs terminée par -1 (cf f0.ens, f1.ens, f2.ens,
	 * f3.ens et f4.ens).
	 */
	public void restore() {
		String fileName = readFileName();
		InputStream inFile;
		try {
			inFile = new FileInputStream(fileName);
			System.out.println(" [version corrigee de restauration]");
			this.clear();
			this.add(inFile);
			inFile.close();
			System.out.println(" nouveau contenu :");
			this.printNewState();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("fichier " + fileName + " inexistant");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("probleme de fermeture du fichier " + fileName);
		}
	}

	/**
	 * Sauvegarder this dans un fichier d'entiers positifs terminé par -1.
	 */
	public void save() {
		System.out.println(" [version corrigee de sauvegarde]");
		OutputStream outFile;
		try {
			outFile = new FileOutputStream(readFileName());
			this.print(outFile);
			outFile.write("-1\n".getBytes());
			outFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("pb ouverture fichier lors de la sauvegarde");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("probleme de fermeture du fichier");
		}
	}

	/**
	 * @return l'ensemble this sous forme de chaîne de caractères.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		int count = 0;
		SubSet subSet;
		int startValue;
		Iterator<SubSet> it = this.iterator();
		while (!it.isOnFlag()) {
			subSet = it.getValue();
			startValue = subSet.rank * 256;
			for (int i = 0; i < 256; ++i) {
				if (subSet.set.contains(i)) {
					String number = String.valueOf(startValue + i);
					int numberLength = number.length();
					for (int j = 6; j > numberLength; --j) {
						number += " ";
					}
					result.append(number);
					++count;
					if (count == 10) {
						result.append("\n");
						count = 0;
					}
				}
			}
			it.goForward();
		}
		if (count > 0) {
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Imprimer this dans outFile.
	 * 
	 * @param outFile
	 *            flux de sortie
	 */
	private void print(OutputStream outFile) {
		try {
			String string = this.toString();
			outFile.write(string.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Afficher l'ensemble avec sa taille et les rangs présents.
	 */
	private void printNewState() {
		this.print(System.out);
		System.out.println(" Nombre d'elements : " + this.size());
		this.printRanksAux();
	}

	/**
	 * @param scanner
	 * @param min
	 *            valeur minimale possible
	 * @return l'entier lu au clavier (doit Ãªtre entre min et 32767)
	 */
	private static int readValue(Scanner scanner, int min) {
		int value = scanner.nextInt();
		while (value < min || value > 32767) {
			System.out.println("valeur incorrecte");
			value = scanner.nextInt();
		}
		return value;
	}

	/**
	 * @return nom de fichier saisi psar l'utilisateur
	 */
	private static String readFileName() {
		System.out.print(" nom du fichier : ");
		String fileName = standardInput.next();
		return fileName;
	}
}



