package rickiewars.guishop;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@Suite
//@SelectPackages({
//        "com.rwconnected.serverkit.util",
//        "com.rwconnected.serverkit.service",
//})
public class GUIShopTest {
    @BeforeAll
    static void setup() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    public void testWithSimpleExpression() {
        var sword = new ItemStack(Items.DIAMOND_SWORD);

        assertEquals(Items.DIAMOND_SWORD, sword.getItem());
    }
}
