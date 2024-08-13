package infraimageconverter;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class FastTarga_lowmem {
    private static int offset;
    
    public static BufferedImage getImage(String fileName) throws IOException {
        File f = new File(fileName);
        BufferedInputStream bis = (new BufferedInputStream(new FileInputStream(f)));

        return decode(bis);
    }


    private static int btoi(byte b) {
        int a = b;
        return (a < 0 ? 256 + a : a);
    }

    private static int read(byte[] buf) {
        return btoi(buf[offset++]);
    }

    public static BufferedImage decode(BufferedInputStream in) throws IOException {
        offset = 0;
    
        // Read the header
        int buf_2 = 0;
        int buf_16 = 0;
        int buf_17 = 0; // Image Descriptor Byte
        for (int i = 0; i < 12; i++) {
            if(i == 2)
                buf_2 = in.read();
            else
                in.read();
        }
        int width = in.read() + (in.read() << 8);
        int height = in.read() + (in.read() << 8);
        
        buf_16 = in.read();
        buf_17 = in.read();  // Image Descriptor Byte
        
        boolean topToBottom = (buf_17 & 0x20) != 0;  // Check the 5th bit
    
        int n = width * height;
        int[] pixels = new int[n];
        int idx = 0;
    
        if (buf_2 == 0x02 && buf_16 == 0x20) { // uncompressed BGRA
            while (n > 0) {
                int b = in.read();
                int g = in.read();
                int r = in.read();
                int a = in.read();
                int v = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[idx++] = v;
                n -= 1;
            }
        } else if (buf_2 == 0x02 && buf_16 == 0x18) {  // uncompressed BGR
            while (n > 0) {
                int b = in.read();
                int g = in.read();
                int r = in.read();
                int a = 255; // opaque pixel
                int v = (a << 24) | (r << 16) | (g << 8) | b;
                pixels[idx++] = v;
                n -= 1;
            }
        } else {
            // RLE compressed
            while (n > 0) {
                int nb = in.read(); // num of pixels
                if ((nb & 0x80) == 0) { // 0x80=dec 128, bits 10000000
                    for (int i = 0; i <= nb; i++) {
                        int b = in.read();
                        int g = in.read();
                        int r = in.read();
                        pixels[idx++] = 0xff000000 | (r << 16) | (g << 8) | b;
                    }
                } else {
                    nb &= 0x7f;
                    int b = in.read();
                    int g = in.read();
                    int r = in.read();
                    int v = 0xff000000 | (r << 16) | (g << 8) | b;
                    for (int i = 0; i <= nb; i++)
                        pixels[idx++] = v;
                }
                n -= nb + 1;
            }
        }
    
        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    
        // Write the pixels to the image, accounting for orientation
        if (topToBottom) {
            bimg.setRGB(0, 0, width, height, pixels, 0, width);
        } else {
            for (int y = 0; y < height; y++) {
                bimg.setRGB(0, y, width, 1, pixels, (height - 1 - y) * width, width);
            }
        }
    
        return bimg;
    }

}