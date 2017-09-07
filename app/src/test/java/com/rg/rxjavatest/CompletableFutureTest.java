package com.rg.rxjavatest;

import android.annotation.TargetApi;
import android.os.Build;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CompletableFutureTest {

    @Test
    public void Test() throws Exception {
        normal_ver();
        newLine();
        thread_ver();
        newLine();
        completable_ver();
        newLine();
        observable_ver();
        newLine();
        completableToObservable_ver();
    }

    public void normal_ver() {
        log("Start Normal");
        String userName = getName();
        int age = getAge();
        log(String.format("User : %s[%d]", userName, age));
        log("End Normal");
    }

    static String userName;
    static int age;
    boolean userNameFlag = false;
    boolean ageFlag = false;

    public void thread_ver() {
        log("Start Thread");
        new Thread(() -> {
            userName = getName();
            userNameFlag = true;
        }).start();
        new Thread(() -> {
            age = getAge();
            ageFlag = true;
        }).start();

        while (!userNameFlag || !ageFlag) {
            try {
                Thread.sleep(1000);
                log("Waiting...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log(String.format("User : %s[%d]", userName, age));
        log("End Thread");
    }

    public void completable_ver() throws ExecutionException, InterruptedException {
        log("Start Completable");
        CompletableFuture<String> name = getNameWithCompletable();
        CompletableFuture<Integer> age = getAgeWithCompletable();
        CompletableFuture<String> logText = name.thenCombine(age, (String tName, Integer tAge) -> String.format("User : %s[%d]", tName, tAge));
        log("get!");
        log(logText.get());
        log("End Completable");
    }

    public void observable_ver() throws InterruptedException {
        log("Start Observable");
        Observable<String> name = Observable.<String>create(subscribe -> subscribe.onNext(getName())).subscribeOn(Schedulers.newThread());
        Observable<Integer> age = Observable.<Integer>create(subscribe -> subscribe.onNext(getAge())).subscribeOn(Schedulers.newThread());
        Observable<String> zip = Observable.zip(name,age,(s, integer) -> String.format("User : %s[%d]", s,integer));
        log("subscribe!");
        zip.observeOn(Schedulers.io()).subscribe(this::log);
        Thread.sleep(8000);
        log("End Observable");
    }

    public void completableToObservable_ver() throws InterruptedException {
        log("Start Completable To Observable");
        CompletableFuture<String> name = getNameWithCompletable();
        CompletableFuture<Integer> age = getAgeWithCompletable();
        CompletableFuture<String> logText = name.thenCombine(age, (String tName, Integer tAge) -> String.format("User : %s[%d]", tName, tAge));
        Observable<String> observable = toObservable(logText);
        log("subscribe!");
        observable.observeOn(Schedulers.io()).subscribe(this::log);
        Thread.sleep(8000);
        log("End Completable To Observable");
    }

    public <T> Observable<T> toObservable(CompletableFuture<T> future) {
        return Observable.create(subscriber -> future.whenComplete((value, exception) -> {
            if (exception != null) {
                subscriber.onError(exception);
            } else {
                subscriber.onNext(value);
                subscriber.onComplete();
            }
        }));
    }

    public String getName() {
        try {
            log("Start getName()");
            Thread.sleep(3000);
            log("End getName()");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "JHK";
    }

    public int getAge() {
        try {
            log("Start getAge()");
            Thread.sleep(3000);
            log("End getAge()");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 30;
    }

    @TargetApi(Build.VERSION_CODES.N)
    CompletableFuture<String> getNameWithCompletable() {
        return CompletableFuture.supplyAsync(() -> getName());
    }

    @TargetApi(Build.VERSION_CODES.N)
    CompletableFuture<Integer> getAgeWithCompletable() {
        return CompletableFuture.supplyAsync(() -> getAge());
    }

    private void log(String log) {
        DateFormat format = new SimpleDateFormat("mm:ss SSS");
        String time = format.format(new Date());
        System.out.println(String.format("%s - [% 5d] %s", time, Thread.currentThread().getId(), log));
    }

    private void newLine(){
        System.out.println();
    }
}