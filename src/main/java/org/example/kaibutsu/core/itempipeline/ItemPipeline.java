package org.example.kaibutsu.core.itempipeline;

import org.example.kaibutsu.core.parser.Item;

public interface ItemPipeline {

    void open();

    void close();

    Item process(Item item);
}
