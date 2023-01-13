package com.thg.accelerator21.connectn.ai.name;

import com.thehutgroup.accelerator.connectn.player.Board;
import com.thehutgroup.accelerator.connectn.player.Counter;
import com.thg.accelerator23.connectn.ai.fourplay.FourPlay;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {
  /**
   * Rigorous Test :-)
   */
  @Test
  public void shouldAnswerWithTrue() {
    assertTrue(true);
  }

  @Test
  public void constructorTest() {
    FourPlay fourPlay = new FourPlay(Counter.O);

    assertEquals(fourPlay.getName(), "com.thg.accelerator23.connectn.ai.fourplay.FourPlay");
    assertEquals(fourPlay.getCounter(), Counter.O);
  }
}
