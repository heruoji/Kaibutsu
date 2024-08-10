package org.example.kaibutsu.item;

import org.example.kaibutsu.core.parser.Item;

public class Author implements Item {
    public String name;
    public String birthday;
    public String bio;

    @Override
    public String toString() {
        return String.format("{ name : %s, birthday : %s, bio : %s }", name, birthday, bio);
    }
}
