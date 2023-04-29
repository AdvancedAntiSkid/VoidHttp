package net.voidhttp.config;

public enum Flag {
    NO_SERVER_NAME(0x000001);

    private final int id;

    Flag(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
