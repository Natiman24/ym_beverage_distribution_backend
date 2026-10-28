package com.YM.Beverage.Distribution.Backend.user.models;

public enum PermissionScope {
    ASSIGNED(1),
    STORE(2),
    ALL(3);

    private final int breadth;

    PermissionScope(int breadth) {
        this.breadth = breadth;
    }

    public PermissionScope broaderOf(PermissionScope other) {
        if (other == null) return this;
        return breadth >= other.breadth ? this : other;
    }

    public PermissionScope limitedTo(PermissionScope maximum) {
        return breadth <= maximum.breadth ? this : maximum;
    }
}
