package com.ssemi.sampleorder.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileRepository<T> {

    private final File file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<List<T>> typeRef;

    public JsonFileRepository(String filePath, TypeReference<List<T>> typeRef) {
        this.file = new File(filePath);
        this.typeRef = typeRef;
    }

    public List<T> readAll() throws IOException {
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        return mapper.readValue(file, typeRef);
    }

    public void writeAll(List<T> items) throws IOException {
        file.getParentFile().mkdirs();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, items);
    }
}
