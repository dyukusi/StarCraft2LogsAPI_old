package dyukusi.com.github;

public enum Race {
    Terran(1),
    Zerg(2),
    Protoss(3),
    Random(4),
    Null(99);

    private int id;
    Race(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
