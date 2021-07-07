package linc.com.amplituda;

public enum AmplitudaCompressOutput {

    SMALL(1),
    MEDIUM(2),
    FULL(0);

    final int value;

    AmplitudaCompressOutput(final int value) {
        this.value = value;
    }

}
