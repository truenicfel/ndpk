package decorator;

import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class representing a broken output stream.
 *
 * Following things are possible:<br>
 * <br>
 * - zufällig mit einer konfigurierbaren Wahrscheinlichkeit einen Bitfehler im
 * Paket verursacht<br>
 * - zufällig mit einer konfigurierbaren Wahrscheinlichkeit ein Paket
 * verwirft<br>
 * - zufällig mit einer konfigurierbaren Wahrscheinlichkeit ein Paket
 * dupliziert<br>
 * <br>
 *
 * @author nico
 */
public class BrokenOutputStream extends FilterOutputStream {

    //Object Variables
    //--------------------------------------------------------------------------
    /**
     * Generates numbers necessary for the posibility calculation.
     */
    private final Random numberGenerator;

    /**
     * Saves the bit Mistake Chance. A number from 0-100(%). Represents the
     * chance of creating a bit mistake.
     */
    private final int bitMistakeChance;

    /**
     * Saves the package loss chance. A number from 0-100(%). Represents the
     * chance of losing a bit.
     */
    private final int packageLossChance;

    /**
     * Saves the package duplicate Chance. A number from 0-100(%). Represents
     * the chance of creating a duplicate package.
     */
    private final int packageDuplicateChance;

    /**
     * Saves the actions performed when a package duplicate/loss or bitMistake
     * occurs.
     */
    private final Map<String, TriConsumer<Byte[], Integer, Integer>> actions;

    //C-Tors
    //--------------------------------------------------------------------------
    /**
     * Initializes the class with standard params:<br>
     * 50% bit mistake chance<br>
     * 50% package loss chance<br>
     * 50% package duplicate chance<br>
     *
     * @param out The output stream to break!
     */
    public BrokenOutputStream(OutputStream out) {
        //call other constructor with standard params
        this(20, 20, 20, out);
    }

    /**
     * Initializes a new Broken Output Stream.
     *
     * @param bitMistakeChance The bit mistake chance. Must be between 0 and
     * 100.
     * @param packageLossChance The package loss chance. Must be between 0 and
     * 100.
     * @param packageDuplicateChance The package duplicate chance. Must be
     * between 0 and 100.
     * @param out The output stream to break!
     */
    public BrokenOutputStream(int bitMistakeChance, int packageLossChance, int packageDuplicateChance, OutputStream out) {

        //call super constructor
        super(out);
        //check if input params are correct
        if (bitMistakeChance > 100 || bitMistakeChance < 0) {
            throw new IllegalArgumentException("Bit mistake chance must be between 0 and 100");
        }
        if (packageLossChance > 100 || packageLossChance < 0) {
            throw new IllegalArgumentException("Package loss chance must be between 0 and 100");
        }
        if (packageDuplicateChance > 100 || packageDuplicateChance < 0) {
            throw new IllegalArgumentException("Package duplicate chance must be between 0 and 100");
        }
        //initialize object variables
        this.numberGenerator = new Random();
        this.bitMistakeChance = bitMistakeChance;
        this.packageLossChance = packageLossChance;
        this.packageDuplicateChance = packageDuplicateChance;
        this.actions = new TreeMap<>();
        //action for bitmistake
        actions.put("bitMistake", (Byte[] input, Integer offset, Integer length) -> {
            //choose the byte to change randomly
            int toChange = getNumberGenerator().nextInt(length - offset);
            //change the byte by changing it to 0
            input[toChange] = 0;
            byte[] output = toPrimitive(input, offset, length);
            try {
                //now call write of underlying writer
                getOut().write(output, offset, length);
            } catch (IOException exception) {
                System.out.println("An error has occured while writing data: ");
                System.out.println(exception.toString());
            }
        });
        //action for packageLoss
        actions.put("packageLoss", (Byte[] input, Integer offset, Integer length)
                -> System.out.println("A package magically disappeared!"));

        //action for packageDuplicate
        actions.put("packageDuplicate", (Byte[] input, Integer offset, Integer length) -> {
            byte[] output = toPrimitive(input, offset, length);
            try {
                //now call write of underlying writer twice
                getOut().write(output, offset, length);
                getOut().write(output, offset, length);
            } catch (IOException exception) {
                System.out.println("An error has occured while writing data: ");
                System.out.println(exception.toString());
            }
        });

        //action for normal 
        actions.put("normal", (Byte[] input, Integer offset, Integer length) -> {
            byte[] output = toPrimitive(input, offset, length);
            try {
                //now call write of underlying writer
                getOut().write(output, offset, length);
            } catch (IOException exception) {
                System.out.println("An error has occured while writing data: ");
                System.out.println(exception.toString());
            }
        });
    }

