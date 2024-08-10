package org.example.kaibutsu.itempipeline;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.kaibutsu.core.itempipeline.ItemPipeline;
import org.example.kaibutsu.core.parser.Item;
import org.example.kaibutsu.item.Author;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class AuthorCsvWriter implements ItemPipeline {
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
    public Item process(Item item) {
        try {
            Author author = (Author) item;
            csvPrinter.printRecord(author.name, author.birthday, author.bio);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return item;
    }
}
