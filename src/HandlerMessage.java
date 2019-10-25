public class HandlerMessage {
    String type;
    String[] data;

    HandlerMessage(String type, String[] data) {
        this.type = type;
        this.data = data;
    }

    String join() {
        StringBuilder joiner = new StringBuilder(type);

        for (String s : data) {
            joiner.append(",").append(s);
        }

        joiner.deleteCharAt(joiner.length() - 1);

        return joiner.toString();
    }
}
