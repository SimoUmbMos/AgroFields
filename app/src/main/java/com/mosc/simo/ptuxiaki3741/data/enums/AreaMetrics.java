package com.mosc.simo.ptuxiaki3741.data.enums;

public enum AreaMetrics {
    SquareFoot(10.76391041671),
    SquareYard(1.1959900463011),
    SquareMeter(1.0),
    Stremma(0.001),
    Hectare(0.0001),
    Acres(0.00024710538146717),
    SquareKiloMeter(1.0E-6),
    SquareMile(3.8610215854245e-7);

    public final double dimensionToSquareMeter;
    AreaMetrics(double dimensionToSquareMeter){
        this.dimensionToSquareMeter = dimensionToSquareMeter;
    }
}
