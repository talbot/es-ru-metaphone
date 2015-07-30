package im.tretyakov.elasticsearch.rumetaphone.phonetic;

import org.apache.commons.codec.EncoderException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests on russian metaphone encoder class
 *
 * @author Dmitry Tretyakov <dmitry@tretyakov.im>
 */
public class RuMetaphoneEncoderTest extends Assert {

    private final RuMetaphoneEncoder encoder = new RuMetaphoneEncoder();

    @Test(testName = "Basic test of encoding Кузнецов Иван Сергеевич")
    public void testEncode() throws Exception {
        assertEquals(this.encoder.encode("Кузнецов Иван Сергеевич"), "КУЗНИЦ4 ИВАН СИРГИ?");
    }

    @Test(
        testName = "Test on trying to encode a byte array",
        expectedExceptions = EncoderException.class,
        expectedExceptionsMessageRegExp = "Russian metaphone encode parameter is not of type String"
    )
    public void testEncodeExceptionOnNotAString() throws Exception {
        this.encoder.encode(new byte[] {0, 1, 2, 3});
    }

    @Test(testName = "Test on encoding double surnames: Всеволод Александрович Михалков-Кончаловский")
    public void testEncodeDoubleSurnames() throws Exception {
        assertEquals(
            this.encoder.encode("Всеволод Александрович Михалков-Кончаловский"),
            "ФСИВАЛАТ АЛИКСАНТР? МИХАЛК4 КАНЧАЛ@"
        );
    }

    @Test(testName = "Test on encoding not russian (i.e. lithuanian) surnames")
    public void testEncodeNotRussianSurnames() throws Exception {
        assertEquals(this.encoder.encode("Айдас Ноктиниус"), "АЙДАС НАКТИНИУС");
        assertEquals(this.encoder.encode("Екатерина Михайловна Ноктинити"), "ИКАТИР1 МИХАЙЛ! НАКТИНИТИ");
        assertEquals(this.encoder.encode("Виталина Айдасавна Ноктинайте"), "ВИТАЛ1 АЙДАСАВНА НАКТИНАЙТИ");
    }

    @Test(testName = "Test on encoding misspelled russian surnames")
    public void testEncodeDifferentSpelling() throws Exception {
        final String expected = "СПИРИДАН9 МАРГАРИТА АФАНАС!";
        assertEquals(this.encoder.encode("Спиридонова маргарита афанасьевна"), expected);
        assertEquals(this.encoder.encode("спередонова моргорита офонасевна"), expected);
    }
}
