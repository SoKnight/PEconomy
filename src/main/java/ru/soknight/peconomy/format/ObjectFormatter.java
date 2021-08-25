package ru.soknight.peconomy.format;

@FunctionalInterface
public interface ObjectFormatter<T> {

    String format(T object);

}
