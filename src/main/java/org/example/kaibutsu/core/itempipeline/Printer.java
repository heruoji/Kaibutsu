package org.example.kaibutsu.core.itempipeline;

import org.example.kaibutsu.core.parser.Item;

public class Printer implements ItemPipeline {
    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public Item process(Item item) {
        System.out.println(item);
        return item;
    }
}
