package com.ssemi.sampleorder.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ssemi.sampleorder.model.Sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleRepository {

    private final JsonFileRepository<Sample> fileRepo;

    public SampleRepository(String filePath) {
        this.fileRepo = new JsonFileRepository<>(filePath, new TypeReference<List<Sample>>() {});
    }

    public void save(Sample sample) throws IOException {
        List<Sample> all = new ArrayList<>(fileRepo.readAll());
        all.add(sample);
        fileRepo.writeAll(all);
    }

    public List<Sample> findAll() throws IOException {
        return fileRepo.readAll();
    }

    public List<Sample> findByName(String keyword) throws IOException {
        if (keyword == null) return new ArrayList<>();
        return fileRepo.readAll().stream()
                .filter(s -> s.getName().contains(keyword))
                .toList();
    }
}
