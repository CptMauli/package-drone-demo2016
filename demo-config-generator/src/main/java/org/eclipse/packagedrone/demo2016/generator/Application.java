package org.eclipse.packagedrone.demo2016.generator;

public class Application {

    public static void main(String[] args) {
        try {
            new Generator().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
