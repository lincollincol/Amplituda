package linc.com.amplituda.exceptions;

import java.io.FileNotFoundException;
import java.io.IOException;

public class AmplitudaException extends Exception {
    public AmplitudaException(String message) {
        super(message);
    }
}
/*
        "Error submitting a packet for decoding (%s)\n", av_err2str(ret));
        "Could not find %s stream in the input file '%s'\n", av_get_media_type_string(type));
        "Failed to find %s codec\n", av_get_media_type_string(type));
        "Failed to allocate the %s codec context\n",
        "Failed to copy %s codec parameters to decoder context\n",
        "Failed to open %s codec\n",
        "sample format %s is not supported as output format\n",
        "Could not open source file %s\n", input_audio);
        "Could not find stream information\n");
        "Could not allocate frame\n");
        "Could not allocate packet\n");



        // IO EX --
        // -- NotFound EX =
        "Could not find %s stream in the input file '%s'\n", av_get_media_type_string(type));
        "Failed to find %s codec\n", av_get_media_type_string(type));
        "Could not find stream information\n");

        // -- Processing EX =
        "Error submitting a packet for decoding (%s)\n", av_err2str(ret));
        "Failed to copy %s codec parameters to decoder context\n",

        // -- Open EX =
        "Failed to open %s codec\n",
        "Could not open source file %s\n", input_audio);

        // Global
        "sample format %s is not supported as output format\n",
        // --

        // Alloc EX --
        "Failed to allocate the %s codec context\n",
        "Could not allocate frame\n");
        "Could not allocate packet\n");
        // --

 */
