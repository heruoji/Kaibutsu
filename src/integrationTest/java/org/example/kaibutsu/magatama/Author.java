package org.example.kaibutsu.magatama;

import org.example.kaibutsu.core.tsuchigumo.Magatama;

public class Author implements Magatama {
    public String name;
    public String birthday;
    public String bio;

    @Override
    public String toString() {
        return String.format("{ name : %s, birthday : %s, bio : %s }", name, birthday, bio);
    }
}
