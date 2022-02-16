/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kubalik;

/**
 *
 * @author Kubalik
 */
public class EvoCOPActionParams implements Cloneable{

    public int id1;         // id prvni LLH
    public int id2;         // id druhe LLH, ktera se ma prohazovat
                            // nebo udavaj, o kolik se LLH posune,
                            // nebo na jakou pozici bude nova akce umistena
    // parametry LLH
    public int name;
    public double depthOfSearch;
    public double intensityOfMutation;
    public int typeLLH;

    // pro akci addLLH
    public void HyFlexActionParams(int id1, int type, int name, double dos, double iom, int id2){
        this.id1 = id1;             // id nove LLH
        this.typeLLH = type;
        this.name = name;
        this.depthOfSearch = dos;
        this.intensityOfMutation = iom;
        this.id2 = id2;             // pozice, kde bude umistena
    }

    // pro akci removeLLH
    public void HyFlexActionParams(int id1){
        this.id1 = id1; // id odstranovane LLH
    }

    // pro akce swapLLH a moveLLH
    public void HyFlexActionParams(int id1, int id2){
        this.id1 = id1;     // id prvni prohazovane LLH (swapLLH), id posouvane LLH (moveLLH)
        this.id2 = id2;     // id druhe prohazovane LLH (swapLLH), hodnota o kolik se ma LLH posunout (+/-)
    }

    // pro akci changeLLH
    public void HyFlexActionParams(int id1, int type, int name, double dos, double iom){
        this.id1 = id1;                 // id menene LLH
        this.typeLLH = type;
        this.name = name;               // nove jmeno LLH
        this.depthOfSearch = dos;
        this.intensityOfMutation = iom;
    }

    protected Object clone(){
        EvoCOPActionParams pom = null;
        try{
            pom = (EvoCOPActionParams)super.clone();
            pom.name = this.name;
            pom.id1 = this.id1;
            pom.id2 = this.id2;
            pom.depthOfSearch = this.depthOfSearch;
            pom.intensityOfMutation = this.intensityOfMutation;
            pom.typeLLH = this.typeLLH;
        }
        catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return pom;
    }
}
