package com.example.weteams.logic;

public interface Callbacks<T> {
    void onSuccess(T value);
    void onFailure(Exception e);
}
