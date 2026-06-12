package com.ssemi.sampleorder.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ssemi.sampleorder.model.ProductionLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProductionLineRepository {

    private final JsonFileRepository<ProductionLine> fileRepo;

    public ProductionLineRepository(String filePath) {
        this.fileRepo = new JsonFileRepository<>(filePath, new TypeReference<List<ProductionLine>>() {});
    }

    public void enqueue(ProductionLine line) throws IOException {
        List<ProductionLine> all = new ArrayList<>(fileRepo.readAll());
        all.add(line);
        fileRepo.writeAll(all);
    }

    public List<ProductionLine> findAll() throws IOException {
        return fileRepo.readAll();
    }

    public ProductionLine dequeueHead() throws IOException {
        List<ProductionLine> all = new ArrayList<>(fileRepo.readAll());
        if (all.isEmpty()) {
            throw new IllegalStateException("생산 큐가 비어 있습니다.");
        }
        ProductionLine head = all.remove(0);
        fileRepo.writeAll(all);
        return head;
    }
}
