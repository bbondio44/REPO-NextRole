package com.nextrole.matching;
/** Reglas deterministas del modelo inicial (sin evidencia externa, que es opcional). */
public final class Scoring {
    private Scoring() {}
    public static Integer affinity(Integer interests,Integer competencies) {
        double weighted=0,weights=0;
        if(interests!=null){weighted+=50.0*interests;weights+=50.0;}
        if(competencies!=null){weighted+=30.0*competencies;weights+=30.0;}
        return weights==0?null:(int)Math.round(weighted/weights);
    }
    public static Integer percent(double numerator,double denominator) {
        return denominator<=0?null:(int)Math.round(100.0*numerator/denominator);
    }
    public static int confidence(Integer coverage,boolean hasInterests) {
        return (int)Math.round((coverage==null?0:coverage)*0.7+(hasInterests?30:0));
    }
}
