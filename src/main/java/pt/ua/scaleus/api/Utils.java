/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.scaleus.api;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 *
 * @author Pedro Sernadela <sernadela at ua.pt>
 */
public class Utils {
    
     /**
     * Get all content in the directory
     *
     * @param directory
     * @return
     */
    public static String[] getFolderContentList(String directory) {

        File src = new File(directory);
        String dir[] = src.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.isDirectory();
            }
        });
        return dir;
    }
    
     /**
     * Delete a Directory.
     *
     * @param file
     * @throws IOException
     */
    public static void deleteDirectory(File file)
            throws IOException {
        
        if (file.isDirectory()) {

            //directory is empty, then delete it
            if (file.list().length == 0) {
                file.setWritable(true);
                boolean b = file.delete();
                if(!b) System.out.println("Directory cannot be deleted : "+ file.getAbsolutePath());

            } else {

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {

                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    deleteDirectory(fileDelete);
                }

                //check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.setWritable(true);
                    boolean b = file.delete();
                    if(!b) System.out.println("Directory cannot be deleted : "+ file.getAbsolutePath());
                }
            }

        } else {
            //if file, then delete it
            file.setWritable(true);
            boolean b = file.delete();
            if(!b) System.out.println("File cannot be deleted : " + file.getAbsolutePath());
        }
    }
    
}
