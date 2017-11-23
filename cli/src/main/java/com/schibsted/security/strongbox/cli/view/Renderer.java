/*
 * Copyright (c) 2016 Schibsted Products & Technology AS. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
 */

package com.schibsted.security.strongbox.cli.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Joiner;
import com.schibsted.security.strongbox.cli.viewmodel.types.BinaryString;
import com.schibsted.security.strongbox.cli.viewmodel.types.View;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Transform the output from the CLI viewmodel to the different output formats
 */
public class Renderer {
    private OutputFormat outputFormat;
    private String fieldName;
    private String saveToFilePath;
    private PrintStream outputStream;

    public Renderer(OutputFormat outputFormat, PrintStream outputStream, String fieldName, String saveToFilePath) {
        this.fieldName = fieldName;
        this.saveToFilePath = saveToFilePath;
        this.outputStream = outputStream;

        this.outputFormat = outputFormat;
        switch (outputFormat) {
            case JSON:
            case TEXT:
            case CSV:
            case RAW:
                break;
            default:
                throw new IllegalArgumentException(String.format("Unexpected output format '%s', reverting to text", outputFormat.toString()));
        }
    }

    public Renderer(OutputFormat outputFormat, String fieldName, String saveToFilePath) {
        this(outputFormat, System.out, fieldName, saveToFilePath);
    }

    public <T extends Collection<? extends View>> void render(T object) {
        if (saveToFilePath != null) {
            createPathIfItDoesNotExist(saveToFilePath);
            for (View o : object) {
                writeToFile(o);
            }
        } else {
            writeToStream(arrayOutput(object).asByteArray());
        }
    }

    public <T extends View> void render(T object) {
        if (saveToFilePath != null) {
            createPathIfItDoesNotExist(saveToFilePath);
            writeToFile(object);
        } else {
            writeToStream(singleOutput(object).asByteArray());
        }
    }

    public void writeToStream(byte[] data) {
        try {
            this.outputStream.write(data);
            this.outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output from CLI", e);
        }
    }

    public <T extends View> void writeToFile(T object) {
        File file = new File(saveToFilePath + "/" + object.uniqueName());
        try {
            Files.write(file.toPath(), singleOutput(object).asByteArray());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write to file '%s'", file), e);
        }
    }

    private void createPathIfItDoesNotExist(String p) {
        File path = new File(p);
        if (!path.exists()) {
            try {
                Files.createDirectory(path.toPath());
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to create directory '%s'", path), e);
            }
        }
    }

    private <T extends Collection<? extends View>> BinaryString arrayOutput(T object) {
        switch (outputFormat) {
            case JSON:
                return new BinaryString(serializeToJSON(object) + "\n");
            case TEXT:
                return new BinaryString(Joiner.on("\n").join(object.stream().map(Object::toString).collect(Collectors.toList())) + "\n");
            case CSV:
                return new BinaryString(Joiner.on("\n").join(object.stream().map(a -> serializeToCSV(a, fieldName)).collect(Collectors.toList())) + "\n");
            case RAW:
                if (object.size() != 1) {
                    throw new IllegalArgumentException(String.format("The output must be exactly 1 field of 1 value, when using '--output raw', but there were %d values", object.size()));
                }
                return outputRaw(object.iterator().next(), fieldName);
            default:
                throw new RuntimeException("Unexpected output format");
        }
    }

    public <T extends View> BinaryString singleOutput(T object) {
        switch (outputFormat) {
            case JSON:
                return new BinaryString(serializeToJSON(object) + "\n");
            case TEXT:
                return new BinaryString(object.toString() + "\n");
            case CSV:
                return new BinaryString(serializeToCSV(object, fieldName) + "\n");
            case RAW:
                return outputRaw(object, fieldName);
            default:
                throw new RuntimeException("Unexpected output format");
        }
    }


    private BinaryString outputRaw(View view, String fieldName) {
        if (fieldName.contains(",")) {
            throw new IllegalArgumentException(String.format("You can only output a single field of a single value when using '--output raw', but got '%s'", fieldName));
        }

        Map<String, BinaryString> map = view.toMap();
        if (map.containsKey(fieldName)) {
            return map.get(fieldName);
        } else {
            throw new IllegalArgumentException(String.format("No output field called '%s', expected one of {%s}", fieldName, Joiner.on(", ").join(map.keySet())));
        }
    }

    private String serializeToCSV(View view, String fieldName) {
        String[] fields = fieldName.split(",");
        Map<String, BinaryString> map = view.toMap();

        for (String field : fields) {
            if (!map.containsKey(field)) {
                throw new IllegalArgumentException(String.format("No output field called '%s', expected one of {%s}", fieldName, Joiner.on(", ").join(map.keySet())));
            }
        }

        List<String> result = new LinkedList<>();
        for (String field : fields) {
            result.add(csvEscape(map.get(field).getStringOrBase64Encode()));
        }

        return Joiner.on(",").join(result);
    }

    /**
     * https://tools.ietf.org/html/rfc4180
     */
    private String csvEscape(String original) {
        if (original.contains("\"") || original.contains(",") || original.contains("\n")) {
            // surround with "
            // replace " with ""
            return "\"" + original.replace("\"", "\"\"") + "\"";
        } else {
            return original;
        }
    }

    private String serializeToJSON(Object object) {
        ObjectMapper m = new ObjectMapper()
                .registerModule(new Jdk8Module());
        m.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            return m.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize output to JSON", e);
        }
    }
}
