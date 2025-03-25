package integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.itmo.Tmp;

public class GetSumTest {
    @Test
    @DisplayName("check get sum smoke test")
    public void getSumSmoke() {
        Assertions.assertEquals(6, Tmp.getSum(2, 4));
    }
}
