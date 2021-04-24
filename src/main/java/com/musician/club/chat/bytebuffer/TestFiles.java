package com.musician.club.chat.bytebuffer;

import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFiles {


    //文件夹的遍历,删除！危险！！！！！！！！！！！！！！
    public static void main(String[] args) {
        try {

            Files.walkFileTree(Paths.get("/Users/xxx/Downloads/"), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    System.out.println("======> 进入"+dir);
                    return super.preVisitDirectory(dir,attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    System.out.println("======> 退出" +dir);
                    return super.postVisitDirectory(dir, exc);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //文件夹的遍历，设计模式：访问者模式 Visitor
    public static void main2(String[] args) {
        try {
            AtomicInteger file_count = new AtomicInteger();

            Files.walkFileTree(Paths.get("/Users/xxx/Downloads/"), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.endsWith(".jar")) {
                        file_count.incrementAndGet();
                        System.out.println(file);
                    }
                    return super.visitFile(file,attrs);
                }

            });
            System.out.println("jar文件数量：" + file_count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //文件夹的遍历，设计模式：访问者模式 Visitor
    public static void main1(String[] args) {
        try {
            AtomicInteger directory_count = new AtomicInteger();
            AtomicInteger file_count = new AtomicInteger();

            Files.walkFileTree(Paths.get("/Users/xxx/Downloads/"), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    System.out.println("======>"+dir);
                    directory_count.incrementAndGet();
                    return super.preVisitDirectory(dir,attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.println("===========>" + file);
                    file_count.incrementAndGet();
                    return super.visitFile(file,attrs);
                }

            });
            System.out.println("文件夹数量：" + directory_count);
            System.out.println("文件数量：" + file_count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
