/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 *       Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 *       Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 *       Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 *       Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package hwp.sqlte;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zero
 * Created on 2018/11/27.
 */
public class ConversionServiceTest {

    ConversionService service = DefaultConversionService.INSTANCE;

    @Test
    public void testInteger2Int() {
        Integer integer = service.convert(Integer.valueOf(10), Integer.TYPE);
        Assert.assertNotNull(integer);
    }

    @Test
    public void testBoolean() {
        Boolean b = service.convert(Boolean.TRUE, Boolean.TYPE);
        Assert.assertTrue(b);
    }

    @Test
    public void testBoolean2Boolean() {
        Boolean b = service.convert(Boolean.TRUE, Boolean.class);
        Assert.assertTrue(b);
    }

    @Test
    public void testBoolean2String() {
        String b = service.convert(Boolean.TRUE, String.class);
        Assert.assertEquals("true", b);
    }

    @Test
    public void testBoolean2Number() {
        Integer b = service.convert(Boolean.TRUE, Integer.class);
        Assert.assertEquals(1, (int) b);
        Long l = service.convert(Boolean.TRUE, Long.class);
        Assert.assertEquals(1, (long) l);
    }

    @Test
    public void testBoolean2Byte() {
        Byte aByte = service.convert(Boolean.TRUE, Byte.TYPE);
        Assert.assertEquals((byte) 1, (byte) aByte);
        Byte aByte2 = service.convert(Boolean.TRUE, Byte.class);
        Assert.assertEquals((byte) 1, (byte) aByte2);
    }

    @Test
    public void testLocalDateTime2UtilDate() {
        LocalDateTime now = LocalDateTime.now();
        Date date1 = new Date();
        Date date2 = service.convert(now, Date.class);
        long d = date1.getTime() - date2.getTime();
        Assert.assertTrue(d >= 0 && d < 1000);
    }


    @Test
    public void testNumbers() {
        int i = service.convert(10, Integer.TYPE);
        Assert.assertEquals(10, i);
        short s = service.convert((short) 10, Short.TYPE);
        Assert.assertEquals(10, s);
        double d = service.convert(10D, Double.TYPE);
        Assert.assertEquals(10D, d, 0.0);
    }

    @Test
    public void testInt2Number() {
        Number value = service.convert(10, Number.class);
        Assert.assertEquals(10, value.intValue());
    }

    @Test
    public void testInt2Long() {
        Long t1 = service.convert(10, Long.class);
        Assert.assertEquals(10L, t1.longValue());
        Integer t2 = service.convert(987, Integer.class);
        Assert.assertEquals(987L, t2.longValue());
    }

    @Test
    public void testDouble2BigDecimal() {
        BigDecimal value = service.convert(10.02D, BigDecimal.class);
        Assert.assertEquals("10.02", value.toString());
    }

    @Test
    public void testInt2BigDecimal() {
        BigDecimal value = service.convert(10, BigDecimal.class);
        Assert.assertEquals("10", value.toString());
    }

    @Test
    public void testLong2BigInteger() {
        BigInteger value = service.convert(100000000L, BigInteger.class);
        Assert.assertEquals("100000000", value.toString());
    }

    @Test
    public void testLong2AtomicInteger() {
        AtomicInteger value = service.convert(100000000L, AtomicInteger.class);
        Assert.assertEquals("100000000", value.toString());
    }

    @Test
    public void testBigDecimal2AtomicInteger() {
        AtomicInteger value = service.convert(BigDecimal.valueOf(2.34), AtomicInteger.class);
        Assert.assertEquals(2, value.intValue());
    }

    @Test
    public void testStringArray2IntegerArray() {
        String[] strings = new String[]{"123", "456", "789"};
        Integer[] values = service.convert(strings, Integer[].class);
        Assert.assertEquals(123, values[0].intValue());
        Assert.assertEquals(456, values[1].intValue());
        Assert.assertEquals(789, values[2].intValue());
    }

    @Test
    public void testStringArray2LongArray() {
        String[] strings = new String[]{"123", "456", "789"};
        Long[] values = service.convert(strings, Long[].class);
        Assert.assertEquals(123L, values[0].longValue());
        Assert.assertEquals(456L, values[1].longValue());
        Assert.assertEquals(789L, values[2].longValue());
    }

    @Test
    public void testString2LocalTime() {
        LocalTime value = service.convert("12:10:08", LocalTime.class);
        Assert.assertEquals(LocalTime.of(12, 10, 8), value);
    }

    @Test
    public void testLocalTime2String() {
        LocalTime now = LocalTime.now();
        String value = service.convert(now, String.class);
        Assert.assertEquals(8, value.length());
    }

}
