package com.musician.club.chat.niobasic;

import java.io.IOException;
import java.nio.file.*;

public class TestPath {

    public static void main(String[] args) {
        /*
            Path source1 = Paths.get("data.txt");   //相对路径
            Path source2 = Paths.get("D:\\data.txt");   //绝对路径
            Path source3 = Paths.get("D:/data.txt");    //绝对路径
            Path source4 = Paths.get("D:\\data", "project");    //代表了D://data/project
        */

        /*
        * D:
        *   |- data
        *       |- project
        *           |- a
        *           |- b
        *   Path path = Paths.get("D:\\data\\project\\a\\..\\b")
        *   System.out.println(path)    //D:\\data\\project\\a\\..\\b
        *   System.out.println(path.normalize())    //D:\\data\\project\\b
        *
        * */

        //Files jdk1.7之后新增的
        Path file_path = Paths.get("data.txt");
        System.out.println(Files.exists(file_path));
        //只能创建1级目录
        Path directory_path = Paths.get("./d1");
        try {
            //如果目录已存在，则会抛出FileAlreadyExistsException
            //如果创建多个目录，则会抛出NoSuchFileException
            Files.createDirectory(directory_path);

            //这个则可以创建多个目录
            Files.createDirectories(directory_path);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //文件复制
        Path source = Paths.get("data.txt");
        Path target = Paths.get("cp-data.txt");

        try {
            Files.copy(source, target);
            //如果存在则覆盖的话
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //删除文件
        Path delete_file_path = Paths.get("cp-data.txt");
        try {
            //如果文件不存在的话NoSuchFileException
            Files.delete(delete_file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path delete_directory_path = Paths.get("./d1");
        try {
            //如果要删除的目录下面还有文件存在，则抛出DirectoryNotEmptyException
            Files.delete(delete_directory_path);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
