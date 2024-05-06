package org.example.kaibutsu.magatamapipeline;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.kaibutsu.core.magatamapipeline.MagatamaPipeline;
import org.example.kaibutsu.core.tsuchigumo.Magatama;
import org.example.kaibutsu.magatama.Author;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class WriteCsvAuthor implements MagatamaPipeline {
    private CSVPrinter csvPrinter;

    @Override
    public void open() {
        try {
            Writer writer = new FileWriter("authors.csv");
            this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("name", "birthday", "bio"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            csvPrinter.flush();
            this.csvPrinter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Magatama processMagatama(Magatama magatama) {
        try {
            Author author = (Author) magatama;
            csvPrinter.printRecord(author.name, author.birthday, author.bio);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return magatama;
    }
}
