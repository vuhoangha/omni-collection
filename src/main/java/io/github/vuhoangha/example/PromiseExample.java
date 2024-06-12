package io.github.vuhoangha.example;

import io.github.vuhoangha.common.Promise;

import java.text.MessageFormat;
import java.util.concurrent.locks.LockSupport;

public class PromiseExample {

    public static void run() throws Exception {

        System.out.println(MessageFormat.format("{0} - Start PromiseExample", System.currentTimeMillis()));

        final Promise<PeopleTest> promise = new Promise<>();
        promise.clear();

        new Thread(() -> {
            LockSupport.parkNanos(3_000_000_000L);
            PeopleTest peopleTest = new PeopleTest();
            peopleTest.age = 100;
            peopleTest.name = "Kendrick";
            promise.complete(peopleTest);
        }).start();

        PeopleTest result = promise.get(1_000_000);
        System.out.println(MessageFormat.format("{0} - Result PromiseExample: {1}", System.currentTimeMillis(), result));
    }


    public static void runWithException() throws Exception {

        System.out.println(MessageFormat.format("{0} - Start PromiseExample", System.currentTimeMillis()));

        final Promise<PeopleTest> promise = new Promise<>();
        promise.clear();

        new Thread(() -> {
            LockSupport.parkNanos(5_000_000_000L);
            promise.completeWithException(new Exception("FAIL !!!"));
        }).start();

        PeopleTest result = promise.get(1_000_000);
        System.out.println(MessageFormat.format("{0} - Result PromiseExample: {1}", System.currentTimeMillis(), result));
    }

}
