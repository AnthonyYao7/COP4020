package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.DynamicJavaCompileAndExecute.PLCLangExec;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class StolenTestsHW5 {

    String packageName = "edu.ufl.cise.cop4020fa23";
    String testURL = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d3/Statue_of_Liberty%2C_NY.jpg/1280px-Statue_of_Liberty%2C_NY.jpg";


    @AfterEach
    public void separatingLine() {
        show("----------------------------------------------");
    }

    // makes it easy to turn output on and off (and less typing than
    // System.out.println)
    static final boolean VERBOSE = true;
    static final boolean WAIT_FOR_INPUT = false;

    void show(Object obj) {
        if (VERBOSE) {
            System.out.println(obj);
        }
    }

    void showPixel(int p) {
        if (VERBOSE) {
            System.out.println(Integer.toHexString(p));
        }
    }

    /**
     * Displays the given image on the screen. If WAIT_FOR_INPUT, a prompt to enter
     * a char is displayed on the console, and execution waits until some character
     * is entered. This is to ensure that displayed images are not immediately
     * closed by Junit before you have a chance to view them.
     *
     * @param image
     * @throws IOException
     */
    void show(BufferedImage image) throws IOException {
        if (VERBOSE) {
            ConsoleIO.displayImageOnScreen(image);
            if (WAIT_FOR_INPUT) {
                System.out.println("Enter a char");
                int ch = System.in.read();
            }
        }

    }

    void compareImages(BufferedImage image0, BufferedImage image1) {
        assertEquals(image0.getWidth(), image1.getWidth(), "widths not equal");
        assertEquals(image0.getHeight(), image1.getHeight(), "heights not equal");
        for (int y = 0; y < image0.getHeight(); y++)
            for (int x = 0; x < image0.getWidth(); x++) {
                int p0 = image0.getRGB(x, y);
                int p1 = image1.getRGB(x, y);
                assertEquals(p0, p1, "pixels at [" + x + "," + y + "], expected: " + Integer.toHexString(p0)
                        + ", but was: " + Integer.toHexString(p1));
            }
    }

    @Test
    void unitTestZ() throws Exception {
        String source = """
                int Zint()<:
                           ^Z;
                         :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        assertEquals(255, (int) result);
    }


    @Test
    void unitTestBLUE() throws Exception {
        String source = """
                pixel BLUEPixel()<:
                           ^BLUE;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.BLUE.getRGB(), (int) result);
    }


    @Test
    void unitTestBLACK() throws Exception {
        String source = """
                pixel BLACKPixel()<:
                           ^BLACK;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.BLACK.getRGB(), (int) result);
    }


    @Test
    void unitTestCYAN() throws Exception {
        String source = """
                pixel CYANPixel()<:
                           ^CYAN;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.CYAN.getRGB(), (int) result);
    }


    @Test
    void unitTestDARK_GRAY() throws Exception {
        String source = """
                pixel DARKGRAY()<:
                           ^DARK_GRAY;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.DARK_GRAY.getRGB(), (int) result);
    }


    @Test
    void unitTestGRAY() throws Exception {
        String source = """
                pixel GRAYPixel()<:
                           ^GRAY;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.GRAY.getRGB(), (int) result);
    }


    @Test
    void unitTestGREEN() throws Exception {
        String source = """
                pixel GREENPixel()<:
                           ^GREEN;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.GREEN.getRGB(), (int) result);
    }


    @Test
    void unitTestLIGHT_GRAY() throws Exception {
        String source = """
                pixel LIGHT_GRAYPixel()<:
                           ^LIGHT_GRAY;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.LIGHT_GRAY.getRGB(), (int) result);
    }


    @Test
    void unitTestMAGENTA() throws Exception {
        String source = """
                pixel MAGENTAPixel()<:
                           ^MAGENTA;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.MAGENTA.getRGB(), (int) result);
    }


    @Test
    void unitTestORANGE() throws Exception {
        String source = """
                pixel ORANGEPixel()<:
                           ^ORANGE;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.ORANGE.getRGB(), (int) result);
    }


    @Test
    void unitTestPINK() throws Exception {
        String source = """
                pixel PINKPixel()<:
                           ^PINK;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.PINK.getRGB(), (int) result);
    }


    @Test
    void unitTestRED() throws Exception {
        String source = """
                pixel REDPixel()<:
                           ^RED;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.RED.getRGB(), (int) result);
    }


    @Test
    void unitTestWHITE() throws Exception {
        String source = """
                pixel WHITEPixel()<:
                           ^WHITE;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.WHITE.getRGB(), (int) result);
    }


    @Test
    void unitTestYELLOW() throws Exception {
        String source = """
                pixel YELLOWPixel()<:
                           ^YELLOW;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(Color.YELLOW.getRGB(), (int) result);
    }


    @Test
    void unitTestExpandedPixelExpression() throws Exception {
        String source = """
                pixel Pixel()<:
                           ^[25, 50, 75];
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        showPixel((int) result);
        assertEquals(PixelOps.pack(25, 50, 75), (int) result);
    }


    @Test
    void unittestWriteStatement() throws Exception {
        String source = """
                void noRet()<:
                    write RED;
                    write 150;
                :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        assertNull(result);


        // To make sure the two compiled correctly, check for an instance of
        // ConsoleIO.writePixel( before ConsoleIO.write( in the java code


        // Obtain AST from parser
        edu.ufl.cise.cop4020fa23.ast.AST ast = ComponentFactory.makeParser(source).parse();
        // Type check and decorate AST with declaration and type info
        ast.visit(ComponentFactory.makeTypeChecker(), null);
        // Generate Java code
        String javaCode = (String) ast.visit(ComponentFactory.makeCodeGenerator(), packageName);


        int writePixelIndex = javaCode.indexOf("ConsoleIO.writePixel(");
        int writeIntIndex = javaCode.indexOf("ConsoleIO.write(");
        assertTrue(writePixelIndex < writeIntIndex);
    }


    @Test
    void unittestGetWidth() throws Exception {
        String source = """
                int scale(int w, int h)
                <:
                image[w,h] im0;
                im0 = RED;
                ^(width im0);
                :>
                """;
        int w = 512;
        int h = 256;


        int result = (int) PLCLangExec.runCode(packageName, source, w, h);
        assertEquals(result, 512);
    }


    @Test
    void unittestGetHeight() throws Exception {
        String source = """
                int scale(int w, int h)
                <:
                image[w,h] im0;
                im0 = RED;
                ^(height im0);
                :>
                """;
        int w = 512;
        int h = 256;


        int result = (int) PLCLangExec.runCode(packageName, source, w, h);
        assertEquals(result, 256);
    }
    // #endregion Tests by Gabriel Aldous

    // #region Tests by Christina Castillo
    @Test
    void unaryWidth() throws Exception {
        String source = """
                int f(string url)
                <:
                image i = url;
                image[50,100] j = i;
                ^width j;
                :>
                """;
        String url = testURL;
        int result = (int) PLCLangExec.runCode(packageName, source, url);
        assertEquals(result, 50);
        show(result);
    }


    @Test
    void unaryHeight() throws Exception {
        String source = """
                int f(string url)
                <:
                image i = url;
                image[50,100] j = i;
                ^height j;
                :>
                """;
        String url = testURL;
        int result = (int) PLCLangExec.runCode(packageName, source, url);
        assertEquals(result, 100);
        show(result);
    }


    @Test
    void doRunsTwice() throws Exception {
        String source = """
                int gcd(int a, int b)
                <:
                do
                   b< a -> <:  a = a -b; :>
                []
                   a < b -> <: b = b-a; :>
                od;
                ^a;
                :>
                """;
        int gcd = (Integer) PLCLangExec.runCode(packageName, source, 10, 15);
        assertEquals(5, gcd);
    }


    @Test
    void doRunsOnce() throws Exception {
        String source = """
                int test(int a)
                <:
                do
                   a == 0 -> <:  a = a - 1; :>
                od;
                ^a;
                :>
                """;
        int result = (Integer) PLCLangExec.runCode(packageName, source, 0);
        assertEquals(-1, result);
    }


    @Test
    void doRunsZeroTimes() throws Exception {
        String source = """
                int test(int a)
                <:
                do
                   a > 0 -> <:  a = a - 1; :>
                od;
                ^a;
                :>
                """;
        int result = (Integer) PLCLangExec.runCode(packageName, source, 0);
        assertEquals(0, result);
    }


    @Test
    void nestedDoStatements() throws Exception {
        String source = """
                int test(int a)
                <:
                do
                    a > 0 -> <:
                        a = a - 1;
                        do a>=1 -> <: a = a - 2; :> od;
                        a = a * 2;
                    :>
                od;
                ^a;
                :>
                """;
        int result = (Integer) PLCLangExec.runCode(packageName, source, 4);
        assertEquals(-2, result);
    }


    @Test
    void ifDoesNotRun() throws Exception {
        String source = """
                int f()
                <:
                int x = 3;
                if
                   x < 2 -> <: x=x+1; :>
                   fi;
                   ^x;
                :>
                """;
        int result = (Integer) PLCLangExec.runCode(packageName, source);
        assertEquals(3, result);
    }


    @Test
    void widthImplicitLoop() throws Exception {
        String source = """
                image checkerBoard(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   im0[x,0] = RED;
                   ^im0;
                   :>
                   """;
        String url = testURL;
        int w = 200;
        int h = 300;
        BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
        BufferedImage expected = FileURLIO.readImage(url, w, h);
        for (int x = 0; x < w; x++) {
            expected.setRGB(x, 0, Color.red.getRGB());
        }
        compareImages(expected, output);
    }


    @Test
    void heightImplicitLoop() throws Exception {
        String source = """
                image checkerBoard(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   im0[0,y] = RED;
                   ^im0;
                   :>
                   """;
        String url = testURL;
        int w = 200;
        int h = 300;
        BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
        BufferedImage expected = FileURLIO.readImage(url, w, h);
        for (int y = 0; y < h; y++) {
            expected.setRGB(0, y, Color.red.getRGB());
        }
        compareImages(expected, output);
    }


    @Test
    void noImplicitLoop() throws Exception {
        String source = """
                image checkerBoard(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   im0[0,0] = RED;
                   ^im0;
                   :>
                   """;
        String url = testURL;
        int w = 200;
        int h = 300;
        BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
        BufferedImage expected = FileURLIO.readImage(url, w, h);
        expected.setRGB(0, 0, Color.red.getRGB());
        compareImages(expected, output);
    }


    @Test
    void assignUrlToImage() throws Exception {
        String source = """
                image test(string url, int w, int h) <:
                   image[w,h] im0;
                   im0 = url;
                   ^im0;
                   :>
                   """;
        String url = testURL;
        int w = 200;
        int h = 300;
        BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
        BufferedImage expected = FileURLIO.readImage(url, w, h);
        compareImages(expected, output);
    }


    // Note that I just updated this test. It was comparing against the wrong expected solution before, since I didn’t account for both shifts in dimensions.
    @Test
    void assignImageToImage() throws Exception {
        String source = """
                image test(string url, int w, int h) <:
                   image[w,h] im0 = url;
                   image[w/2,h/2] im1;
                   im1 = im0;
                   ^im1;
                   :>
                   """;
        String url = testURL;
        int w = 200;
        int h = 300;
        BufferedImage expected = FileURLIO.readImage(url, w, h);
        expected = ImageOps.copyAndResize(expected, w/2, h/2);
        BufferedImage output = (BufferedImage) PLCLangExec.runCode(packageName, source, url, w, h);
        compareImages(expected, output);
    }


    @Test
    void pixelsAreEqual() throws Exception {
        String source = """
                boolean test()<:
                pixel i = RED;
                pixel j = RED;
                           ^i == j;
                         :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        assertEquals(true, (boolean) result);
    }


    @Test
    void pixelsAreNotEqual() throws Exception {
        String source = """
                boolean test()<:
                pixel i = RED;
                pixel j = BLUE;
                           ^i == j;
                         :>
                """;
        Object result = PLCLangExec.runCode(packageName, source);
        assertEquals(false, (boolean) result);
    }


    @Test
    void extractRedFromPixel() throws Exception {
        String source = """
                int example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im[0,0]:red;
                :>
                """;
        int w = 512;
        int h = 512;
        int result = (int) PLCLangExec.runCode(packageName, source, w, h);
        assertEquals(result, 50);
    }


    @Test
    void extractGreenFromPixel() throws Exception {
        String source = """
                int example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im[0,0]:green;
                :>
                """;
        int w = 512;
        int h = 512;
        int result = (int) PLCLangExec.runCode(packageName, source, w, h);
        assertEquals(result, 100);
    }


    @Test
    void extractBlueFromPixel() throws Exception {
        String source = """
                int example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im[0,0]:blue;
                :>
                """;
        int w = 512;
        int h = 512;
        int result = (int) PLCLangExec.runCode(packageName, source, w, h);
        assertEquals(result, 150);
    }


    @Test
    void setRedInPixel() throws Exception {
        String source = """
                int example() <:
                pixel p = RED;
                p:red = 0;
                ^p:red;
                :>
                """;
        int w = 512;
        int h = 512;
        int result = (int) PLCLangExec.runCode(packageName, source);
        assertEquals(result, 0);
    }


    @Test
    void setGreenInPixel() throws Exception {
        String source = """
                int example() <:
                pixel p = GREEN;
                p:green = 0;
                ^p:green;
                :>
                """;
        int w = 512;
        int h = 512;
        int result = (int) PLCLangExec.runCode(packageName, source);
        assertEquals(result, 0);
    }


    @Test
    void setBlueInPixel() throws Exception {
        String source = """
                int example() <:
                pixel p = BLUE;
                p:blue = 0;
                ^p:blue;
                :>
                """;
        int w = 512;
        int h = 512;
        int result = (int) PLCLangExec.runCode(packageName, source);
        assertEquals(result, 0);
    }


    @Test
    void extractRedFromImage() throws Exception {
        String source = """
                image example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im:red;
                :>
                """;
        int w = 512;
        int h = 512;
        BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
        BufferedImage expected = ImageOps.makeImage(w, h);
        expected = ImageOps.setAllPixels(expected, PixelOps.pack(50, 0, 0));
        compareImages(expected, result);
    }


    // Although a function to do binary operations between images and pixels is given, type checker should not allow it.
    @Test
    void binaryImagePixelOp() throws Exception {
        String source = """
                image example(string url) <:
                image im = url;
                pixel p = [0, 0, 100];
                im = im - p;
                ^im;
                :>
                """;
        String url = testURL;
        assertThrows(TypeCheckException.class, () -> PLCLangExec.runCode(packageName, source, url));
    }


    @Test
    void extractGreenFromImage() throws Exception {
        String source = """
                image example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im:green;
                :>
                """;
        int w = 512;
        int h = 512;
        BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
        BufferedImage expected = ImageOps.makeImage(w, h);
        expected = ImageOps.setAllPixels(expected, PixelOps.pack(0, 100, 0));
        compareImages(expected, result);
    }


    @Test
    void extractBlueFromImage() throws Exception {
        String source = """
                image example(int w, int h) <:
                image[w,h] im;
                im[x,y] = [50, 100, 150];
                ^im:blue;
                :>
                """;
        int w = 512;
        int h = 512;
        BufferedImage result = (BufferedImage) PLCLangExec.runCode(packageName, source, w, h);
        BufferedImage expected = ImageOps.makeImage(w, h);
        expected = ImageOps.setAllPixels(expected, PixelOps.pack(0, 0, 150));
        compareImages(expected, result);
    }


    @Test
    void binaryPackedPixelScalarOp() throws Exception {
        String source = """
                pixel test()<:
                           ^RED * 2;
                         :>


                """;
        Object result = PLCLangExec.runCode(packageName, source);
        int expected = ImageOps.binaryPackedPixelScalarOp(ImageOps.OP.TIMES, Color.RED.getRGB(), 2);
        assertEquals(expected, (int) result);
    }

}
