package linc.com.amplituda;


import java.util.LinkedHashSet;

class AmplitudaResultJNI {
    private String amplitudes;
    private String errors;

    String getAmplitudes() {
        return amplitudes;
    }

    LinkedHashSet<Integer> getErrors() {
        LinkedHashSet<Integer> errors = new LinkedHashSet<>();
        for(String error : this.errors.split(" ")) {
            if(error.isEmpty())
                continue;
            errors.add(Integer.valueOf(error));
        }
        return errors;
    }

}
