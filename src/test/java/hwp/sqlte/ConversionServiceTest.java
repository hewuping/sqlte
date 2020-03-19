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

/**
 * @author Zero
 * Created on 2018/11/27.
 */
public class ConversionServiceTest {

    ConversionService service = ConversionService.DEFAULT;

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
}
