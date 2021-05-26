package com.mosc.simo.ptuxiaki3741.interfaces;

public interface OnPermissionResult {
    default void permissionGranted() {}
    default void permissionDenied() {}
}
