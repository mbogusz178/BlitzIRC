package org.schumiwildeprojects.kck1.backend;

public enum ConnectionState {
    SERVER_TIMEOUT("Połączenie zostało przerwane. Sprawdź poprawność nazwy serwera."),
    SERVER_NAME_EMPTY("Brak nazwy serwera. Wpisz adres serwera."),
    FIELDS_NOT_FILLED("Jedno lub więcej pól jest puste. Wypełnij puste pola."),
    USERNAME_EXISTS("Taki użytkownik już istnieje."),
    SUCCESSFUL(null),
    INVALID_NICKNAME("Nazwa użytkownika może zawierać tylko małe i wielkie litery, cyfry, oraz następujące znaki:\n- _ [ ] { } \\ ` |");

    private final String msg;

    ConnectionState(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
