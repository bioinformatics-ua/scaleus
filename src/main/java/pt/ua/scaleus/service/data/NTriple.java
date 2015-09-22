/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.service.data;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class NTriple {

    String s;
    String p;
    String o;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    @Override
    public String toString() {
        return "Triple [" + s + " " + p + " " + o + "]";
    }
}
