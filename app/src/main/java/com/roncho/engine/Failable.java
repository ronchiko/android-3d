package com.roncho.engine;

public interface Failable {
    void failed();
    boolean isFailure();
}
