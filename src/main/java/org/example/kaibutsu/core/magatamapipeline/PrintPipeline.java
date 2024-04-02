package org.example.kaibutsu.core.magatamapipeline;

import org.example.kaibutsu.core.tsuchigumo.Magatama;

public class PrintPipeline implements MagatamaPipeline {
    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public Magatama processMagatama(Magatama magatama) {
        System.out.println(magatama);
        return magatama;
    }
}
