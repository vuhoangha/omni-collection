package io.github.vuhoangha.example;

import java.text.MessageFormat;

public class PeopleTest {

    public int age;

    public String name;

    @Override
    public String toString() {
        return MessageFormat.format("[age={0} name={1}]", age, name);
    }

}
