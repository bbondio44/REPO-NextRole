package com.nextrole.skillgap;
/** No evaluada != nivel cero: se conserva la diferencia en el cálculo y en el DTO. */
public final class GapPolicy {
    private GapPolicy() {}
    public static Integer amount(Integer current,int required){ return current==null?null:Math.max(0,required-current); }
    public static String status(Integer current,int required){
        Integer gap=amount(current,required);
        if(gap==null)return "no_evaluada";
        if(gap==0)return "cubierta";
        return current==0?"faltante":"parcial";
    }
    public static String priority(Integer current,int required,boolean essential){
        Integer gap=amount(current,required);
        if(gap==null)return "pendiente";
        if(gap==0)return "baja";
        if(essential&&gap>=2)return "alta";
        if(essential||gap>=2)return "media";
        return "baja";
    }
}
