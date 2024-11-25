package tech.lin2j.idea.plugin.enums;

public enum TransferMode {

    SFTP(0, "SFTP"),
    SCP(1, "SCP"),
    ;

    private final int type;
    private final String text;

    TransferMode(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public static TransferMode getByType(int type) {
        for (TransferMode value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return SFTP;
    }

    public Integer getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
