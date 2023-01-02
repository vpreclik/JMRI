package jmri.jmrix.openlcb;

import cucumber.api.java8.No;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.openlcb.DatagramAcknowledgedMessage;
import org.openlcb.DatagramMessage;
import org.openlcb.EventState;
import org.openlcb.NodeID;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.implementations.DatagramService;

/**
 * OlcbProgrammerTest.java
 * <p>
 * Test for the jmri.jmrix.openlcb.OlcbProgrammer class
 *
 * @author Bob Jacobsen
 */
public class OlcbProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {
    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE, programmer.getMode());
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                ((OlcbProgrammer) programmer).getBestMode());
    }

    @Test
    @Override
    public void testSetGetMode() {
        Assert.assertThrows(IllegalArgumentException.class, () -> programmer.setMode(ProgrammingMode.REGISTERMODE));
    }

    @Test
    @Override
    public void testWriteCVNullListener() throws ProgrammerException {
        registerProgramTrack();
        super.testWriteCVNullListener();
        // Write CV1 = 42.
        h.expectMessage(new DatagramMessage(localNid, progNid, new int[]{0x20, 0x00, 0, 0, 0, 0, 0xF8, 42}));
    }

    @Test
    public void testFindProgramTrack() {
        programmer = prog = new OlcbProgrammer(h.iface, null);
        h.expectFrame(":X19914333N090099FEFFFF0002;");
        Assert.assertNull(prog.nid);
        // Seeds alias map with a node ID.
        h.setRemoteAlias(0x555, progNid);
        // Producer identified.
        h.sendFrame(":X19547555N090099FEFFFF0002;");
        Assert.assertEquals(progNid, prog.nid);
        h.expectNoFrames();
    }

    @Test
    public void testAddressedReadLookup() {
        programmer = prog = new OlcbProgrammer(h.iface, true, 15);
        Assert.assertEquals(new NodeID("06.01.00.00.C0.0F"), prog.nid);
        // There is a verify node ID message sent.
        h.expectFrame(":X19490333N06010000C00F;");
    }

    @Test
    public void testAddressedReadShortAddress() {
        programmer = prog = new OlcbProgrammer(h.iface, false, 15);
        Assert.assertEquals(new NodeID("06.01.00.00.00.0F"), prog.nid);
        // There is a verify node ID message sent.
        h.expectFrame(":X19490333N06010000000F;");
    }

    @Test
    public void testAddressedReadSuccess() throws ProgrammerException {
        programmer = prog = new OlcbProgrammer(h.iface, true, 15);
        Assert.assertEquals(new NodeID("06.01.00.00.C0.0F"), prog.nid);
        // There is a verify node ID message sent.
        h.expectFrame(":X19490333N06010000C00F;");
        h.setRemoteAlias(0x999, pomNid);
        programmer.readCV("1", p);
        // Read datagram
        h.expectMessageAndNoMore(new DatagramMessage(localNid, pomNid, new int[]{0x20, 0x40, 0, 0, 0, 0, 0xF8, 1}));
        // Acknowledged
        h.sendMessage(new DatagramAcknowledgedMessage(pomNid, localNid));
        Mockito.verifyNoInteractions(p);
        // Response datagram
        h.sendMessageAndExpectResult(new DatagramMessage(pomNid, localNid, new int[]{0x20, 0x50, 0, 0, 0, 0, 0xF8, 37}),
                new DatagramAcknowledgedMessage(localNid, pomNid));
        // Passed on to listener
        Mockito.verify(p).programmingOpReply(37, ProgListener.OK);
        h.expectNoMessages();
    }

    @Test
    public void testWriteOK() throws ProgrammerException {
        registerProgramTrack();
        prog.writeCV("13", 42, p);
        expectInteraction(new int[]{0x20,0x00,0,0,0,12,0xF8, 42}, new int[]{0x20,0x10,0,0,0,12,0xF8});
        Mockito.verify(p).programmingOpReply(42, 0); //ProgListener.OK
    }

    @Test
    public void testReadOK() throws ProgrammerException {
        registerProgramTrack();
        prog.readCV("13", p);
        expectInteraction(new int[]{0x20, 0x40, 0, 0, 0, 12, 0xF8, 1}, new int[]{0x20, 0x50, 0, 0, 0, 12, 0xF8, 37});
        Mockito.verify(p).programmingOpReply(37, ProgListener.OK);
    }

    @Test
    public void testReadNoReply() throws ProgrammerException {
        registerProgramTrack();
        prog.readCV("13", p);
        expectInteraction(new int[]{0x20, 0x40, 0, 0, 0, 12, 0xF8, 1},
                new int[]{0x20, 0x58, 0, 0, 0, 12, 0xF8, 0x20, 0x31});
        Mockito.verify(p).programmingOpReply(0, ProgListener.NoLocoDetected);
    }

    @Test
    public void testWriteNoAck() throws ProgrammerException {
        registerProgramTrack();
        prog.writeCV("13", 42, p);
        expectInteraction(new int[]{0x20, 0x00, 0, 0, 0, 12, 0xF8, 42},
                new int[]{0x20, 0x18, 0, 0, 0, 12, 0xF8, 0x20, 0x32});
        Mockito.verify(p).programmingOpReply(0, ProgListener.ConfirmFailed);
    }

    @Test
    public void testWriteNoRailcom() throws ProgrammerException {
        registerProgramTrack();
        prog.writeCV("13", 42, p);
        expectInteraction(new int[]{0x20, 0x00, 0, 0, 0, 12, 0xF8, 42},
                new int[]{0x20, 0x18, 0, 0, 0, 12, 0xF8, 0x20, 0x33});
        Mockito.verify(p).programmingOpReply(0, ProgListener.NoAck);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        programmer = prog = new OlcbProgrammer(h.iface, null);
        h.expectFrame(":X19914333N090099FEFFFF0002;");
        h.expectNoFrames();
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer = null;
        h.expectNoFrames();
        Mockito.verifyNoMoreInteractions(p);
        h.dispose();
        JUnitUtil.tearDown();
    }

    /**
     * Helper function that simulates the startup of the remote program track. This interaction is necessary before the
     * default programmer object can execute any programming operations.
     */
    private void registerProgramTrack() {
        h.setRemoteAlias(0x555, progNid);
        h.sendMessage(
                new ProducerIdentifiedMessage(progNid, OlcbProgrammer.IS_PROGRAMMINGTRACK_EVENT, EventState.Unknown));
    }

    /**
     * Helper function to verify a programming track interaction comprised of a request datagram, and a matching
     * response datagram. Covers the necessary datagram acknowledgements as well. Assumes that the program track is
     * assigned (not a POM).
     *
     * @param dgRequest int array of the request datagram contents, example: `new int[]{0x20, 0x40, 0,0,0,0, 0xF8, 1}` for read CV 1.
     * @param dgResponse int array of the response datagram contents, example: `new int[]{0x20, 0x50, 0,0,0,0, 0xF8, 23}` for read response=23.
     */
    private void expectInteraction(int[] dgRequest, int[] dgResponse) {
        h.expectMessageAndNoMore(new DatagramMessage(localNid, progNid, dgRequest));
        // Acknowledged
        h.sendMessage(new DatagramAcknowledgedMessage(progNid, localNid, DatagramService.FLAG_REPLY_PENDING));
        Mockito.verifyNoInteractions(p);
        // Response datagram
        h.sendMessageAndExpectResult(new DatagramMessage(progNid, localNid, dgResponse),
                new DatagramAcknowledgedMessage(localNid, progNid));
        h.expectNoMessages();
    }

    OlcbTestHelper h = new OlcbTestHelper();
    OlcbProgrammer prog;

    NodeID localNid = h.iface.getNodeId();
    NodeID progNid = new NodeID(0x050101011807L);
    NodeID pomNid = new NodeID(0x06010000C00FL);

    ProgListener p = Mockito.mock(ProgListener.class);
}
