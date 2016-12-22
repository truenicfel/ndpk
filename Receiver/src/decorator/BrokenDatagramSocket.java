package decorator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImplFactory;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.Consumer;

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
public class BrokenDatagramSocket extends DatagramSocket {

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
    private final Map<String, Consumer<DatagramPacket>> actions;

    //C-Tors
    //--------------------------------------------------------------------------
    /**
     * Initializes the class with standard params:<br>
     * 20% bit mistake chance<br>
     * 20% package loss chance<br>
     * 20% package duplicate chance<br>
     *
     */
    public BrokenDatagramSocket() throws SocketException {
        //call other constructor with standard params
        this(20, 20, 20);
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
     */
    public BrokenDatagramSocket(int bitMistakeChance, int packageLossChance, int packageDuplicateChance) throws SocketException {

        //call super constructor
        super();
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
        actions.put("bitMistake", (DatagramPacket packet) -> {
            //load data
            final byte[] data = packet.getData();
            //choose the byte to change randomly
            final int toChange = getNumberGenerator().nextInt(data.length);
            //change the byte by changing it to 0
            data[toChange] = 0;
            try {
                //now call send of the underlying DatagramScoket
                super.send(new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort()));
                System.out.println("\tA bit mistake happened!");
            } catch (IOException exception) {
                System.out.println("An error has occured while writing data: ");
                System.out.println(exception.toString());
            }
        });
        //action for packageLoss
        actions.put("packageLoss", (DatagramPacket packet)
                -> System.out.println("\tA package magically disappeared!"));

        //action for packageDuplicate
        actions.put("packageDuplicate", (DatagramPacket packet) -> {
            try {
                //now call write of underlying writer twice
                super.send(packet);
                super.send(packet);
                System.out.println("\tA duplicate packet was send!");
            } catch (IOException exception) {
                System.out.println("An error has occured while writing data: ");
                System.out.println(exception.toString());
            }
        });

        //action for normal 
        actions.put("normal", (DatagramPacket packet) -> {
            try {
                //now call write of underlying writer
                super.send(packet);
            } catch (IOException exception) {
                System.out.println("An error has occured while writing data: ");
                System.out.println(exception.toString());
            }
        });
    }

    /**
     * The only method that we change. Before sending Data we will calculate
     * wich error happened and change the type of sending according to that
     * (Duplicate, Bit mistake or Package loss)
     *
     * @param packet The packet to be sent.
     * @throws IOException
     */
    @Override
    public void send(DatagramPacket packet) throws IOException {

        //bit mistake
        final boolean bitMistake = calculateChance(getBitMistakeChance());
        //package loss
        final boolean packageLoss = calculateChance(getPackageLossChance());
        //package duplicate
        final boolean packageDuplicate = calculateChance(getPackageDuplicateChance());

        final List<Consumer<DatagramPacket>> list = new ArrayList<>();
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
            getActions().get("normal").accept(packet);
        } //list is not empty choose randomly one of the actions to execute
        else {
            list.get(getNumberGenerator().nextInt(list.size())).accept(packet);
        }

    }

    public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac) throws IOException {
        DatagramSocket.setDatagramSocketImplFactory(fac);
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

    private Map<String, Consumer<DatagramPacket>> getActions() {
        return actions;
    }

    /**
     * This function will give true with a given chance
     *
     * @param chance The chance an integer from 0 to 100.
     * @return boolean
     */
    private boolean calculateChance(int chance) {
        //if the number generated by numberGenerator is smaller or equal to the
        //chance (in %) divided by 100 this will result true 
        return getNumberGenerator().nextDouble() <= chance / 100.0;
    }
}
