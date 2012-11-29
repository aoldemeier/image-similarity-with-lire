package de.mayflower.samplecode.SimilaritySearchWithLIRE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import org.apache.lucene.document.Document;

public class SimilaritySearchWithLIRE {

    static class ImageInImageDatabase {

        public String fileName;
        public double[] fcthFeatureVector;
        public double distanceToSearchImage;
    }

    static class ImageComparator implements Comparator<ImageInImageDatabase> {

        @Override
        public int compare(ImageInImageDatabase object1, ImageInImageDatabase object2) {
            if (object1.distanceToSearchImage < object2.distanceToSearchImage) {
                return -1;
            } else if (object1.distanceToSearchImage > object2.distanceToSearchImage) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static double[] getFCTHFeatureVector(String fullFilePath) throws FileNotFoundException, IOException {

        DocumentBuilder builder = DocumentBuilderFactory.getFCTHDocumentBuilder();
        FileInputStream istream = new FileInputStream(fullFilePath);
        Document doc = builder.createDocument(istream, fullFilePath);
        istream.close();

        FCTH fcthDescriptor = new FCTH();
        fcthDescriptor.setByteArrayRepresentation(doc.getFields().get(0).getBinaryValue());

        return fcthDescriptor.getDoubleHistogram();

    }

    public static double calculateEuclideanDistance(double[] vector1, double[] vector2) {

        double innerSum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            innerSum += Math.pow(vector1[i] - vector2[i], 2.0);
        }

        return Math.sqrt(innerSum);

    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        if (args.length != 2) {
            
            System.out.println("This application requires two parameters: "
                    + "the name of a directory containing JPEG images, and a file name of a JPEG image.");
            return;
            
        }
        
        String imageDatabaseDirectoryName = args[0];
        String searchImageFilePath = args[1];

        double[] searchImageFeatureVector = getFCTHFeatureVector(searchImageFilePath);

        System.out.println("Search image FCTH vector: " + Arrays.toString(searchImageFeatureVector));

        ArrayList<ImageInImageDatabase> database = new ArrayList();

        File directory = new File(imageDatabaseDirectoryName);

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".jpeg");
            }
        };

        String[] fileNames = directory.list(filter);

        for (String fileName : fileNames) {

            double[] fcthFeatureVector = getFCTHFeatureVector(imageDatabaseDirectoryName + "\\" + fileName);
            double distanceToSearchImage = calculateEuclideanDistance(fcthFeatureVector, searchImageFeatureVector);

            ImageInImageDatabase imageInImageDatabase = new ImageInImageDatabase();

            imageInImageDatabase.fileName = fileName;
            imageInImageDatabase.fcthFeatureVector = fcthFeatureVector;
            imageInImageDatabase.distanceToSearchImage = distanceToSearchImage;

            database.add(imageInImageDatabase);

        }

        Collections.sort(database, new ImageComparator());

        for (ImageInImageDatabase result : database) {

            System.out.println("Distance " + Double.toString(result.distanceToSearchImage) + ": " + result.fileName);

        }

    }
}