    /**
     * Redirection to write(byte[] input, int offset, int lenngth)
     *
     * @param input
     * @throws IOException
     */
    @Override
    public void write(int input) throws IOException {
        final byte[] output = new byte[1];
        output[0] = (byte) input;
        getOut().write(output);
    }

    /**
     * Redirection to write(byte[] input, int offset, int lenngth)
     *
     * @param input
     * @throws IOException
     */
    @Override
    public void write(byte[] input) throws IOException {
        getOut().write(input, 0, input.length);
    }

    @Override
    public void write(byte[] input, int offset, int length) throws IOException {

        //bit mistake
        final boolean bitMistake = calculateChance(getBitMistakeChance());
        //package loss
        final boolean packageLoss = calculateChance(getPackageLossChance());
        //package duplicate
        final boolean packageDuplicate = calculateChance(getPackageDuplicateChance());

        final List<TriConsumer<Byte[], Integer, Integer>> list = new ArrayList();
        //add them to the list if they exist
        if (bitMistake) {
            list.add(getActions().get("bitMistake"));
        }
        if (packageLoss) {
            list.add(getActions().get("packageLoss"));
        }
        if (packageDuplicate) {
            list.add(getActions().get("packageDuplicate"));
        }
        //the list is empty -> do nothing except the normal procedure
        if (list.isEmpty()) {
            getActions().get("normal").accept(toBoxed(input, offset, length), offset, length);
        }
        //list is not empty choose randomly one of the actions to execute
        else {
            list.get(getNumberGenerator().nextInt(list.size())).accept(toBoxed(input, offset, length), offset, length);
        }

        getOut().write(input, offset, length);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    //Getter and Setter and Private Methods
    //--------------------------------------------------------------------------
    private Random getNumberGenerator() {
        return numberGenerator;
    }

    private int getBitMistakeChance() {
        return bitMistakeChance;
    }

    private int getPackageLossChance() {
        return packageLossChance;
    }

    private int getPackageDuplicateChance() {
        return packageDuplicateChance;
    }

    private Map<String, TriConsumer<Byte[], Integer, Integer>> getActions() {
        return actions;
    }

    private OutputStream getOut() {
        return out;
    }

    /**
     * This function will give true with a given chance
     *
     * @param chance The chance an integer from 0 to 100.
     * @return boolean
     */
    private boolean calculateChance(int chance) {
        return getNumberGenerator()
                .ints(chance, 0, 100)
                .filter(input -> input == 0)
                .findAny()
                .isPresent();
    }

    /**
     * Turns a Byte[] into a byte[]
     *
     * @param input
     * @param offset
     * @param length
     * @return
     */
    private byte[] toPrimitive(Byte[] input, Integer offset, Integer length) {
        final byte[] output = new byte[input.length];
        for (int i = offset; offset < length - offset; i++) {
            output[i] = input[i];
        }
        return output;
    }

    /**
     * Turns a byte[] into a byte.
     * @param input
     * @param offset
     * @param length
     * @return 
     */
    private Byte[] toBoxed(byte[] input, Integer offset, Integer length) {
        final Byte[] output = new Byte[input.length];
        for (int i = offset; offset < length - offset; i++) {
            output[i] = input[i];
        }
        return output;
    }
}
