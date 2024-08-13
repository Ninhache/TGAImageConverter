package infraimageconverter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class InfraImageConverter {

    public static BufferedImage ren(Image im) {
        BufferedImage ret = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        ret.createGraphics().drawImage(im, 0, 0, null);
        return ret;
    }

    public static void main(String[] args) {
        Options options = new Options();

        Option coresOption = new Option("c", "cores", true, "Number of cores to use");
        coresOption.setRequired(true);
        options.addOption(coresOption);

        Option formatOption = new Option("f", "format", true, "Target format (e.g., png, jpg)");
        formatOption.setRequired(true);
        options.addOption(formatOption);

        Option outputPathOption = new Option("o", "output", true, "Output directory");
        outputPathOption.setRequired(false);
        options.addOption(outputPathOption);

        Option inputPathOption = new Option("i", "input", true, "Input directory containing .tga files");
        inputPathOption.setRequired(true);
        options.addOption(inputPathOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("InfraImageConverter", options);

            System.exit(1);
            return;
        }

        int cores = Integer.parseInt(cmd.getOptionValue("cores"));
        String targetFormat = cmd.getOptionValue("format");
        String directoryPath = cmd.getOptionValue("input");
        final String outputPath = cmd.hasOption("output") ? cmd.getOptionValue("output") : directoryPath;

        System.out.println("THIS PROGRAM WAS MADE BY AERO FOR INFRA");
        System.out.println("Do not give it to anyone who is pro changes, or a monkey");
        System.out.println("#nochanges");
        System.out.println("This will convert all .tga images into ." + targetFormat + " and maintain the directory structure");
        System.out.println();

        ConcurrentLinkedDeque<File> queue = new ConcurrentLinkedDeque<>();
        scanDirectory(new File(directoryPath), queue, directoryPath, outputPath);

        System.out.println("Starting " + cores + " threads to convert " + queue.size() + " images...");
        for (int i = 0; i < cores; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!queue.isEmpty()) {
                        File f = queue.poll();
                        if (f != null) {
                            try {
                                System.out.println("Reading " + f.getName() + "...");
                                BufferedImage image = FastTarga_lowmem.getImage(f.toString());

                                String relativePath = f.getParent().substring(directoryPath.length());
                                
                                File outputDir = new File(outputPath + "/" + relativePath);
                                outputDir.mkdirs();
                                
                                File outputFile = new File(outputDir, f.getName().substring(0, f.getName().length() - 3) + targetFormat);
                                ImageIO.write(image, targetFormat, new File(outputFile.toString() + ".temp"));
                                new File(outputFile.toString() + ".temp").renameTo(outputFile);
                            } catch (Throwable t) {
                                System.out.println("ERROR! BLAME AERO! SCREENSHOT THIS!");
                                t.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }

    private static void scanDirectory(File dir, ConcurrentLinkedDeque<File> queue, String sourceRoot, String outputRoot) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                scanDirectory(f, queue, sourceRoot, outputRoot);
            } else if (f.getName().toLowerCase().endsWith("tga")) {
                queue.offer(f);
            }
        }
    }
}
