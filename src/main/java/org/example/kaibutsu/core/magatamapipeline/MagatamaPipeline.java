package org.example.kaibutsu.core.magatamapipeline;

import org.example.kaibutsu.core.tsuchigumo.Magatama;

public interface MagatamaPipeline {

    void open();

    void close();

    Magatama processMagatama(Magatama magatama);
}
