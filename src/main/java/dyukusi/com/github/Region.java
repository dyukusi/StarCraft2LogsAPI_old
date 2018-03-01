package dyukusi.com.github;

public enum Region {
    Americas(1),
    Europe(2),
    Asia(3);

    private int id;
    Region(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}

