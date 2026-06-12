package com.ssemi.sampleorder.service;

import com.ssemi.sampleorder.model.Sample;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.io.IOException;
import java.util.List;

public class SampleService {

    private final SampleRepository repository;

    public SampleService(SampleRepository repository) {
        this.repository = repository;
    }

    public void register(Sample sample) throws IOException {
        repository.save(sample);
    }

    public List<Sample> getAll() throws IOException {
        return repository.findAll();
    }

    public List<Sample> searchByName(String keyword) throws IOException {
        return repository.findByName(keyword);
    }
}
